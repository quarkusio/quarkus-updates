package io.quarkus.updates.quarkiverse.minio;

import io.quarkus.updates.core.CoreTestUtil;
import io.quarkus.updates.quarkiverse.minio.minio38.AdjustURLPropertyValue;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

public class MinioUpdate38Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.minio", "quarkus-minio", "3.8.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testPropertiesFileUpdate() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.minio.url=localhost
                                %dev.quarkus.minio.url=localhost
                                %dev.quarkus.minio.named.url=localhost
                                another.property=another-value
                                """,
                        """
                                quarkus.minio.host=localhost
                                %dev.quarkus.minio.host=localhost
                                %dev.quarkus.minio.named.host=localhost
                                another.property=another-value
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void testYamlFileUpdate() {
        //language=yaml
        rewriteRun(
                yaml(
                        """
                                quarkus:
                                  minio:
                                    url: localhost
                                  another.property: another-value
                                
                                "%dev":
                                  quarkus:
                                    minio:
                                      url: localhost
                                    another.property: another-value
                                """,
                        """
                                quarkus:
                                  another.property: another-value
                                  minio:
                                    host: localhost
                                
                                "%dev":
                                  quarkus:
                                    another.property: another-value
                                    minio:
                                      host: localhost
                                """,
                        spec -> spec.path("src/main/resources/application.yaml"))
        );
    }

    @Test
    void testAdjustInjection() {
        //language=java
        rewriteRun(
                spec -> spec.recipe(new AdjustURLPropertyValue())
                        .parser(JavaParser.fromJavaVersion()
                                .logCompilationWarningsAndErrors(true)
                                .classpath("microprofile-config-api")),
                java(
                """
                       import org.eclipse.microprofile.config.inject.ConfigProperty;
                       
                       public class ForTestingPurpose {
                       
                           @ConfigProperty(name = "quarkus.minio.url")
                           String minioUrl;
                       
                       
                           private String toString(@ConfigProperty(name = "quarkus.minio.url") String param) {
                               return minioUrl;
                           }
                       
                       }
                       """,
                """
                      import org.eclipse.microprofile.config.inject.ConfigProperty;
                      
                      public class ForTestingPurpose {
                      
                          @ConfigProperty(name = "quarkus.minio.host")
                          String minioUrl;
                          
                          
                          private String toString(@ConfigProperty(name = "quarkus.minio.host") String param) {
                              return minioUrl;
                          }
                      
                      }
                      """));
    }
}
