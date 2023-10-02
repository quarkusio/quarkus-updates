package io.quarkus.updates.camel30;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CamelJavaTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe(spec, "org.openrewrite.java.migrate.UpgradeJavaVersion")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testJava17Update() {
        rewriteRun(
                org.openrewrite.maven.Assertions.pomXml(
                        """
                                <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">
                                    <modelVersion>4.0.0</modelVersion>
                                    <groupId>org.apache.camel.quarkus</groupId>
                                    <version>2.13.4-SNAPSHOT</version>
                                    <artifactId>camel-quarkus-migration-test-microprofile</artifactId>
                                    <properties>
                                        <maven.compiler.source>11</maven.compiler.source>
                                        <maven.compiler.target>11</maven.compiler.target>
                                    </properties>
                                   <dependencyManagement>
                                       <dependencies>
                                           <dependency>
                                               <groupId>io.quarkus.platform</groupId>
                                               <artifactId>quarkus-camel-bom</artifactId>
                                               <version>2.13.3.Final</version>
                                               <type>pom</type>
                                               <scope>import</scope>
                                           </dependency>
                                       </dependencies>
                                   </dependencyManagement>
                                    <dependencies>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId>camel-quarkus-bean</artifactId>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        , """
                                <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">
                                    <modelVersion>4.0.0</modelVersion>
                                    <groupId>org.apache.camel.quarkus</groupId>
                                    <version>2.13.4-SNAPSHOT</version>
                                    <artifactId>camel-quarkus-migration-test-microprofile</artifactId>
                                    <properties>
                                        <maven.compiler.source>17</maven.compiler.source>
                                        <maven.compiler.target>17</maven.compiler.target>
                                    </properties>
                                   <dependencyManagement>
                                       <dependencies>
                                           <dependency>
                                               <groupId>io.quarkus.platform</groupId>
                                               <artifactId>quarkus-camel-bom</artifactId>
                                               <version>2.13.3.Final</version>
                                               <type>pom</type>
                                               <scope>import</scope>
                                           </dependency>
                                       </dependencies>
                                   </dependencyManagement>
                                    <dependencies>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId>camel-quarkus-bean</artifactId>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """)
        );
    }

}
