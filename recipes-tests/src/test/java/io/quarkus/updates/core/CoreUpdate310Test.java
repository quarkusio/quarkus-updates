package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;

import java.nio.file.Path;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate310Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.10.alpha1.yaml"), "3.10.0.CR1")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testPackageNativeUpdated() {
        //language=xml
        rewriteRun(pomXml("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.acme</groupId>
                    <artifactId>code-with-quarkus</artifactId>
                    <version>1.0.0-SNAPSHOT</version>

                    <properties>
                        <compiler-plugin.version>3.12.1</compiler-plugin.version>
                        <maven.compiler.release>17</maven.compiler.release>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
                        <quarkus.platform.version>3.9.5</quarkus.platform.version>
                        <skipITs>true</skipITs>
                        <surefire-plugin.version>3.2.5</surefire-plugin.version>
                    </properties>

                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>${quarkus.platform.group-id}</groupId>
                                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                                <version>${quarkus.platform.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>

                    <dependencies>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-arc</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-rest</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-junit5</artifactId>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>io.rest-assured</groupId>
                            <artifactId>rest-assured</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>${quarkus.platform.group-id}</groupId>
                                <artifactId>quarkus-maven-plugin</artifactId>
                                <version>${quarkus.platform.version}</version>
                                <extensions>true</extensions>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>build</goal>
                                            <goal>generate-code</goal>
                                            <goal>generate-code-tests</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
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
                            <plugin>
                                <artifactId>maven-failsafe-plugin</artifactId>
                                <version>${surefire-plugin.version}</version>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>integration-test</goal>
                                            <goal>verify</goal>
                                        </goals>
                                    </execution>
                                </executions>
                                <configuration>
                                    <systemPropertyVariables>
                                        <native.image.path>${project.build.directory}/${project.build.finalName}-runner</native.image.path>
                                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                                        <maven.home>${maven.home}</maven.home>
                                    </systemPropertyVariables>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>

                    <profiles>
                        <profile>
                            <id>fast-jar</id>
                            <activation>
                                <property>
                                    <name>fast-jar</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.type>fast-jar</quarkus.package.type>
                                <quarkus.package.create-appcds>true</quarkus.package.create-appcds>
                                <quarkus.package.compress-jar>true</quarkus.package.compress-jar>
                            </properties>
                        </profile>
                        <profile>
                            <id>uber-jar</id>
                            <activation>
                                <property>
                                    <name>uber-jar</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.type>uber-jar</quarkus.package.type>
                            </properties>
                        </profile>
                        <profile>
                            <id>mutable-jar</id>
                            <activation>
                                <property>
                                    <name>uber-jar</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.type>mutable-jar</quarkus.package.type>
                            </properties>
                        </profile>
                        <profile>
                            <id>native</id>
                            <activation>
                                <property>
                                    <name>native</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.type>native</quarkus.package.type>
                            </properties>
                        </profile>
                        <profile>
                            <id>native-sources</id>
                            <activation>
                                <property>
                                    <name>native-sources</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.type>native-sources</quarkus.package.type>
                            </properties>
                        </profile>
                    </profiles>
                </project>
                """,
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.acme</groupId>
                    <artifactId>code-with-quarkus</artifactId>
                    <version>1.0.0-SNAPSHOT</version>

                    <properties>
                        <compiler-plugin.version>3.12.1</compiler-plugin.version>
                        <maven.compiler.release>17</maven.compiler.release>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
                        <quarkus.platform.version>3.10.0.CR1</quarkus.platform.version>
                        <skipITs>true</skipITs>
                        <surefire-plugin.version>3.2.5</surefire-plugin.version>
                    </properties>

                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>${quarkus.platform.group-id}</groupId>
                                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                                <version>${quarkus.platform.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>

                    <dependencies>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-arc</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-rest</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-junit5</artifactId>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>io.rest-assured</groupId>
                            <artifactId>rest-assured</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>${quarkus.platform.group-id}</groupId>
                                <artifactId>quarkus-maven-plugin</artifactId>
                                <version>${quarkus.platform.version}</version>
                                <extensions>true</extensions>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>build</goal>
                                            <goal>generate-code</goal>
                                            <goal>generate-code-tests</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
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
                            <plugin>
                                <artifactId>maven-failsafe-plugin</artifactId>
                                <version>${surefire-plugin.version}</version>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>integration-test</goal>
                                            <goal>verify</goal>
                                        </goals>
                                    </execution>
                                </executions>
                                <configuration>
                                    <systemPropertyVariables>
                                        <native.image.path>${project.build.directory}/${project.build.finalName}-runner</native.image.path>
                                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                                        <maven.home>${maven.home}</maven.home>
                                    </systemPropertyVariables>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>

                    <profiles>
                        <profile>
                            <id>fast-jar</id>
                            <activation>
                                <property>
                                    <name>fast-jar</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.jar.appcds.enabled>true</quarkus.package.jar.appcds.enabled>
                                <quarkus.package.jar.compress>true</quarkus.package.jar.compress>
                                <quarkus.package.jar.type>fast-jar</quarkus.package.jar.type>
                            </properties>
                        </profile>
                        <profile>
                            <id>uber-jar</id>
                            <activation>
                                <property>
                                    <name>uber-jar</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.jar.type>uber-jar</quarkus.package.jar.type>
                            </properties>
                        </profile>
                        <profile>
                            <id>mutable-jar</id>
                            <activation>
                                <property>
                                    <name>uber-jar</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.package.jar.type>mutable-jar</quarkus.package.jar.type>
                            </properties>
                        </profile>
                        <profile>
                            <id>native</id>
                            <activation>
                                <property>
                                    <name>native</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.native.enabled>true</quarkus.native.enabled>
                                <quarkus.package.jar.enabled>false</quarkus.package.jar.enabled>
                            </properties>
                        </profile>
                        <profile>
                            <id>native-sources</id>
                            <activation>
                                <property>
                                    <name>native-sources</name>
                                </property>
                            </activation>
                            <properties>
                                <skipITs>false</skipITs>
                                <quarkus.native.enabled>true</quarkus.native.enabled>
                                <quarkus.native.sources-only>true</quarkus.native.sources-only>
                                <quarkus.package.jar.enabled>false</quarkus.package.jar.enabled>
                            </properties>
                        </profile>
                    </profiles>
                </project>
                """));
    }

    @Test
    void testConfigFastJar() {
        @Language("properties")
        String originalProperties = """
            quarkus.package.type=fast-jar
            quarkus.package.manifest.attributes.key1=value1
            quarkus.package.manifest.attributes.key2=value2
            quarkus.package.manifest.sections."section1"."key1"=value11
            quarkus.package.manifest.sections."section1"."key2"=value12
            quarkus.package.manifest.sections."section2"."key1"=value21
            quarkus.package.manifest.sections."section2"."key2"=value22
            quarkus.package.manifest.add-implementation-entries=true
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.package.jar.type=fast-jar
            quarkus.package.jar.manifest.attributes.key1=value1
            quarkus.package.jar.manifest.attributes.key2=value2
            quarkus.package.jar.manifest.sections."section1"."key1"=value11
            quarkus.package.jar.manifest.sections."section1"."key2"=value12
            quarkus.package.jar.manifest.sections."section2"."key1"=value21
            quarkus.package.jar.manifest.sections."section2"."key2"=value22
            quarkus.package.jar.manifest.add-implementation-entries=true
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testConfigUberJar() {
        @Language("properties")
        String originalProperties = """
            quarkus.package.type=uber-jar
            quarkus.package.add-runner-suffix=false
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.package.jar.add-runner-suffix=false
            quarkus.package.jar.type=uber-jar
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testConfigJar() {
        @Language("properties")
        String originalProperties = """
            quarkus.package.type=jar
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.package.jar.type=jar
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testConfigMutableJar() {
        @Language("properties")
        String originalProperties = """
            quarkus.package.type=mutable-jar
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.package.jar.type=mutable-jar
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testConfigNative() {
        @Language("properties")
        String originalProperties = """
            quarkus.package.type=native
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.native.enabled=true
            quarkus.package.jar.enabled=false
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testConfigNativeSources() {
        @Language("properties")
        String originalProperties = """
            quarkus.package.type=native-sources
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.native.enabled=true
            quarkus.native.sources-only=true
            quarkus.package.jar.enabled=false
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    @Disabled("Somehow, the change is not applied in tests")
    void testFlywayPostgreSQL() {
        //language=xml
        rewriteRun(pomXml("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.acme</groupId>
                    <artifactId>code-with-quarkus</artifactId>
                    <version>1.0.0-SNAPSHOT</version>

                    <properties>
                        <compiler-plugin.version>3.12.1</compiler-plugin.version>
                        <maven.compiler.release>17</maven.compiler.release>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
                        <quarkus.platform.version>3.9.5</quarkus.platform.version>
                        <skipITs>true</skipITs>
                        <surefire-plugin.version>3.2.5</surefire-plugin.version>
                    </properties>

                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>${quarkus.platform.group-id}</groupId>
                                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                                <version>${quarkus.platform.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>

                    <dependencies>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-flyway</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-jdbc-postgresql</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-junit5</artifactId>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>io.rest-assured</groupId>
                            <artifactId>rest-assured</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
                """,
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.acme</groupId>
                    <artifactId>code-with-quarkus</artifactId>
                    <version>1.0.0-SNAPSHOT</version>

                    <properties>
                        <compiler-plugin.version>3.12.1</compiler-plugin.version>
                        <maven.compiler.release>17</maven.compiler.release>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
                        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
                        <quarkus.platform.version>3.10.0.CR1</quarkus.platform.version>
                        <skipITs>true</skipITs>
                        <surefire-plugin.version>3.2.5</surefire-plugin.version>
                    </properties>

                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>${quarkus.platform.group-id}</groupId>
                                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                                <version>${quarkus.platform.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>

                    <dependencies>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-flyway</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-jdbc-postgresql</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.flywaydb</groupId>
                            <artifactId>flyway-database-postgresql</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-junit5</artifactId>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>io.rest-assured</groupId>
                            <artifactId>rest-assured</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
                """));
    }
}
