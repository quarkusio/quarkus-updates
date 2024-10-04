package io.quarkus.updates.camel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelUpdate45Test extends org.apache.camel.upgrade.CamelUpdate45Test {

    @Override
    public void defaults(RecipeSpec spec) {
        //let the parser be initialized in the camel parent
        super.defaults(spec);
        //recipe has to be loaded differently
        CamelQuarkusTestUtil.recipe3_15(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    @Override
    public void testSearch() {
        //test has to be changed, because the result of camel 4.5 migration is a;so migrated in camel 4.6,
        // therefore camel-quarkus migration from 3.8 to 3.15 has to expect both changes
        rewriteRun(java(
                """
                            public class SearchTest {
                                 public void test() {
                             
                                     org.apache.camel.component.es.aggregation.BulkRequestAggregationStrategy elasticAggregationStrategy = null;
                                     org.apache.camel.component.opensearch.aggregation.BulkRequestAggregationStrategy openAggregationStrategy = null;
                                 }
                            }
                        """,
                """
                            public class SearchTest {
                                 public void test() {
                            
                                     org.apache.camel.component.es.aggregation.ElasticsearchBulkRequestAggregationStrategy elasticAggregationStrategy = null;
                                     org.apache.camel.component.opensearch.aggregation.OpensearchBulkRequestAggregationStrategy openAggregationStrategy = null;
                                 }
                            }
                        """));
    }
}
