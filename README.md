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

### How to provide extension recipes

Recipes, which are applied for the specific extension only, have to fulfill several restrictions.

#### Extension recipes from src 

The tooling decides whether the yaml recipe is applied to the migrated project. 
Non-core recipes are applied only if the location inside `recipes/src/main/resources` **matches** a dependency from the migrated.
For example, if the extension depends on the artifact `my.groupId:my.artifactId-anything:1.0`, only recipes from the location `recipes/src/main/resources/my.groupId/my.artifactId` are applied (the artifactId matches via startsWith, therefore in this example, the folder can be named `my` or `my.art`, ...).
The location should be unique so no other extension would trigger the recipe as well.

#### Extension recipes from external dependency

When the recipes come as a dependency, be aware of the following requirements

- Yaml recipe has to be created in the same way (the correct location) described in the previous chapter.
- Test coverage has to be added to this project (even if the recipes are tested in their own project).

## Contributing

Contributions are welcome, see [CONTRIBUTING.md](./CONTRIBUTING.md) for more information.

## Release

To release a new version, follow these steps:

https://github.com/smallrye/smallrye/wiki/Release-Process#releasing

The staging repository is automatically closed. The sync with Maven Central should take ~30 minutes.
