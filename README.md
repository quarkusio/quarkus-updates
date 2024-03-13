# Quarkus Update Recipes

[![Version](https://img.shields.io/maven-central/v/io.quarkus/quarkus-update-recipes?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkus/quarkus-update-recipes)

This repository contains the recipes used by the Quarkus tooling to update Quarkus projects to newer versions.

The recipes are contained in the `recipes/src/main/resources/quarkus-updates/core` directory and follow a pattern. Recipes are placed in `[major.minor].yaml` (i.e. `3.0.yaml`).

Given:
- `currentVersion` the current Quarkus core version of the project
- `targetVersion` the target Quarkus core version to update to 
- `recipeVersion` the version of the recipe file

Then, the recipe is applied if (only comparing major.minor):
`currentVersion < recipeVersion AND targetVersion >= recipeVersion`.

The Quarkus tooling will always use the latest GitHub release of the recipe directory.

Example:

Content of the `quarkus-updates` directory:
- 2.7.yaml
- 2.9.yaml
- 3alpha.yaml
- 3.1.yaml
- 3.5.yaml

Recipes applied for a project in version 2.0.0.Final updating to 3.0.0.Alpha1 (`currentVersion=2.0`, `targetVersion=3.0`):
- 2.7.yaml
- 2.9.yaml
- 3alpha.yaml

Recipes applied for a project in version 2.7.0.Final updating to 3.1.0.Final (`currentVersion=2.7`, `targetVersion=3.1`):
- 2.9.yaml
- 3alpha.yaml
- 3.1.yaml

## Contributing

Contributions are welcome, see [CONTRIBUTING.md](./CONTRIBUTING.md) for more information.

## Release

```
./mvnw -Prelease release:prepare release:perform
```
