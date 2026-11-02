package io.quarkus.updates.core.quarkus40;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

/**
 * Unlike the tests in recipes-tests, the test classpath of this module does not contain jakarta.annotation-api,
 * which mirrors what happens when the recipe is executed by the rewrite Maven plugin
 * (see https://github.com/quarkusio/quarkus-updates/issues/572).
 */
class AddPriorityToReserveAnnotationTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddPriorityToReserveAnnotation())
                .parser(JavaParser.fromJavaVersion().dependsOn(
                        "package jakarta.enterprise.inject;\n"
                                + "public @interface Reserve {}\n",
                        "package jakarta.annotation;\n"
                                + "public @interface Priority { int value(); }\n",
                        "package jakarta.enterprise.context;\n"
                                + "public @interface ApplicationScoped {}\n",
                        "package jakarta.enterprise.inject;\n"
                                + "public @interface Produces {}\n"));
    }

    @Test
    void existingPriorityIsKept() {
        rewriteRun(java(
                "package org.acme;\n"
                        + "\n"
                        + "import jakarta.annotation.Priority;\n"
                        + "import jakarta.enterprise.inject.Reserve;\n"
                        + "\n"
                        + "@Reserve\n"
                        + "@Priority(10)\n"
                        + "public class MyService {\n"
                        + "}\n"));
    }

    @Test
    void priorityAndImportAddedOnProducerMethod() {
        rewriteRun(java(
                "package org.acme;\n"
                        + "\n"
                        + "import jakarta.enterprise.context.ApplicationScoped;\n"
                        + "import jakarta.enterprise.inject.Produces;\n"
                        + "import jakarta.enterprise.inject.Reserve;\n"
                        + "\n"
                        + "@ApplicationScoped\n"
                        + "public class GreetingProducer {\n"
                        + "\n"
                        + "    @Produces\n"
                        + "    @Reserve\n"
                        + "    @ApplicationScoped\n"
                        + "    String plainGreeter() {\n"
                        + "        return \"Hello\";\n"
                        + "    }\n"
                        + "}\n",
                "package org.acme;\n"
                        + "\n"
                        + "import jakarta.annotation.Priority;\n"
                        + "import jakarta.enterprise.context.ApplicationScoped;\n"
                        + "import jakarta.enterprise.inject.Produces;\n"
                        + "import jakarta.enterprise.inject.Reserve;\n"
                        + "\n"
                        + "@ApplicationScoped\n"
                        + "public class GreetingProducer {\n"
                        + "\n"
                        + "    @Produces\n"
                        + "    @Reserve\n"
                        + "    @ApplicationScoped\n"
                        + "    @Priority(0)\n"
                        + "    String plainGreeter() {\n"
                        + "        return \"Hello\";\n"
                        + "    }\n"
                        + "}\n"));
    }
}
