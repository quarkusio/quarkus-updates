package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class CamelEIPRecipeTest implements RewriteTest{
    
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CamelEIPRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("camel-activemq"))
                .typeValidationOptions(TypeValidation.none());;
    }

    @Test
    void testRemovedEIPInOptionalOut() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelEIPRecipe().getVisitor())),
                java(
                        """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                        from("direct:a")
                                        .inOut("activemq:queue:testqueue")
                                        .to("log:result_a");
                                    }
                                }
                            """,
                            """
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
                        
                )
        );
    }

    @Test
    void testRemovedEIPOutOptionalIn() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelEIPRecipe().getVisitor())),
                java(
                        """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                        from("direct:a")
                                        .inOut("activemq:queue:testqueue")
                                        .to("log:result_a");
                                    }
                                }
                            """,
                            """
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
                        
                )
        );
    }

    @Test
    void testRemovedEIPOutIn() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelEIPRecipe().getVisitor())),
                java(
                        """
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
                        
                )
        );
    }

}
