package io.quarkus.updates.core.quarkus37;

import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenTagInsertionComparator;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.table.MavenMetadataFailures;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.maven.tree.ResolvedManagedDependency;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class SyncMavenCompilerAnnotationProcessorVersion extends Recipe {

    @EqualsAndHashCode.Exclude
    MavenMetadataFailures metadataFailures = new MavenMetadataFailures(this);

    @Option(displayName = "Annotation processor groupId", description = "The groupId of the annotation processor.", example = "org.hibernate.orm", required = true)
    String groupId;

    @Option(displayName = "Annotation processor artifactId", description = "The artifactId of the annotation processor.", example = "hibernate-jpamodelgen", required = true)
    String artifactId;

    @Override
    public String getDisplayName() {
        return "Sync Maven Compiler plugin annotation processor version with the one provided by the BOM";
    }

    @Override
    public String getDescription() {
        return "Sync Maven Compiler plugin annotation processor version with the one provided by the BOM.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new MavenVisitor<ExecutionContext>() {
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
                                    .filter(t -> groupId.equals(t.getChildValue("groupId").orElse(null))
                                            && artifactId.equals(t.getChildValue("artifactId").orElse(null)))
                                    .findFirst();

                            if (annotationProcessorPathCandidate.isPresent()) {
                                Xml.Tag annotationProcessorPath = annotationProcessorPathCandidate.get();

                                String newVersion = getManagedVersion(groupId, artifactId);
                                if (newVersion != null) {
                                    Optional<Xml.Tag> versionTag = annotationProcessorPath.getChild("version");
                                    if (!versionTag.isPresent()) {
                                        Xml.Tag newVersionTag = Xml.Tag
                                                .build("<version>" + newVersion + "</version>");
                                        mavenCompilerPluginTag = AddToTagVisitor.addToTag(
                                                mavenCompilerPluginTag, annotationProcessorPath, newVersionTag,
                                                getCursor().getParent());
                                    } else {
                                        mavenCompilerPluginTag = changeChildTagValue(mavenCompilerPluginTag, annotationProcessorPath,
                                                "version", newVersion, ctx);
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

            private String getManagedVersion(String groupId, String artifactId) {
                MavenResolutionResult result = getResolutionResult();
                for (ResolvedManagedDependency managedDependency : result.getPom().getDependencyManagement()) {
                    if (groupId.equals(managedDependency.getGroupId())
                            && artifactId.equals(managedDependency.getArtifactId())) {
                        return managedDependency.getVersion();
                    }
                }
                return null;
            }
        };
    }
}
