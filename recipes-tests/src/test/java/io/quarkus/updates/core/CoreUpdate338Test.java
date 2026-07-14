package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;

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
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.38.alpha1.yaml"))
                .parser(JavaParser.fromJavaVersion()
                        .dependsOn(restClient, restClientBuilder, request, response,
                                responseException, responseListener, requestOptions,
                                cancellable)
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
}
