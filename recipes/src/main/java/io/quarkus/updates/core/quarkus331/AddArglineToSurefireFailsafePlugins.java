package io.quarkus.updates.core.quarkus331;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddArglineToSurefireFailsafePlugins extends Recipe {

    private static final String QUARKUS_MAVEN_PLUGIN_ARTIFACT_ID = "quarkus-maven-plugin";
    private static final String MAVEN_SUREFIRE_PLUGIN_ARTIFACT_ID = "maven-surefire-plugin";
    private static final String MAVEN_FAILSAFE_PLUGIN_ARTIFACT_ID = "maven-failsafe-plugin";
    private static final String ARGLINE_PLACEHOLDER = "@{argLine}";

    @Override
    public String getDisplayName() {
        return "Add @{argLine} to maven-surefire-plugin and maven-failsafe-plugin";
    }

    @Override
    public String getDescription() {
        return "Add or update <argLine> in maven-surefire-plugin and maven-failsafe-plugin configuration to include @{argLine} placeholder.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            private boolean hasQuarkusMavenPlugin = false;

            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext ctx) {
                // First scan: check if quarkus-maven-plugin is present
                hasQuarkusMavenPlugin = false;
                new MavenVisitor<ExecutionContext>() {
                    @Override
                    public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                        Xml.Tag pluginTag = (Xml.Tag) super.visitTag(tag, ctx);
                        if (isPluginTag() && QUARKUS_MAVEN_PLUGIN_ARTIFACT_ID.equals(pluginTag.getChildValue("artifactId").orElse(null))) {
                            hasQuarkusMavenPlugin = true;
                        }
                        return pluginTag;
                    }
                }.visit(document, ctx);

                // Second pass: modify surefire and failsafe plugins if quarkus-maven-plugin is present
                if (!hasQuarkusMavenPlugin) {
                    return document;
                }
                return super.visitDocument(document, ctx);
            }

            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag pluginTag = (Xml.Tag) super.visitTag(tag, ctx);

                if (!isPluginTag()) {
                    return pluginTag;
                }

                String artifactId = pluginTag.getChildValue("artifactId").orElse(null);
                if (!MAVEN_SUREFIRE_PLUGIN_ARTIFACT_ID.equals(artifactId) && !MAVEN_FAILSAFE_PLUGIN_ARTIFACT_ID.equals(artifactId)) {
                    return pluginTag;
                }

                // Get configuration tag
                Xml.Tag configurationTag = pluginTag.getChild("configuration").orElse(null);

                if (configurationTag == null) {
                    // Add configuration with argLine
                    Xml.Tag newConfiguration = Xml.Tag.build("<configuration><argLine>" + ARGLINE_PLACEHOLDER + "</argLine></configuration>");
                    doAfterVisit(new AddToTagVisitor<>(pluginTag, newConfiguration));
                    return pluginTag;
                }

                // Check if argLine exists in configuration
                Xml.Tag argLineTag = configurationTag.getChild("argLine").orElse(null);

                if (argLineTag == null) {
                    // Add argLine to configuration
                    Xml.Tag newArgLine = Xml.Tag.build("<argLine>" + ARGLINE_PLACEHOLDER + "</argLine>");
                    doAfterVisit(new AddToTagVisitor<>(configurationTag, newArgLine));
                } else {
                    // Check if argLine already contains @{argLine}
                    String currentValue = argLineTag.getValue().orElse("");
                    if (!currentValue.contains(ARGLINE_PLACEHOLDER)) {
                        // Prepend @{argLine} to existing value
                        String newValue = ARGLINE_PLACEHOLDER + " " + currentValue;
                        doAfterVisit(new ChangeTagValueVisitor<>(argLineTag, newValue));
                    }
                }

                return pluginTag;
            }
        };
    }
}
