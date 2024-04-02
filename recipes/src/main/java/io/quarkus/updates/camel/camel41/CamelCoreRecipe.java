package io.quarkus.updates.camel.camel41;

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

import java.util.regex.Pattern;

/**
 * Recipe migrating changes between Camel 4.3 to 4.4, for more details see the
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core" >documentation</a>.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class CamelCoreRecipe extends Recipe {

    private static final String M_TO = "org.apache.camel.model.ProcessorDefinition to(..)";
    private static final String M_FROM = "org.apache.camel.model.ProcessorDefinition from(..)";
    private static final String AWS2_URL_WITH_QUEUE_REGEXP =  "(aws2-sns://[a-zA-z]+?.*)queueUrl=https://(.+)";
    private static final Pattern AWS2_URL_WITH_QUEUE_URL =  Pattern.compile(AWS2_URL_WITH_QUEUE_REGEXP);

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
            public J.Literal visitLiteral(J.Literal literal, ExecutionContext context) {
                J.Literal l =  super.visitLiteral(literal, context);

                //is it possible to precondition that aws2 is present?
                if(JavaType.Primitive.String.equals(l.getType()) && AWS2_URL_WITH_QUEUE_URL.matcher((String)l.getValue()).matches()) {
                    String newUrl = ((String) l.getValue()).replaceFirst(AWS2_URL_WITH_QUEUE_REGEXP, "$1queueArn=arn:aws:sqs:$2");
                    l = RecipesUtil.createStringLiteral(newUrl);
                }

                return l;
            }
        });
    }
}