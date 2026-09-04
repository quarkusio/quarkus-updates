package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.properties.Assertions.properties;

public class CxfUpdate320Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.20.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void renameUsernameAndPassword() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.username=admin
                                quarkus.cxf.client.myService.password=secret
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        """
                                quarkus.cxf.client.myService.auth.username=admin
                                quarkus.cxf.client.myService.auth.password=secret
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void renameWithProfile() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                %dev.quarkus.cxf.client.myService.username=devuser
                                %dev.quarkus.cxf.client.myService.password=devpass
                                """,
                        """
                                %dev.quarkus.cxf.client.myService.auth.username=devuser
                                %dev.quarkus.cxf.client.myService.auth.password=devpass
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeForAlreadyMigratedOrOtherKeys() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.auth.username=admin
                                quarkus.cxf.client.myService.proxy-username=proxyuser
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
