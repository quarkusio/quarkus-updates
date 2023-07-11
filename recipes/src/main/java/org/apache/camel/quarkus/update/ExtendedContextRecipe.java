package org.apache.camel.quarkus.update;

import org.apache.camel.catalog.RuntimeCamelCatalog;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.Collections;
import java.util.regex.Pattern;

public class ExtendedContextRecipe extends Recipe {
    private static final MethodMatcher MATCHER_CONTEXT_GET_EXT =
            new MethodMatcher("org.apache.camel.CamelContext getExtension(java.lang.Class)");
    private static final MethodMatcher MATCHER_GET_NAME_RESOLVER =
            new MethodMatcher("org.apache.camel.ExtendedCamelContext getComponentNameResolver()");

    private static final MethodMatcher MATCHER_CONTEXT_ADAPT =
            new MethodMatcher("org.apache.camel.CamelContext adapt(java.lang.Class.class)");

    @Override
    public String getDisplayName() {
        return "Replaces removed method involving ExternalCamelContext.";
    }

    @Override
    public String getDescription() {
        return "Changes method call `context.getExtension(ExtendedCamelContext.class).getComponentNameResolver()` to a call of static method `PluginHelper.getComponentNameResolver(context)`";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, executionContext);

                // context.getExtension(ExtendedCamelContext.class).getComponentNameResolver() -> PluginHelper.getComponentNameResolver(context)
                if (MATCHER_GET_NAME_RESOLVER.matches(mi)) {
                    if (mi.getSelect() instanceof J.MethodInvocation && MATCHER_CONTEXT_GET_EXT.matches(((J.MethodInvocation) mi.getSelect()).getMethodType())) {
                        J.MethodInvocation innerInvocation = (J.MethodInvocation) mi.getSelect();
                        mi = mi.withTemplate(JavaTemplate.builder(() -> getCursor().getParentOrThrow(), "PluginHelper.getComponentNameResolver(#{any(org.apache.camel.CamelContext)})")
                                        .build(),
                                mi.getCoordinates().replace(), innerInvocation.getSelect());
                        doAfterVisit(new AddImport<>("org.apache.camel.support.PluginHelper", null, false));
                    }

                    //context.getExtension(RuntimeCamelCatalog.class) -> context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                } else if (MATCHER_CONTEXT_GET_EXT.matches(mi) && mi.getType().isAssignableFrom(Pattern.compile(
                        RuntimeCamelCatalog.class.getCanonicalName()))) {

                    mi = mi.withName(mi.getName().withSimpleName("getCamelContextExtension().getContextPlugin"))
                            .withMethodType(mi.getMethodType());

                    //context.adapt(ModelCamelContext.class) -> ((ModelCamelContext) context)
                } else if (MATCHER_CONTEXT_ADAPT.matches(method)) {
                    if (method.getType().isAssignableFrom(Pattern.compile("org.apache.camel.model.ModelCamelContext"))) {
                        mi = mi.withName(mi.getName().withSimpleName("(ModelCamelContext)"))
                                .withPrefix(mi.getPrefix().withWhitespace("("))
                                .withMethodType(mi.getMethodType())
                                .withArguments(Collections.singletonList(method.getSelect()))
                                .withSelect(null);


                    } else if (method.getType().isAssignableFrom(Pattern.compile("org.apache.camel.ExtendedCamelContext"))) {
                        mi = mi.withName(mi.getName().withSimpleName("((ExtendedCamelContext)"))
                                .withMethodType(mi.getMethodType())
                                .withArguments(Collections.singletonList(new J.Identifier(mi.getSelect().getId(), mi.getSelect().getPrefix(), mi.getSelect().getMarkers(), ((J.Identifier) mi.getSelect()).getSimpleName() + ")", mi.getSelect().getType(), null)))
                                .withSelect(null);
                    }
                }


                return mi;
            }

        };
    }
}

