package io.quarkus.updates.camel;

import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Parent of Camel xml visitors,  catches any exception, logs it and then continues.
 */
public abstract class AbstractCamelQuarkusXmlVisitor extends XmlIsoVisitor<ExecutionContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCamelQuarkusXmlVisitor.class);

    @Override
    public final Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        return executeVisitWithCatch(() -> doVisitTag(tag, executionContext), tag, executionContext);
    }


    //-------------------------------- internal methods used by children---------------------------------


    public Xml.Tag doVisitTag(Xml.Tag tag, ExecutionContext executionContext) {
        return super.visitTag(tag, executionContext);
    }

    // If the migration fails - do not fail whole migration process, only this one recipe
    protected <T extends Xml> T executeVisitWithCatch(Supplier<T> visitMethod, T origValue, ExecutionContext context) {
        try {
            return visitMethod.get();
        } catch (Exception e) {
            LOGGER.warn(String.format("Internal error detected in %s, recipe is skipped.", getClass().getName()), e);
            return origValue;
        }
    }

}
