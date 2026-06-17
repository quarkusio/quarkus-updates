# Quarkus Update Recipes

[![Version](https://img.shields.io/maven-central/v/io.quarkus/quarkus-update-recipes?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkus/quarkus-update-recipes)

This repository contains the [OpenRewrite](https://docs.openrewrite.org/) recipes used by the Quarkus tooling (Quarkus CLI, IDE plugins) to update Quarkus projects to newer versions.

The Quarkus tooling will always use the latest release of the recipe artifact.

## Project structure

```
recipes/
  src/main/java/              # Custom Java recipe implementations
  src/main/resources/
    quarkus-updates/
      core/                    # Core Quarkus recipes
      {groupId}/{artifactId}/  # Extension-specific recipes
recipes-tests/
  src/test/java/               # Unit and integration tests
```

## How recipes work

### Recipe location and naming

Core recipes are placed in `recipes/src/main/resources/quarkus-updates/core/` and follow the naming pattern `{major.minor}.alpha1.yaml` (e.g. `3.7.alpha1.yaml`, `3.31.alpha1.yaml`).

Extension recipes are placed under `recipes/src/main/resources/quarkus-updates/{groupId}/{artifactId}/` and are only applied when the project being updated has a matching dependency (see [Contributing](./CONTRIBUTING.md) for details).

### Recipe selection logic

Given:
- `currentVersion`: the current Quarkus core version of the project
- `targetVersion`: the target Quarkus core version to update to
- `recipeVersion`: the version extracted from the recipe file name

A recipe is applied if (only comparing major.minor):
`currentVersion < recipeVersion AND targetVersion >= recipeVersion`.

**Example:**

Content of the `core/` directory:
- 3.7.alpha1.yaml
- 3.9.alpha1.yaml
- 3.24.alpha1.yaml
- 3.31.alpha1.yaml

Recipes applied for a project in version 3.6.0.Final updating to 3.31.0.Final (`currentVersion=3.6`, `targetVersion=3.31`):
- 3.7.alpha1.yaml
- 3.9.alpha1.yaml
- 3.24.alpha1.yaml
- 3.31.alpha1.yaml

Recipes applied for a project in version 3.9.0.Final updating to 3.25.0.Final (`currentVersion=3.9`, `targetVersion=3.25`):
- 3.24.alpha1.yaml

## Contributing

Contributions are welcome. See [CONTRIBUTING.md](./CONTRIBUTING.md) for how to write, test, and submit recipes for both core Quarkus and extensions.

## Release

To release a new version, follow these steps:

https://github.com/smallrye/smallrye/wiki/Release-Process#releasing

The staging repository is automatically closed. The sync with Maven Central should take ~30 minutes.
