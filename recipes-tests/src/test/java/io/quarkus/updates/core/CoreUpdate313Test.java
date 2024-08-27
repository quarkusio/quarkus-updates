package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

import java.nio.file.Path;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate313Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.13.alpha1.yaml"))
                .parser(JavaParser.fromJavaVersion()
                        .classpath("quarkus-test-common")
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.all());
    }

    @Test
    void testUpdateTestOIDCAuthServerUrl() {
        @Language("properties")
        String originalProperties = """
            %test.quarkus.oidc.auth-server-url=${keycloak.url}/realms/quarkus/
            """;

        @Language("properties")
        String afterProperties = """
            %test.quarkus.oidc.auth-server-url=${keycloak.url:replaced-by-test-resource}/realms/quarkus/
            """;

        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));

        @Language("properties")
        String unchangedWrongProfileOriginalProperties = """
            quarkus.oidc.auth-server-url=${keycloak.url}/realms/quarkus/
            """;

        rewriteRun(properties(unchangedWrongProfileOriginalProperties, spec -> spec.path("src/main/resources/application.properties")));

        @Language("properties")
        String unchangedWrongValueOriginalProperties = """
            %test.quarkus.oidc.auth-server-url=another-value
            """;

        rewriteRun(properties(unchangedWrongValueOriginalProperties, spec -> spec.path("src/main/resources/application.properties")));
    }
}
