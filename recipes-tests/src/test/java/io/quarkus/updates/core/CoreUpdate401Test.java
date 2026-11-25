package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class CoreUpdate401Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "4.0.1.yaml"));
    }

    @Test
    void testSpringTestDependencyRename() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0-SNAPSHOT</version>
                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-test-spring</artifactId>
                        <version>4.0.1</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0-SNAPSHOT</version>
                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-spring-test</artifactId>
                        <version>4.0.1</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """));
    }
}
