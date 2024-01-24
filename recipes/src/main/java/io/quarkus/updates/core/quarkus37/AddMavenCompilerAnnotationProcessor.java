package io.quarkus.updates.core.quarkus37;

import static org.openrewrite.xml.AddToTagVisitor.addToTag;

import java.util.List;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.maven.tree.ResolvedManagedDependency;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddMavenCompilerAnnotationProcessor extends Recipe {

    @Option(displayName = "Annotation processor groupId", description = "The groupId of the annotation processor.", example = "org.hibernate.orm", required = true)
    String groupId;

    @Option(displayName = "Annotation processor artifactId", description = "The artifactId of the annotation processor.", example = "hibernate-jpamodelgen", required = true)
    String artifactId;

    @Option(displayName = "Enforce managed version", description = "If the new annotation processor has a managed version, this flag can be used to explicitly set the version on the annotation processor with the version of the managed dependency. The default for this flag is `false`.", required = false)
    @Nullable
    Boolean enforceManagedVersion;

    @Override
    public String getDisplayName() {
        return "Add an annotation processor to the Maven Compiler plugin configuration";
    }

    @Override
    public String getDescription() {
        return "Add an annotation processor to the Maven Compiler plugin configuration.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag mavenCompilerPluginTag = (Xml.Tag) super.visitTag(tag, ctx);

                if (isPluginTag("org.apache.maven.plugins", "maven-compiler-plugin")) {
                    Optional<Xml.Tag> configuration = mavenCompilerPluginTag.getChild("configuration");

                    String annotationProcessorPath = "<groupId>" + groupId + "</groupId>\n<artifactId>" + artifactId
                            + "</artifactId>";

                    if (Boolean.TRUE.equals(enforceManagedVersion)) {
                        String managedVersion = getManagedVersion(groupId, artifactId);
                        if (managedVersion != null) {
                            annotationProcessorPath += "<version>" + managedVersion + "</version>";
                        }
                    }

                    if (configuration.isPresent()) {
                        Optional<Xml.Tag> annotationProcessorPathsWrapper = configuration.get()
                                .getChild("annotationProcessorPaths");
                        if (annotationProcessorPathsWrapper.isPresent()) {
                            List<Xml.Tag> annotationProcessorPaths = annotationProcessorPathsWrapper.get().getChildren();
                            String childName = annotationProcessorPaths.size() > 0 ? annotationProcessorPaths.get(0).getName()
                                    : "path";

                            if (!annotationProcessorPaths.stream()
                                    .anyMatch(t -> groupId.equals(t.getChildValue("groupId").orElse(null))
                                            && artifactId.equals(t.getChildValue("artifactId").orElse(null)))) {
                                mavenCompilerPluginTag = addToTag(mavenCompilerPluginTag, annotationProcessorPathsWrapper.get(),
                                        Xml.Tag.build("<" + childName + ">\n" + annotationProcessorPath + "\n</" + childName
                                                + ">"),
                                        getCursor().getParentOrThrow());
                            }
                        } else {
                            mavenCompilerPluginTag = addToTag(mavenCompilerPluginTag, configuration.get(),
                                    Xml.Tag.build("<annotationProcessorPaths>\n<path>\n" + annotationProcessorPath
                                            + "\n</path>\n</annotationProcessorPaths>"),
                                    getCursor().getParentOrThrow());
                        }
                    } else {
                        mavenCompilerPluginTag = addToTag(mavenCompilerPluginTag,
                                Xml.Tag.build("<configuration>\n<annotationProcessorPaths>\n<path>\n" + annotationProcessorPath
                                        + "\n</path>\n</annotationProcessorPaths>\n</configuration>"),
                                getCursor().getParentOrThrow());
                    }
                }
                return mavenCompilerPluginTag;
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
