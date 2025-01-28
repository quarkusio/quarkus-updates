package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate33Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.3.alpha1.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testGraalVMSDK() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>
                <dependencies>
                    <dependency>
                        <groupId>org.graalvm.nativeimage</groupId>
                        <artifactId>svm</artifactId>
                        <version>23.1.1</version>
                    </dependency>
                </dependencies>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>
                <dependencies>
                    <dependency>
                        <groupId>org.graalvm.sdk</groupId>
                        <artifactId>graal-sdk</artifactId>
                        <version>23.1.1</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

}
