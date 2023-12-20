package io.quarkus.updates.core.quarkus37;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class UpgradeMavenCompilerJavaVersion extends Recipe {

    @Option(displayName = "key", description = "The name of the Maven Compiler Plugin property to update.", example = "maven.compiler.release")
    String key;

    @Option(displayName = "newVersion", description = "The new Java version to target at the mininum.", example = "17")
    String newVersion;

    @Override
    public String getDisplayName() {
        return "Upgrade Maven Compiler Java version";
    }

    @Override
    public String getDescription() {
        return "Upgrade the Maven Compiler Java version if needed.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenIsoVisitor<ExecutionContext>() {
            final String propertyName = key.replace("${", "").replace("}", "");

            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
                if (isPropertyTag() && propertyName.equals(tag.getName())
                        && isUpdateNeeded(tag.getValue().orElse(null), newVersion)) {
                    doAfterVisit(new ChangeTagValueVisitor<>(tag, newVersion));
                }
                return super.visitTag(tag, ctx);
            }
        };
    }

    private static boolean isUpdateNeeded(String oldVersionString, String newVersionString) {
        if (oldVersionString == null) {
            return true;
        }

        Integer oldVersion = Integer.valueOf(oldVersionString);
        Integer newVersion = Integer.valueOf(newVersionString);

        return newVersion.compareTo(oldVersion) > 0;
    }
}
