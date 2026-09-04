package io.quarkus.updates.quarkiverse.cxf;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.tree.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Advisory only: adds the configured warning comment at the top of every properties file that contains
 * a key matching the configured pattern, and changes nothing else. A comment is inert at runtime, so
 * the recipe cannot break a configuration; the comment text itself doubles as the marker that keeps
 * reruns from stacking comments. The affected keys and the warning wording live in the declarative
 * recipe files, one per quarkus-cxf version.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class CommentDeprecatedProperties extends Recipe {

    @Option(displayName = "Key pattern",
            description = "A regular expression; a properties file containing a key that fully matches it gets the comment.",
            example = "(%[^.]+\\.)?quarkus\\.cxf\\.client\\.[^.]+\\.proxy-server")
    String keyPattern;

    @Option(displayName = "Comment",
            description = "The warning text added as a single comment line at the top of every affected file, without the leading `#`.",
            example = "Deprecation warning: ...")
    String comment;

    @Override
    public String getDisplayName() {
        return "Comment on deprecated properties";
    }

    @Override
    public String getDescription() {
        return "Adds the configured warning comment at the top of every properties file that contains a key matching " +
                "the configured pattern. The configuration itself is never changed and a file already carrying the " +
                "comment is left alone.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        Pattern pattern = Pattern.compile(keyPattern);
        return new PropertiesIsoVisitor<ExecutionContext>() {
            @Override
            public Properties.File visitFile(Properties.File file, ExecutionContext ctx) {
                Properties.File f = super.visitFile(file, ctx);
                boolean affected = f.getContent().stream().anyMatch(content -> content instanceof Properties.Entry
                        && pattern.matcher(((Properties.Entry) content).getKey()).matches());
                boolean alreadyMarked = f.getContent().stream().anyMatch(content -> content instanceof Properties.Comment
                        && ((Properties.Comment) content).getMessage().contains(comment));
                if (!affected || alreadyMarked) {
                    return f;
                }
                List<Properties.Content> newContent = new ArrayList<>(f.getContent());
                newContent.set(0, (Properties.Content) newContent.get(0)
                        .withPrefix("\n" + newContent.get(0).getPrefix()));
                newContent.add(0, new Properties.Comment(Tree.randomId(), "", Markers.EMPTY,
                        Properties.Comment.Delimiter.HASH_TAG, " " + comment));
                return f.withContent(newContent);
            }
        };
    }
}
