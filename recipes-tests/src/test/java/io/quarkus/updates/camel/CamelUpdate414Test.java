package io.quarkus.updates.camel;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelUpdate414Test extends org.apache.camel.upgrade.CamelUpdate414Test {

    @Override
    public void defaults(RecipeSpec spec) {
        //let the parser be initialized in the camel parent
        super.defaults(spec);
        //recipe has to be loaded differently
        CamelQuarkusTestUtil.recipe3_26(spec)
                .typeValidationOptions(TypeValidation.none());
    }
}
