package io.quarkus.updates.core.quarkus339;

import java.util.ArrayList;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.ShortenFullyQualifiedTypeReferences;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JLeftPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Wraps {@link ChangeType} but also adds the outer class import when the
 * target is an inner class. Works around a known OpenRewrite bug where
 * {@code ChangeType} with {@code $} inner-class notation does not add
 * the import for the enclosing class.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class ChangeTypeWithInnerClass extends Recipe {

    @Option(displayName = "Old fully-qualified type name",
            description = "Fully-qualified name of the type to replace, using $ for inner classes.")
    String oldFullyQualifiedTypeName;

    @Option(displayName = "New fully-qualified type name",
            description = "Fully-qualified name of the replacement type, using $ for inner classes.")
    String newFullyQualifiedTypeName;

    @Override
    public String getDisplayName() {
        return "Change type (with inner class import fix)";
    }

    @Override
    public String getDescription() {
        return "Changes a type reference and ensures the outer class import is added when the target is an inner class.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        String outerClass = getOuterClass(newFullyQualifiedTypeName);
        if (outerClass != null) {
            List<Recipe> recipes = new ArrayList<>();
            if (oldFullyQualifiedTypeName.contains("$") && newFullyQualifiedTypeName.contains("$")) {
                recipes.add(new ChangeNestedType(oldFullyQualifiedTypeName, newFullyQualifiedTypeName));
            } else {
                recipes.add(new ChangeType(oldFullyQualifiedTypeName, newFullyQualifiedTypeName, null));
            }
            recipes.add(new AddImportRecipe(outerClass));
            recipes.add(new RemoveInnerImportRecipe(newFullyQualifiedTypeName, outerClass));
            return recipes;
        }
        return List.of(new ChangeType(oldFullyQualifiedTypeName, newFullyQualifiedTypeName, null));
    }

    /**
     * Changes nested types in one traversal. Running ChangeType repeatedly over a nested
     * reference can leave a FieldAccess as the name child of another FieldAccess; OpenRewrite
     * 8.89's ChangeType then assumes that child is an Identifier and throws.
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    private static class ChangeNestedType extends Recipe {
        String oldType;
        String newType;

        @Override
        public String getDisplayName() {
            return "Change nested type";
        }

        @Override
        public String getDescription() {
            return "Changes references to a nested type without creating invalid intermediate field accesses.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaType.FullyQualified oldJavaType = TypeUtils.asFullyQualified(JavaType.buildType(oldType));
            JavaType.FullyQualified newJavaType = TypeUtils.asFullyQualified(JavaType.buildType(newType));
            if (oldJavaType == null || newJavaType == null) {
                return TreeVisitor.noop();
            }
            return new JavaVisitor<>() {
                @Override
                public J visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
                    String reference = typeName(fieldAccess);
                    if (TypeUtils.isOfType(fieldAccess.getType(), oldJavaType)
                            || oldType.replace('$', '.').equals(reference)) {
                        maybeRemoveImport(oldJavaType.getOwningClass());
                        Expression replacement = TypeTree.build(newType.replace('$', '.')).withPrefix(fieldAccess.getPrefix());
                        doAfterVisit(ShortenFullyQualifiedTypeReferences.modifyOnly(replacement));
                        return (J) replacement;
                    }
                    return super.visitFieldAccess(fieldAccess, ctx);
                }

                @Override
                public J visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
                    if (TypeUtils.isOfType(identifier.getType(), oldJavaType)
                            || oldType.replace('$', '.').equals(identifier.getSimpleName())) {
                        maybeRemoveImport(oldJavaType.getOwningClass());
                        Expression replacement = TypeTree.build(newType.replace('$', '.')).withPrefix(identifier.getPrefix());
                        doAfterVisit(ShortenFullyQualifiedTypeReferences.modifyOnly(replacement));
                        return (J) replacement;
                    }
                    return super.visitIdentifier(identifier, ctx);
                }

                private String typeName(Expression expression) {
                    if (expression instanceof J.Identifier) {
                        return ((J.Identifier) expression).getSimpleName();
                    }
                    if (expression instanceof J.FieldAccess) {
                        J.FieldAccess access = (J.FieldAccess) expression;
                        if (!(access.getName() instanceof J.Identifier)) {
                            return "";
                        }
                        String prefix = typeName(access.getTarget());
                        String name = ((J.Identifier) access.getName()).getSimpleName();
                        return prefix.isEmpty() ? name : prefix + "." + name;
                    }
                    return "";
                }
            };
        }
    }

    private static String getOuterClass(String fqn) {
        int dollarIndex = fqn.indexOf('$');
        if (dollarIndex < 0) {
            return null;
        }
        return fqn.substring(0, dollarIndex);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    private static class RemoveInnerImportRecipe extends Recipe {
        String innerTypeFqn;
        String outerTypeFqn;

        @Override
        public String getDisplayName() {
            return "Remove inner class import for " + innerTypeFqn;
        }

        @Override
        public String getDescription() {
            return "Removes direct inner class import and qualifies type references with the outer class name.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            String dotNotation = innerTypeFqn.replace('$', '.');
            int lastDot = outerTypeFqn.lastIndexOf('.');
            String qualifiedName = dotNotation.substring(lastDot + 1);
            String innerSimpleName = dotNotation.substring(dotNotation.lastIndexOf('.') + 1);

            return new JavaVisitor<>() {
                boolean needsQualification = false;

                @Override
                public J visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                    needsQualification = false;
                    for (J.Import imp : cu.getImports()) {
                        if (imp.getTypeName().equals(innerTypeFqn)) {
                            needsQualification = true;
                            break;
                        }
                    }
                    if (!needsQualification) {
                        return cu;
                    }
                    J.CompilationUnit result = (J.CompilationUnit) super.visitCompilationUnit(cu, ctx);
                    List<J.Import> filtered = new ArrayList<>();
                    for (J.Import imp : result.getImports()) {
                        String typeName = imp.getTypeName();
                        if (!typeName.equals(innerTypeFqn) && !typeName.equals(dotNotation)) {
                            filtered.add(imp);
                        }
                    }
                    return result.withImports(filtered);
                }

                @Override
                public J visitImport(J.Import import_, ExecutionContext ctx) {
                    return import_;
                }

                @Override
                public J visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
                    if (needsQualification && identifier.getSimpleName().equals(innerSimpleName)) {
                        return buildQualifiedAccess(qualifiedName, identifier.getPrefix(), identifier.getType());
                    }
                    return identifier;
                }

                private Expression buildQualifiedAccess(String name, Space prefix, JavaType type) {
                    String[] parts = name.split("\\.");
                    Expression result = new J.Identifier(
                            Tree.randomId(), Space.EMPTY, Markers.EMPTY,
                            List.of(), parts[0], null, null);
                    for (int i = 1; i < parts.length; i++) {
                        JavaType partType = (i == parts.length - 1) ? type : null;
                        J.Identifier partId = new J.Identifier(
                                Tree.randomId(), Space.EMPTY, Markers.EMPTY,
                                List.of(), parts[i], partType, null);
                        result = new J.FieldAccess(
                                Tree.randomId(), Space.EMPTY, Markers.EMPTY,
                                result,
                                new JLeftPadded<>(Space.EMPTY, partId, Markers.EMPTY),
                                partType);
                    }
                    return result.withPrefix(prefix);
                }
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    private static class AddImportRecipe extends Recipe {
        String type;

        @Override
        public String getDisplayName() {
            return "Add import for " + type;
        }

        @Override
        public String getDescription() {
            return "Adds import for " + type + " if referenced.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            String simpleOuterName = type.substring(type.lastIndexOf('.') + 1);
            return new JavaIsoVisitor<>() {
                @Override
                public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                    String source = cu.printAll();
                    if (source.contains(simpleOuterName + ".")) {
                        AddImport<ExecutionContext> addImport = new AddImport<>(type, null, false);
                        return (J.CompilationUnit) addImport.visit(cu, ctx);
                    }
                    return cu;
                }
            };
        }
    }
}
