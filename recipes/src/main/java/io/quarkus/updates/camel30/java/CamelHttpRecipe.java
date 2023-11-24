package io.quarkus.updates.camel30.java;

import lombok.EqualsAndHashCode;
import lombok.Value;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.ChangeFieldType;
import org.openrewrite.java.ChangePackage;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import io.quarkus.updates.camel30.AbstractCamelQuarkusJavaVisitor;
import org.openrewrite.java.tree.JavaType;

@EqualsAndHashCode(callSuper = true)
@Value
public class CamelHttpRecipe extends Recipe {

    private static final String SET_CREDENTIALS = "org.apache.http.impl.client.BasicCredentialsProvider setCredentials(..)";
    private static final String SCOPE_ANY = "AuthScope.ANY";

    @Override
    public String getDisplayName() {
        return "Camel Http Extension changes";
    }

    @Override
    public String getDescription() {
        return "Camel Http Extension changes.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {


        return new AbstractCamelQuarkusJavaVisitor() {
            @Override
            protected J.Import doVisitImport(J.Import _import, ExecutionContext context) {
                doAfterVisit(
                        new ChangeType("org.apache.http.HttpHost",
                                "org.apache.hc.core5.http.HttpHost", true).getVisitor());
                doAfterVisit(
                        new ChangeType(
                                "org.apache.http.client.protocol.HttpClientContext",
                                "org.apache.hc.client5.http.protocol.HttpClientContext", true).getVisitor());
                doAfterVisit(
                        new ChangeType(
                                "org.apache.http.protocol.HttpContext",
                                "org.apache.hc.core5.http.protocol.HttpContext", true).getVisitor());
                doAfterVisit(
                    new ChangeType(
                            "org.apache.http.impl.auth.BasicScheme",
                            "org.apache.hc.client5.http.impl.auth.BasicScheme", true).getVisitor());
                doAfterVisit(
                    new ChangeType(
                            "org.apache.http.impl.client.BasicAuthCache",
                            "org.apache.hc.client5.http.impl.auth.BasicAuthCache", true).getVisitor());
                doAfterVisit(
                    new ChangeType(
                            "org.apache.http.impl.client.BasicCredentialsProvider",
                            "org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider", true).getVisitor());
                doAfterVisit(
                    new ChangeType(
                            "org.apache.http.auth.AuthScope",
                            "org.apache.hc.client5.http.auth.AuthScope", true).getVisitor());
                doAfterVisit(
                    new ChangeType(
                            "org.apache.http.auth.UsernamePasswordCredentials",
                            "org.apache.hc.client5.http.auth.UsernamePasswordCredentials", true).getVisitor());

                return super.doVisitImport(_import, context);
            }

            @Override
            protected J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
                J.FieldAccess f = super.doVisitFieldAccess(fieldAccess, context);

                //The component has been upgraded to use Apache HttpComponents v5
                //AuthScope.ANY -> new AuthScope(null, -1)
                if("ANY".equals(f.getSimpleName()) && "org.apache.http.auth.AuthScope".equals(f.getType().toString())) {
                    JavaTemplate.Builder templateBuilder = JavaTemplate.builder( "new AuthScope(null, -1)")/*[Rewrite8 migration] contextSensitive() could be unnecessary, please follow the migration guide*/.contextSensitive()
                            .imports("org.apache.hc.client5.http.auth.AuthScope");
                    J.NewClass nc =  templateBuilder.build().apply(/*[Rewrite8 migration] getCursor() could be updateCursor() if the J instance is updated, or it should be updated to point to the correct cursor, please follow the migration guide*/getCursor(),
                            f.getCoordinates().replace())
                                     .withPrefix(f.getPrefix()
                    );
                    getCursor().putMessage("authScopeNewClass", nc);
                }
                return f;
            }

            @Override
            public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext context) {
                return super.visitNewClass(newClass, context);
            }


            @Override
            public @Nullable J postVisit(J tree, ExecutionContext context) {
                J j = super.postVisit(tree, context);

                //use a new class instead of original element
                J.NewClass newClass = getCursor().getMessage("authScopeNewClass");
                if(newClass != null) {
                    return newClass;
                }

                return j;
            }

        };
    }

}