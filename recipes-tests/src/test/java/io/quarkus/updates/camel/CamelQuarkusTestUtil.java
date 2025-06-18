package io.quarkus.updates.camel;

import org.openrewrite.Recipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.test.RecipeSpec;

import java.net.URI;
import java.util.Properties;

public class CamelQuarkusTestUtil {

    public static RecipeSpec recipe3alpha(RecipeSpec spec) {
       return recipe(spec, "3alpha");
    }

    public static RecipeSpec recipe3alpha(RecipeSpec spec, String... activeRecipes) {
       return recipe(spec, "3alpha", activeRecipes);
    }

    public static RecipeSpec recipe3_8(RecipeSpec spec, String... activeRecipes) {
        return recipeForVersion("3.8", spec, activeRecipes);
    }

    public static RecipeSpec recipe3_15(RecipeSpec spec, String... activeRecipes) {
        return recipeForVersion("3.15", spec, activeRecipes);
    }

    public static RecipeSpec recipe3_17(RecipeSpec spec, String... activeRecipes) {
        return recipeForVersion("3.17", spec, activeRecipes);
    }

    public static RecipeSpec recipe3_18(RecipeSpec spec, String... activeRecipes) {
        return recipeForVersion("3.18", spec, activeRecipes);
    }

    public static RecipeSpec recipe3_22(RecipeSpec spec, String... activeRecipes) {
        return recipeForVersion("3.22", spec, activeRecipes);
    }

    public static RecipeSpec recipe3_24(RecipeSpec spec, String... activeRecipes) {
        return recipeForVersion("3.24", spec, activeRecipes);
    }

    private static RecipeSpec recipeForVersion(String version, RecipeSpec spec, String... activeRecipes) {
        if(activeRecipes.length == 0) {
            return recipe(spec, version);
        }

       return recipe(spec, version, activeRecipes);
    }


    private static RecipeSpec recipe(RecipeSpec spec, String version) {
        String[] defaultRecipes = switch (version) {
            case "3.8" -> new String[] {"io.quarkus.updates.camel.camel44.CamelQuarkusMigrationRecipe"};
            case "3alpha" -> new String[] {"io.quarkus.updates.camel.camel40.CamelQuarkusMigrationRecipe"};
            case "3.15" -> new String[] {"io.quarkus.updates.camel.camel47.CamelQuarkusMigrationRecipe"};
            case "3.17" -> new String[] {"io.quarkus.updates.camel.camel49.CamelQuarkusMigrationRecipe"};
            case "3.18" -> new String[] {"io.quarkus.updates.camel.camel410.CamelQuarkusMigrationRecipe"};
            case "3.22" -> new String[] {"io.quarkus.updates.camel.camel411.CamelQuarkusMigrationRecipe"};
            case "3.24" -> new String[] {"io.quarkus.updates.camel.camel412.CamelQuarkusMigrationRecipe"};
            default -> throw new IllegalArgumentException("Version '" + version + "' is not allowed!");
        };
        return recipe(spec, version, defaultRecipes);
    }

    public static RecipeSpec recipe(RecipeSpec spec, String version, String... activerecipes) {

        YamlResourceLoader yrl = new YamlResourceLoader(
                CamelQuarkusTestUtil.class.getResourceAsStream("/quarkus-updates/org.apache.camel.quarkus/camel-quarkus/" + version + ".yaml"), URI.create("rewrite.yml"), new Properties());
        Recipe r = Environment.builder()
                //classpath loader has to be initialized to load recipes from camel-upgrade-recipes dependency
                .scanYamlResources()
                //yaml single recipe with Quarkus recipe has to be present as it contains active recipe
                .load(yrl)
                .build()
                .activateRecipes(activerecipes);

        spec.recipes(r);
        return spec;
    }
}
