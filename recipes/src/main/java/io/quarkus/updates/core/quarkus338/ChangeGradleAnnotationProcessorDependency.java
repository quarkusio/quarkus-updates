package io.quarkus.updates.core.quarkus338;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.Validated;
import org.openrewrite.gradle.AddDependencyVisitor;
import org.openrewrite.gradle.marker.GradleDependencyConfiguration;
import org.openrewrite.gradle.marker.GradleProject;
import org.openrewrite.gradle.trait.GradleMultiDependency;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.semver.DependencyMatcher;

import lombok.EqualsAndHashCode;
import lombok.Value;

import static java.util.Objects.requireNonNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class ChangeGradleAnnotationProcessorDependency extends Recipe {

    @Option(displayName = "Old groupId", description = "The old groupId to replace.", example = "org.hibernate.orm")
    String oldGroupId;

    @Option(displayName = "Old artifactId", description = "The old artifactId to replace.", example = "hibernate-processor")
    String oldArtifactId;

    @Option(displayName = "New groupId", description = "The new groupId to use.", example = "io.quarkus")
    String newGroupId;

    @Option(displayName = "New artifactId", description = "The new artifactId to use.", example = "quarkus-data-processor")
    String newArtifactId;

    @Override
    public String getDisplayName() {
        return "Change Gradle annotation processor dependency and remove version";
    }

    @Override
    public String getDescription() {
        return "Change the groupId and artifactId of a Gradle annotation processor dependency, remove any explicit version, "
                + "and add an enforcedPlatform for the Quarkus BOM if the old dependency had a version and no platform was present.";
    }

    @Override
    public Validated<Object> validate() {
        return super.validate().and(DependencyMatcher.build(oldGroupId + ":" + oldArtifactId));
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            final DependencyMatcher depMatcher = requireNonNull(DependencyMatcher.build(oldGroupId + ":" + oldArtifactId).getValue());
            final MethodMatcher enforcedPlatformMatcher = new MethodMatcher("* enforcedPlatform(..)");

            @SuppressWarnings("NotNullFieldNotInitialized")
            GradleProject gradleProject;
            boolean hasEnforcedPlatformForAP;
            boolean oldDepHadVersion;

            @Override
            public @Nullable J visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof JavaSourceFile) {
                    JavaSourceFile sf = (JavaSourceFile) tree;
                    Optional<GradleProject> maybeGp = sf.getMarkers().findFirst(GradleProject.class);
                    if (!maybeGp.isPresent()) {
                        return sf;
                    }
                    gradleProject = maybeGp.get();
                    hasEnforcedPlatformForAP = false;
                    oldDepHadVersion = false;

                    J result = super.visit(tree, ctx);

                    if (result != tree && result instanceof JavaSourceFile) {
                        JavaSourceFile updated = (JavaSourceFile) result;
                        updated = updated.withMarkers(updated.getMarkers().setByType(updateGradleModel(gradleProject)));

                        if (oldDepHadVersion && !hasEnforcedPlatformForAP) {
                            updated = (JavaSourceFile) new AddDependencyVisitor(
                                    "io.quarkus.platform", "quarkus-bom",
                                    "$quarkusPlatformVersion", null,
                                    "annotationProcessor",
                                    null, null, null, null,
                                    AddDependencyVisitor.DependencyModifier.ENFORCED_PLATFORM)
                                    .visitNonNull(updated, ctx);
                        }

                        return updated;
                    }
                    return sf;
                }
                return super.visit(tree, ctx);
            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

                if ("annotationProcessor".equals(m.getSimpleName())) {
                    for (Expression arg : m.getArguments()) {
                        if (arg instanceof J.MethodInvocation && enforcedPlatformMatcher.matches((J.MethodInvocation) arg, true)) {
                            hasEnforcedPlatformForAP = true;
                            break;
                        }
                    }
                }

                return GradleMultiDependency.matcher()
                        .groupId(oldGroupId)
                        .artifactId(oldArtifactId)
                        .get(getCursor())
                        .map(gmd -> gmd.map(gd -> {
                            String declaredVersion = gd.getDeclaredVersion();
                            if (declaredVersion != null && !declaredVersion.isEmpty()) {
                                oldDepHadVersion = true;
                            }
                            return gd
                                    .withDeclaredGroupId(newGroupId)
                                    .withDeclaredArtifactId(newArtifactId)
                                    .withDeclaredVersion(null)
                                    .getTree();
                        }))
                        .orElse(m);
            }

            private GradleProject updateGradleModel(GradleProject gp) {
                Map<String, GradleDependencyConfiguration> nameToConfiguration = gp.getNameToConfiguration();
                Map<String, GradleDependencyConfiguration> newNameToConfiguration = new HashMap<>(nameToConfiguration.size());
                boolean anyChanged = false;
                for (GradleDependencyConfiguration gdc : nameToConfiguration.values()) {
                    GradleDependencyConfiguration newGdc = gdc;
                    newGdc = newGdc.withRequested(ListUtils.map(gdc.getRequested(), requested -> {
                        if (depMatcher.matches(requested.getGroupId(), requested.getArtifactId())) {
                            return requested.withGav(requested.getGav()
                                    .withGroupId(newGroupId)
                                    .withArtifactId(newArtifactId)
                                    .withVersion(null));
                        }
                        return requested;
                    }));
                    newGdc = newGdc.withDirectResolved(ListUtils.map(gdc.getDirectResolvedShallow(), resolved -> {
                        if (depMatcher.matches(resolved.getGroupId(), resolved.getArtifactId())) {
                            return resolved.withGav(resolved.getGav()
                                    .withGroupId(newGroupId)
                                    .withArtifactId(newArtifactId));
                        }
                        return resolved;
                    }));
                    anyChanged |= newGdc != gdc;
                    newNameToConfiguration.put(newGdc.getName(), newGdc);
                }
                if (anyChanged) {
                    gp = gp.withNameToConfiguration(newNameToConfiguration);
                }
                return gp;
            }
        };
    }
}
