package io.quarkus.updates.core.quarkus310;

import static org.openrewrite.xml.AddToTagVisitor.addToTag;

import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Related to https://github.com/quarkusio/quarkus/pull/39295
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class AdjustPackageProperty extends Recipe {

    private static final String PROPERTIES = "properties";
    private static final String QUARKUS_PACKAGE_TYPE = "quarkus.package.type";
    private static final String QUARKUS_PACKAGE_JAR_TYPE = "quarkus.package.jar.type";
    private static final String QUARKUS_PACKAGE_JAR_ENABLED = "quarkus.package.jar.enabled";
    private static final String QUARKUS_NATIVE_ENABLED = "quarkus.native.enabled";
    private static final String QUARKUS_NATIVE_SOURCES_ONLY = "quarkus.native.sources-only";
    private static final String JAR = "jar";
    private static final String UBER_JAR = "uber-jar";
    private static final String FAST_JAR = "fast-jar";
    private static final String MUTABLE_JAR = "mutable-jar";
    private static final String NATIVE = "native";
    private static final String NATIVE_SOURCES = "native-sources";

    @Override
    public String getDisplayName() {
        return "Adjust the package properties";
    }

    @Override
    public String getDescription() {
        return "Adjust the package properties";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag propertiesTag = (Xml.Tag) super.visitTag(tag, ctx);

                if (PROPERTIES.equals(propertiesTag.getName())) {
                    Optional<Xml.Tag> packageType = propertiesTag.getChild(QUARKUS_PACKAGE_TYPE);
                    if (packageType.isPresent()) {
                        Optional<String> packageTypeValue = packageType.get().getValue();
                        if (packageTypeValue.isPresent()) {
                            switch (packageTypeValue.get()) {
                                case JAR:
                                case UBER_JAR:
                                case FAST_JAR:
                                case MUTABLE_JAR:
                                    propertiesTag = addToTag(propertiesTag, Xml.Tag.build("<" + QUARKUS_PACKAGE_JAR_TYPE + ">" + packageTypeValue.get() + "</" + QUARKUS_PACKAGE_JAR_TYPE + ">"), getCursor().getParentOrThrow());
                                    doAfterVisit(new RemoveContentVisitor<>(packageType.get(), false));
                                    break;
                                case NATIVE:
                                    propertiesTag = addToTag(propertiesTag, Xml.Tag.build("<"+ QUARKUS_NATIVE_ENABLED +">true</" + QUARKUS_NATIVE_ENABLED + ">"), getCursor().getParentOrThrow());
                                    propertiesTag = addToTag(propertiesTag, Xml.Tag.build("<"+ QUARKUS_PACKAGE_JAR_ENABLED +">false</" + QUARKUS_PACKAGE_JAR_ENABLED + ">"), getCursor().getParentOrThrow());
                                    doAfterVisit(new RemoveContentVisitor<>(packageType.get(), false));
                                    break;
                                case NATIVE_SOURCES:
                                    propertiesTag = addToTag(propertiesTag, Xml.Tag.build("<"+ QUARKUS_NATIVE_ENABLED +">true</" + QUARKUS_NATIVE_ENABLED + ">"), getCursor().getParentOrThrow());
                                    propertiesTag = addToTag(propertiesTag, Xml.Tag.build("<"+ QUARKUS_NATIVE_SOURCES_ONLY +">true</" + QUARKUS_NATIVE_SOURCES_ONLY + ">"), getCursor().getParentOrThrow());
                                    propertiesTag = addToTag(propertiesTag, Xml.Tag.build("<"+ QUARKUS_PACKAGE_JAR_ENABLED +">false</" + QUARKUS_PACKAGE_JAR_ENABLED + ">"), getCursor().getParentOrThrow());
                                    doAfterVisit(new RemoveContentVisitor<>(packageType.get(), false));
                                    break;
                                default:
                                    // do nothing for legacy jars
                                    break;
                            }
                        }
                    }
                }

                return propertiesTag;
            }
        };
    }
}
