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
        @Language("java")
        String defaultBean = """
                package io.quarkus.arc;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface DefaultBean {
                }
                """;
        @Language("java")
        String reserve = """
                package jakarta.enterprise.inject;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface Reserve {
                }
                """;
        @Language("java")
        String priority = """
                package jakarta.annotation;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface Priority {
                    int value();
                }
                """;
        @Language("java")
        String applicationScoped = """
                package jakarta.enterprise.context;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface ApplicationScoped {
                }
                """;
        @Language("java")
        String produces = """
                package jakarta.enterprise.inject;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.METHOD, ElementType.FIELD})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface Produces {
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
                                newObjectMapper, newJsonMapper,
                                defaultBean, reserve, priority,
                                applicationScoped, produces)
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

    @Test
    void testSmallRyeHealthEnableNotRenamed() {
        //language=properties
        rewriteRun(properties(
            """
            quarkus.smallrye-health.enable=false
            """,
            spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testInitTaskImage() {
        //language=properties
        rewriteRun(properties(
            """
            quarkus.kubernetes.init-task-defaults.image=wait-for:1.0
            quarkus.kubernetes.init-tasks.migrate.image=wait-for:1.1
            quarkus.openshift.init-task-defaults.image=wait-for:2.0
            quarkus.openshift.init-tasks."my.task".image=wait-for:2.1
            quarkus.knative.init-task-defaults.image=wait-for:3.0
            quarkus.knative.init-tasks.migrate.image=wait-for:3.1
            quarkus.kubernetes.init-tasks.seed.wait-for-container.image=wait-for:4.0
            """,
            """
            quarkus.kubernetes.init-task-defaults.wait-for-container.image=wait-for:1.0
            quarkus.kubernetes.init-tasks.migrate.wait-for-container.image=wait-for:1.1
            quarkus.openshift.init-task-defaults.wait-for-container.image=wait-for:2.0
            quarkus.openshift.init-tasks."my.task".wait-for-container.image=wait-for:2.1
            quarkus.knative.init-task-defaults.wait-for-container.image=wait-for:3.0
            quarkus.knative.init-tasks.migrate.wait-for-container.image=wait-for:3.1
            quarkus.kubernetes.init-tasks.seed.wait-for-container.image=wait-for:4.0
            """,
            spec -> spec.path("src/main/resources/application.properties")));
    }

    @Test
    void testDefaultBeanToReserveWithPriorityAdded() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.arc.DefaultBean;
                    import jakarta.enterprise.context.ApplicationScoped;

                    @DefaultBean
                    @ApplicationScoped
                    class MyDefaultService {
                    }
                """,
                """
                    package org.acme;

                    import jakarta.annotation.Priority;
                    import jakarta.enterprise.context.ApplicationScoped;
                    import jakarta.enterprise.inject.Reserve;

                    @Reserve
                    @ApplicationScoped
                    @Priority(0)
                    class MyDefaultService {
                    }
                """));
    }

    @Test
    void testDefaultBeanToReserveWithExistingPriority() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.arc.DefaultBean;
                    import jakarta.annotation.Priority;
                    import jakarta.enterprise.context.ApplicationScoped;

                    @DefaultBean
                    @Priority(10)
                    @ApplicationScoped
                    class MyPrioritizedService {
                    }
                """,
                """
                    package org.acme;

                    import jakarta.annotation.Priority;
                    import jakarta.enterprise.context.ApplicationScoped;
                    import jakarta.enterprise.inject.Reserve;

                    @Reserve
                    @Priority(10)
                    @ApplicationScoped
                    class MyPrioritizedService {
                    }
                """));
    }

    @Test
    void testDefaultBeanOnProducerMethod() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.arc.DefaultBean;
                    import jakarta.enterprise.context.ApplicationScoped;
                    import jakarta.enterprise.inject.Produces;

                    @ApplicationScoped
                    class MyProducer {

                        @Produces
                        @DefaultBean
                        String defaultValue() {
                            return "default";
                        }
                    }
                """,
                """
                    package org.acme;

                    import jakarta.annotation.Priority;
                    import jakarta.enterprise.context.ApplicationScoped;
                    import jakarta.enterprise.inject.Produces;
                    import jakarta.enterprise.inject.Reserve;

                    @ApplicationScoped
                    class MyProducer {

                        @Priority(0)
                        @Produces
                        @Reserve
                        String defaultValue() {
                            return "default";
                        }
                    }
                """));
    }

    @Test
    void testDroppedRelocations() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>21</maven.compiler.release>
                </properties>
                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5-component</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit5-mockito</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-webjars-locator</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-hibernate-search-orm-coordination-outbox-polling</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-hibernate-panache-next</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                </dependencies>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>21</maven.compiler.release>
                </properties>
                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit-component</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-junit-mockito</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-web-dependency-locator</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-hibernate-search-orm-outbox-polling</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-data-hibernate</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

    @Test
    void testDroppedRelocationsExtensionDeveloper() {
        //language=xml
        rewriteRun(pomXml("""
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>my-extension</artifactId>
                <version>1.0-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>21</maven.compiler.release>
                </properties>
                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-vertx-http-dev-ui-spi</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-vertx-http-dev-ui-tests</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-webjars-locator-deployment</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus.junit5</groupId>
                        <artifactId>junit5-virtual-threads</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """,
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.acme</groupId>
                <artifactId>my-extension</artifactId>
                <version>1.0-SNAPSHOT</version>
                <properties>
                    <maven.compiler.release>21</maven.compiler.release>
                </properties>
                <dependencies>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-devui-deployment-spi</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-devui-test-spi</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus</groupId>
                        <artifactId>quarkus-web-dependency-locator-deployment</artifactId>
                        <version>3.39.0</version>
                    </dependency>
                    <dependency>
                        <groupId>io.quarkus.junit</groupId>
                        <artifactId>junit-virtual-threads</artifactId>
                        <version>3.39.0</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

    @Test
    void testObserverInfoGetIdAndMessagingCapability() {
        @Language("java")
        String observerInfo = """
                package io.quarkus.arc.processor;

                public class ObserverInfo {
                    public String getId() { return null; }
                    public String getUserId() { return null; }
                }
                """;
        @Language("java")
        String capability = """
                package io.quarkus.deployment;

                public interface Capability {
                    String SMALLRYE_REACTIVE_MESSAGING = "io.quarkus.smallrye.reactive.messaging";
                    String MESSAGING = "io.quarkus.messaging";
                }
                """;
        //language=java
        rewriteRun(spec -> spec.parser(JavaParser.fromJavaVersion()
                        .dependsOn(observerInfo, capability)
                        .logCompilationWarningsAndErrors(true)),
                java(
                """
                    package org.acme.deployment;

                    import io.quarkus.arc.processor.ObserverInfo;
                    import io.quarkus.deployment.Capability;

                    class MyProcessor {
                        String observerId(ObserverInfo observer) {
                            return observer.getId();
                        }

                        String messagingCapability() {
                            return Capability.SMALLRYE_REACTIVE_MESSAGING;
                        }
                    }
                """,
                """
                    package org.acme.deployment;

                    import io.quarkus.arc.processor.ObserverInfo;
                    import io.quarkus.deployment.Capability;

                    class MyProcessor {
                        String observerId(ObserverInfo observer) {
                            return observer.getUserId();
                        }

                        String messagingCapability() {
                            return Capability.MESSAGING;
                        }
                    }
                """));
    }

    @Test
    void testMicrometerPrometheusMetricsPackage() {
        @Language("java")
        String prometheusConfig = """
                package io.micrometer.prometheus;

                public interface PrometheusConfig {
                    PrometheusConfig DEFAULT = null;
                }
                """;
        @Language("java")
        String collectorRegistry = """
                package io.prometheus.client;

                public class CollectorRegistry {
                    public CollectorRegistry() { }
                    public CollectorRegistry(boolean autoDescribe) { }
                }
                """;
        @Language("java")
        String prometheusMeterRegistry = """
                package io.micrometer.prometheus;

                import io.prometheus.client.CollectorRegistry;

                public class PrometheusMeterRegistry {
                    public PrometheusMeterRegistry(PrometheusConfig config) { }
                    public CollectorRegistry getPrometheusRegistry() { return null; }
                    public String scrape() { return null; }
                }
                """;
        //language=java
        rewriteRun(spec -> spec.parser(JavaParser.fromJavaVersion()
                        .dependsOn(prometheusConfig, collectorRegistry, prometheusMeterRegistry)
                        .logCompilationWarningsAndErrors(true)),
                java(
                """
                    package org.acme;

                    import io.micrometer.prometheus.PrometheusConfig;
                    import io.micrometer.prometheus.PrometheusMeterRegistry;
                    import io.prometheus.client.CollectorRegistry;

                    class MyMetrics {
                        CollectorRegistry registry() {
                            PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
                            return registry.getPrometheusRegistry();
                        }

                        CollectorRegistry collectorRegistry() {
                            return new CollectorRegistry(true);
                        }
                    }
                """,
                """
                    package org.acme;

                    import io.micrometer.prometheusmetrics.PrometheusConfig;
                    import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
                    import io.prometheus.metrics.model.registry.PrometheusRegistry;

                    class MyMetrics {
                        PrometheusRegistry registry() {
                            PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
                            return registry.getPrometheusRegistry();
                        }

                        PrometheusRegistry collectorRegistry() {
                            return new PrometheusRegistry();
                        }
                    }
                """));
    }
}
