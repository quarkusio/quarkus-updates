package io.quarkus.updates.camel.camel43;

import io.quarkus.updates.camel.AbstractCamelQuarkusJavaVisitor;
import io.quarkus.updates.camel.RecipesUtil;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;

import java.util.Collections;

/**
 * Recipe migrating changes between Camel 4.3 to 4.4, for more details see the
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core" >documentation</a>.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class CamelThrottleEIPRecipe extends Recipe {

    private static final String M_THROTTLE_PRIMITIVE = "org.apache.camel.model.ProcessorDefinition throttle(long)";
    private static final String M_THROTTLE_TIME_PERIOD_MILLIS_PRIMITIVE = "org.apache.camel.model.ThrottleDefinition timePeriodMillis(long)";
    private static String WARNING_COMMENT = " Throttle now uses the number of concurrent requests as the throttling measure instead of the number of requests per period.";

    @Override
    public String getDisplayName() {
        return "Camel Core changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel Core migration from version 4.0 to 4.1.";
    }


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return RecipesUtil.newVisitor(new AbstractCamelQuarkusJavaVisitor() {
            @Override
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(method, context);

                if (getMethodMatcher(M_THROTTLE_PRIMITIVE).matches(mi, false)
                        && !RecipesUtil.isCommentBeforeElement(mi, WARNING_COMMENT)) {
                    mi = mi.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment(WARNING_COMMENT)));
                    getCursor().putMessage("throttle-migrated", true);
                }
                else if (getMethodMatcher(M_THROTTLE_TIME_PERIOD_MILLIS_PRIMITIVE).matches(mi, false)) {
                    if(mi.getSelect() instanceof J.MethodInvocation) {
                        return (J.MethodInvocation)mi.getSelect();
                    } else {
                        return null;
                    }
                }

                return mi;
            }
        });
    }
}