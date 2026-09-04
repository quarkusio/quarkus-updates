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
 * The 3.16 recipe is advisory only: it adds a single deprecation warning comment at the top of a
 * properties file that uses the deprecated TLS options and never changes the configuration itself.
 */
public class CxfUpdate316Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.16.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void commentsFileWithHostnameVerifier() {
        // profile scoped keys count as well; the configuration itself is untouched
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.hello.client-endpoint-url=https://localhost:8444/services/hello
                                %dev.quarkus.cxf.client.hello.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        """
                                # Deprecation warning: this file uses deprecated quarkus-cxf TLS options (hostname-verifier, trust-store*, key-store*); if they are not migrated to the Quarkus TLS registry, they can silently stop working in 4.x - see https://docs.quarkiverse.io/quarkus-cxf/dev/release-notes/3.16.0.html
                                quarkus.cxf.client.hello.client-endpoint-url=https://localhost:8444/services/hello
                                %dev.quarkus.cxf.client.hello.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void commentsFileWithStoreOptions() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.mtls.trust-store=client-truststore.p12
                                quarkus.cxf.client.mtls.key-store-password=secret
                                quarkus.cxf.client.mtls.key-password=keypass
                                """,
                        """
                                # Deprecation warning: this file uses deprecated quarkus-cxf TLS options (hostname-verifier, trust-store*, key-store*); if they are not migrated to the Quarkus TLS registry, they can silently stop working in 4.x - see https://docs.quarkiverse.io/quarkus-cxf/dev/release-notes/3.16.0.html
                                quarkus.cxf.client.mtls.trust-store=client-truststore.p12
                                quarkus.cxf.client.mtls.key-store-password=secret
                                quarkus.cxf.client.mtls.key-password=keypass
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
                                # Deprecation warning: this file uses deprecated quarkus-cxf TLS options (hostname-verifier, trust-store*, key-store*); if they are not migrated to the Quarkus TLS registry, they can silently stop working in 4.x - see https://docs.quarkiverse.io/quarkus-cxf/dev/release-notes/3.16.0.html
                                quarkus.cxf.client.hello.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWithoutAffectedOptions() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.hello.client-endpoint-url=https://localhost:8444/services/hello
                                quarkus.cxf.client.hello.auth.username=bob
                                quarkus.cxf.client.hello.tls-configuration-name=myTls
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
