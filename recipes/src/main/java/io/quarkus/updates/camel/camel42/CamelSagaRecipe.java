package io.quarkus.updates.camel.camel42;

import io.quarkus.updates.camel.AbstractCamelQuarkusJavaVisitor;
import io.quarkus.updates.camel.RecipesUtil;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Recipe migrating changes between Camel 4.3 to 4.4, for more details see the
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core" >documentation</a>.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class CamelSagaRecipe extends Recipe {

    private static final String M_NEW_SAGA = "org.apache.camel.saga.InMemorySagaService newSaga()";
    private static final String M_SAGA_COORDINATOR_COMPENSATE = "org.apache.camel.saga.CamelSagaCoordinator compensate()";
    private static final String M_SAGA_COORDINATOR_COMPLETE = "org.apache.camel.saga.CamelSagaCoordinator complete()";

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

                if ((getMethodMatcher(M_NEW_SAGA).matches(mi, false)
                        || getMethodMatcher(M_SAGA_COORDINATOR_COMPENSATE).matches(mi, false)
                        || getMethodMatcher(M_SAGA_COORDINATOR_COMPLETE).matches(mi, false))
                        && RecipesUtil.methodInvocationAreArgumentEmpty(mi)) {
                    J.Identifier type = RecipesUtil.createIdentifier(Space.EMPTY, "Exchange", "import org.apache.camel.Exchange");
                    J.TypeCast cp = (J.TypeCast) RecipesUtil.createTypeCast(type, RecipesUtil.createNullExpression());
                    mi = mi.withArguments(Collections.singletonList(cp.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment("Exchange parameter was added.")))));
                }

                return mi;
            }
        });
    }
}