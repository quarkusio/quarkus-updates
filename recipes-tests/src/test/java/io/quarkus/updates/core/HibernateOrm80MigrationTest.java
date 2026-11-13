package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;

import java.net.URI;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.config.Environment;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class HibernateOrm80MigrationTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        String resource = "/quarkus-updates/core/4.0.alpha1.yaml";
        spec.recipe(Environment.builder()
                .scanYamlResources()
                .load(new YamlResourceLoader(getClass().getResourceAsStream(resource),
                        URI.create("rewrite.yml"), new Properties()))
                .build()
                .activateRecipes("io.quarkus.updates.core.quarkus40.HibernateORM80"))
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(),
                                "hibernate-core-7.4.11.Final", "jakarta.persistence-api-3.2.0"));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#query-api">Selection queries and mutation statements</a>
     */
    @Test
    void migratesMutationNamedQueriesButPreservesSelectQueries() {
        rewriteRun(java(
                """
                package org.acme;

                import jakarta.persistence.Entity;
                import jakarta.persistence.Id;
                import jakarta.persistence.NamedQueries;
                import jakarta.persistence.NamedQuery;

                @NamedQueries({
                    @NamedQuery(name = "Book.findAll", query = "select b from Book b"),
                    @NamedQuery(name = "Book.deleteAll", query = "delete from Book")
                })
                @Entity
                class Book {
                    @Id
                    Long id;
                }
                """,
                """
                package org.acme;

                import jakarta.persistence.*;

                @NamedQueries({
                    @NamedQuery(name = "Book.findAll", query = "select b from Book b")
                })
                @NamedStatement(name = "Book.deleteAll", statement = "delete from Book")
                @Entity
                class Book {
                    @Id
                    Long id;
                }
                """));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#query-api">Native selection queries and mutation statements</a>
     */
    @Test
    void migratesMutationNamedNativeQueriesButPreservesSelectQueries() {
        rewriteRun(java(
                """
                package org.acme;

                import jakarta.persistence.NamedNativeQueries;
                import jakarta.persistence.NamedNativeQuery;

                @NamedNativeQueries({
                    @NamedNativeQuery(name = "Book.findAll", query = "select * from books"),
                    @NamedNativeQuery(name = "Book.deleteAll", query = "delete from books")
                })
                class Book {
                }
                """,
                """
                package org.acme;

                import jakarta.persistence.NamedNativeQueries;
                import jakarta.persistence.NamedNativeQuery;
                import jakarta.persistence.NamedNativeStatement;

                @NamedNativeQueries({
                    @NamedNativeQuery(name = "Book.findAll", query = "select * from books")
                })
                @NamedNativeStatement(name = "Book.deleteAll", statement = "delete from books")
                class Book {
                }
                """));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/javadocs/org/hibernate/query/Query.html#setQueryFlushMode(jakarta.persistence.QueryFlushMode)">Jakarta Persistence query flush mode</a>
     */
    @Test
    void migratesHibernateQueryFlushMode() {
        rewriteRun(java(
                """
                package org.acme;

                import org.hibernate.query.QueryFlushMode;

                class Queries {
                    QueryFlushMode mode = QueryFlushMode.NO_FLUSH;
                }
                """,
                """
                package org.acme;

                import jakarta.persistence.QueryFlushMode;

                class Queries {
                    QueryFlushMode mode = QueryFlushMode.NO_FLUSH;
                }
                """));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/javadocs/org/hibernate/jpa/HibernateHints.html#HINT_READ_ONLY">Replacement read-only query hint</a>
     */
    @Test
    void migratesRemovedQueryHintConstants() {
        rewriteRun(java(
                """
                package org.acme;

                import org.hibernate.jpa.QueryHints;

                class Queries {
                    String hint = QueryHints.HINT_READONLY;
                }
                """,
                """
                package org.acme;

                import org.hibernate.jpa.HibernateHints;

                class Queries {
                    String hint = HibernateHints.HINT_READ_ONLY;
                }
                """));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#FindMultipleOption">Relocated multi-ID loading options</a>
     */
    @Test
    void migratesMultiIdLoadingOptions() {
        rewriteRun(java(
                """
                package org.acme;

                import org.hibernate.BatchSize;
                import org.hibernate.OrderingMode;
                import org.hibernate.RemovalsMode;
                import org.hibernate.SessionCheckMode;

                class LoadingOptions {
                    OrderingMode ordering = OrderingMode.ORDERED;
                    SessionCheckMode checking = SessionCheckMode.ENABLED;
                    RemovalsMode removals = RemovalsMode.INCLUDE;
                    BatchSize batch = new BatchSize(20);
                }
                """,
                """
                package org.acme;

                import org.hibernate.FindMultipleOption.BatchSize;
                import org.hibernate.FindMultipleOption.OrderingMode;
                import org.hibernate.FindMultipleOption.RemovalsMode;
                import org.hibernate.FindMultipleOption.SessionCheckMode;

                class LoadingOptions {
                    OrderingMode ordering = OrderingMode.ORDERED;
                    SessionCheckMode checking = SessionCheckMode.ENABLED;
                    RemovalsMode removals = RemovalsMode.INCLUDE;
                    BatchSize batch = new BatchSize(20);
                }
                """));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/javadocs/org/hibernate/annotations/SQLInsert.html#verify()">Custom SQL result verification</a>
     */
    @Test
    void migratesSqlResultChecksButPreservesExplicitVerification() {
        rewriteRun(java(
                """
                package org.acme;

                import org.hibernate.annotations.ResultCheckStyle;
                import org.hibernate.annotations.SQLDelete;
                import org.hibernate.annotations.SQLDeleteAll;
                import org.hibernate.annotations.SQLInsert;
                import org.hibernate.annotations.SQLUpdate;
                import org.hibernate.jdbc.Expectation;

                @SQLInsert(sql = "insert into books (title, id) values (?, ?)", check = ResultCheckStyle.COUNT)
                @SQLUpdate(sql = "update books set title = ? where id = ?", check = ResultCheckStyle.NONE)
                @SQLDelete(sql = "{call delete_book(?)}", callable = true, check = ResultCheckStyle.PARAM)
                @SQLDeleteAll(sql = "delete from book_authors where book_id = ?", check = ResultCheckStyle.COUNT,
                        verify = Expectation.None.class)
                class Book {
                }
                """,
                """
                package org.acme;

                import org.hibernate.annotations.SQLDelete;
                import org.hibernate.annotations.SQLDeleteAll;
                import org.hibernate.annotations.SQLInsert;
                import org.hibernate.annotations.SQLUpdate;
                import org.hibernate.jdbc.Expectation;

                @SQLInsert(sql = "insert into books (title, id) values (?, ?)", verify = Expectation.RowCount.class)
                @SQLUpdate(sql = "update books set title = ? where id = ?", verify = Expectation.None.class)
                @SQLDelete(sql = "{call delete_book(?)}", callable = true, verify = Expectation.OutParameter.class)
                @SQLDeleteAll(sql = "delete from book_authors where book_id = ?",
                        verify = Expectation.None.class)
                class Book {
                }
                """));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#_managed_package_and_module_descriptors">Managed package and module descriptors</a>
     */
    @Test
    void migratesPackageDescriptorsInPersistenceXml() {
        rewriteRun(xml(
                """
                <persistence xmlns="https://jakarta.ee/xml/ns/persistence" version="3.2">
                    <persistence-unit name="books">
                        <class>org.acme.package-info</class>
                        <class>org.acme.Book</class>
                    </persistence-unit>
                </persistence>
                """,
                """
                <persistence xmlns="https://jakarta.ee/xml/ns/persistence" version="4.0">
                    <persistence-unit name="books">
                        <class>org.acme.Book</class>
                        <package-descriptor>org.acme</package-descriptor>
                    </persistence-unit>
                </persistence>
                """,
                source -> source.path("src/main/resources/META-INF/persistence.xml")));
    }

    /**
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#xsd-mapping-https">HTTPS mapping namespace</a>
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#xsd-comments">Table and column comment elements</a>
     */
    @Test
    void migratesHibernateMappingXml() {
        rewriteRun(xml(
                """
                <entity-mappings xmlns="http://www.hibernate.org/xsd/orm/mapping" version="7.0">
                    <entity class="org.acme.Book">
                        <table name="books" comment="Books"/>
                    </entity>
                </entity-mappings>
                """,
                """
                <entity-mappings xmlns="https://www.hibernate.org/xsd/orm/mapping" version="8.0">
                    <entity class="org.acme.Book">
                        <table name="books">
                            <comment>Books</comment>
                        </table>
                    </entity>
                </entity-mappings>
                """,
                source -> source.path("src/main/resources/books.xml")));
    }

    /**
     * JPA descriptor modernization is optional, unlike the Hibernate mapping changes.
     *
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#jpa4">Jakarta Persistence 4.0 support</a>
     * @see <a href="https://github.com/hibernate/hibernate-migration-recipes/blob/main/migrations/orm/orm80/index.adoc#recipe-content">Required versus optional recipe membership</a>
     */
    @Test
    void preservesOptionalJpaMappingModernization() {
        rewriteRun(xml(
                """
                <entity-mappings xmlns="https://jakarta.ee/xml/ns/persistence/orm" version="3.2"/>
                """,
                source -> source.path("src/main/resources/META-INF/orm.xml")));
    }

    /***
     * @see <a href="https://docs.hibernate.org/orm/8.0/javadocs/org/hibernate/Session.html">Hibernate's EntityManager implementation and inherited APIs</a>
     * @see <a href="https://github.com/hibernate/hibernate-migration-recipes/blob/main/migrations/orm/orm80/index.adoc#recipe-content">Required versus optional recipe membership</a>
     */
    @Test
    void preservesDeprecatedApisOutsideTheRequiredRecipe() {
        rewriteRun(java(
                """
                package org.acme;

                import jakarta.persistence.EntityManager;

                class Queries {
                    Object delegate(EntityManager entityManager) {
                        return entityManager.getDelegate();
                    }
                }
                """));
    }

    /**
     * Coordinate migration must leave Quarkus BOM-managed dependencies versionless.
     *
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#dependency-changes-relocation">Hibernate ORM artifact coordinates</a>
     * @see <a href="https://quarkus.io/guides/platform#quarkus-platform-bom">Quarkus dependency management</a>
     */
    @Test
    void preservesQuarkusBomManagedVersions() {
        rewriteRun(pomXml(
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.acme</groupId>
                    <artifactId>books</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>io.quarkus</groupId>
                                <artifactId>quarkus-bom</artifactId>
                                <version>4.0.0.CR1</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-hibernate-orm-panache</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.hibernate.orm</groupId>
                            <artifactId>hibernate-core</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.hibernate.orm</groupId>
                            <artifactId>hibernate-processor</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """));
    }

    /**
     * Relocation must preserve dependency versions supplied by the Quarkus platform.
     *
     * @see <a href="https://docs.hibernate.org/orm/8.0/migration-guide/#dependency-changes-relocation">Hibernate ORM artifact coordinates</a>
     * @see <a href="https://quarkus.io/guides/platform#quarkus-platform-bom">Quarkus platform dependency management</a>
     */
    @Test
    void preservesGradlePlatformManagedVersionsWhenRelocatingDependencies() {
        rewriteRun(buildGradle(
                """
                plugins {
                    id 'java'
                }

                dependencies {
                    implementation enforcedPlatform('io.quarkus:quarkus-bom:4.0.0.CR1')
                    implementation 'io.quarkus:quarkus-hibernate-orm-panache'
                    implementation 'org.hibernate:hibernate-core'
                    annotationProcessor 'org.hibernate.orm:hibernate-jpamodelgen'
                }
                """,
                """
                plugins {
                    id 'java'
                }

                dependencies {
                    implementation enforcedPlatform('io.quarkus:quarkus-bom:4.0.0.CR1')
                    implementation 'io.quarkus:quarkus-hibernate-orm-panache'
                    implementation 'org.hibernate.orm:hibernate-core'
                    annotationProcessor 'org.hibernate.orm:hibernate-processor'
                }
                """));
    }
}
