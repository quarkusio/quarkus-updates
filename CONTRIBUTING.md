# Contributing to Quarkus Update Recipes

If the recipes we provide need adjustments for your use case and you think that your changes could benefit others,
your contributions are very welcome.

## Writing a recipe for core Quarkus

### Step 1: Create or edit the YAML recipe file

Recipes are defined as YAML files in `recipes/src/main/resources/quarkus-updates/core/`.

If you are adding a recipe for a Quarkus version that already has a YAML file, add your recipe definition to that existing file.
Otherwise, create a new file following the naming convention `{major.minor}.alpha1.yaml`.

A simple YAML recipe file looks like this:

```yaml
#####
# Description of what the recipe does
#####
---
type: specs.openrewrite.org/v1beta/recipe
name: io.quarkus.updates.core.quarkus331.CoreUpdate331
recipeList:
  - io.quarkus.updates.core.quarkus331.AddExtensionsTrueToQuarkusMavenPlugin
```

Key points:
- `type` is always `specs.openrewrite.org/v1beta/recipe`.
- `name` follows the pattern `io.quarkus.updates.core.quarkus{majorminor}.{RecipeName}`.
- `recipeList` references either built-in OpenRewrite recipes (e.g. `org.openrewrite.java.ChangeType`, `org.openrewrite.java.dependencies.ChangeDependency`) or custom Java recipes defined in this project.

You can define multiple recipes in the same YAML file, separated by `---`. Recipes can also have `preconditions` to control when they are applied:

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: io.quarkus.updates.core.quarkus324.AddHibernateAnnotationProcessorIfNewJpaModelgenDependency
recipeList:
  - io.quarkus.updates.core.quarkus37.AddMavenCompilerAnnotationProcessor:
      groupId: org.hibernate.orm
      artifactId: hibernate-processor
      mavenCompilerPluginVersion: 3.12.1
preconditions:
  - org.openrewrite.maven.search.FindDependency:
      groupId: org.hibernate.orm
      artifactId: hibernate-jpamodelgen
```

### Step 2: (Optional) Write a custom Java recipe

If the built-in OpenRewrite recipes are not sufficient, you can write a custom Java recipe class in `recipes/src/main/java/io/quarkus/updates/core/quarkus{majorminor}/`.

Custom recipes extend `org.openrewrite.Recipe` and use the OpenRewrite visitor pattern. Here is a simplified example:

```java
package io.quarkus.updates.core.quarkus331;

import org.openrewrite.*;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.maven.MavenIsoVisitor;
import java.time.Duration;

@lombok.Value
@lombok.EqualsAndHashCode(callSuper = false)
public class AddExtensionsTrueToQuarkusMavenPlugin extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add <extensions>true</extensions> to quarkus-maven-plugin";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenIsoVisitor<>() {
            // visitor implementation
        };
    }
}
```

Note that the `recipes` module uses Java 11 as target.

### Step 3: Write tests

Tests that use `CoreTestUtil.recipe(spec, path, quarkusVersion)` resolve dependencies from the Quarkus BOM at the given version. This means the version must be actually released and available in Maven Central. For recipes targeting an upcoming Quarkus version, you typically need to wait for the CR1 release before you can write tests that rely on BOM version resolution.

Tests live in `recipes-tests/src/test/java/io/quarkus/updates/core/` and use OpenRewrite's testing framework.

Create a test class that implements `RewriteTest`:

```java
package io.quarkus.updates.core;

