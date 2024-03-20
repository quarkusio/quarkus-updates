package io.quarkus.updates.core.quarkus37;

import static org.openrewrite.xml.AddToTagVisitor.addToTag;

import java.util.List;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.maven.tree.Plugin;
import org.openrewrite.maven.tree.ResolvedManagedDependency;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddMavenCompilerAnnotationProcessor extends Recipe {

    private static final XPathMatcher BUILD_MATCHER = new XPathMatcher("/project/build");
    private static final String MAVEN_COMPILER_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    private static final String MAVEN_COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin";

    @Option(displayName = "Annotation processor groupId", description = "The groupId of the annotation processor.", example = "org.hibernate.orm", required = true)
    String groupId;

    @Option(displayName = "Annotation processor artifactId", description = "The artifactId of the annotation processor.", example = "hibernate-jpamodelgen", required = true)
    String artifactId;

    @Option(displayName = "Enforce managed version", description = "If the new annotation processor has a managed version, this flag can be used to explicitly set the version on the annotation processor with the version of the managed dependency. The default for this flag is `false`.", required = false)
    @Nullable
    Boolean enforceManagedVersion;

    @Option(displayName = "Annotation processor artifactId", description = "The version of the Maven Compiler plugin to use if not present.", example = "hibernate-jpamodelgen", required = false)
    @Nullable
    String mavenCompilerPluginVersion;

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
        return new AddMavenCompilerPluginAnnotationProcessorVisitor();
    }

    private class AddMavenCompilerPluginAnnotationProcessorVisitor extends MavenIsoVisitor<ExecutionContext> {

        @Override
        public Xml.Document visitDocument(Xml.Document document, ExecutionContext ctx) {
            Xml.Tag root = document.getRoot();
            if (!root.getChild("build").isPresent()) {
                document = (Xml.Document) new AddToTagVisitor<>(root, Xml.Tag.build("<build/>"))
                        .visitNonNull(document, ctx, getCursor().getParentOrThrow());
            }
            return super.visitDocument(document, ctx);
        }

        @Override
        public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag buildTag = super.visitTag(tag, ctx);

            if (BUILD_MATCHER.matches(getCursor())) {
                Optional<Xml.Tag> maybePluginsTag = buildTag.getChild("plugins");
                Xml.Tag pluginsTag;
                if(maybePluginsTag.isPresent()) {
                    pluginsTag = maybePluginsTag.get();
                } else {
                    buildTag = (Xml.Tag) new AddToTagVisitor<>(buildTag, Xml.Tag.build("<plugins/>")).visitNonNull(buildTag, ctx, getCursor().getParentOrThrow());
                    //noinspection OptionalGetWithoutIsPresent
                    pluginsTag = buildTag.getChild("plugins").get();
                }

                Optional<Xml.Tag> maybeMavenCompilerPluginTag = pluginsTag.getChildren().stream()
                        .filter(plugin ->
                                "plugin".equals(plugin.getName()) &&
                                        MAVEN_COMPILER_PLUGIN_GROUP_ID.equals(plugin.getChildValue("groupId").orElse(MAVEN_COMPILER_PLUGIN_GROUP_ID)) &&
                                        MAVEN_COMPILER_PLUGIN_ARTIFACT_ID.equals(plugin.getChildValue("artifactId").orElse(null))
                        )
                        .findAny();

                String annotationProcessorPath = "<groupId>" + groupId + "</groupId>\n<artifactId>" + artifactId
                        + "</artifactId>";
                if (Boolean.TRUE.equals(enforceManagedVersion)) {
                    String managedVersion = getManagedVersion(groupId, artifactId);
                    if (managedVersion != null) {
                        annotationProcessorPath += "<version>" + managedVersion + "</version>";
                    }
                }

                if (maybeMavenCompilerPluginTag.isPresent()) {
                    Xml.Tag mavenCompilerPluginTag = maybeMavenCompilerPluginTag.get();

                    Optional<Xml.Tag> configuration = mavenCompilerPluginTag.getChild("configuration");

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
                                buildTag = addToTag(buildTag, annotationProcessorPathsWrapper.get(),
                                        Xml.Tag.build("<" + childName + ">\n" + annotationProcessorPath + "\n</" + childName
                                                + ">"),
                                        getCursor().getParentOrThrow());
                            }
                        } else {
                            buildTag = addToTag(buildTag, configuration.get(),
                                    Xml.Tag.build("<annotationProcessorPaths>\n<path>\n" + annotationProcessorPath
                                            + "\n</path>\n</annotationProcessorPaths>"),
                                    getCursor().getParentOrThrow());
                        }
                    } else {
                        buildTag = addToTag(buildTag, mavenCompilerPluginTag,
                                Xml.Tag.build("<configuration>\n<annotationProcessorPaths>\n<path>\n" + annotationProcessorPath
                                        + "\n</path>\n</annotationProcessorPaths>\n</configuration>"),
                                getCursor().getParentOrThrow());
                    }
                } else {
                    MavenResolutionResult currentResolutionResult = getResolutionResult();
                    boolean mavenCompilerPluginVersionDefined = false;
                    while (currentResolutionResult != null) {
                        if (currentResolutionResult.getPom().getPluginManagement().stream()
                                .anyMatch(p -> (MAVEN_COMPILER_PLUGIN_GROUP_ID.equals(p.getGroupId()) || p.getGroupId() == null)
                                        && MAVEN_COMPILER_PLUGIN_ARTIFACT_ID.equals(p.getArtifactId())
                                        && p.getVersion() != null) ||
                                currentResolutionResult.getPom().getPlugins().stream()
                                        .anyMatch(p -> (MAVEN_COMPILER_PLUGIN_GROUP_ID.equals(p.getGroupId()) || p.getGroupId() == null)
                                                && MAVEN_COMPILER_PLUGIN_ARTIFACT_ID.equals(p.getArtifactId())
                                                && p.getVersion() != null)) {
                            mavenCompilerPluginVersionDefined = true;
                            break;
                        }

                        currentResolutionResult = currentResolutionResult.getParent();
                    }

                    buildTag = addToTag(buildTag, pluginsTag,
                            Xml.Tag.build("<plugin>\n" +
                            "<artifactId>" + MAVEN_COMPILER_PLUGIN_ARTIFACT_ID + "</artifactId>\n" +
                            (mavenCompilerPluginVersion != null && !mavenCompilerPluginVersionDefined ? "<version>" + mavenCompilerPluginVersion + "</version>\n" : "") +
                            "<configuration>\n<annotationProcessorPaths>\n<path>\n" + annotationProcessorPath
                                        + "\n</path>\n</annotationProcessorPaths>\n</configuration>\n" +
                            "</plugin>"),
                            getCursor().getParentOrThrow());
                }
            }

            return buildTag;
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
    }
}
