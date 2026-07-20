package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate338Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        @Language("java")
        String restClient = """
                package org.elasticsearch.client;

                import java.io.Closeable;

                public class RestClient implements Closeable {
                    public static RestClientBuilder builder(Object... hosts) {
                        return null;
                    }
                    public Response performRequest(Request request) { return null; }
                    public void close() {}

                    public static class FailureListener {}
                }
                """;
        @Language("java")
        String restClientBuilder = """
                package org.elasticsearch.client;

                public class RestClientBuilder {
                    public RestClient build() { return null; }
                    public interface HttpClientConfigCallback {}
                }
                """;
        @Language("java")
        String request = """
                package org.elasticsearch.client;

                public class Request {
                    public Request(String method, String endpoint) {}
                    public void setJsonEntity(String entity) {}
                }
                """;
        @Language("java")
        String response = """
                package org.elasticsearch.client;

                public class Response {
                    public Object getEntity() { return null; }
                }
                """;
        @Language("java")
        String responseException = """
                package org.elasticsearch.client;

                public class ResponseException extends java.io.IOException {
                    public Response getResponse() { return null; }
                }
                """;
        @Language("java")
        String responseListener = """
                package org.elasticsearch.client;

                public interface ResponseListener {
                    void onSuccess(Response response);
                    void onFailure(Exception exception);
                }
                """;
        @Language("java")
        String requestOptions = """
                package org.elasticsearch.client;

                public class RequestOptions {
                    public static final RequestOptions DEFAULT = null;
                    public static class Builder {}
                }
                """;
        @Language("java")
        String cancellable = """
                package org.elasticsearch.client;

                public interface Cancellable {
                    void cancel();
                }
                """;
        @Language("java")
        String panacheClass = """
                package io.quarkus.hibernate.panache;

                public class Panache {
                }
                """;
        @Language("java")
        String panacheQueryInterface = """
                package io.quarkus.hibernate.panache;

                public interface PanacheQuery<Entity> {
                }
                """;
        @Language("java")
        String withId = """
                package io.quarkus.hibernate.panache;

                public interface WithId<Id> {
                    public abstract class AutoLong implements WithId<Long> {
                    }
                }
                """;
        @Language("java")
        String panacheManagedBlockingEntity = """
                package io.quarkus.hibernate.panache.managed.blocking;

                public interface PanacheManagedBlockingEntity {
                }
                """;
        @Language("java")
        String panacheStatelessBlockingEntity = """
                package io.quarkus.hibernate.panache.stateless.blocking;

                public interface PanacheStatelessBlockingEntity {
                }
                """;
        @Language("java")
        String panacheManagedReactiveEntity = """
                package io.quarkus.hibernate.panache.managed.reactive;

                public interface PanacheManagedReactiveEntity {
                }
                """;
        @Language("java")
        String panacheStatelessReactiveEntity = """
                package io.quarkus.hibernate.panache.stateless.reactive;

                public interface PanacheStatelessReactiveEntity {
                }
                """;
        @Language("java")
        String panacheEntity = """
                package io.quarkus.hibernate.panache;

                import io.quarkus.hibernate.panache.managed.blocking.PanacheManagedBlockingEntity;
                import io.quarkus.hibernate.panache.managed.reactive.PanacheManagedReactiveEntity;
                import io.quarkus.hibernate.panache.stateless.blocking.PanacheStatelessBlockingEntity;
                import io.quarkus.hibernate.panache.stateless.reactive.PanacheStatelessReactiveEntity;

                public abstract class PanacheEntity extends WithId.AutoLong implements PanacheManagedBlockingEntity {
                    public interface Managed extends PanacheManagedBlockingEntity {}
                    public interface Stateless extends PanacheStatelessBlockingEntity {}
                    public interface Reactive extends PanacheManagedReactiveEntity {
                        public interface Stateless extends PanacheStatelessReactiveEntity {}
                    }
                }
                """;
        @Language("java")
        String panacheEntityBase = """
                package io.quarkus.hibernate.panache;

                import io.quarkus.hibernate.panache.managed.blocking.PanacheManagedBlockingEntity;

                public class PanacheEntityBase implements PanacheManagedBlockingEntity {
                }
                """;
        @Language("java")
        String panacheManagedBlockingRepositoryBase = """
                package io.quarkus.hibernate.panache.managed.blocking;

                public interface PanacheManagedBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheStatelessBlockingRepositoryBase = """
                package io.quarkus.hibernate.panache.stateless.blocking;

                public interface PanacheStatelessBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheManagedReactiveRepositoryBase = """
                package io.quarkus.hibernate.panache.managed.reactive;

                public interface PanacheManagedReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheStatelessReactiveRepositoryBase = """
                package io.quarkus.hibernate.panache.stateless.reactive;

                public interface PanacheStatelessReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheRepository = """
                package io.quarkus.hibernate.panache;

                import io.quarkus.hibernate.panache.managed.blocking.PanacheManagedBlockingRepositoryBase;
                import io.quarkus.hibernate.panache.managed.reactive.PanacheManagedReactiveRepositoryBase;
                import io.quarkus.hibernate.panache.stateless.blocking.PanacheStatelessBlockingRepositoryBase;
                import io.quarkus.hibernate.panache.stateless.reactive.PanacheStatelessReactiveRepositoryBase;

                public interface PanacheRepository<Entity> extends PanacheManagedBlockingRepositoryBase<Entity, Long> {
                    public interface Managed<Entity, Id> extends PanacheManagedBlockingRepositoryBase<Entity, Id> {}
                    public interface Stateless<Entity, Id> extends PanacheStatelessBlockingRepositoryBase<Entity, Id> {}
                    public interface Reactive<Entity, Id> extends PanacheManagedReactiveRepositoryBase<Entity, Id> {
                        public interface Stateless<Entity, Id> extends PanacheStatelessReactiveRepositoryBase<Entity, Id> {}
                    }
                }
                """;
        @Language("java")
        String panacheEntityMarker = """
                package io.quarkus.hibernate.panache;

                public interface PanacheEntityMarker {
                }
                """;
        @Language("java")
        String panacheRepositoryQueries = """
                package io.quarkus.hibernate.panache;

                public interface PanacheRepositoryQueries {
                }
                """;
        @Language("java")
        String panacheRepositorySwitcher = """
                package io.quarkus.hibernate.panache;

                public interface PanacheRepositorySwitcher {
                }
                """;
        @Language("java")
        String panacheBlockingQuery = """
                package io.quarkus.hibernate.panache.blocking;

                public interface PanacheBlockingQuery<Entity> {
                }
                """;
        @Language("java")
        String newWithId = """
                package io.quarkus.data.hibernate;

                public interface WithId<Id> {
                    public abstract class AutoLong implements WithId<Long> {
                    }
                }
                """;
        @Language("java")
        String newManagedBlockingEntity = """
                package io.quarkus.data.hibernate.managed.blocking;

                public interface PanacheManagedBlockingEntity {
                }
                """;
        @Language("java")
        String newStatelessBlockingEntity = """
                package io.quarkus.data.hibernate.stateless.blocking;

                public interface PanacheStatelessBlockingEntity {
                }
                """;
        @Language("java")
        String newManagedReactiveEntity = """
                package io.quarkus.data.hibernate.managed.reactive;

                public interface PanacheManagedReactiveEntity {
                }
                """;
        @Language("java")
        String newStatelessReactiveEntity = """
                package io.quarkus.data.hibernate.stateless.reactive;

                public interface PanacheStatelessReactiveEntity {
                }
                """;
        @Language("java")
        String managedEntity = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.managed.blocking.PanacheManagedBlockingEntity;
                import io.quarkus.data.hibernate.managed.reactive.PanacheManagedReactiveEntity;

                public class ManagedEntity extends WithId.AutoLong implements PanacheManagedBlockingEntity {
                    public interface CustomId extends PanacheManagedBlockingEntity {}
                    public static class Reactive extends WithId.AutoLong implements PanacheManagedReactiveEntity {
                        public interface CustomId extends PanacheManagedReactiveEntity {}
                    }
                }
                """;
        @Language("java")
        String recordEntity = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.stateless.blocking.PanacheStatelessBlockingEntity;
                import io.quarkus.data.hibernate.stateless.reactive.PanacheStatelessReactiveEntity;

                public class RecordEntity extends WithId.AutoLong implements PanacheStatelessBlockingEntity {
                    public interface CustomId extends PanacheStatelessBlockingEntity {}
                    public static class Reactive extends WithId.AutoLong implements PanacheStatelessReactiveEntity {
                        public interface CustomId extends PanacheStatelessReactiveEntity {}
                    }
                }
                """;
        @Language("java")
        String newManagedBlockingRepositoryBase = """
                package io.quarkus.data.hibernate.managed.blocking;

                public interface PanacheManagedBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String newStatelessBlockingRepositoryBase = """
                package io.quarkus.data.hibernate.stateless.blocking;

                public interface PanacheStatelessBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String newManagedReactiveRepositoryBase = """
                package io.quarkus.data.hibernate.managed.reactive;

                public interface PanacheManagedReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String newStatelessReactiveRepositoryBase = """
                package io.quarkus.data.hibernate.stateless.reactive;

                public interface PanacheStatelessReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String managedRepository = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.managed.blocking.PanacheManagedBlockingRepositoryBase;
                import io.quarkus.data.hibernate.managed.reactive.PanacheManagedReactiveRepositoryBase;

                public interface ManagedRepository<Entity> extends PanacheManagedBlockingRepositoryBase<Entity, Long> {
                    interface CustomId<Entity, Id> extends PanacheManagedBlockingRepositoryBase<Entity, Id> {}
                    interface Reactive<Entity> extends PanacheManagedReactiveRepositoryBase<Entity, Long> {
                        interface CustomId<Entity, Id> extends PanacheManagedReactiveRepositoryBase<Entity, Id> {}
                    }
                }
                """;
        @Language("java")
        String recordRepository = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.stateless.blocking.PanacheStatelessBlockingRepositoryBase;
                import io.quarkus.data.hibernate.stateless.reactive.PanacheStatelessReactiveRepositoryBase;

                public interface RecordRepository<Entity> extends PanacheStatelessBlockingRepositoryBase<Entity, Long> {
                    interface CustomId<Entity, Id> extends PanacheStatelessBlockingRepositoryBase<Entity, Id> {}
                    interface Reactive<Entity> extends PanacheStatelessReactiveRepositoryBase<Entity, Long> {
                        interface CustomId<Entity, Id> extends PanacheStatelessReactiveRepositoryBase<Entity, Id> {}
                    }
                }
                """;
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.38.alpha1.yaml"))
                .parser(JavaParser.fromJavaVersion()
                        .dependsOn(restClient, restClientBuilder, request, response,
                                responseException, responseListener, requestOptions,
                                cancellable,
                                panacheClass, panacheQueryInterface, withId,
                                panacheManagedBlockingEntity, panacheStatelessBlockingEntity,
                                panacheManagedReactiveEntity, panacheStatelessReactiveEntity,
                                panacheEntity, panacheEntityBase,
                                panacheManagedBlockingRepositoryBase, panacheStatelessBlockingRepositoryBase,
                                panacheManagedReactiveRepositoryBase, panacheStatelessReactiveRepositoryBase,
                                panacheRepository,
                                panacheEntityMarker, panacheRepositoryQueries, panacheRepositorySwitcher,
                                panacheBlockingQuery,
                                newWithId,
                                newManagedBlockingEntity, newStatelessBlockingEntity,
                                newManagedReactiveEntity, newStatelessReactiveEntity,
                                managedEntity, recordEntity,
                                newManagedBlockingRepositoryBase, newStatelessBlockingRepositoryBase,
                                newManagedReactiveRepositoryBase, newStatelessReactiveRepositoryBase,
                                managedRepository, recordRepository)
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testRestClientToRest5Client() {
        //language=java
        rewriteRun(java(
                """
                package org.acme;

                import org.elasticsearch.client.RestClient;

                public class ElasticsearchService {
                    private RestClient client;

                    public void setClient(RestClient client) {
                        this.client = client;
                    }
                }
                """,
                """
                package org.acme;

                import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

                public class ElasticsearchService {
                    private Rest5Client client;

                    public void setClient(Rest5Client client) {
                        this.client = client;
                    }
                }
                """));
    }

    @Test
    void testRestClientBuilderToRest5ClientBuilder() {
        //language=java
        rewriteRun(java(
                """
                package org.acme;

                import org.elasticsearch.client.RestClientBuilder;

                public class ClientFactory {
                    private RestClientBuilder builder;
                }
                """,
                """
                package org.acme;

                import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;

                public class ClientFactory {
                    private Rest5ClientBuilder builder;
                }
                """));
    }

    @Test
    void testRequestAndResponseMigration() {
        //language=java
        rewriteRun(java(
                """
                package org.acme;

                import org.elasticsearch.client.Request;
                import org.elasticsearch.client.Response;
                import org.elasticsearch.client.RestClient;

                public class SearchService {
                    private RestClient client;

                    public Response search() throws Exception {
                        Request request = new Request("GET", "/index/_search");
                        return client.performRequest(request);
                    }
                }
                """,
                """
                package org.acme;

                import co.elastic.clients.transport.rest5_client.low_level.Request;
                import co.elastic.clients.transport.rest5_client.low_level.Response;
                import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

                public class SearchService {
                    private Rest5Client client;

                    public Response search() throws Exception {
                        Request request = new Request("GET", "/index/_search");
                        return client.performRequest(request);
                    }
                }
                """));
    }

    @Test
    void testHttpClientConfigCallbackMigration() {
        //language=java
        rewriteRun(java(
                """
                package org.acme;

                import org.elasticsearch.client.RestClientBuilder;

                public class MyConfigurator implements RestClientBuilder.HttpClientConfigCallback {
                }
                """,
                """
                package org.acme;

                import io.quarkus.elasticsearch.restclient.lowlevel.ElasticsearchClientConfigConfigurer;

                public class MyConfigurator implements ElasticsearchClientConfigConfigurer {
                }
                """));
    }

    @Test
    void testPanacheToQuarkusDataTypeRename() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.Panache;

                    class MyService {
                        void doSomething() {
                            Panache.withTransaction(() -> null);
                        }
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.QuarkusData;

                    class MyService {
                        void doSomething() {
                            QuarkusData.withTransaction(() -> null);
                        }
                    }
                """));
    }

    @Test
    void testPanacheQueryToDataQuery() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheQuery;

                    class MyService {
                        PanacheQuery<Object> query;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.DataQuery;

                    class MyService {
                        DataQuery<Object> query;
                    }
                """));
    }

    @Test
    void testPanacheEntityToManagedEntity() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity extends PanacheEntity {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity extends ManagedEntity {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityBaseToManagedEntityCustomId() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntityBase;

                    public class MyEntity extends PanacheEntityBase {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity extends ManagedEntity.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityManagedInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Managed {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity implements ManagedEntity.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityStatelessInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Stateless {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordEntity;

                    public class MyEntity implements RecordEntity.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityReactiveInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Reactive {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity implements ManagedEntity.Reactive.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityReactiveStatelessInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Reactive.Stateless {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordEntity;

                    public class MyEntity implements RecordEntity.Reactive.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheRepositoryToManagedRepository() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository<Object> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedRepository;

                    public class MyRepository implements ManagedRepository<Object> {
                    }
                """));
    }

    @Test
    void testPanacheRepositoryStatelessToRecordRepository() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository.Stateless<Object, Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordRepository;

                    public class MyRepository implements RecordRepository.CustomId<Object, Long> {
                    }
                """));
    }

    @Test
    void testPanacheRepositoryReactiveToManagedRepositoryReactive() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository.Reactive<Object, Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedRepository;

                    public class MyRepository implements ManagedRepository.Reactive.CustomId<Object, Long> {
                    }
                """));
    }

    @Test
    void testPanacheRepositoryReactiveStatelessToRecordRepositoryReactive() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository.Reactive.Stateless<Object, Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordRepository;

                    public class MyRepository implements RecordRepository.Reactive.CustomId<Object, Long> {
                    }
                """));
    }

    @Test
    void testPackageRenameForRemainingTypes() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.blocking.PanacheBlockingQuery;

                    class MyService {
                        PanacheBlockingQuery<Object> query;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.blocking.PanacheBlockingQuery;

                    class MyService {
                        PanacheBlockingQuery<Object> query;
                    }
                """));
    }

    @Test
    void testPanacheEntityMarkerToEntitySwitcher() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntityMarker;

                    class MyService {
                        PanacheEntityMarker marker;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.EntitySwitcher;

                    class MyService {
                        EntitySwitcher marker;
                    }
                """));
    }

    @Test
    void testWithIdPackageRename() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.WithId;

                    class MyEntity implements WithId<Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.WithId;

                    class MyEntity implements WithId<Long> {
                    }
                """));
    }

    @Test
    void testHibernateProcessorToQuarkusDataProcessor() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate.orm</groupId>
                                                    <artifactId>hibernate-processor</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>io.quarkus</groupId>
                                                    <artifactId>quarkus-data-processor</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """));
    }

    @Test
    void testHibernateProcessorWithVersionToQuarkusDataProcessor() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-data-processor</artifactId>
                                        <version>3.38.0</version>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate.orm</groupId>
                                                    <artifactId>hibernate-processor</artifactId>
                                                    <version>6.6.3.Final</version>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-data-processor</artifactId>
                                        <version>3.38.0</version>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>io.quarkus</groupId>
                                                    <artifactId>quarkus-data-processor</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """));
    }

    @Test
    void testOldJpaModelgenToQuarkusDataProcessor() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate</groupId>
                                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>io.quarkus</groupId>
                                                    <artifactId>quarkus-data-processor</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """));
    }

    @Test
    void testGradleHibernateProcessorWithVersionToQuarkusDataProcessor() {
        org.openrewrite.maven.tree.Dependency dep = org.openrewrite.maven.tree.Dependency.builder()
                .gav(new org.openrewrite.maven.tree.GroupArtifactVersion("org.hibernate.orm", "hibernate-processor", "6.6.3.Final"))
                .build();
        org.openrewrite.gradle.marker.GradleDependencyConfiguration apConfig =
                new org.openrewrite.gradle.marker.GradleDependencyConfiguration("annotationProcessor", null, true, true, true, true,
                        java.util.Collections.emptyList(), java.util.List.of(dep), java.util.Collections.emptyList(), null, null, null, null);
        org.openrewrite.gradle.marker.GradleProject gp = org.openrewrite.gradle.marker.GradleProject.builder()
                .group("org.acme")
                .name("my-app")
                .version("1.0-SNAPSHOT")
                .nameToConfiguration(java.util.Map.of("annotationProcessor", apConfig))
                .build();
        rewriteRun(
                buildGradle(
                        """
                            plugins {
                                id 'java'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                annotationProcessor enforcedPlatform("io.quarkus.platform:quarkus-bom:3.38.0")
                                annotationProcessor 'org.hibernate.orm:hibernate-processor:6.6.3.Final'
                            }
                            """,
                        """
                            plugins {
                                id 'java'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                annotationProcessor enforcedPlatform("io.quarkus.platform:quarkus-bom:3.38.0")
                                annotationProcessor 'io.quarkus:quarkus-data-processor'
                            }
                            """,
                        spec2 -> spec2.markers(gp)));
    }

    @Test
    void testGradleHibernateProcessorWithoutVersionToQuarkusDataProcessor() {
        org.openrewrite.maven.tree.Dependency dep = org.openrewrite.maven.tree.Dependency.builder()
                .gav(new org.openrewrite.maven.tree.GroupArtifactVersion("org.hibernate.orm", "hibernate-processor", null))
                .build();
        org.openrewrite.gradle.marker.GradleDependencyConfiguration apConfig =
                new org.openrewrite.gradle.marker.GradleDependencyConfiguration("annotationProcessor", null, true, true, true, true,
                        java.util.Collections.emptyList(), java.util.List.of(dep), java.util.Collections.emptyList(), null, null, null, null);
        org.openrewrite.gradle.marker.GradleProject gp = org.openrewrite.gradle.marker.GradleProject.builder()
                .group("org.acme")
                .name("my-app")
                .version("1.0-SNAPSHOT")
                .nameToConfiguration(java.util.Map.of("annotationProcessor", apConfig))
                .build();
        rewriteRun(
                buildGradle(
                        """
                            plugins {
                                id 'java'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                annotationProcessor enforcedPlatform("io.quarkus.platform:quarkus-bom:3.38.0")
                                annotationProcessor 'org.hibernate.orm:hibernate-processor'
                            }
                            """,
                        """
                            plugins {
                                id 'java'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                annotationProcessor enforcedPlatform("io.quarkus.platform:quarkus-bom:3.38.0")
                                annotationProcessor 'io.quarkus:quarkus-data-processor'
                            }
                            """,
                        spec2 -> spec2.markers(gp)));
    }

    @Test
    void testGradleHibernateProcessorWithVersionNoEnforcedPlatform() {
        org.openrewrite.maven.tree.Dependency dep = org.openrewrite.maven.tree.Dependency.builder()
                .gav(new org.openrewrite.maven.tree.GroupArtifactVersion("org.hibernate.orm", "hibernate-processor", "6.6.3.Final"))
                .build();
        org.openrewrite.gradle.marker.GradleDependencyConfiguration apConfig =
                new org.openrewrite.gradle.marker.GradleDependencyConfiguration("annotationProcessor", null, true, true, true, true,
                        java.util.Collections.emptyList(), java.util.List.of(dep), java.util.Collections.emptyList(), null, null, null, null);
        org.openrewrite.gradle.marker.GradleProject gp = org.openrewrite.gradle.marker.GradleProject.builder()
                .group("org.acme")
                .name("my-app")
                .version("1.0-SNAPSHOT")
                .nameToConfiguration(java.util.Map.of("annotationProcessor", apConfig))
                .build();
        rewriteRun(
                buildGradle(
                        """
                            plugins {
                                id 'java'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                annotationProcessor 'org.hibernate.orm:hibernate-processor:6.6.3.Final'
                            }
                            """,
                        """
                            plugins {
                                id 'java'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                annotationProcessor enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion")
                                annotationProcessor 'io.quarkus:quarkus-data-processor'
                            }
                            """,
                        spec2 -> spec2.markers(gp)));
    }
}