import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate331Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec,
                Path.of("quarkus-updates", "core", "3.31.alpha1.yaml"),
                "3.31.0.CR1")
            .parser(JavaParser.fromJavaVersion()
                .logCompilationWarningsAndErrors(true))
            .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testAddExtensionsToQuarkusMavenPlugin() {
        //language=xml
        rewriteRun(pomXml(
            // before
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <build>
                    <plugins>
                        <plugin>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """,
            // after
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>io.quarkus</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <build>
                    <plugins>
                        <plugin>
                            <artifactId>quarkus-maven-plugin</artifactId>
                            <version>3.30.0</version>
                            <extensions>true</extensions>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """));
    }

    @Test
    void testDoNotAddExtensionsIfAlreadyPresent() {
        // When providing a single argument to pomXml(), the test asserts
        // that the recipe does NOT modify the input
        //language=xml
        rewriteRun(pomXml("""
            <project>
                ...
            </project>
            """));
    }
}
```

Key testing patterns:
- `CoreTestUtil.recipe(spec, path, quarkusVersion)` loads the YAML recipe and optionally upgrades the Quarkus BOM version in test POMs. Use the overload without `quarkusVersion` when BOM version resolution is not needed.
- `pomXml(before, after)` asserts that the recipe transforms the input POM to the expected output.
- `pomXml(unchanged)` (single argument) asserts that the recipe does **not** modify the input.

### Step 4: Run the tests

```
./mvnw clean install
```

This builds the recipes module and runs all the tests.

## Writing a recipe for an extension

Extension recipes allow individual Quarkus extensions (from Quarkiverse or other projects) to provide their own update recipes.

### Recipes hosted in this repository

Extension recipe YAML files are placed under `recipes/src/main/resources/quarkus-updates/{groupId}/{artifactId}/`.

The directory structure **must match** a dependency from the project being migrated. The tooling decides whether to apply an extension recipe based on whether the project being updated has a matching dependency:
- The `{groupId}` must match exactly.
- The `{artifactId}` uses `startsWith` matching, so a directory named `quarkus-minio` will match artifacts `quarkus-minio`, `quarkus-minio-client`, etc.

**Example:** for the MinIO extension (`io.quarkiverse.minio:quarkus-minio`):

```
recipes/src/main/resources/quarkus-updates/
  io.quarkiverse.minio/
    quarkus-minio/
      3.8.yaml
```

The YAML recipe file (`3.8.yaml`) follows the same format as core recipes:

```yaml
type: specs.openrewrite.org/v1beta/recipe
name: io.quarkus.updates.minio.minio38.UpdateAll
recipeList:
  - io.quarkus.updates.minio.minio38.UpdateProperties
  - io.quarkus.updates.quarkiverse.minio.minio38.AdjustURLPropertyValue

---
type: specs.openrewrite.org/v1beta/recipe
name: io.quarkus.updates.minio.minio38.UpdateProperties
recipeList:
  - org.openrewrite.quarkus.ChangeQuarkusPropertyKey:
      oldPropertyKey: quarkus.minio.(.*\.)?url
      newPropertyKey: quarkus.minio.$1host
      changeAllProfiles: true
```

Custom Java recipe classes for extensions go in `recipes/src/main/java/io/quarkus/updates/quarkiverse/{extension}/{version}/`.

The version selection logic for extension recipes works the same way as for core recipes.

### Recipes hosted in an external dependency

Extensions can also provide recipes in their own project/repository. When recipes come as an external dependency, the following requirements apply:

- The YAML recipe files must follow the same directory structure (`{groupId}/{artifactId}/`) described above.
- The dependency containing the recipes must be added to the `recipes/pom.xml`.
- Test coverage **must** be added to this project (in the `recipes-tests` module), even if the recipes are also tested in their own project.

See the Apache Camel Quarkus extension for an example of this pattern:
- Recipe YAML files in `recipes/src/main/resources/quarkus-updates/org.apache.camel.quarkus/camel-quarkus/`
- Tests in `recipes-tests/src/test/java/io/quarkus/updates/camel/`

## Testing your changes against a real project

If you want to test your changes on a real application:

1. Install your changes to your local repository:
   ```
   ./mvnw clean install
   ```

2. Run `quarkus update` with your local recipes:
   ```
   quarkus update --quarkus-update-recipes=999-SNAPSHOT
   ```
