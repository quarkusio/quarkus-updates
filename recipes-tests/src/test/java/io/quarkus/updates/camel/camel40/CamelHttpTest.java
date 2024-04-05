package io.quarkus.updates.camel.camel40;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelHttpTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3alpha(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-api",
                        "camel-support", "camel-core-model", "camel-util", "camel-catalog", "camel-main", "httpclient"
                ,"httpcore", "httpclient"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testHttp() {
        //language=java
        rewriteRun(java(
                """
                            import jakarta.inject.Named;

                            import org.apache.http.HttpHost;
                            import org.apache.http.auth.AuthScope;
                            import org.apache.http.auth.UsernamePasswordCredentials;
                            import org.apache.http.client.protocol.HttpClientContext;
                            import org.apache.http.impl.auth.BasicScheme;
                            import org.apache.http.impl.client.BasicAuthCache;
                            import org.apache.http.impl.client.BasicCredentialsProvider;
                            import org.apache.http.protocol.HttpContext;
                            import org.eclipse.microprofile.config.ConfigProvider;

                            import static org.apache.camel.quarkus.component.http.it.HttpResource.USER_ADMIN;
                            import static org.apache.camel.quarkus.component.http.it.HttpResource.USER_ADMIN_PASSWORD;

                            public class HttpProducers {

                                @Named
                                HttpContext basicAuthContext() {
                                    Integer port = ConfigProvider.getConfig().getValue("quarkus.http.test-port", Integer.class);

                                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USER_ADMIN, USER_ADMIN_PASSWORD);
                                    BasicCredentialsProvider provider = new BasicCredentialsProvider();
                                    provider.setCredentials(AuthScope.ANY, credentials);

                                    BasicAuthCache authCache = new BasicAuthCache();
                                    BasicScheme basicAuth = new BasicScheme();
                                    authCache.put(new HttpHost("localhost", port), basicAuth);

                                    HttpClientContext context = HttpClientContext.create();
                                    context.setAuthCache(authCache);
                                    context.setCredentialsProvider(provider);

                                    return context;
                                }
                            }
                        """,
                """
                            import jakarta.inject.Named;
                            import org.apache.hc.client5.http.auth.AuthScope;
                            import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
                            import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
                            import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
                            import org.apache.hc.client5.http.impl.auth.BasicScheme;
                            import org.apache.hc.client5.http.protocol.HttpClientContext;
                            import org.apache.hc.core5.http.HttpHost;
                            import org.apache.hc.core5.http.protocol.HttpContext;
                            import org.eclipse.microprofile.config.ConfigProvider;

                            import static org.apache.camel.quarkus.component.http.it.HttpResource.USER_ADMIN;
                            import static org.apache.camel.quarkus.component.http.it.HttpResource.USER_ADMIN_PASSWORD;

                            public class HttpProducers {

                                @Named
                                HttpContext basicAuthContext() {
                                    Integer port = ConfigProvider.getConfig().getValue("quarkus.http.test-port", Integer.class);

                                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USER_ADMIN, USER_ADMIN_PASSWORD);
                                    BasicCredentialsProvider provider = new BasicCredentialsProvider();
                                    provider.setCredentials(new AuthScope(null, -1), credentials);

                                    BasicAuthCache authCache = new BasicAuthCache();
                                    BasicScheme basicAuth = new BasicScheme();
                                    authCache.put(new HttpHost("localhost", port), basicAuth);

                                    HttpClientContext context = HttpClientContext.create();
                                    context.setAuthCache(authCache);
                                    context.setCredentialsProvider(provider);

                                    return context;
                                }
                            }
                        """));
    }
    @Test
    void testNoopHostnameVerifier() {
        //language=java
        rewriteRun(java(
                """
                            import jakarta.inject.Named;
                            import org.apache.camel.CamelContext;
                            import org.apache.http.conn.ssl.NoopHostnameVerifier;
                            import org.eclipse.microprofile.config.ConfigProvider;
                            
                            public class HttpProducers {
                            
                                CamelContext context;

                                @Named
                                public NoopHostnameVerifier x509HostnameVerifier() {
                                    return NoopHostnameVerifier.INSTANCE;
                                }  
                            }
                        """,
                """
                            import jakarta.inject.Named;
                            import org.apache.camel.CamelContext;
                            import org.apache.hc.client5.http.conn.ssl.NoopHostnameVerifier;
                            import org.eclipse.microprofile.config.ConfigProvider;
                            
                            public class HttpProducers {
                                
                                CamelContext context;
                                
                                @Named
                                public NoopHostnameVerifier x509HostnameVerifier() {
                                    return NoopHostnameVerifier.INSTANCE;
                                }                                  
                            }
                        """));
    }
}
