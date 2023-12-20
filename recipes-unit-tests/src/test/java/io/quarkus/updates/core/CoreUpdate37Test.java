package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate37Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.7.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testMavenCompilerJavaVersion() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>11</maven.compiler.release>
                    <maven.compiler.target>11</maven.compiler.target>
                    <maven.compiler.source>11</maven.compiler.source>
                </properties>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>17</maven.compiler.release>
                    <maven.compiler.target>17</maven.compiler.target>
                    <maven.compiler.source>17</maven.compiler.source>
                </properties>
            </project>
            """));

        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>21</maven.compiler.release>
                    <maven.compiler.target>21</maven.compiler.target>
                    <maven.compiler.source>21</maven.compiler.source>
                </properties>
            </project>
            """));
    }

    @Test
    void testMavenPluginUpdates() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>

                <properties>
                    <compiler-plugin.version>3.11.0</compiler-plugin.version>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                    <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                    <quarkus.platform.group-id>io.quarkus</quarkus.platform.group-id>
                    <quarkus.platform.version>3.6.4</quarkus.platform.version>
                    <skipITs>true</skipITs>
                    <surefire-plugin.version>3.1.2</surefire-plugin.version>
                </properties>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>${compiler-plugin.version}</version>
                            <configuration>
                                <compilerArgs>
                                    <arg>-parameters</arg>
                                </compilerArgs>
                            </configuration>
                        </plugin>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>${surefire-plugin.version}</version>
                            <configuration>
                                <systemPropertyVariables>
                                    <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                                    <maven.home>${maven.home}</maven.home>
                                </systemPropertyVariables>
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

                <properties>
                    <compiler-plugin.version>3.12.0</compiler-plugin.version>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                    <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                    <quarkus.platform.group-id>io.quarkus</quarkus.platform.group-id>
                    <quarkus.platform.version>3.6.4</quarkus.platform.version>
                    <skipITs>true</skipITs>
                    <surefire-plugin.version>3.2.3</surefire-plugin.version>
                </properties>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>${compiler-plugin.version}</version>
                            <configuration>
                                <compilerArgs>
                                    <arg>-parameters</arg>
                                </compilerArgs>
                            </configuration>
                        </plugin>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>${surefire-plugin.version}</version>
                            <configuration>
                                <systemPropertyVariables>
                                    <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                                    <maven.home>${maven.home}</maven.home>
                                </systemPropertyVariables>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testMavenPluginUpdatesDefaultGroupId() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>

                <properties>
                    <compiler-plugin.version>3.11.0</compiler-plugin.version>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                    <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                    <quarkus.platform.group-id>io.quarkus</quarkus.platform.group-id>
                    <quarkus.platform.version>3.6.4</quarkus.platform.version>
                    <skipITs>true</skipITs>
                    <surefire-plugin.version>3.1.2</surefire-plugin.version>
                </properties>

                <build>
                    <plugins>
                        <plugin>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>${compiler-plugin.version}</version>
                            <configuration>
                                <compilerArgs>
                                    <arg>-parameters</arg>
                                </compilerArgs>
                            </configuration>
                        </plugin>
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>${surefire-plugin.version}</version>
                            <configuration>
                                <systemPropertyVariables>
                                    <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                                    <maven.home>${maven.home}</maven.home>
                                </systemPropertyVariables>
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

                <properties>
                    <compiler-plugin.version>3.12.0</compiler-plugin.version>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                    <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                    <quarkus.platform.group-id>io.quarkus</quarkus.platform.group-id>
                    <quarkus.platform.version>3.6.4</quarkus.platform.version>
                    <skipITs>true</skipITs>
                    <surefire-plugin.version>3.2.3</surefire-plugin.version>
                </properties>

                <build>
                    <plugins>
                        <plugin>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>${compiler-plugin.version}</version>
                            <configuration>
                                <compilerArgs>
                                    <arg>-parameters</arg>
                                </compilerArgs>
                            </configuration>
                        </plugin>
                        <plugin>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>${surefire-plugin.version}</version>
                            <configuration>
                                <systemPropertyVariables>
                                    <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                                    <maven.home>${maven.home}</maven.home>
                                </systemPropertyVariables>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }
}
