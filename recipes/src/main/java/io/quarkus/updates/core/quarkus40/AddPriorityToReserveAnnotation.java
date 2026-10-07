package io.quarkus.updates.core.quarkus40;

import java.util.Comparator;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddPriorityToReserveAnnotation extends Recipe {

    private static final String RESERVE = "jakarta.enterprise.inject.Reserve";
    private static final String PRIORITY = "jakarta.annotation.Priority";

    @Override
    public String getDisplayName() {
        return "Add `@Priority(0)` to beans annotated with `@Reserve`";
    }

    @Override
    public String getDescription() {
        return "Beans migrated from `@DefaultBean` to `@Reserve` need `@Priority(0)` unless they already have a `@Priority` annotation.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            private final JavaTemplate priorityTemplate = JavaTemplate.builder("@Priority(0)")
                    .imports(PRIORITY)
                    .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                    .build();

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);
                if (shouldAddPriority(cd.getLeadingAnnotations())) {
                    maybeAddImport(PRIORITY);
                    cd = priorityTemplate.apply(getCursor(),
                            cd.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                }
                return cd;
            }

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
                J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);
                if (shouldAddPriority(md.getLeadingAnnotations())) {
                    maybeAddImport(PRIORITY);
                    md = priorityTemplate.apply(getCursor(),
                            md.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                }
                return md;
            }

            @Override
            public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable,
                    ExecutionContext ctx) {
                J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);
                if (shouldAddPriority(vd.getLeadingAnnotations())) {
                    maybeAddImport(PRIORITY);
                    vd = priorityTemplate.apply(getCursor(),
                            vd.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                }
                return vd;
            }

            private boolean shouldAddPriority(List<J.Annotation> annotations) {
                boolean hasReserve = false;
                boolean hasPriority = false;

                for (J.Annotation annotation : annotations) {
                    JavaType annotationType = annotation.getType();
                    if (annotationType instanceof JavaType.FullyQualified) {
                        String fqn = ((JavaType.FullyQualified) annotationType).getFullyQualifiedName();
                        if (RESERVE.equals(fqn)) {
                            hasReserve = true;
                        } else if (PRIORITY.equals(fqn)) {
                            hasPriority = true;
                        }
                    }
                }

                return hasReserve && !hasPriority;
            }
        };
    }
}
