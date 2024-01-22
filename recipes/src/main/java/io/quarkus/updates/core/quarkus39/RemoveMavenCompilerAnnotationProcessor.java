package io.quarkus.updates.core.quarkus39;

import java.util.List;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.FilterTagChildrenVisitor;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class RemoveMavenCompilerAnnotationProcessor extends Recipe {

    @Option(displayName = "Annotation processor groupId", description = "The groupId of the annotation processor.", example = "org.hibernate.orm", required = true)
    String groupId;

    @Option(displayName = "Annotation processor artifactId", description = "The artifactId of the annotation processor.", example = "hibernate-jpamodelgen", required = true)
    String artifactId;

    @Option(displayName = "Annotation processor fully-qualified class", description = "The fully-qualified class of the annotation processor.", example = "com.example.MyProcessor", required = false)
    String processorClass;

    @Override
    public String getDisplayName() {
        return "Remove an annotation processor from the Maven Compiler plugin configuration";
    }

    @Override
    public String getDescription() {
        return "Remove an annotation processor from the Maven Compiler plugin configuration.";
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
                            List<Xml.Tag> annotationProcessorPaths = annotationProcessorPathsWrapper.get().getChildren();
                            String childName = annotationProcessorPaths.size() > 0 ? annotationProcessorPaths.get(0).getName()
                                    : "path";

                            mavenCompilerPluginTag = FilterTagChildrenVisitor.filterTagChildren(mavenCompilerPluginTag, annotationProcessorPathsWrapper.get(), 
                            			child ->
                            			!(child.getName().equals(childName)
                            			&& groupId.equals(child.getChildValue("groupId").orElse(null))
                                        && artifactId.equals(child.getChildValue("artifactId").orElse(null))));
                        }
                        if(processorClass != null) {
                        	Optional<Xml.Tag> annotationProcessorsWrapper = configuration.get()
                        			.getChild("annotationProcessors");
                        	if (annotationProcessorsWrapper.isPresent()) {
                        		List<Xml.Tag> annotationProcessors = annotationProcessorsWrapper.get().getChildren();
                        		String childName = annotationProcessors.size() > 0 ? annotationProcessors.get(0).getName()
                        				: "annotationProcessor";

                        		mavenCompilerPluginTag = FilterTagChildrenVisitor.filterTagChildren(mavenCompilerPluginTag, annotationProcessorsWrapper.get(), 
                        				child ->
                        		!(child.getName().equals(childName)
                        		&& processorClass.equals(child.getValue().orElse(null))));
                        	}
                        }
                    }
                }
                return mavenCompilerPluginTag;
            }
        };
    }
}
