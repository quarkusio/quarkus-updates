package io.quarkus.updates.core.quarkus30;

import org.openrewrite.*;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AdjustApplicationYamlWithJakarta extends Recipe {

    @Override
    public String getDisplayName() {
        return "Adjust application.yaml for jakarta.* packages";
    }

    @Override
    public String getDescription() {
        return "Adjust application.yaml for jakarta.* packages";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new FindSourceFiles("**/application*.y*ml"), new YamlIsoVisitor<>() {
            @Override
            public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
                Yaml.Mapping.Entry e = super.visitMappingEntry(entry, ctx);
                Yaml.Scalar updatedValue = updateValue(e.getValue());
                if (updatedValue != null) {
                    e = e.withValue(updatedValue);
                }
                return e;
            }
        });
    }

    @Nullable // returns null if value should not change
    private Yaml.Scalar updateValue(Yaml.Block value) {
        if (!(value instanceof Yaml.Scalar)) {
            return null;
        }

        Yaml.Scalar scalar = (Yaml.Scalar) value;

        String oldValue = scalar.getValue();

        if (StringUtils.isNullOrEmpty(oldValue)) {
            return null;
        }

        String newValue = oldValue;
        for (String jakartaPackage : AdjustApplicationPropertiesWithJakarta.JAKARTA_PACKAGES) {
            newValue = newValue.replace(jakartaPackage + ".", jakartaPackage.replace("javax.", "jakarta.") + ".");
        }

        if (oldValue.equals(newValue)) {
            return null;
        }

        return scalar.withValue(newValue);
    }
}