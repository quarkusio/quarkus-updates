package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public class CoreUpdate338Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        @Language("java")
        String panacheClass = """
                package io.quarkus.hibernate.panache;

                public class Panache {
                }
                """;
        @Language("java")
        String panacheQueryInterface = """
                package io.quarkus.hibernate.panache;

                public interface PanacheQuery<Entity> {
                }
                """;
        @Language("java")
        String withId = """
                package io.quarkus.hibernate.panache;

                public interface WithId<Id> {
                    public abstract class AutoLong implements WithId<Long> {
                    }
                }
                """;
        @Language("java")
        String panacheManagedBlockingEntity = """
                package io.quarkus.hibernate.panache.managed.blocking;

                public interface PanacheManagedBlockingEntity {
                }
                """;
        @Language("java")
        String panacheStatelessBlockingEntity = """
                package io.quarkus.hibernate.panache.stateless.blocking;

                public interface PanacheStatelessBlockingEntity {
                }
                """;
        @Language("java")
        String panacheManagedReactiveEntity = """
                package io.quarkus.hibernate.panache.managed.reactive;

                public interface PanacheManagedReactiveEntity {
                }
                """;
        @Language("java")
        String panacheStatelessReactiveEntity = """
                package io.quarkus.hibernate.panache.stateless.reactive;

                public interface PanacheStatelessReactiveEntity {
                }
                """;
        @Language("java")
        String panacheEntity = """
                package io.quarkus.hibernate.panache;

                import io.quarkus.hibernate.panache.managed.blocking.PanacheManagedBlockingEntity;
                import io.quarkus.hibernate.panache.managed.reactive.PanacheManagedReactiveEntity;
                import io.quarkus.hibernate.panache.stateless.blocking.PanacheStatelessBlockingEntity;
                import io.quarkus.hibernate.panache.stateless.reactive.PanacheStatelessReactiveEntity;

                public abstract class PanacheEntity extends WithId.AutoLong implements PanacheManagedBlockingEntity {
                    public interface Managed extends PanacheManagedBlockingEntity {}
                    public interface Stateless extends PanacheStatelessBlockingEntity {}
                    public interface Reactive extends PanacheManagedReactiveEntity {
                        public interface Stateless extends PanacheStatelessReactiveEntity {}
                    }
                }
                """;
        @Language("java")
        String panacheEntityBase = """
                package io.quarkus.hibernate.panache;

                import io.quarkus.hibernate.panache.managed.blocking.PanacheManagedBlockingEntity;

                public class PanacheEntityBase implements PanacheManagedBlockingEntity {
                }
                """;
        @Language("java")
        String panacheManagedBlockingRepositoryBase = """
                package io.quarkus.hibernate.panache.managed.blocking;

                public interface PanacheManagedBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheStatelessBlockingRepositoryBase = """
                package io.quarkus.hibernate.panache.stateless.blocking;

                public interface PanacheStatelessBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheManagedReactiveRepositoryBase = """
                package io.quarkus.hibernate.panache.managed.reactive;

                public interface PanacheManagedReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheStatelessReactiveRepositoryBase = """
                package io.quarkus.hibernate.panache.stateless.reactive;

                public interface PanacheStatelessReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String panacheRepository = """
                package io.quarkus.hibernate.panache;

                import io.quarkus.hibernate.panache.managed.blocking.PanacheManagedBlockingRepositoryBase;
                import io.quarkus.hibernate.panache.managed.reactive.PanacheManagedReactiveRepositoryBase;
                import io.quarkus.hibernate.panache.stateless.blocking.PanacheStatelessBlockingRepositoryBase;
                import io.quarkus.hibernate.panache.stateless.reactive.PanacheStatelessReactiveRepositoryBase;

                public interface PanacheRepository<Entity> extends PanacheManagedBlockingRepositoryBase<Entity, Long> {
                    public interface Managed<Entity, Id> extends PanacheManagedBlockingRepositoryBase<Entity, Id> {}
                    public interface Stateless<Entity, Id> extends PanacheStatelessBlockingRepositoryBase<Entity, Id> {}
                    public interface Reactive<Entity, Id> extends PanacheManagedReactiveRepositoryBase<Entity, Id> {
                        public interface Stateless<Entity, Id> extends PanacheStatelessReactiveRepositoryBase<Entity, Id> {}
                    }
                }
                """;
        @Language("java")
        String panacheEntityMarker = """
                package io.quarkus.hibernate.panache;

                public interface PanacheEntityMarker {
                }
                """;
        @Language("java")
        String panacheRepositoryQueries = """
                package io.quarkus.hibernate.panache;

                public interface PanacheRepositoryQueries {
                }
                """;
        @Language("java")
        String panacheRepositorySwitcher = """
                package io.quarkus.hibernate.panache;

                public interface PanacheRepositorySwitcher {
                }
                """;
        @Language("java")
        String panacheBlockingQuery = """
                package io.quarkus.hibernate.panache.blocking;

                public interface PanacheBlockingQuery<Entity> {
                }
                """;
        @Language("java")
        String newWithId = """
                package io.quarkus.data.hibernate;

                public interface WithId<Id> {
                    public abstract class AutoLong implements WithId<Long> {
                    }
                }
                """;
        @Language("java")
        String newManagedBlockingEntity = """
                package io.quarkus.data.hibernate.managed.blocking;

                public interface PanacheManagedBlockingEntity {
                }
                """;
        @Language("java")
        String newStatelessBlockingEntity = """
                package io.quarkus.data.hibernate.stateless.blocking;

                public interface PanacheStatelessBlockingEntity {
                }
                """;
        @Language("java")
        String newManagedReactiveEntity = """
                package io.quarkus.data.hibernate.managed.reactive;

                public interface PanacheManagedReactiveEntity {
                }
                """;
        @Language("java")
        String newStatelessReactiveEntity = """
                package io.quarkus.data.hibernate.stateless.reactive;

                public interface PanacheStatelessReactiveEntity {
                }
                """;
        @Language("java")
        String managedEntity = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.managed.blocking.PanacheManagedBlockingEntity;
                import io.quarkus.data.hibernate.managed.reactive.PanacheManagedReactiveEntity;

                public class ManagedEntity extends WithId.AutoLong implements PanacheManagedBlockingEntity {
                    public interface CustomId extends PanacheManagedBlockingEntity {}
                    public static class Reactive extends WithId.AutoLong implements PanacheManagedReactiveEntity {
                        public interface CustomId extends PanacheManagedReactiveEntity {}
                    }
                }
                """;
        @Language("java")
        String recordEntity = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.stateless.blocking.PanacheStatelessBlockingEntity;
                import io.quarkus.data.hibernate.stateless.reactive.PanacheStatelessReactiveEntity;

                public class RecordEntity extends WithId.AutoLong implements PanacheStatelessBlockingEntity {
                    public interface CustomId extends PanacheStatelessBlockingEntity {}
                    public static class Reactive extends WithId.AutoLong implements PanacheStatelessReactiveEntity {
                        public interface CustomId extends PanacheStatelessReactiveEntity {}
                    }
                }
                """;
        @Language("java")
        String newManagedBlockingRepositoryBase = """
                package io.quarkus.data.hibernate.managed.blocking;

                public interface PanacheManagedBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String newStatelessBlockingRepositoryBase = """
                package io.quarkus.data.hibernate.stateless.blocking;

                public interface PanacheStatelessBlockingRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String newManagedReactiveRepositoryBase = """
                package io.quarkus.data.hibernate.managed.reactive;

                public interface PanacheManagedReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String newStatelessReactiveRepositoryBase = """
                package io.quarkus.data.hibernate.stateless.reactive;

                public interface PanacheStatelessReactiveRepositoryBase<Entity, Id> {
                }
                """;
        @Language("java")
        String managedRepository = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.managed.blocking.PanacheManagedBlockingRepositoryBase;
                import io.quarkus.data.hibernate.managed.reactive.PanacheManagedReactiveRepositoryBase;

                public interface ManagedRepository<Entity> extends PanacheManagedBlockingRepositoryBase<Entity, Long> {
                    interface CustomId<Entity, Id> extends PanacheManagedBlockingRepositoryBase<Entity, Id> {}
                    interface Reactive<Entity> extends PanacheManagedReactiveRepositoryBase<Entity, Long> {
                        interface CustomId<Entity, Id> extends PanacheManagedReactiveRepositoryBase<Entity, Id> {}
                    }
                }
                """;
        @Language("java")
        String recordRepository = """
                package io.quarkus.data.hibernate;

                import io.quarkus.data.hibernate.stateless.blocking.PanacheStatelessBlockingRepositoryBase;
                import io.quarkus.data.hibernate.stateless.reactive.PanacheStatelessReactiveRepositoryBase;

                public interface RecordRepository<Entity> extends PanacheStatelessBlockingRepositoryBase<Entity, Long> {
                    interface CustomId<Entity, Id> extends PanacheStatelessBlockingRepositoryBase<Entity, Id> {}
                    interface Reactive<Entity> extends PanacheStatelessReactiveRepositoryBase<Entity, Long> {
                        interface CustomId<Entity, Id> extends PanacheStatelessReactiveRepositoryBase<Entity, Id> {}
                    }
                }
                """;
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.38.alpha1.yaml"))
                .parser(JavaParser.fromJavaVersion()
                        .dependsOn(panacheClass, panacheQueryInterface, withId,
                                panacheManagedBlockingEntity, panacheStatelessBlockingEntity,
                                panacheManagedReactiveEntity, panacheStatelessReactiveEntity,
                                panacheEntity, panacheEntityBase,
                                panacheManagedBlockingRepositoryBase, panacheStatelessBlockingRepositoryBase,
                                panacheManagedReactiveRepositoryBase, panacheStatelessReactiveRepositoryBase,
                                panacheRepository,
                                panacheEntityMarker, panacheRepositoryQueries, panacheRepositorySwitcher,
                                panacheBlockingQuery,
                                newWithId,
                                newManagedBlockingEntity, newStatelessBlockingEntity,
                                newManagedReactiveEntity, newStatelessReactiveEntity,
                                managedEntity, recordEntity,
                                newManagedBlockingRepositoryBase, newStatelessBlockingRepositoryBase,
                                newManagedReactiveRepositoryBase, newStatelessReactiveRepositoryBase,
                                managedRepository, recordRepository)
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testPanacheToQuarkusDataTypeRename() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.Panache;

                    class MyService {
                        void doSomething() {
                            Panache.withTransaction(() -> null);
                        }
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.QuarkusData;

                    class MyService {
                        void doSomething() {
                            QuarkusData.withTransaction(() -> null);
                        }
                    }
                """));
    }

    @Test
    void testPanacheQueryToDataQuery() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheQuery;

                    class MyService {
                        PanacheQuery<Object> query;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.DataQuery;

                    class MyService {
                        DataQuery<Object> query;
                    }
                """));
    }

    @Test
    void testPanacheEntityToManagedEntity() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity extends PanacheEntity {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity extends ManagedEntity {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityBaseToManagedEntityCustomId() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntityBase;

                    public class MyEntity extends PanacheEntityBase {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity extends ManagedEntity.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityManagedInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Managed {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity implements ManagedEntity.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityStatelessInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Stateless {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordEntity;

                    public class MyEntity implements RecordEntity.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityReactiveInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Reactive {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedEntity;

                    public class MyEntity implements ManagedEntity.Reactive.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheEntityReactiveStatelessInnerType() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntity;

                    public class MyEntity implements PanacheEntity.Reactive.Stateless {
                        public String name;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordEntity;

                    public class MyEntity implements RecordEntity.Reactive.CustomId {
                        public String name;
                    }
                """));
    }

    @Test
    void testPanacheRepositoryToManagedRepository() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository<Object> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedRepository;

                    public class MyRepository implements ManagedRepository<Object> {
                    }
                """));
    }

    @Test
    void testPanacheRepositoryStatelessToRecordRepository() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository.Stateless<Object, Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordRepository;

                    public class MyRepository implements RecordRepository.CustomId<Object, Long> {
                    }
                """));
    }

    @Test
    void testPanacheRepositoryReactiveToManagedRepositoryReactive() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository.Reactive<Object, Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.ManagedRepository;

                    public class MyRepository implements ManagedRepository.Reactive.CustomId<Object, Long> {
                    }
                """));
    }

    @Test
    void testPanacheRepositoryReactiveStatelessToRecordRepositoryReactive() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheRepository;

                    public class MyRepository implements PanacheRepository.Reactive.Stateless<Object, Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.RecordRepository;

                    public class MyRepository implements RecordRepository.Reactive.CustomId<Object, Long> {
                    }
                """));
    }

    @Test
    void testPackageRenameForRemainingTypes() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.blocking.PanacheBlockingQuery;

                    class MyService {
                        PanacheBlockingQuery<Object> query;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.blocking.PanacheBlockingQuery;

                    class MyService {
                        PanacheBlockingQuery<Object> query;
                    }
                """));
    }

    @Test
    void testPanacheEntityMarkerToEntitySwitcher() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.PanacheEntityMarker;

                    class MyService {
                        PanacheEntityMarker marker;
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.EntitySwitcher;

                    class MyService {
                        EntitySwitcher marker;
                    }
                """));
    }

    @Test
    void testWithIdPackageRename() {
        //language=java
        rewriteRun(java(
                """
                    package org.acme;

                    import io.quarkus.hibernate.panache.WithId;

                    class MyEntity implements WithId<Long> {
                    }
                """,
                """
                    package org.acme;

                    import io.quarkus.data.hibernate.WithId;

                    class MyEntity implements WithId<Long> {
                    }
                """));
    }

    @Test
    void testHibernateProcessorToQuarkusDataProcessor() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate.orm</groupId>
                                                    <artifactId>hibernate-processor</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>io.quarkus</groupId>
                                                    <artifactId>quarkus-data-processor</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """));
    }

    @Test
    void testOldJpaModelgenToQuarkusDataProcessor() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate</groupId>
                                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>org.acme</groupId>
                            <artifactId>my-app</artifactId>
                            <version>1.0-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.13.0</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>io.quarkus</groupId>
                                                    <artifactId>quarkus-data-processor</artifactId>
                                                </path>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """));
    }
}
