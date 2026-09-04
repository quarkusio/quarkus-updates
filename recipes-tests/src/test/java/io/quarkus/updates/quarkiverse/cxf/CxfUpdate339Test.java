package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;
import static org.openrewrite.maven.Assertions.pomXml;

public class CxfUpdate339Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.39.yaml"))
                .parser(JavaParser.fromJavaVersion()
                        .classpath(JavaParser.runtimeClasspath())
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void addQuarkusJacksonWhenJacksonIsUsed() {
        // quarkus-cxf 3.39.0 does not bring quarkus-jackson transitively anymore; the added
        // quarkus-jackson version is resolved dynamically (latest.release), asserted by pattern
        rewriteRun(
                mavenProject("test-project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                                import com.fasterxml.jackson.databind.ObjectMapper;

                                                public class UsesJackson {
                                                    ObjectMapper mapper = new ObjectMapper();
                                                }
                                                """)),
                        //language=xml
                        pomXml(
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
                                                    <version>3.39.0</version>
                                                </dependency>
                                            </dependencies>
                                        </project>
                                        """,
                                spec -> spec.after(pom -> {
                                    assertThat(pom)
                                            .contains("<groupId>io.quarkus</groupId>")
                                            .contains("<artifactId>quarkus-jackson</artifactId>");
                                    return pom;
                                }))));
    }

    @Test
    void addQuarkusJacksonWithoutVersionWhenManagedByImportedBom() {
        // quarkus-jackson is managed by the imported quarkus-bom, so AddDependency must not
        // write a hard coded <version> resolved from latest.release into the pom
        rewriteRun(
                mavenProject("test-project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                                import com.fasterxml.jackson.databind.ObjectMapper;

                                                public class UsesJackson {
                                                    ObjectMapper mapper = new ObjectMapper();
                                                }
                                                """)),
                        //language=xml
                        pomXml(
                                """
                                        <project>
                                            <modelVersion>4.0.0</modelVersion>
                                            <groupId>org.acme</groupId>
                                            <artifactId>test-project</artifactId>
                                            <version>1.0.0-SNAPSHOT</version>
                                            <dependencyManagement>
                                                <dependencies>
                                                    <dependency>
                                                        <groupId>io.quarkus</groupId>
                                                        <artifactId>quarkus-bom</artifactId>
                                                        <version>3.39.1</version>
                                                        <type>pom</type>
                                                        <scope>import</scope>
                                                    </dependency>
                                                </dependencies>
                                            </dependencyManagement>
                                            <dependencies>
                                                <dependency>
                                                    <groupId>io.quarkiverse.cxf</groupId>
                                                    <artifactId>quarkus-cxf</artifactId>
                                                    <version>3.39.0</version>
                                                </dependency>
                                            </dependencies>
                                        </project>
                                        """,
                                spec -> spec.after(pom -> {
                                    assertThat(pom)
                                            .contains("<artifactId>quarkus-jackson</artifactId>")
                                            .doesNotContainPattern("quarkus-jackson</artifactId>\\s*<version>");
                                    return pom;
                                }))));
    }

    @Test
    void noChangeWhenJacksonStillTransitive() {
        // quarkus-cxf 3.38.0 still pulls quarkus-jackson transitively, nothing to add
        rewriteRun(
                mavenProject("test-project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                                import com.fasterxml.jackson.databind.ObjectMapper;

                                                public class UsesJackson {
                                                    ObjectMapper mapper = new ObjectMapper();
                                                }
                                                """)),
                        //language=xml
                        pomXml(
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
                                                    <version>3.38.0</version>
                                                </dependency>
                                            </dependencies>
                                        </project>
                                        """)));
    }

    @Test
    void noChangeWhenJacksonNotUsed() {
        rewriteRun(
                mavenProject("test-project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                                public class NoJackson {
                                                }
                                                """)),
                        //language=xml
                        pomXml(
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
                                                    <version>3.38.0</version>
                                                </dependency>
                                            </dependencies>
                                        </project>
                                        """)));
    }
}
