package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate325Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.25.alpha1.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testDevUiSpiRelocations() {
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
                        <artifactId>quarkus-vertx-http-dev-ui-spi</artifactId>
                        <version>3.25.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-vertx-http-dev-ui-tests</artifactId>
                        <version>3.25.0</version>
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
                        <artifactId>quarkus-devui-deployment-spi</artifactId>
                        <version>3.25.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-devui-test-spi</artifactId>
                        <version>3.25.0</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

    @Test
    void testDevUiSpiLeftUntouchedWhenAlreadyRenamed() {
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
                        <artifactId>quarkus-devui-deployment-spi</artifactId>
                        <version>3.25.0</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }
}
