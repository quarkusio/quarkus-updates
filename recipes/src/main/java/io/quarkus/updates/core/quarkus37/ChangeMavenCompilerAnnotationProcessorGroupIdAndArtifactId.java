package io.quarkus.updates.core.quarkus37;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.Validated;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenDownloadingException;
import org.openrewrite.maven.MavenTagInsertionComparator;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.table.MavenMetadataFailures;
import org.openrewrite.maven.tree.MavenMetadata;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.maven.tree.ResolvedManagedDependency;
import org.openrewrite.semver.Semver;
import org.openrewrite.semver.VersionComparator;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ChangeMavenCompilerAnnotationProcessorGroupIdAndArtifactId extends Recipe {

    @EqualsAndHashCode.Exclude
    MavenMetadataFailures metadataFailures = new MavenMetadataFailures(this);

    @Option(displayName = "Old groupId", description = "The old groupId to replace. The groupId is the first part of a dependency coordinate `com.google.guava:guava:VERSION`. Supports glob expressions.", example = "org.hibernate")
    String oldGroupId;

    @Option(displayName = "Old artifactId", description = "The old artifactId to replace. The artifactId is the second part of a dependency coordinate `com.google.guava:guava:VERSION`. Supports glob expressions.", example = "hibernate-jpamodelgen")
    String oldArtifactId;

    @Option(displayName = "New groupId", description = "The new groupId to use. Defaults to the existing group id.", example = "org.hibernate.orm", required = false)
    @Nullable
    String newGroupId;

    @Option(displayName = "New artifactId", description = "The new artifactId to use. Defaults to the existing artifact id.", example = "hibernate-jpamodelgen", required = false)
    @Nullable
    String newArtifactId;

    @Option(displayName = "New version", description = "An exact version number or node-style semver selector used to select the version number.", example = "29.X", required = false)
    @Nullable
    String newVersion;

    @Option(displayName = "Version pattern", description = "Allows version selection to be extended beyond the original Node Semver semantics. So for example,"
            +
            "Setting 'version' to \"25-29\" can be paired with a metadata pattern of \"-jre\" to select Guava 29.0-jre", example = "-jre", required = false)
    @Nullable
    String versionPattern;

    @Option(displayName = "Override managed version", description = "If the new annotation processor has a managed version, this flag can be used to explicitly set the version on the annotation processor. The default for this flag is `false`.", required = false)
    @Nullable
    Boolean overrideManagedVersion;

    @Option(displayName = "Enforce managed version", description = "If the new annotation processor has a managed version, this flag can be used to explicitly set the version on the annotation processor with the version of the managed dependency. The default for this flag is `false`.", required = false)
    @Nullable
    Boolean enforceManagedVersion;

    @Override
    public String getDisplayName() {
        return "Change Maven Compiler plugin annotation processor groupId, artifactId and/or the version";
    }

    @Override
    public String getDescription() {
        return "Change the groupId, artifactId and/or the version of a specified Maven Compiler plugin annotation processor.";
    }

    @Override
    public Validated<Object> validate() {
        Validated<Object> validated = super.validate();
        if (newVersion != null) {
            validated = validated.and(Semver.validate(newVersion, versionPattern));
        }
        return validated;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new MavenVisitor<ExecutionContext>() {
            @Nullable
            final VersionComparator versionComparator = newVersion != null
                    ? Semver.validate(newVersion, versionPattern).getValue()
                    : null;
            @Nullable
            private Collection<String> availableVersions;

            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag mavenCompilerPluginTag = (Xml.Tag) super.visitTag(tag, ctx);

                if (isPluginTag("org.apache.maven.plugins", "maven-compiler-plugin")) {
                    Optional<Xml.Tag> configuration = mavenCompilerPluginTag.getChild("configuration");
                    if (configuration.isPresent()) {
                        Optional<Xml.Tag> annotationProcessorPathsWrapper = configuration.get()
                                .getChild("annotationProcessorPaths");
                        if (annotationProcessorPathsWrapper.isPresent()) {
                            Optional<Xml.Tag> annotationProcessorPathCandidate = annotationProcessorPathsWrapper.get()
                                    .getChildren().stream()
                                    .filter(t -> oldGroupId.equals(t.getChildValue("groupId").orElse(null))
                                            && oldArtifactId.equals(t.getChildValue("artifactId").orElse(null)))
                                    .findFirst();

                            if (annotationProcessorPathCandidate.isPresent()) {
                                Xml.Tag annotationProcessorPath = annotationProcessorPathCandidate.get();
                                String groupId = newGroupId;
                                if (groupId != null) {
                                    mavenCompilerPluginTag = changeChildTagValue(mavenCompilerPluginTag, annotationProcessorPath, "groupId", groupId,
                                            ctx);
                                } else {
                                    groupId = annotationProcessorPathCandidate.get().getChildValue("groupId")
                                            .orElseThrow(NoSuchElementException::new);
                                }
                                String artifactId = newArtifactId;
                                if (artifactId != null) {
                                    mavenCompilerPluginTag = changeChildTagValue(mavenCompilerPluginTag, annotationProcessorPath, "artifactId",
                                            artifactId, ctx);
                                } else {
                                    artifactId = annotationProcessorPathCandidate.get().getChildValue("artifactId")
                                            .orElseThrow(NoSuchElementException::new);
                                }

                                String versionToApply = Boolean.TRUE.equals(enforceManagedVersion) ? getManagedVersion(groupId, artifactId) : null;
                                if (versionToApply == null && newVersion != null) {
                                    try {
                                        versionToApply = resolveSemverVersion(ctx, groupId, artifactId);
                                    } catch (MavenDownloadingException e) {
                                        return e.warn(tag);
                                    }
                                }
                                if (versionToApply != null) {
                                    // starting with Maven Compiler plugin 3.12.0, the annotation processor versions will honor the dependency management
                                    Optional<Xml.Tag> versionTag = annotationProcessorPath.getChild("version");
                                    if (!versionTag.isPresent()) {
                                        if ((Boolean.TRUE.equals(overrideManagedVersion)
                                                || !isDependencyManaged(groupId, artifactId))
                                                || Boolean.TRUE.equals(enforceManagedVersion)) {
                                            //If the version is not present, add the version if we are explicitly overriding a managed version or if no managed version exists.
                                            Xml.Tag newVersionTag = Xml.Tag
                                                    .build("<version>" + versionToApply + "</version>");
                                            //noinspection ConstantConditions
                                            mavenCompilerPluginTag = AddToTagVisitor.addToTag(
                                                    mavenCompilerPluginTag, annotationProcessorPath, newVersionTag,
                                                    getCursor().getParent());
                                        }
                                    } else {
                                        if (isDependencyManaged(groupId, artifactId)
                                                && !Boolean.TRUE.equals(overrideManagedVersion)
                                                && !Boolean.TRUE.equals(enforceManagedVersion)) {
                                            //If the previous dependency had a version but the new artifact is managed, removed the
                                            //version tag.
                                            mavenCompilerPluginTag = (Xml.Tag) new RemoveContentVisitor<>(versionTag.get(),
                                                    false).visit(mavenCompilerPluginTag, ctx);
                                        } else {
                                            //Otherwise, change the version to the new value.
                                            mavenCompilerPluginTag = changeChildTagValue(mavenCompilerPluginTag, annotationProcessorPath,
                                                    "version", versionToApply, ctx);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //noinspection ConstantConditions
                return mavenCompilerPluginTag;
            }

            private Xml.Tag changeChildTagValue(Xml.Tag parentScope, Xml.Tag tag, String childTagName, String newValue, ExecutionContext ctx) {
                Optional<Xml.Tag> childTag = tag.getChild(childTagName);
                if (childTag.isPresent() && !newValue.equals(childTag.get().getValue().orElse(null))) {
                    return (Xml.Tag) new ChangeTagValueVisitor<>(childTag.get(), newValue).visitNonNull(parentScope, ctx);
                }
                return parentScope;
            }

            private boolean isDependencyManaged(String groupId, String artifactId) {

                MavenResolutionResult result = getResolutionResult();
                for (ResolvedManagedDependency managedDependency : result.getPom().getDependencyManagement()) {
                    if (groupId.equals(managedDependency.getGroupId())
                            && artifactId.equals(managedDependency.getArtifactId())) {
                        return true;
                    }
                }
                return false;
            }

            private String getManagedVersion(String groupId, String artifactId) {
                if (!isDependencyManaged(groupId, artifactId)) {
                    return null;
                }

                MavenResolutionResult result = getResolutionResult();
                for (ResolvedManagedDependency managedDependency : result.getPom().getDependencyManagement()) {
                    if (groupId.equals(managedDependency.getGroupId())
                            && artifactId.equals(managedDependency.getArtifactId())) {
                        return managedDependency.getVersion();
                    }
                }
                return null;
            }

            @SuppressWarnings("ConstantConditions")
            private String resolveSemverVersion(ExecutionContext ctx, String groupId, String artifactId)
                    throws MavenDownloadingException {
                if (versionComparator == null) {
                    return newVersion;
                }
                if (availableVersions == null) {
                    availableVersions = new ArrayList<>();
                    MavenMetadata mavenMetadata = metadataFailures.insertRows(ctx,
                            () -> downloadMetadata(groupId, artifactId, ctx));
                    for (String v : mavenMetadata.getVersioning().getVersions()) {
                        if (versionComparator.isValid(newVersion, v)) {
                            availableVersions.add(v);
                        }
                    }

                }
                return availableVersions.isEmpty() ? newVersion : Collections.max(availableVersions, versionComparator);
            }
        };
    }
}
