package io.quarkus.updates.core.quarkus331;

import static org.openrewrite.xml.AddToTagVisitor.addToTag;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.xml.tree.Xml.Tag;

import java.util.Comparator;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddExtensionsTrueToQuarkusMavenPlugin extends Recipe {

    private static final String QUARKUS_MAVEN_PLUGIN_ARTIFACT_ID = "quarkus-maven-plugin";

    @Override
    public String getDisplayName() {
        return "Add <extensions>true</extensions> to quarkus-maven-plugin";
    }

    @Override
    public String getDescription() {
        return "Add <extensions>true</extensions> to the quarkus-maven-plugin configuration if not already present.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag pluginTag = (Xml.Tag) super.visitTag(tag, ctx);
                if (!isPluginTag()
                        || !QUARKUS_MAVEN_PLUGIN_ARTIFACT_ID.equals(pluginTag.getChildValue("artifactId").orElse(null))
                        || pluginTag.getChild("extensions").isPresent()) {
                    return pluginTag;
                }

                Xml.Tag extensionsTag = Xml.Tag.build("<extensions>true</extensions>");
                doAfterVisit(new AddToTagVisitor<>(pluginTag, extensionsTag, PluginTagOrdering.INSTANCE));
                return pluginTag;
            }
        };
    }

    private static class PluginTagOrdering implements Comparator<Content> {

        private static final PluginTagOrdering INSTANCE = new PluginTagOrdering();

        @Override
        public int compare(Content o1, Content o2) {
            int priority1 = getPriority(o1);
            int priority2 = getPriority(o2);
            return Integer.compare(priority1, priority2);
        }

        private int getPriority(Content content) {
            if (!(content instanceof Xml.Tag)) {
                return Integer.MAX_VALUE;
            }
            Xml.Tag tag = (Xml.Tag) content;
            String tagName = tag.getName();

            switch (tagName) {
                case "groupId":
                case "artifactId":
                case "version":
                    return 1;
                case "extensions":
                    return 2;
                default:
                    return 3;
            }
        }
    }
}
