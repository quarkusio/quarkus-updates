package io.quarkus.updates.camel30;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelJmxTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe(spec, "org.openrewrite.java.camel.migrate.ChangeManagedChoiceMBeanMethodName",
                        "org.openrewrite.java.camel.migrate.ChangeManagedFailoverLoadBalancerMBeanMethodName")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-management-api"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testRenamedMethods() {
        rewriteRun(java(
                """
                            import org.apache.camel.api.management.mbean.ManagedChoiceMBean;
                            import org.apache.camel.api.management.mbean.ManagedFailoverLoadBalancerMBean;

                            public class Test {

                                void test() {
                                    ManagedChoiceMBean mbean = null;
                                    mbean.choiceStatistics();
                                    ManagedFailoverLoadBalancerMBean mbean2 = null;
                                    mbean2.exceptionStatistics();
                                }
                            }
                        """,
                """
                            import org.apache.camel.api.management.mbean.ManagedChoiceMBean;
                            import org.apache.camel.api.management.mbean.ManagedFailoverLoadBalancerMBean;

                            public class Test {

                                void test() {
                                    ManagedChoiceMBean mbean = null;
                                    mbean.extendedInformation();
                                    ManagedFailoverLoadBalancerMBean mbean2 = null;
                                    mbean2.extendedInformation();
                                }
                            }
                        """));
    }
}
