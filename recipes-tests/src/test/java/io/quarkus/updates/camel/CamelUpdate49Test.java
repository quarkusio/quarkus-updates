package io.quarkus.updates.camel;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelUpdate49Test extends org.apache.camel.upgrade.CamelUpdate49Test {

    @Override
    public void defaults(RecipeSpec spec) {
        //let the parser be initialized in the camel parent
        super.defaults(spec);
        //recipe has to be loaded differently
        CamelQuarkusTestUtil.recipe3_17(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * Quarkus-updates recipes somehow groups imports in comparison to camel-upgrade-recipes.
     * (The original test method contains a lot of imports which are grouped in the result)
     * Therefore, I override this test method to avoid grouping behavior.
     * According to the doc, it should b possible to declar import style, see https://docs.openrewrite.org/reference/yaml-format-reference#style-example
     *
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_9.html#_camel_debezium">camel-debezium</a>
     */
    @Test
    @Override
    public void testDebezium() {
        //language=java
        rewriteRun(java(
                """
                        import org.apache.camel.component.debezium.configuration.Db2ConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.configuration.MongodbConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumMongodbComponentConfigurer;
                        import org.apache.camel.component.debezium.DebeziumMongodbComponent;
                        import org.apache.camel.component.debezium.configuration.MySqlConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumMySqlComponent;
                        import org.apache.camel.component.debezium.configuration.OracleConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumOracleComponent;
                        import org.apache.camel.component.debezium.configuration.PostgresConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumPostgresComponent;
                        import org.apache.camel.component.debezium.configuration.SqlserverConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.DebeziumSqlserverComponent;
                        
                        public class DebeziumTest {
    
                            public void method() {
                                //db2
                                Db2ConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumDb2Component component = null;
                                //mongodb
                                MongodbConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMongodbComponent component = null;
                                //mysql
                                MySqlConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMySqlComponent component = null;
                                //oracle
                                OracleConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumOracleComponent component = null;
                                //postgres
                                PostgresConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumPostgresComponent component = null;
                                //sqlserver
                                SqlserverConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumSqlserverComponent component = null;
                            }
                        }
                        """,
                """
                        import org.apache.camel.component.debezium.configuration.MongodbConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.configuration.SqlserverConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.db2.configuration.Db2ConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.mongodb.DebeziumMongodbComponent;
                        import org.apache.camel.component.debezium.mysql.DebeziumMySqlComponent;
                        import org.apache.camel.component.debezium.mysql.configuration.MySqlConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.oracle.DebeziumOracleComponent;
                        import org.apache.camel.component.debezium.oracle.configuration.OracleConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.postgres.DebeziumPostgresComponent;
                        import org.apache.camel.component.debezium.postgres.configuration.PostgresConnectorEmbeddedDebeziumConfiguration;
                        import org.apache.camel.component.debezium.sqlserver.DebeziumSqlserverComponent;
                         
                        public class DebeziumTest {
                        
                            public void method() {
                                //db2
                                Db2ConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumDb2Component component = null;
                                //mongodb
                                MongodbConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMongodbComponent component = null;
                                //mysql
                                MySqlConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumMySqlComponent component = null;
                                //oracle
                                OracleConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumOracleComponent component = null;
                                //postgres
                                PostgresConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumPostgresComponent component = null;
                                //sqlserver
                                SqlserverConnectorEmbeddedDebeziumConfiguration conf = null;
                                DebeziumSqlserverComponent component = null;
                            }
                        }
                      """));
    }
}
