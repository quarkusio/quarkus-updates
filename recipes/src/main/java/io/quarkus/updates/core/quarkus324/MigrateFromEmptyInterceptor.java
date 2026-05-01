package io.quarkus.updates.core.quarkus324;

import java.util.ArrayList;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.marker.Markers;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class MigrateFromEmptyInterceptor extends Recipe {

    private static final String EMPTY_INTERCEPTOR = "org.hibernate.EmptyInterceptor";
    private static final String INTERCEPTOR = "org.hibernate.Interceptor";
    private static final String SERIALIZABLE = "java.io.Serializable";

    @Override
    public String getDisplayName() {
        return "Replace `EmptyInterceptor` with `Interceptor`";
    }

    @Override
    public String getDescription() {
        return "Replace `extends EmptyInterceptor` with `implements Interceptor, Serializable` as `EmptyInterceptor` was removed in Hibernate ORM 7.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);

                TypeTree extendsClause = cd.getExtends();
                if (extendsClause == null) {
                    return cd;
                }

                JavaType extendsType = extendsClause.getType();
                if (extendsType == null || !(extendsType instanceof JavaType.FullyQualified)) {
                    return cd;
                }

                if (!EMPTY_INTERCEPTOR.equals(((JavaType.FullyQualified) extendsType).getFullyQualifiedName())) {
                    return cd;
                }

                cd = cd.withExtends(null);

                JavaType.FullyQualified interceptorType = JavaType.ShallowClass.build(INTERCEPTOR);
                JavaType.FullyQualified serializableType = JavaType.ShallowClass.build(SERIALIZABLE);

                J.Identifier interceptorId = new J.Identifier(
                        java.util.UUID.randomUUID(),
                        Space.format(" "),
                        Markers.EMPTY,
                        List.of(),
                        "Interceptor",
                        interceptorType,
                        null);

                J.Identifier serializableId = new J.Identifier(
                        java.util.UUID.randomUUID(),
                        Space.format(" "),
                        Markers.EMPTY,
                        List.of(),
                        "Serializable",
                        serializableType,
                        null);

                List<JRightPadded<TypeTree>> implementsList = new ArrayList<>();
                implementsList.add(JRightPadded.build((TypeTree) interceptorId).withAfter(Space.EMPTY));
                implementsList.add(JRightPadded.build((TypeTree) serializableId).withAfter(Space.EMPTY));

                JContainer<TypeTree> implementsContainer = JContainer.build(
                        Space.format(" "),
                        implementsList,
                        Markers.EMPTY);

                cd = cd.getPadding().withImplements(implementsContainer);

                maybeRemoveImport(EMPTY_INTERCEPTOR);
                maybeAddImport(INTERCEPTOR);
                maybeAddImport(SERIALIZABLE);

                return cd;
            }
        };
    }
}
