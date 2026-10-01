package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;

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
}
