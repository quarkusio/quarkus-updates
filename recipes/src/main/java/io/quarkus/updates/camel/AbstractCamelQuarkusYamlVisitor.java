package io.quarkus.updates.camel;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Parent of Camel yaml visitors,  catches any exception, logs it and then continues.
 */
public abstract class AbstractCamelQuarkusYamlVisitor extends YamlIsoVisitor<ExecutionContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCamelQuarkusYamlVisitor.class);

    /**
     * Method is called before start of visiting a new document.
     * Implementations might need to clear all local state from previous document.
     */
    protected abstract void clearLocalCache();


    @Override
    public final Yaml.Document visitDocument(Yaml.Document document, ExecutionContext o) {
        clearLocalCache();
        Yaml.Document d =  super.visitDocument(document, o);
        return d;
    }

    @Override
    public final Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext executionContext) {
        return executeVisitWithCatch(() -> doVisitMappingEntry(entry, executionContext), entry, executionContext);
    }

    @Override
    public final Yaml.Sequence visitSequence(Yaml.Sequence sequence, ExecutionContext executionContext) {
        return executeVisitWithCatch(() -> doVisitSequence(sequence, executionContext), sequence, executionContext);
    }

    @Override
    public final Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext executionContext) {
        return executeVisitWithCatch(() ->
                doVisitMapping(mapping, executionContext)
                , mapping, executionContext);
    }

    //-------------------------------- internal methods used by children---------------------------------


    public Yaml.Mapping.Entry doVisitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext executionContext) {
        return super.visitMappingEntry(entry, executionContext);
    }

    public Yaml.Sequence doVisitSequence(Yaml.Sequence sequence, ExecutionContext executionContext) {
        return super.visitSequence(sequence, executionContext);
    }

    public Yaml.Mapping doVisitMapping(Yaml.Mapping mapping, ExecutionContext executionContext) {
        return super.visitMapping(mapping, executionContext);
    }

    // If the migration fails - do not fail whole migration process, only this one recipe
    protected <T extends Yaml> T executeVisitWithCatch(Supplier<T> visitMethod, T origValue, ExecutionContext context) {
        try {
            return visitMethod.get();
        } catch (Exception e) {
            LOGGER.warn(String.format("Internal error detected in %s, recipe is skipped.", getClass().getName()), e);
            return origValue;
        }
    }

}
