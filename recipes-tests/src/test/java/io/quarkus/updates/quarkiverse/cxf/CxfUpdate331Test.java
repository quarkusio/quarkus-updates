package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.properties.Assertions.properties;

/**
 * The 3.31 recipe is advisory only: it adds a single deprecation warning comment at the top of a
 * properties file that uses the deprecated proxy options and never changes the configuration itself.
 */
public class CxfUpdate331Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.31.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void commentsFileWithProxyOptions() {
        // profile scoped keys count as well; the configuration itself is untouched
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.proxy-server=proxy.example.com
                                quarkus.cxf.client.myService.proxy-server-port=3128
                                %dev.quarkus.cxf.client.myService.non-proxy-hosts=localhost|*.example.com
                                """,
                        """
                                # Deprecation warning: this file uses deprecated quarkus-cxf proxy options (proxy-server*, proxy-username, proxy-password, non-proxy-hosts); they will be removed in quarkus-cxf 4.0.0 - migrate them to quarkus.proxy.* referenced via quarkus.cxf.client."client-name".proxy-configuration-name, see https://docs.quarkiverse.io/quarkus-cxf/dev/release-notes/3.31.1.html
                                quarkus.cxf.client.myService.proxy-server=proxy.example.com
                                quarkus.cxf.client.myService.proxy-server-port=3128
                                %dev.quarkus.cxf.client.myService.non-proxy-hosts=localhost|*.example.com
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void alreadyMarkedFileStaysUntouched() {
        // rerunning the recipe must not stack further comments
        //language=properties
        rewriteRun(
                properties(
                        """
                                # Deprecation warning: this file uses deprecated quarkus-cxf proxy options (proxy-server*, proxy-username, proxy-password, non-proxy-hosts); they will be removed in quarkus-cxf 4.0.0 - migrate them to quarkus.proxy.* referenced via quarkus.cxf.client."client-name".proxy-configuration-name, see https://docs.quarkiverse.io/quarkus-cxf/dev/release-notes/3.31.1.html
                                quarkus.cxf.client.myService.proxy-server=proxy.example.com
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWithoutDeprecatedProxyOptions() {
        // proxy-configuration-name is the replacement, not a deprecated option
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.client-endpoint-url=https://localhost:8444/services/hello
                                quarkus.cxf.client.myService.proxy-configuration-name=corporate
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
