package io.quarkus.updates.camel.camel40;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CamelAPIsPropertiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3alpha(spec,"org.openrewrite.java.camel.migrate.ChangePropertyValue")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testRejectedPolicyDiscardOldeste() {
        rewriteRun(Assertions.properties("""
                   #test
                   camel.threadpool.rejectedPolicy=DiscardOldest
                """,
                """
                            #test
                            camel.threadpool.rejectedPolicy=Abort #DiscardOldest has been removed, consider Abort
                        """));
    }

    @Test
    void testRejectedPolicyDiscard() {
        rewriteRun(Assertions.properties("""
                   #test
                   camel.threadpool.rejectedPolicy=Discard
                """,
                """
                            #test
                            camel.threadpool.rejectedPolicy=Abort #Discard has been removed, consider Abort
                        """));
    }

}
