package io.quarkus.updates.core;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

public class CoreUpdate319Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        @Language("java")
        String accessTokenAnnotation = """
                package io.quarkus.oidc.token.propagation;

                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ ElementType.TYPE })
                @Retention(RetentionPolicy.RUNTIME)
                @Documented
                public @interface AccessToken {
                    String exchangeTokenClient() default "";
                }
                """;
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.19.alpha1.yaml"), "3.19.0")
                .parser(JavaParser.fromJavaVersion()
                                .dependsOn(accessTokenAnnotation)
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.all());
    }

    @Test
    void testAccessTokenAnnotationMovedToNewPackage() {
        //language=java
        rewriteRun(java(
                """
                package io.quarkus.it.keycloak;

                import io.quarkus.oidc.token.propagation.AccessToken;

                @AccessToken
                public interface AccessTokenPropagationService {
                    String getUserName();
                }
                """,
                """
                package io.quarkus.it.keycloak;

                import io.quarkus.oidc.token.propagation.common.AccessToken;

                @AccessToken
                public interface AccessTokenPropagationService {
                    String getUserName();
                }
                """));
        //language=java
        rewriteRun(java(
                """
                package io.quarkus.it.keycloak;

                import io.quarkus.oidc.token.propagation.AccessToken;

                @AccessToken(exchangeTokenClient = "Default")
                public interface AccessTokenPropagationService {
                    String getUserName();
                }
                """,
                """
                package io.quarkus.it.keycloak;

                import io.quarkus.oidc.token.propagation.common.AccessToken;

                @AccessToken(exchangeTokenClient = "Default")
                public interface AccessTokenPropagationService {
                    String getUserName();
                }
                """));
    }

    @Test
    void testConfigurationPropertiesChange() {
        @Language("properties")
        String originalProperties = """
            quarkus.http.cors=false
            quarkus.log.console.json=false
            quarkus.log.syslog.json=true
            quarkus.log.file.json=false
            quarkus.log.socket.json=true
            quarkus.log.foobar.json=true
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.http.cors.enabled=false
            quarkus.log.console.json.enabled=false
            quarkus.log.syslog.json.enabled=true
            quarkus.log.file.json.enabled=false
            quarkus.log.socket.json.enabled=true
            quarkus.log.foobar.json=true
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testHibernateORMValidationModeFalse() {
        @Language("properties")
        String originalProperties = """
            quarkus.hibernate-orm.validation.enabled=false
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.hibernate-orm.validation.mode=none
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testHibernateORMValidationModeTrue() {
        @Language("properties")
        String originalProperties = """
            quarkus.hibernate-orm.validation.enabled=true
            """;

        @Language("properties")
        String afterProperties = """
            quarkus.hibernate-orm.validation.mode=auto
            """;

        //language=xml
        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }
}
