package io.quarkus.updates.core;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.java.Assertions.java;

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
}
