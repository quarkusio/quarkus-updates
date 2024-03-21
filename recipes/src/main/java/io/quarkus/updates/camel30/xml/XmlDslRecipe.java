package io.quarkus.updates.camel30.xml;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.Markers;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Xml;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4-migration-guide.html#_xml_dsl">Camel Migration guide</a>
 * </p>
 * The <description> to set a description on a route or node, has been changed from an element to an attribute.
 *
 * Before:
 * <pre>
 * &lt;route id=&quot;myRoute&quot;&gt;
 *   &lt;description&gt;Something that this route do&lt;/description&gt;
 *   &lt;from uri=&quot;kafka:cheese&quot;/&gt;
 *   ...
 * &lt;/route&gt;
 * </pre>
 * After:
 * <pre>
 * &lt;route id=&quot;myRoute&quot; description=&quot;Something that this route do&quot;&gt;
 *   &lt;from uri=&quot;kafka:cheese&quot;/&gt;
 *   ...
 * &lt;/route&gt;
 * </pre>
 */
public class XmlDslRecipe extends Recipe {

    private static final XPathMatcher ROUTE_DESCRIPTION_XPATH_MATCHER = new XPathMatcher("/routes/route/description");
    private static final XPathMatcher ROUTE_XPATH_MATCHER = new XPathMatcher("/routes/route");

    @Override
    public String getDisplayName() {
        return "Camel XMl DSL changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel XML DSL migration from version 3.20 or higher to 4.0.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>() {

            @Override
            public Xml.Tag visitTag(final Xml.Tag tag, final ExecutionContext ctx) {
                Xml.Tag t = super.visitTag(tag, ctx);


                if (ROUTE_XPATH_MATCHER.matches(getCursor())) {
                    String d = ctx.pollMessage("description");
                    if(d != null) {
                        return t.withAttributes(ListUtils.concat(t.getAttributes(), autoFormat(new Xml.Attribute(Tree.randomId(), "", Markers.EMPTY,
                                new Xml.Ident(Tree.randomId(), "", Markers.EMPTY, "description"),
                                "",
                                autoFormat(new Xml.Attribute.Value(Tree.randomId(), "", Markers.EMPTY,
                                        Xml.Attribute.Value.Quote.Double,
                                         d), ctx)), ctx)));
                    }
                }
                if (ROUTE_DESCRIPTION_XPATH_MATCHER.matches(getCursor())) {
                    //save description into context for parent
                    t.getValue().ifPresent(s -> ctx.putMessage("description", s));
                    //skip tag
                    return null;

                }

                return t;
            }
        };
    }
}
