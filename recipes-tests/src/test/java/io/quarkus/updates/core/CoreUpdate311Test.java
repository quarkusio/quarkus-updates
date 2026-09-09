package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate311Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.11.alpha1.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testWebDependencyLocatorRelocations() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-webjars-locator</artifactId>
                        <version>3.11.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-webjars-locator-deployment</artifactId>
                        <version>3.11.0</version>
                    </dependency>
                </dependencies>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-web-dependency-locator</artifactId>
                        <version>3.11.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-web-dependency-locator-deployment</artifactId>
                        <version>3.11.0</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

    @Test
    void testWebDependencyLocatorLeftUntouchedWhenAlreadyRenamed() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-web-dependency-locator</artifactId>
                        <version>3.11.0</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }
}
