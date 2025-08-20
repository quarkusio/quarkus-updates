package io.quarkus.updates.core;

import static org.openrewrite.properties.Assertions.properties;

import java.nio.file.Path;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate326Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.26.alpha1.yaml"), "3.26.0.CR1")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testEnabledConfigChanges() {
        @Language("properties")
        String originalProperties = """
            quarkus.log.console.enable=true
            quarkus.log.console.async.enable=true
            quarkus.log.file.enable=true
            quarkus.log.file.async.enable=true
            quarkus.log.syslog.enable=true
            quarkus.log.syslog.async.enable=true
            quarkus.log.socket.enable=true
            quarkus.log.socket.async.enable=true
            quarkus.log.handler.console."myconsole".enable=true
            quarkus.log.handler.console."myconsole".async.enable=true
            quarkus.log.handler.file."myfile".enable=true
            quarkus.log.handler.file."myfile".async.enable=true
            quarkus.log.handler.syslog."mysyslog".enable=true
            quarkus.log.handler.syslog."mysyslog".async.enable=true
            quarkus.log.handler.socket."mysocket".enable=true
            quarkus.log.handler.socket."mysocket".async.enable=true
            quarkus.snapstart.enable=true
            quarkus.smallrye-health.ui.enable=true
            quarkus.smallrye-graphql.ui.enable=true
            quarkus.smallrye-openapi.enable=true
            quarkus.swagger-ui.enable=true
            quarkus.keycloak.policy-enforcer.enable=true
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.log.console.enabled=true
            quarkus.log.console.async.enabled=true
            quarkus.log.file.enabled=true
            quarkus.log.file.async.enabled=true
            quarkus.log.syslog.enabled=true
            quarkus.log.syslog.async.enabled=true
            quarkus.log.socket.enabled=true
            quarkus.log.socket.async.enabled=true
            quarkus.log.handler.console."myconsole".enabled=true
            quarkus.log.handler.console."myconsole".async.enabled=true
            quarkus.log.handler.file."myfile".enabled=true
            quarkus.log.handler.file."myfile".async.enabled=true
            quarkus.log.handler.syslog."mysyslog".enabled=true
            quarkus.log.handler.syslog."mysyslog".async.enabled=true
            quarkus.log.handler.socket."mysocket".enabled=true
            quarkus.log.handler.socket."mysocket".async.enabled=true
            quarkus.snapstart.enabled=true
            quarkus.smallrye-health.ui.enabled=true
            quarkus.smallrye-graphql.ui.enabled=true
            quarkus.smallrye-openapi.enabled=true
            quarkus.swagger-ui.enabled=true
            quarkus.keycloak.policy-enforcer.enabled=true
            """;

        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }
}
