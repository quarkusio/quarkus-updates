package io.quarkus.updates.core;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.properties.Assertions.properties;

public class CoreUpdate330Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.30.alpha1.yaml"), "3.30.0")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testEnabledConfigChanges() {
        @Language("properties")
        String originalProperties = """
            quarkus.datasource.jdbc.enable-metrics=true
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.datasource.jdbc.metrics.enabled=true
            """;

        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }
}