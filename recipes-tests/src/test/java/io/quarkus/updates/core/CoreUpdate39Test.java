package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate39Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.9.yaml"), "3.9.0")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testPanacheAnnotationProcessorRemoved() {
        //language=xml
        rewriteRun(pomXml("""
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>io.quarkus.bot</groupId>
                    <artifactId>release</artifactId>
                    <version>999-SNAPSHOT</version>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <configuration>
                                    <annotationProcessorPaths>
                                        <path>
                                            <groupId>org.hibernate.orm</groupId>
                                            <artifactId>hibernate-jpamodelgen</artifactId>
                                        </path>
                                        <path>
                                            <groupId>io.quarkus</groupId>
                                            <artifactId>quarkus-panache-common</artifactId>
                                        </path>
                                    </annotationProcessorPaths>
                                    <annotationProcessors>
                                        <annotationProcessor>io.something.Class</annotationProcessor>
                                        <annotationProcessor>io.quarkus.panache.common.runtime.PanacheAnnotationProcessor</annotationProcessor>
                                    </annotationProcessors>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>io.quarkus.bot</groupId>
                    <artifactId>release</artifactId>
                    <version>999-SNAPSHOT</version>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <configuration>
                                    <annotationProcessorPaths>
                                        <path>
                                            <groupId>org.hibernate.orm</groupId>
                                            <artifactId>hibernate-jpamodelgen</artifactId>
                                        </path>
                                    </annotationProcessorPaths>
                                    <annotationProcessors>
                                        <annotationProcessor>io.something.Class</annotationProcessor>
                                    </annotationProcessors>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """));
    }
}
