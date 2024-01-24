package io.quarkus.updates.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.openrewrite.Recipe;
import org.openrewrite.config.CompositeRecipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.maven.UpgradeDependencyVersion;
import org.openrewrite.maven.ChangePropertyValue;
import org.openrewrite.test.RecipeSpec;

public final class CoreTestUtil {

    private CoreTestUtil() {
    }

    public static RecipeSpec recipe(RecipeSpec spec, Path recipeFile) {
        try (InputStream yamlRecipeInputStream = CoreTestUtil.class.getClassLoader().getResourceAsStream(recipeFile.toString())) {
            YamlResourceLoader yamlResourceLoader = new YamlResourceLoader(yamlRecipeInputStream, recipeFile.toUri(), new Properties());
            Collection<Recipe> recipes = yamlResourceLoader.listRecipes();

            return spec.recipeFromResource(Path.of("/").resolve(recipeFile).toAbsolutePath().toString(), recipes.stream()
                    .map(r -> r.getName()).toArray(String[]::new));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to open recipe file " + recipeFile, e);
        }
    }

    public static RecipeSpec recipe(RecipeSpec spec, Path recipeFile, String quarkusVersion) {
        try (InputStream yamlRecipeInputStream = CoreTestUtil.class.getClassLoader().getResourceAsStream(recipeFile.toString())) {
            YamlResourceLoader yamlResourceLoader = new YamlResourceLoader(yamlRecipeInputStream, recipeFile.toUri(), new Properties());
            String[] recipeNames = yamlResourceLoader.listRecipes().stream()
                    .map(r -> r.getName()).toArray(String[]::new);

            Recipe updateQuarkusRecipe = updateQuarkus(quarkusVersion);
            Recipe updateRecipes = recipeFromResource(Path.of("/").resolve(recipeFile).toAbsolutePath().toString(), recipeNames);

            return spec.recipes(updateQuarkusRecipe, updateRecipes);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to open recipe file " + recipeFile, e);
        }
    }

    private static Recipe updateQuarkus(String quarkusVersion) {
        return new CompositeRecipe(List.of(new UpgradeDependencyVersion("io.quarkus.platform", "quarkus-bom", quarkusVersion, null, null, null),
                new UpgradeDependencyVersion("io.quarkus", "quarkus-bom", quarkusVersion, null, null, null)));
    }

    private static Recipe recipeFromResource(String yamlResource, String... activeRecipes) {
        return Environment.builder()
                .load(new YamlResourceLoader(RecipeSpec.class.getResourceAsStream(yamlResource), URI.create("rewrite.yml"), new Properties()))
                .build()
                .activateRecipes(activeRecipes);
    }
}
