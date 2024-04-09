package io.quarkus.updates.camel;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.openrewrite.yaml.Assertions;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;

public class CamelUpdate41Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-core-model", "camel-tracing"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_1.html#_camel_aws2_sns">doc</a>
     */
    @Test
    void testAws2SnsQueueUrl() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:start")
                              .to("aws2-sns://mytopic?subject=mySubject&autoCreateTopic=true&subscribeSNStoSQS=true&queueUrl=https://xxxxx");
                        }
                    }
                """,
                """
                        import org.apache.camel.builder.RouteBuilder;
                        
                        public class Jsonpath2Test extends RouteBuilder {
                            @Override
                            public void configure()  {
                                from("direct:start")
                                  .to("aws2-sns://mytopic?subject=mySubject&autoCreateTopic=true&subscribeSNStoSQS=true&queueArn=arn:aws:sqs:xxxxx");
                            }
                        }
                        """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_1.html#_camel_tracing">doc</a>
     */
    @Test
    void testTracingTag() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.tracing.Tag;
                    
                    public class Test {
                      
                        public Tag test() {
                            return Tag.URL_SCHEME;
                        }
                    }
                """,
                """
                    import org.apache.camel.tracing.TagConstants;
                    
                    public class Test {
                      
                        public TagConstants test() {
                            return TagConstants.URL_SCHEME;
                        }
                    }
                        """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_1.html#_xml_and_yaml_dsl">doc</a>
     */
    @Test
    void testYamlDsl() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - beans:
                  - name: "myClient"
                    beanType: "com.foo.MyBean"
                    type: "groovy"
                    script: |
                      # groovy script here
                """, """
                - beans:
                  - name: "myClient"
                    type: "com.foo.MyBean"
                    scriptLanguage: "groovy"
                    script: |
                      # groovy script here
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_1.html#_xml_and_yaml_dsl">doc</a>
     */
    @Test
    void testYamlDslNPE() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                apiVersion: v1
                kind: ServiceAccount
                metadata:
                  name: camel-leader-election
                ---
                apiVersion: rbac.authorization.k8s.io/v1
                kind: Role
                metadata:
                  name: camel-leader-election
                rules:
                  - apiGroups:
                      - ""
                      - "coordination.k8s.io"
                    resources:
                      - configmaps
                      - secrets
                      - pods
                      - leases
                    verbs:
                      - create
                      - delete
                      - deletecollection
                      - get
                      - list
                      - patch
                      - update
                      - watch
                ---
                apiVersion: rbac.authorization.k8s.io/v1
                kind: RoleBinding
                metadata:
                  name: camel-leader-election
                subjects:
                  - kind: ServiceAccount
                    name: camel-leader-election
                roleRef:
                  kind: Role
                  name: camel-leader-election
                  apiGroup: rbac.authorization.k8s.io
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_1.html#_xml_and_yaml_dsl">doc</a>
     */
    @Test
    void testXmlDsl() {
        //language=xml
        rewriteRun(xml("""
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="myRoute">
                        <bean name="myBean" type="groovy" beanType="com.foo.MyBean">
                            <script>
                              <!-- groovy code here to create the bean -->
                            </script>
                        </bean>
                    </route>
                </routes>
                                                """, """
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="myRoute">
                        <bean name="myBean" type="com.foo.MyBean" scriptLanguage="groovy">
                            <script>
                              <!-- groovy code here to create the bean -->
                            </script>
                        </bean>
                    </route>
                </routes>                        
                """));
    }



}
