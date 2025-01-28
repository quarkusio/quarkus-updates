package io.quarkus.updates.core;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.properties.Assertions.properties;

public class CoreUpdate318Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.18.alpha1.yaml"), "3.18.0")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testFlywayConfigurationUpdated() {

        @Language("properties")
        String originalProperties = """
            quarkus.flyway."datasource-name".clean-on-validation-error=true
            %test.quarkus.flyway."datasource-name".clean-on-validation-error=true
            quarkus.flyway.clean-on-validation-error=true
            """;

        @Language("properties")
        String afterProperties = """
            """;

        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }
}
