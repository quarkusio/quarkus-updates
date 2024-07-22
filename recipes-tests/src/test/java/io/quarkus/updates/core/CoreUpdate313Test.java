package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;

import java.nio.file.Path;

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
    void testWithTestResource() {
        //language=java
        rewriteRun(java(
                """
                    package io.quarkiverse.githubapp.test;

                    import io.quarkus.test.common.QuarkusTestResource;

                    @QuarkusTestResource
                    public class GitHubAppTest {
                    }
                """,
                """
                    package io.quarkiverse.githubapp.test;

                    import io.quarkus.test.common.WithTestResource;

                    @WithTestResource(restrictToAnnotatedClass = false)
                    public class GitHubAppTest {
                    }
                """));

        //language=java
        rewriteRun(java(
            """
                package io.quarkiverse.githubapp.test;

                import io.quarkus.test.common.QuarkusTestResource;

                @QuarkusTestResource(restrictToAnnotatedClass = false)
                public class GitHubAppTest {
                }
            """,
            """
                package io.quarkiverse.githubapp.test;

                import io.quarkus.test.common.WithTestResource;

                @WithTestResource(restrictToAnnotatedClass = false)
                public class GitHubAppTest {
                }
            """));

        //language=java
        rewriteRun(java(
            """
                package io.quarkiverse.githubapp.test;

                import io.quarkus.test.common.QuarkusTestResource;

                @QuarkusTestResource(restrictToAnnotatedClass = true)
                class GitHubAppTest {
                }
            """,
            """
                package io.quarkiverse.githubapp.test;

                import io.quarkus.test.common.WithTestResource;

                @WithTestResource(restrictToAnnotatedClass = true)
                class GitHubAppTest {
                }
            """));
    }
}
