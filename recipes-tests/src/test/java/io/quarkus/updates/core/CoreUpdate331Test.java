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

    @Test
    void testJUnit5ToJupiterRelocations() {
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
                        <artifactId>quarkus-junit5</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5-mockito</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5-component</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5-config</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5-internal</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5-mockito-config</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus.junit5</groupId>
                        <artifactId>junit5-virtual-threads</artifactId>
                        <version>3.31.0.CR1</version>
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
                        <artifactId>quarkus-junit</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit-mockito</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit-component</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit-config</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit-internal</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit-mockito-config</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus.junit</groupId>
                        <artifactId>junit-virtual-threads</artifactId>
                        <version>3.31.0.CR1</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

    @Test
    void testTestcontainersPostgresqlArtifactRewrite() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>test-testcontainers</artifactId>
                <version>999-SNAPSHOT</version>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-bom</artifactId>
                            <version>3.30.6</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>

                <dependencies>
                    <dependency>
                        <groupId>org.testcontainers</groupId>
                        <artifactId>postgresql</artifactId>
                        <version>1.20.0</version>
                    </dependency>
                </dependencies>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>test-testcontainers</artifactId>
                <version>999-SNAPSHOT</version>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-bom</artifactId>
                            <version>3.31.0.CR1</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>

                <dependencies>
                    <dependency>
                        <groupId>org.testcontainers</groupId>
                        <artifactId>testcontainers-postgresql</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

    @Test
    void testDoNothingWhenQuarkusMavenPluginNotPresent() {
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
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                        </plugin>
                        <plugin>
                            <artifactId>maven-failsafe-plugin</artifactId>
                            <version>3.5.4</version>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testAddArgLineToSurefireWhenNoConfiguration() {
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
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
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
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>@{argLine}</argLine>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testAddArgLineToFailsafeWhenNoConfiguration() {
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
                        <plugin>
                            <artifactId>maven-failsafe-plugin</artifactId>
                            <version>3.5.4</version>
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
                        <plugin>
                            <artifactId>maven-failsafe-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>@{argLine}</argLine>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testAddArgLineToSurefireWhenConfigurationExistsButNoArgLine() {
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
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
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
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <skip>false</skip>
                                <argLine>@{argLine}</argLine>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testPrependArgLineToSurefireWhenArgLineExistsWithoutPlaceholder() {
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
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>-Xmx512m</argLine>
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
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>@{argLine} -Xmx512m</argLine>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testDoNotModifyArgLineWhenPlaceholderAlreadyPresent() {
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
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>@{argLine} -Xmx512m</argLine>
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
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>@{argLine} -Xmx512m</argLine>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testHandleBothSurefireAndFailsafeTogether() {
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
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>-Xmx512m</argLine>
                            </configuration>
                        </plugin>
                        <plugin>
                            <artifactId>maven-failsafe-plugin</artifactId>
                            <version>3.5.4</version>
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
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>@{argLine} -Xmx512m</argLine>
                            </configuration>
                        </plugin>
                        <plugin>
                            <artifactId>maven-failsafe-plugin</artifactId>
                            <version>3.5.4</version>
                            <configuration>
                                <argLine>@{argLine}</argLine>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }
}
