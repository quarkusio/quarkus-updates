package io.quarkus.updates.core;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import io.quarkus.updates.core.hibernate.HibernateOrm66;

public class CoreUpdate324Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(spec, Path.of("quarkus-updates", "core", "3.24.alpha1.yaml"), "3.24.0.CR1")
                .parser(JavaParser.fromJavaVersion()
                        .classpath("quarkus-test-common")
                        // Each new call of dependsOn overrides the previous one -- we need to concatenate.
                        // I wasn't able to make TypeTable work, so this is the workaround.
                        .dependsOn(HibernateOrm66.api())
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.all());
    }

    @Test
    void testJpaModelgenOldDependency() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>org.hibernate</groupId>
                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                    <version>6.4.3.Final</version>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenNewDependency() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>org.hibernate.orm</groupId>
                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                    <version>6.1.2.Final</version>
                                    <scope>provided</scope>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenNewDependencyManagedByQuarkusBom() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.23.0</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>org.hibernate.orm</groupId>
                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                    <version>6.1.2.Final</version>
                                    <scope>provided</scope>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.24.0.CR1</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenExistingConfig() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>org.hibernate.orm</groupId>
                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                    <version>6.1.2.Final</version>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <proc>none</proc>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <proc>none</proc>
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
                        """));
    }

    @Test
    void testJpaModelgenAnotherProcessor() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencies>
                                <dependency>
                                    <groupId>org.hibernate.orm</groupId>
                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                    <version>6.1.2.Final</version>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <proc>none</proc>
                                            <annotationProcessorPaths>
                                                <annotationProcessorPath>
                                                    <groupId>another.annotation</groupId>
                                                    <artifactId>processor</artifactId>
                                                </annotationProcessorPath>
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
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <proc>none</proc>
                                            <annotationProcessorPaths>
                                                <annotationProcessorPath>
                                                    <groupId>another.annotation</groupId>
                                                    <artifactId>processor</artifactId>
                                                </annotationProcessorPath>
                                                <annotationProcessorPath>
                                                    <groupId>org.hibernate.orm</groupId>
                                                    <artifactId>hibernate-processor</artifactId>
                                                </annotationProcessorPath>
                                            </annotationProcessorPaths>
                                        </configuration>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """));
    }

    @Test
    void testJpaModelgenNewAnnotationProcessorAlreadyPresent() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate.orm</groupId>
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
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenOldAnnotationProcessorAlreadyPresent() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.23.0</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.24.0.CR1</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenNewAnnotationProcessorAlreadyPresentWithOutdatedVersion() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.23.0</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate</groupId>
                                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                                    <version>6.6.6.Final</version>
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
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.24.0.CR1</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenOldAnnotationProcessorAlreadyPresentWithOutdatedVersion() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.23.0</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate</groupId>
                                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                                    <version>5.6.15.Final</version>
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
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.24.0.CR1</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenNoBuildSection() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.23.0</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>org.hibernate.orm</groupId>
                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                    <version>6.1.2.Final</version>
                                    <scope>provided</scope>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.24.0.CR1</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testJpaModelgenNoMavenCompilerPlugin() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.23.0</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>org.hibernate.orm</groupId>
                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                    <version>6.1.2.Final</version>
                                    <scope>provided</scope>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-jar-plugin</artifactId>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>3.24.0.CR1</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-jar-plugin</artifactId>
                                    </plugin>
                                    <plugin>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testSyncVersion() {
        //language=xml
        rewriteRun(pomXml(
                """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <properties>
                                <quarkus.version>3.23.0</quarkus.version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>${quarkus.version}</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
                                        <configuration>
                                            <annotationProcessorPaths>
                                                <path>
                                                    <groupId>org.hibernate.orm</groupId>
                                                    <artifactId>hibernate-jpamodelgen</artifactId>
                                                    <version>6.2.13.Final</version>
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
                            <groupId>io.quarkus.bot</groupId>
                            <artifactId>release</artifactId>
                            <version>999-SNAPSHOT</version>
                            <properties>
                                <quarkus.version>3.24.0.CR1</quarkus.version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.quarkus</groupId>
                                        <artifactId>quarkus-bom</artifactId>
                                        <version>${quarkus.version}</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.apache.maven.plugins</groupId>
                                        <artifactId>maven-compiler-plugin</artifactId>
                                        <version>3.12.1</version>
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
                        """));
    }

    @Test
    void testSessionMethods() {
        //language=java
        rewriteRun(java("""
                package com.acme;

                import jakarta.persistence.Entity;
                import jakarta.persistence.Id;
                import jakarta.persistence.ManyToOne;
                import org.hibernate.annotations.Cascade;
                import org.hibernate.annotations.CascadeType;
                import org.hibernate.Session;
                import org.hibernate.LockMode;
                import org.hibernate.LockOptions;

                public class MyRepo {
                	@Entity
                	public static class MyEntity {
                		@Id
                		public Long id;

                		public String text;

                		@ManyToOne
                		@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE })
                		public MyEntity other;
                	}

                	public void doThings(Session session) {
                		MyEntity entity1 = session.load(MyEntity.class, 1L);
                		MyEntity entity2 = session.load(MyEntity.class.getName(), 2L);
                		MyEntity entity3 = session.load(MyEntity.class, 3L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity4 = session.load(MyEntity.class.getName(), 4L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity5 = session.load(MyEntity.class, 5L, LockOptions.PESSIMISTIC_WRITE);
                		MyEntity entity6 = session.load(MyEntity.class.getName(), 6L, LockOptions.PESSIMISTIC_WRITE);
                		MyEntity entity7 = session.get(MyEntity.class, 7L);
                		MyEntity entity8 = session.get(MyEntity.class.getName(), 8L);
                		MyEntity entity9 = session.get(MyEntity.class, 9L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity10 = session.get(MyEntity.class.getName(), 10L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity11 = session.get(MyEntity.class, 11L, LockOptions.PESSIMISTIC_WRITE);
                		MyEntity entity12 = session.get(MyEntity.class.getName(), 12L, LockOptions.PESSIMISTIC_WRITE);
                		session.delete(entity1);
                		// not handled
                		session.delete(MyEntity.class.getName(), entity2);
                		MyEntity entity101 = session.refresh(MyEntity.class.getName(), entity1);
                		MyEntity entity102 = session.refresh(MyEntity.class.getName(), entity2, LockOptions.PESSIMISTIC_WRITE);
                		// end not handled
                		session.save(new MyEntity());
                		session.save(MyEntity.class.getName(), new MyEntity());
                		session.update(entity1);
                		// not handled
                		session.saveOrUpdate(entity1);
                		session.saveOrUpdate(MyEntity.class.getName(), entity1);
                		// end not handled
                	}
                }
                """, """
                package com.acme;

                import jakarta.persistence.Entity;
                import jakarta.persistence.Id;
                import jakarta.persistence.ManyToOne;
                import org.hibernate.annotations.Cascade;
                import org.hibernate.annotations.CascadeType;
                import org.hibernate.Session;
                import org.hibernate.LockMode;
                import org.hibernate.LockOptions;

                public class MyRepo {
                	@Entity
                	public static class MyEntity {
                		@Id
                		public Long id;

                		public String text;

                		@ManyToOne
                		@Cascade({CascadeType.SAVE_UPDATE, CascadeType.REMOVE })
                		public MyEntity other;
                	}

                	public void doThings(Session session) {
                		MyEntity entity1 = session.getReference(MyEntity.class, 1L);
                		MyEntity entity2 = session.getReference(MyEntity.class.getName(), 2L);
                		MyEntity entity3 = session.get(MyEntity.class, 3L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity4 = session.get(MyEntity.class.getName(), 4L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity5 = session.get(MyEntity.class, 5L, LockOptions.PESSIMISTIC_WRITE);
                		MyEntity entity6 = session.get(MyEntity.class.getName(), 6L, LockOptions.PESSIMISTIC_WRITE);
                		MyEntity entity7 = session.find(MyEntity.class, 7L);
                		MyEntity entity8 = session.find(MyEntity.class.getName(), 8L);
                		MyEntity entity9 = session.get(MyEntity.class, 9L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity10 = session.get(MyEntity.class.getName(), 10L, LockMode.PESSIMISTIC_WRITE);
                		MyEntity entity11 = session.get(MyEntity.class, 11L, LockOptions.PESSIMISTIC_WRITE);
                		MyEntity entity12 = session.get(MyEntity.class.getName(), 12L, LockOptions.PESSIMISTIC_WRITE);
                		session.remove(entity1);
                		// not handled
                		session.delete(MyEntity.class.getName(), entity2);
                		MyEntity entity101 = session.refresh(MyEntity.class.getName(), entity1);
                		MyEntity entity102 = session.refresh(MyEntity.class.getName(), entity2, LockOptions.PESSIMISTIC_WRITE);
                		// end not handled
                		session.persist(new MyEntity());
                		session.persist(MyEntity.class.getName(), new MyEntity());
                		session.merge(entity1);
                		// not handled
                		session.saveOrUpdate(entity1);
                		session.saveOrUpdate(MyEntity.class.getName(), entity1);
                		// end not handled
                	}
                }
                """));
    }

}
