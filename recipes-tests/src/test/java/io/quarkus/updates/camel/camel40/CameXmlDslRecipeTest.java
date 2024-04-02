package io.quarkus.updates.camel.camel40;

import static org.openrewrite.xml.Assertions.xml;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CameXmlDslRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3alpha(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testDescription() {
        //language=xml
        rewriteRun(xml("""
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="myRoute">
                        <description>Something that this route do</description>
                        <from uri="kafka:cheese"/>
                        <setBody>
                           <constant>Hello Camel K!</constant>
                        </setBody>
                       <to uri="log:info"/>
                    </route>
                    <route id="myRoute2">
                        <description>Something that this route2 do</description>
                        <from uri="kafka:cheese"/>
                        <setBody>
                           <constant>Hello Camel K!</constant>
                        </setBody>
                       <to uri="log:info"/>
                    </route>
                </routes>
                                                """, """
                    <routes xmlns="http://camel.apache.org/schema/spring">
                        <route id="myRoute" description="Something that this route do">
                            <from uri="kafka:cheese"/>
                            <setBody>
                               <constant>Hello Camel K!</constant>
                            </setBody>
                           <to uri="log:info"/>
                        </route>
                        <route id="myRoute2" description="Something that this route2 do">
                            <from uri="kafka:cheese"/>
                            <setBody>
                               <constant>Hello Camel K!</constant>
                            </setBody>
                           <to uri="log:info"/>
                        </route>
                    </routes>
                """));
    }

    @Test
    void testCircuitBreakerFull() {
        //language=xml
        rewriteRun(xml("""
                <differentContext>
                    <circuitBreaker>
                        <resilience4jConfiguration>
                            <bulkheadEnabled>5643</bulkheadEnabled>
                            <bulkheadMaxConcurrentCalls>aaaa</bulkheadMaxConcurrentCalls>
                            <bulkheadMaxWaitDuration>1</bulkheadMaxWaitDuration>
                            <timeoutEnabled>true</timeoutEnabled>
                            <timeoutExecutorService>1</timeoutExecutorService>
                            <timeoutDuration>1</timeoutDuration>
                            <timeoutCancelRunningFuture></timeoutCancelRunningFuture>
                        </resilience4jConfiguration>
                    </circuitBreaker>
                </differentContext>
                                                """, """
            <differentContext>
                <circuitBreaker>
                    <resilience4jConfiguration bulkheadEnabled="5643" bulkheadMaxConcurrentCalls="aaaa" bulkheadMaxWaitDuration="1" timeoutEnabled="true" timeoutExecutorService="1" timeoutDuration="1">
                    </resilience4jConfiguration>
                </circuitBreaker>
            </differentContext>
                """));
    }

    @Test
    void testCircuitBreaker() {
        //language=xml
        rewriteRun(xml("""
                <route>
                    <from uri="direct:start"/>
                    <circuitBreaker>
                        <resilience4jConfiguration>
                            <timeoutEnabled>true</timeoutEnabled>
                            <timeoutDuration>2000</timeoutDuration>
                        </resilience4jConfiguration>
                    </circuitBreaker>
                    <to uri="mock:result"/>
                </route>
                                                """, """
                <route>
                    <from uri="direct:start"/>
                    <circuitBreaker>
                        <resilience4jConfiguration timeoutEnabled="true" timeoutDuration="2000">
                        </resilience4jConfiguration>
                    </circuitBreaker>
                    <to uri="mock:result"/>
                </route>
                """));
    }
}
