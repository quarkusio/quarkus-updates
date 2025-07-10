package io.quarkus.updates.camel;

import org.junit.jupiter.api.Disabled;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelUpdate46Test extends org.apache.camel.upgrade.CamelUpdate46Test {

    @Override
    public void defaults(RecipeSpec spec) {
        //let the parser be initialized in the camel parent
        super.defaults(spec);
        //recipe has to be loaded differently
        CamelQuarkusTestUtil.recipe3_15(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    //following tests have to be disabled, the migration they cover is happening only between Camel 4.5-4.6
    // module which introduced the code before migration does not exist in Camel 4.4 (which is used by camel-quarkus 3.8)
    @Disabled
    @Override
    public void langchainEmbeddings() {
    }

    @Disabled
    @Override
    public void langchainEmbeddings2() {
    }

    @Disabled
    @Override
    public void langchainChat() {
    }

    @Disabled
    @Override
    public void langchainChat2() {

    }

    //followig test is covered by CamelUpdate45Test.testSearch
    @Disabled
    @Override
    public void search() {
        super.search();
    }
}
