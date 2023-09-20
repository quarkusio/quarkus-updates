package io.quarkus.updates.camel30.yaml;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.format.IndentsVisitor;
import org.openrewrite.yaml.style.IndentsStyle;
import org.openrewrite.yaml.tree.Yaml;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixes following yaml change.
 *
 * The backwards compatible mode Camel 3.14 or older, which allowed to have steps as child to route has been removed.
 *
 * The old syntax:
 *
 * <pre>
 * - route:
 *     from:
 *       uri: "direct:info"
 *     steps:
 *     - log: "message"
 * </pre>
 * should be changed to:
 * <pre>
 * - route:
 *     from:
 *       uri: "direct:info"
 *       steps:
 *       - log: "message"
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class CamelQuarkusYamlStepsInFromRecipe extends Recipe {

    private  static String[] PATHS_TO_PRE_CHECK = new String[] {"route.from"};
    private static JsonPathMatcher MATCHER_WITHOUT_ROUTE = new JsonPathMatcher("$.steps");
    private static JsonPathMatcher MATCHER_WITH_ROUTE = new JsonPathMatcher("$.route.steps");


    @Override
    public String getDisplayName() {
        return "Camel Yaml steps not allowed as route child";
    }

    @Override
    public String getDescription() {
        return "The YAML DSL backwards compatible mode in Camel 3.14 or older, which allowed 'steps' to be defined as a child of 'route' has been removed.";
    }


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

            return new YamlIsoVisitor<>() {

                //both variables has to be set to null, to mark the migration done
                Yaml.Mapping from = null;
                Yaml.Mapping.Entry steps = null;

                @Override
                public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext context) {
                    Yaml.Mapping.Entry e = super.visitMappingEntry(entry, context);

                    if(steps == null && (MATCHER_WITH_ROUTE.matches(getCursor()) || MATCHER_WITHOUT_ROUTE.matches(getCursor()))) {
                        steps = e;
                        if(from != null) {
                            moveSteps();
                        }
                        return null;
                    }
                    return e;

                }

                @Override
                public Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext context) {
                    Yaml.Mapping m =  super.visitMapping(mapping, context);

                    String prop = YamlRecipesUtil.getProperty(getCursor());
                    if(("route.from".equals(prop) || "from".equals(prop)) && from == null) {
                        from = m;
                        if(steps != null) {
                            moveSteps();
                        }
                    }

                    return m;
                }

                private void moveSteps() {
                    doAfterVisit(new YamlIsoVisitor<ExecutionContext>()  {

                        @Override
                        public Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext c) {
                            Yaml.Mapping m = super.visitMapping(mapping, c);

                            if(m == from) {
                                List<Yaml.Mapping.Entry> entries = new ArrayList<>(m.getEntries());
                                entries.add(steps.copyPaste().withPrefix("\n"));
                                m = m.withEntries(entries);
                            }

                            return m;
                        }});

                    //TODO might probably change indent in original file, may this happen?
                    doAfterVisit(new IndentsVisitor(new IndentsStyle(2), null));
                }
            };
        }

}

