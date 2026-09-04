package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.maven.Assertions.pomXml;

public class CxfUpdate38Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.8.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void removeLoggingExtension() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>test-project</artifactId>
                            <version>1.0.0-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>io.quarkiverse.cxf</groupId>
                                    <artifactId>quarkus-cxf</artifactId>
                                    <version>2.7.0</version>
                                </dependency>
                                <dependency>
                                    <groupId>io.quarkiverse.cxf</groupId>
                                    <artifactId>quarkus-cxf-rt-features-logging</artifactId>
                                    <version>2.7.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>test-project</artifactId>
                            <version>1.0.0-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>io.quarkiverse.cxf</groupId>
                                    <artifactId>quarkus-cxf</artifactId>
                                    <version>2.7.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """));
    }

}
