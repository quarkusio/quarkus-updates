package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate331Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.31.alpha1.yaml"), "3.31.0.CR1")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testAddExtensionsToQuarkusMavenPlugin() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testDoNotAddExtensionsIfAlreadyPresent() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testAddExtensionsWithGroupId() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>io.quarkus.platform</groupId>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>io.quarkus.platform</groupId>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testDoNotAffectOtherPlugins() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.11.0</version>
                        </plugin>
                        <plugin>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.11.0</version>
                        </plugin>
                        <plugin>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testExtensionsAddedBeforeConfiguration() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>io.quarkus.platform</groupId>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <executions>
                                <execution>
                                    <id>test</id>
                                </execution>
                            </executions>
                            <configuration>
                                <skip>false</skip>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>io.quarkus.platform</groupId>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                            <executions>
                                <execution>
                                    <id>test</id>
                                </execution>
                            </executions>
                            <configuration>
                                <skip>false</skip>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }
}
