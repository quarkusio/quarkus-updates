package io.quarkus.updates.camel;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;

public class CamelUpdate417Test extends org.apache.camel.upgrade.CamelUpdate417Test {

    @Override
    public void defaults(RecipeSpec spec) {
        //let the parser be initialized in the camel parent
        super.defaults(spec);
        //recipe has to be loaded differently
        CamelQuarkusTestUtil.recipe3_31(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testJunitArtifactIdChange() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.quarkus.test</groupId>
                           <version>1.0.0</version>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel.quarkus</groupId>
                                   <artifactId>camel-quarkus-junit5</artifactId>
                                   <version>3.31.0</version>
                               </dependency>
                           </dependencies>
                        </project>
                        """,
                """
                        <project>
                           <modelVersion>4.0.0</modelVersion>

                           <artifactId>test</artifactId>
                           <groupId>org.apache.camel.quarkus.test</groupId>
                           <version>1.0.0</version>

                           <dependencies>
                               <dependency>
                                   <groupId>org.apache.camel.quarkus</groupId>
                                   <artifactId>camel-quarkus-junit</artifactId>
                                   <version>3.31.0</version>
                               </dependency>
                           </dependencies>
                        </project>
                        """));
    }

    /**
     * <a href="https://github.com/apache/camel-quarkus/blob/main/docs/modules/ROOT/pages/migration-guide/3.31.0.adoc">camel-quarkus-junit5</a>
     */
    @Test
    void junitPackageRenameTest() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.test.junit5.CamelTestSupport;
      
                  public abstract class JunitTest extends CamelTestSupport {
                  }
                  """,
                """
                  import org.apache.camel.test.junit6.CamelTestSupport;
      
                  public abstract class JunitTest extends CamelTestSupport {
                  }
                  """));
    }

}
