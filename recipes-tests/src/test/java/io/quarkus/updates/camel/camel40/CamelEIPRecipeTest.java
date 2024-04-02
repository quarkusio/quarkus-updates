package io.quarkus.updates.camel.camel40;

import static org.openrewrite.java.Assertions.java;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CamelEIPRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3alpha(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-activemq", "camel-core-model"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testRemovedEIPInOptionalOut() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;

                    public class MySimpleToDRoute extends RouteBuilder {

                        @Override
                        public void configure() {
                            from("direct:a")
                            .inOut("activemq:queue:testqueue")
                            .to("log:result_a");
                        }
                    }
                """, """
                    import org.apache.camel.ExchangePattern;
                    import org.apache.camel.builder.RouteBuilder;

                    public class MySimpleToDRoute extends RouteBuilder {

                        @Override
                        public void configure() {
                            from("direct:a")
                            .setExchangePattern(ExchangePattern.InOut).to("activemq:queue:testqueue")
                            .to("log:result_a");
                        }
                    }
                """

        ));
    }

    @Test
    void testRemovedEIPOutOptionalIn() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;

                    public class MySimpleToDRoute extends RouteBuilder {

                        @Override
                        public void configure() {
                            from("direct:a")
                            .inOut("activemq:queue:testqueue")
                            .to("log:result_a");
                        }
                    }
                """, """
                    import org.apache.camel.ExchangePattern;
                    import org.apache.camel.builder.RouteBuilder;

                    public class MySimpleToDRoute extends RouteBuilder {

                        @Override
                        public void configure() {
                            from("direct:a")
                            .setExchangePattern(ExchangePattern.InOut).to("activemq:queue:testqueue")
                            .to("log:result_a");
                        }
                    }
                """

        ));
    }

    @Test
    void testRemovedEIPOutIn() {
        //language=java
        rewriteRun(java("""
                        import org.apache.camel.ExchangePattern;
                        import org.apache.camel.builder.RouteBuilder;

                        public class MySimpleToDRoute extends RouteBuilder {

                            @Override
                            public void configure() {
                                from("direct:a")
                                .setExchangePattern(ExchangePattern.InOut).to("activemq:queue:testqueue")
                                .to("log:result_a");
                            }
                        }
                """

        ));
    }

}
