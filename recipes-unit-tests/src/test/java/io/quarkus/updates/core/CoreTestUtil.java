package io.quarkus.updates.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import org.openrewrite.Recipe;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.test.RecipeSpec;

public class CoreTestUtil {

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
}
