package io.quarkus.updates.camel.customRecipes;

import io.quarkus.updates.camel.AbstractCamelQuarkusJavaVisitor;
import io.quarkus.updates.camel.RecipesUtil;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.regex.Pattern;

/**
 * Replaces prefix with the new one and changes the suffix tp start with lower case
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class MoveGetterToPluginHelper extends Recipe {

    private static final String MATCHER_GET_NAME_RESOLVER = "org.apache.camel.ExtendedCamelContext getComponentNameResolver()";
    private static final String MATCHER_GET_MODEL_JAXB_CONTEXT_FACTORY = "org.apache.camel.ExtendedCamelContext getModelJAXBContextFactory()";
    private static final String MATCHER_GET_MODEL_TO_XML_DUMPER = "org.apache.camel.ExtendedCamelContext getModelToXMLDumper()";
    private static final Pattern EXTERNAL_CONTEXT_TYPE = Pattern.compile("org.apache.camel.ExtendedCamelContext");
    private static final String MATCHER_CONTEXT_GET_EXT = "org.apache.camel.CamelContext getExtension(java.lang.Class)";

    @Option(displayName = "Method name",
            description = "Name of the method on external camel context.")
    String oldMethodName;

    @Override
    public String getDisplayName() {
        return "Move getter from context to PluginHelper.";
    }

    @Override
    public String getDescription() {
        return "Move getter from context to PluginHelper";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new AbstractCamelQuarkusJavaVisitor() {
            @Override
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(method, context);

                // extendedContext.getModelJAXBContextFactory() -> PluginHelper.getModelJAXBContextFactory(extendedContext)
                if (getMethodMatcher(getOldMethodMatcher()).matches(mi, false)) {
                    if (mi.getSelect() instanceof J.MethodInvocation && getMethodMatcher(MATCHER_CONTEXT_GET_EXT).matches(((J.MethodInvocation) mi.getSelect()).getMethodType())) {
                        J.MethodInvocation innerInvocation = (J.MethodInvocation) mi.getSelect();
                        mi = JavaTemplate.builder(getNewMethodFromContext())
                                //.contextSensitive()
                                .build()
                                .apply(getCursor(), mi.getCoordinates().replace(), innerInvocation.getSelect());
                        doAfterVisit(new AddImport<>("org.apache.camel.support.PluginHelper", null, false));
                    } else if (mi.getSelect().getType().isAssignableFrom(EXTERNAL_CONTEXT_TYPE)) {
                        mi = JavaTemplate.builder(getNewMethodFromExternalContextContext())
                                //.contextSensitive()
                                .build()
                                .apply(getCursor(), mi.getCoordinates().replace(), mi.getSelect());
                        doAfterVisit(new AddImport<>("org.apache.camel.support.PluginHelper", null, false));
                    }
                }

                return mi;
            }

            private String getOldMethodMatcher() {
                return "org.apache.camel.ExtendedCamelContext " + oldMethodName + "()";
            }
            private String getNewMethodFromContext() {
                return "PluginHelper." + oldMethodName + "(#{any(org.apache.camel.CamelContext)})";
            }
            private String getNewMethodFromExternalContextContext() {
                return "PluginHelper." + oldMethodName + "(#{any(org.apache.camel.ExtendedCamelContext)})";
            }
        });
    }
}
