package io.quarkus.updates.core.quarkus30;

import java.util.List;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AdjustKotlinAllOpenDirectives extends Recipe {

    private static final String ALL_OPEN_ANNOTATION = "all-open:annotation=";
    private static final XPathMatcher PLUGIN_MATCHER = new XPathMatcher("/project/build/plugins/plugin");

    private static final List<String> JAVAX_PACKAGES = List.of("javax.ws.", "javax.enterprise.");

    @Override
    public String getDisplayName() {
        return "Adjust all-open directives in Kotlin plugin configuration";
    }

    @Override
    public String getDescription() {
        return "Adjust all-open directives in Kotlin plugin configuration";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new MavenVisitor<ExecutionContext>() {

            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag t = (Xml.Tag) super.visitTag(tag, ctx);

                if (!PLUGIN_MATCHER.matches(getCursor())) {
                    return t;
                }

                if (!"org.jetbrains.kotlin".equals(t.getChildValue("groupId").orElse(null))
                    || !"kotlin-maven-plugin".equals(t.getChildValue("artifactId").orElse(null))) {
                    return t;
                }

                Optional<Xml.Tag> configurationTag = t.getChild("configuration");
                if (configurationTag.isEmpty()) {
                    return t;
                }

                Optional<Xml.Tag> pluginOptionsTag = configurationTag.get().getChild("pluginOptions");
                if (pluginOptionsTag.isEmpty()) {
                    return t;
                }

                for (Xml.Tag pluginOptionTag : pluginOptionsTag.get().getChildren()) {
                    t = adjustAllOpenDirectives(ctx, t, pluginOptionTag);
                }

                return t;
            }

            private Xml.Tag adjustAllOpenDirectives(ExecutionContext ctx, Xml.Tag pluginTag, Xml.Tag pluginOptionTag) {
                if (pluginOptionTag.getValue().isEmpty()) {
                    return pluginTag;
                }

                if (!pluginOptionTag.getValue().get().startsWith(ALL_OPEN_ANNOTATION)) {
                    return pluginTag;
                }

                for (String javaxPackage : JAVAX_PACKAGES) {
                    if (!pluginOptionTag.getValue().get().startsWith(ALL_OPEN_ANNOTATION + javaxPackage)) {
                        continue;
                    }

                    pluginTag = (Xml.Tag) new ChangeTagValueVisitor<>(pluginOptionTag,
                            pluginOptionTag.getValue().get().replace(ALL_OPEN_ANNOTATION + "javax.", ALL_OPEN_ANNOTATION + "jakarta."))
                            .visitNonNull(pluginTag, ctx);
                }

                return pluginTag;
            }
        };
    }
}
