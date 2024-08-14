package io.quarkus.updates.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static io.quarkus.updates.integration.UpdateProjectEcosystemIT.ECOSYSTEM_CI;
import static org.assertj.core.api.Assertions.assertThat;

@DisabledIfEnvironmentVariable(named = ECOSYSTEM_CI, matches = "true")
public class UpdateProjectIT {
    private static final String MAVEN_CENTRAL_QUARKUS_REPO = "https://repo1.maven.org/maven2/io/quarkus/";
    public static final String QUARKUS_TEST_CLI = "quarkus-test-cli";

    private static volatile boolean quarkusRepoTrusted = false;
    private static Path TMP_DIR;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException, TimeoutException {
        TMP_DIR = Files.createTempDirectory("junit");
        installCLI(TMP_DIR, "quarkus@quarkusio");
    }

    @ParameterizedTest
    @DisabledIfEnvironmentVariable(named = ECOSYSTEM_CI, matches = "true")
    @CsvFileSource(resources = "/update-tests.csv", numLinesToSkip = 1)
    void testMaven(String currentStream, String updateStream) throws IOException, InterruptedException, TimeoutException {
        final Path tmpDir = TMP_DIR.resolve(currentStream + "-to-" + updateStream);
        testUpdateMaven(tmpDir, currentStream, updateStream);
    }

    @ParameterizedTest
    @DisabledIfEnvironmentVariable(named = ECOSYSTEM_CI, matches = "true")
    @CsvFileSource(resources = "/update-tests.csv", numLinesToSkip = 1)
    void testGradle(String currentStream, String updateStream) throws IOException, InterruptedException, TimeoutException {
        final Path tmpDir = TMP_DIR.resolve(currentStream + "-to-" + updateStream);
        testUpdateGradle(tmpDir, currentStream, updateStream);
    }

    static void testUpdateGradle(Path tempDir, String currentStream, String updateStream) throws IOException, InterruptedException, TimeoutException {
        final Path dir = tempDir.resolve("gradle");
        Files.createDirectories(dir);
        final Path appDir = createProject(dir, currentStream, "--gradle");
        assertThat(appDir.resolve("gradle.properties")).content().containsPattern(Pattern.compile("quarkusPluginVersion=" + currentStream + "[0-9a-zA-Z.]+"));
        assertThat(appDir.resolve("gradle.properties")).content().containsPattern(Pattern.compile("quarkusPlatformVersion=" + currentStream + "[0-9a-zA-Z.]+"));
        updateProject(appDir, updateStream);
        assertThat(appDir.resolve("gradle.properties")).content().containsPattern(Pattern.compile("quarkusPluginVersion=" + updateStream + "[0-9a-zA-Z.]+"));
        assertThat(appDir.resolve("gradle.properties")).content().containsPattern(Pattern.compile("quarkusPlatformVersion=" + updateStream + "[0-9a-zA-Z.]+"));
        String output = jbang(appDir, QUARKUS_TEST_CLI, "build", "--", "--no-daemon");
        assertThat(output).contains("BUILD SUCCESS");
    }

    static void testUpdateMaven(Path tempDir, String currentStream, String updateStream) throws IOException, InterruptedException, TimeoutException {
        final Path dir = tempDir.resolve("maven");
        Files.createDirectories(dir);
        final Path appDir = createProject(dir, currentStream, "--maven");
        assertThat(appDir.resolve("pom.xml")).content().containsPattern(Pattern.compile("<quarkus.platform.version>" + currentStream + "[0-9a-zA-Z.]+</quarkus.platform.version>"));
        updateProject(appDir, updateStream);
        assertThat(appDir.resolve("pom.xml")).content().containsPattern(Pattern.compile("<quarkus.platform.version>" + updateStream + "[0-9a-zA-Z.]+</quarkus.platform.version>"));
        buildProject(appDir);
    }

    static void buildProject(Path tempDir) throws IOException, InterruptedException, TimeoutException {
        String output = jbang(tempDir, QUARKUS_TEST_CLI, "build");
        assertThat(output).contains("BUILD SUCCESS");
    }

    static void updateProject(Path tempDir, String updateStream) throws IOException, InterruptedException, TimeoutException {
        String output = jbang(tempDir, QUARKUS_TEST_CLI, "update", "-S=" + updateStream, "--quarkus-update-recipes=999-SNAPSHOT");

    }

    private static Path createProject(Path tempDir, String stream, String... args) throws IOException, InterruptedException, TimeoutException {
        List<String> commands = List.of(QUARKUS_TEST_CLI, "create", "app", "my-app", "-S=" + stream);
        List<String> realArgs = new ArrayList<>();
        realArgs.addAll(commands);
        realArgs.addAll(Arrays.asList(args));
        jbang(tempDir, realArgs);
        final Path appDir = tempDir.resolve("my-app");
        assertThat(appDir.toFile()).isNotEmptyDirectory();
        return appDir;
    }

    static void installCLI(Path tempDir, String repo) throws IOException, InterruptedException, TimeoutException {
        trustQuarkusRepo(tempDir);
        String output = jbang(tempDir, "alias", "add", "-f", ".", "--name", QUARKUS_TEST_CLI, repo);
        assertThat(output).containsPattern(Pattern.compile("\\[jbang\\] Alias '" + QUARKUS_TEST_CLI + "' added to '.*'"));
    }



    private static void propagateSystemPropertyIfSet(String name, List<String> command) {
        if (System.getProperties().containsKey(name)) {
            final StringBuilder buf = new StringBuilder();
            buf.append("-D").append(name);
            final String value = System.getProperty(name);
            if (value != null && !value.isEmpty()) {
                buf.append("=").append(value);
            }
            command.add(buf.toString());
        }
    }

    static void trustQuarkusRepo(Path tempDir) throws IOException, InterruptedException, TimeoutException {
        if (!quarkusRepoTrusted) {
            jbang(tempDir, "trust", "add", MAVEN_CENTRAL_QUARKUS_REPO);
            quarkusRepoTrusted = true;
        }
    }

    static String jbang(Path workingDir, List<String> args) throws IOException, InterruptedException, TimeoutException {
        List<String> realArgs = new ArrayList<>();
        realArgs.add(Path.of("./jbang").toAbsolutePath().toString());
        realArgs.addAll(args);

        return run(workingDir, realArgs).execute().outputUTF8();
    }

    static String jbang(Path workingDir, String... args) throws IOException, InterruptedException, TimeoutException {
        return jbang(workingDir, Arrays.asList(args));
    }

    static String run(Path workingDir, String... args) throws IOException, InterruptedException, TimeoutException {
        return run(workingDir, Arrays.asList(args)).execute().outputUTF8();
    }

    static ProcessExecutor run(Path workingDir, List<String> args) throws IOException, InterruptedException, TimeoutException {
        List<String> realArgs = new ArrayList<>();
        realArgs.addAll(args);
        propagateSystemPropertyIfSet("maven.repo.local", realArgs);

        System.out.println("run: " + String.join(" ", realArgs));
        return new ProcessExecutor().command(realArgs)
                .directory(workingDir.toFile())
                .redirectOutputAlsoTo(System.out)
                .exitValue(0)
                .readOutput(true);

    }
}
