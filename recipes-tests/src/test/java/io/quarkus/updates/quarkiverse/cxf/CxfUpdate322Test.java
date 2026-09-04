package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;

public class CxfUpdate322Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.22.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void removeHc5Dependency() {
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
                                    <version>3.21.0</version>
                                </dependency>
                                <dependency>
                                    <groupId>io.quarkiverse.cxf</groupId>
                                    <artifactId>quarkus-cxf-rt-transports-http-hc5</artifactId>
                                    <version>3.21.0</version>
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
                                    <version>3.21.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """));
    }

    @Test
    void deleteHttpConduitFactoryProperty() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.http-conduit-factory=HttpClientHTTPConduitFactory
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        """
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void deletePerClientHttpConduitFactoryProperty() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.http-conduit-factory=HttpClientHTTPConduitFactory
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        """
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void keepUrlConnectionHttpConduitFactoryProperty() {
        // URLConnectionHTTPConduitFactory stays fully supported, only the removed
        // HttpClientHTTPConduitFactory value may be dropped
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.http-conduit-factory=URLConnectionHTTPConduitFactory
                                quarkus.cxf.client.myService.http-conduit-factory=VertxHttpClientHTTPConduitFactory
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
