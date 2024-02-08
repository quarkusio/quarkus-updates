package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate30Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.0.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testExtensionUpdate() {
        //language=java
        rewriteRun(java(
                """
                    package io.quarkiverse.githubapp.deployment;

                    import javax.enterprise.event.Event;
                    import javax.enterprise.util.AnnotationLiteral;
                    import javax.inject.Inject;
                    import javax.inject.Singleton;

                    class GitHubAppProcessor {
                    }
                """,
                """
                    package io.quarkiverse.githubapp.deployment;

                    import jakarta.enterprise.event.Event;
                    import jakarta.enterprise.util.AnnotationLiteral;
                    import jakarta.inject.Inject;
                    import jakarta.inject.Singleton;

                    class GitHubAppProcessor {
                    }
                """));
    }
}
