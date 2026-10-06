package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;

import java.net.URI;
import java.util.Properties;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.Recipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate40Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        @Language("java")
        String objectMapper = """
                package com.fasterxml.jackson.databind;

                public class ObjectMapper {
                    public String writeValueAsString(Object value) { return null; }
                    public <T> T readValue(String content, Class<T> valueType) { return null; }
                }
                """;
        @Language("java")
        String jsonProperty = """
                package com.fasterxml.jackson.annotation;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface JsonProperty {
                    String value() default "";
                }
                """;
        @Language("java")
        String newObjectMapper = """
                package tools.jackson.databind;

                public class ObjectMapper {
                    public String writeValueAsString(Object value) { return null; }
                    public <T> T readValue(String content, Class<T> valueType) { return null; }
                }
                """;
        @Language("java")
        String newJsonMapper = """
                package tools.jackson.databind.json;

                import tools.jackson.databind.ObjectMapper;

                public class JsonMapper extends ObjectMapper {
                }
                """;

        String recipeResource = "quarkus-updates/core/4.0.alpha1.yaml";
        YamlResourceLoader yrl = new YamlResourceLoader(
                getClass().getClassLoader().getResourceAsStream(recipeResource),
                URI.create("rewrite.yml"), new Properties());
        String[] recipeNames = yrl.listRecipes().stream()
                .map(Recipe::getName).toArray(String[]::new);

        Recipe recipe = Environment.builder()
                .scanRuntimeClasspath()
                .load(new YamlResourceLoader(
                        getClass().getClassLoader().getResourceAsStream(recipeResource),
                        URI.create("rewrite.yml"), new Properties()))
                .build()
                .activateRecipes(recipeNames);

        spec.recipe(recipe)
                .parser(JavaParser.fromJavaVersion()
                        .dependsOn(objectMapper, jsonProperty,
                                newObjectMapper, newJsonMapper)
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testJacksonDatabindPackageRename() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import com.fasterxml.jackson.databind.ObjectMapper;

                    class MyService {
                        ObjectMapper getMapper() {
                            return null;
                        }
                    }
                """,
                """
                    package org.acme;

                    import tools.jackson.databind.ObjectMapper;

                    class MyService {
                        ObjectMapper getMapper() {
                            return null;
                        }
                    }
                """));
    }

    @Test
    void testJacksonAnnotationPackageNotChanged() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import com.fasterxml.jackson.annotation.JsonProperty;

                    class MyDto {
                        @JsonProperty("user_name")
                        private String userName;
                    }
                """));
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
                    <maven.compiler.release>17</maven.compiler.release>
                    <maven.compiler.target>17</maven.compiler.target>
                    <maven.compiler.source>17</maven.compiler.source>
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
                    <maven.compiler.release>21</maven.compiler.release>
                    <maven.compiler.target>21</maven.compiler.target>
                    <maven.compiler.source>21</maven.compiler.source>
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

        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus.bot</groupId>
                <artifactId>release</artifactId>
                <version>999-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>25</maven.compiler.release>
                    <maven.compiler.target>25</maven.compiler.target>
                    <maven.compiler.source>25</maven.compiler.source>
                </properties>
            </project>
            """));
    }

    @Test
    void testConfigPropertyRenames() {
        @Language("properties")
        String originalProperties = """
            # Hibernate ORM
            quarkus.hibernate-orm.batch-fetch-size=16
            quarkus.hibernate-orm.max-fetch-depth=3
            quarkus.hibernate-orm.log.bind-param=true
            quarkus.hibernate-orm.multitenant-schema-datasource=other
            quarkus.hibernate-orm.dialect.storage-engine=InnoDB
            # Hibernate ORM - named persistence unit
            quarkus.hibernate-orm."inventory".batch-fetch-size=8
            quarkus.hibernate-orm."inventory".max-fetch-depth=2
            quarkus.hibernate-orm."inventory".log.bind-param=false
            quarkus.hibernate-orm."inventory".multitenant-schema-datasource=inventory-ds
            quarkus.hibernate-orm."inventory".dialect.storage-engine=InnoDB
            # OIDC
            quarkus.oidc.authentication.pkce-secret=my-secret
            quarkus.oidc."tenant1".authentication.pkce-secret=tenant-secret
            # REST Client
            quarkus.rest-client.multipart.max-chunk-size=8192
            quarkus.rest-client."my-client".multipart.max-chunk-size=4096
            # Infinispan
            quarkus.infinispan-client.server-list=localhost:11222
            quarkus.infinispan-client.auth-username=admin
            quarkus.infinispan-client.auth-password=secret
            quarkus.infinispan-client."named".server-list=remote:11222
            quarkus.infinispan-client."named".auth-username=user
            quarkus.infinispan-client."named".auth-password=pass
            # Mailer
            quarkus.mailer.ssl=true
            quarkus.mailer."named".ssl=false
            # Micrometer
            quarkus.micrometer.binder.vertx.match-patterns=/api/.*
            quarkus.micrometer.binder.vertx.ignore-patterns=/health
            # Redis Cache
            quarkus.cache.redis.ttl=10S
            quarkus.cache.redis."my-cache".ttl=30S
            # SmallRye
            quarkus.smallrye-health.enable=true
            # Kubernetes
            quarkus.kubernetes.init-task.image=my-image:latest
            # HTTP SSL
            quarkus.http.ssl.certificate.key-store-key-alias=myalias
            quarkus.http.ssl.certificate.key-store-key-password=mypassword
            quarkus.http.ssl.certificate.key-store-key-password-key=mykey
            # Keycloak Policy Enforcer
            quarkus.keycloak.policy-enforcer.paths.login.path=/api/login
            quarkus.keycloak."tenant1".policy-enforcer.paths.login.path=/api/login
            # Misc
            quarkus.debug.dump-build-metrics=true
            quarkus.package.jar.appcds.use-aot=true
            """;

        @Language("properties")
        String afterProperties = """
            # Hibernate ORM
            quarkus.hibernate-orm.fetch.batch-size=16
            quarkus.hibernate-orm.fetch.max-depth=3
            quarkus.hibernate-orm.log.bind-parameters=true
            quarkus.hibernate-orm.datasource=other
            quarkus.hibernate-orm.dialect.mysql.storage-engine=InnoDB
            # Hibernate ORM - named persistence unit
            quarkus.hibernate-orm."inventory".fetch.batch-size=8
            quarkus.hibernate-orm."inventory".fetch.max-depth=2
            quarkus.hibernate-orm."inventory".log.bind-parameters=false
            quarkus.hibernate-orm."inventory".datasource=inventory-ds
            quarkus.hibernate-orm."inventory".dialect.mysql.storage-engine=InnoDB
            # OIDC
            quarkus.oidc.authentication.state-secret=my-secret
            quarkus.oidc."tenant1".authentication.state-secret=tenant-secret
            # REST Client
            quarkus.rest-client.max-chunk-size=8192
            quarkus.rest-client."my-client".max-chunk-size=4096
            # Infinispan
            quarkus.infinispan-client.hosts=localhost:11222
            quarkus.infinispan-client.username=admin
            quarkus.infinispan-client.password=secret
            quarkus.infinispan-client."named".hosts=remote:11222
            quarkus.infinispan-client."named".username=user
            quarkus.infinispan-client."named".password=pass
            # Mailer
            quarkus.mailer.tls=true
            quarkus.mailer."named".tls=false
            # Micrometer
            quarkus.micrometer.binder.http-server.match-patterns=/api/.*
            quarkus.micrometer.binder.http-server.ignore-patterns=/health
            # Redis Cache
            quarkus.cache.redis.expire-after-write=10S
            quarkus.cache.redis."my-cache".expire-after-write=30S
            # SmallRye
            quarkus.smallrye-health.enabled=true
            # Kubernetes
            quarkus.kubernetes.init-task-defaults.wait-for-container.image=my-image:latest
            # HTTP SSL
            quarkus.http.ssl.certificate.key-store-alias=myalias
            quarkus.http.ssl.certificate.key-store-alias-password=mypassword
            quarkus.http.ssl.certificate.key-store-alias-password-key=mykey
            # Keycloak Policy Enforcer
            quarkus.keycloak.policy-enforcer.paths.login.paths=/api/login
            quarkus.keycloak."tenant1".policy-enforcer.paths.login.paths=/api/login
            # Misc
            quarkus.builder.metrics.enabled=true
            quarkus.package.jar.aot.enabled=true
            """;

        rewriteRun(properties(originalProperties, afterProperties, spec -> spec.path("src/main/resources/application.properties")));
    }
}
