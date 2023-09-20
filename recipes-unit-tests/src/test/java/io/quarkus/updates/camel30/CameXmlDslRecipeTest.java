/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.updates.camel30;

import static org.openrewrite.xml.Assertions.xml;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CameXmlDslRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testDescription() {
        rewriteRun(xml("""
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="myRoute">
                        <description>Something that this route do</description>
                        <from uri="kafka:cheese"/>
                        <setBody>
                           <constant>Hello Camel K!</constant>
                        </setBody>
                       <to uri="log:info"/>
                    </route>
                    <route id="myRoute2">
                        <description>Something that this route2 do</description>
                        <from uri="kafka:cheese"/>
                        <setBody>
                           <constant>Hello Camel K!</constant>
                        </setBody>
                       <to uri="log:info"/>
                    </route>
                </routes>
                                                """, """
                    <routes xmlns="http://camel.apache.org/schema/spring">
                        <route id="myRoute" description="Something that this route do">
                            <from uri="kafka:cheese"/>
                            <setBody>
                               <constant>Hello Camel K!</constant>
                            </setBody>
                           <to uri="log:info"/>
                        </route>
                        <route id="myRoute2" description="Something that this route2 do">
                            <from uri="kafka:cheese"/>
                            <setBody>
                               <constant>Hello Camel K!</constant>
                            </setBody>
                           <to uri="log:info"/>
                        </route>
                    </routes>
                """));
    }
}
