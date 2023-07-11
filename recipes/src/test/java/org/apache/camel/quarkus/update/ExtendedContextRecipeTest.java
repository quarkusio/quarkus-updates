package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class ExtendedContextRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ExtendedContextRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("camel-api"))
                .typeValidationOptions(TypeValidation.none());;
    }

    @Test
    void testComponentNameResolver() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new ExtendedContextRecipe().getVisitor())),
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;

                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                import org.apache.camel.spi.ComponentNameResolver;

                                public class Test {

                                    CamelContext context;

                                    public void test() {
                                        ComponentNameResolver ec = context.getExtension(ExtendedCamelContext.class).getComponentNameResolver();
                                    }
                                }
                            """,
                        """
                                package org.apache.camel.quarkus.component.test.it;

                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                import org.apache.camel.spi.ComponentNameResolver;
                                import org.apache.camel.support.PluginHelper;

                                public class Test {

                                    CamelContext context;

                                    public void test() {
                                        ComponentNameResolver ec = PluginHelper.getComponentNameResolver(context);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testRuntimeCatalog() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new ExtendedContextRecipe().getVisitor())),
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.catalog.RuntimeCamelCatalog;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        final CamelRuntimeCatalog catalog = (CamelRuntimeCatalog) context.getExtension(RuntimeCamelCatalog.class);
                                    }
                                }
                            """,
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.catalog.RuntimeCamelCatalog;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        final CamelRuntimeCatalog catalog = (CamelRuntimeCatalog) context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testAdapt() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new ExtendedContextRecipe().getVisitor())),
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        ModelCamelContext m = context.adapt(ModelCamelContext.class);
                                    }
                                }
                            """,
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        (ModelCamelContext)(context);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testAdapt2() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new ExtendedContextRecipe().getVisitor())),
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        return context.adapt(ExtendedCamelContext.class).getHeadersMapFactory();
                                    }
                                }
                                """,
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        return ((ExtendedCamelContext)(context)).getHeadersMapFactory();
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testAdaptRouteDefinition() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new ExtendedContextRecipe().getVisitor())),
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        AdviceWith.adviceWith(context.adapt(ModelCamelContext.class).getRouteDefinition("forMocking"), context, null);
                                    }
                                }
                                """,
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        AdviceWith.adviceWith(((ModelCamelContext)(context)).getRouteDefinition("forMocking"), context, null);
                                    }
                                }
                                """
                )
        );
    }
}