package io.quarkus.updates.camel30;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.openrewrite.yaml.Assertions;

public class CamelYamlTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testStepsToFrom1() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                  route:
                    from:
                      uri: "direct:info"
                    steps:
                      log: "message"
                """, """
                  route:
                    from:
                      uri: "direct:info"
                      steps:
                        log: "message"
                """));
    }

    @Test
    void testStepsToFrom2() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                    from:
                      uri: "direct:info"
                    steps:
                      log: "message"
                """, """
                    from:
                      uri: "direct:info"
                      steps:
                        log: "message"
                """));
    }

    @Test
    void testStepsToFrom3() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - from:
                    uri: "direct:start"
                  steps:
                  - filter:
                      expression:
                        simple: "${in.header.continue} == true"
                      steps:
                        - to:
                            uri: "log:filtered"
                  - to:
                      uri: "log:original"
                """, """
                  - from:
                      uri: "direct:start"
                      steps:
                        - filter:
                            expression:
                              simple: "${in.header.continue} == true"
                            steps:
                              - to:
                                  uri: "log:filtered"
                        - to:
                            uri: "log:original"
                """));
    }

    @Test
    void testRouteConfigurationWithOnException() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - route-configuration:
                    - id: "yamlRouteConfiguration"
                    - on-exception:
                        handled:
                          constant: "true"
                        exception:
                          - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                        steps:
                          - set-body:
                              constant:
                                  expression: "onException has been triggered in yamlRouteConfiguration"
                """, """
                  - route-configuration:
                      id: "yamlRouteConfiguration"
                      on-exception:
                        - on-exception:
                            handled:
                              constant: "true"
                            exception:
                              - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                            steps:
                              - set-body:
                                  constant:
                                    expression: "onException has been triggered in yamlRouteConfiguration"
                """));
    }

    @Test
    void testRouteConfigurationWithoutOnException() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - route-configuration:
                    - id: "__id"
                """, """
                  - route-configuration:
                      id: "__id"
                """));
    }

    @Test
    void testDoubleDocument() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - route-configuration:
                    - id: "yamlRouteConfiguration1"
                    - on-exception:
                        handled:
                          constant: "true"
                        exception:
                          - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                        steps:
                          - set-body:
                              constant:
                                  expression: "onException has been triggered in yamlRouteConfiguration"
                ---
                - route-configuration:
                    - id: "yamlRouteConfiguration2"
                    - on-exception:
                        handled:
                          constant: "true"
                        exception:
                          - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                        steps:
                          - set-body:
                              constant:
                                  expression: "onException has been triggered in yamlRouteConfiguration"
                """, """
                  - route-configuration:
                      id: "yamlRouteConfiguration1"
                      on-exception:
                        - on-exception:
                            handled:
                              constant: "true"
                            exception:
                              - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                            steps:
                              - set-body:
                                  constant:
                                    expression: "onException has been triggered in yamlRouteConfiguration"
                  ---
                  - route-configuration:
                      id: "yamlRouteConfiguration2"
                      on-exception:
                        - on-exception:
                            handled:
                              constant: "true"
                            exception:
                              - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                            steps:
                              - set-body:
                                  constant:
                                    expression: "onException has been triggered in yamlRouteConfiguration"
                """));
    }

    @Test
    void testDoubleDocumentSimple() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - route-configuration:
                    - id: "__id1"
                ---
                - route-configuration:
                    - id: "__id2"
                """, """
                  - route-configuration:
                      id: "__id1"
                  ---
                  - route-configuration:
                      id: "__id2"
                """));
    }

    @Test
    void testRouteConfigurationIdempotent() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                  - route-configuration:
                      id: "yamlRouteConfiguration"
                      on-exception:
                        - on-exception:
                            handled:
                              constant: "true"
                            exception:
                              - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                            steps:
                              - set-body:
                                  constant:
                                    expression: "onException has been triggered in yamlRouteConfiguration"
                """));
    }
}
