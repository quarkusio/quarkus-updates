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
import static io.quarkus.updates.integration.UpdateProjectIT.*;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = ECOSYSTEM_CI, matches = "true")
public class UpdateProjectEcosystemIT {
    public static final String ECOSYSTEM_CI = "ECOSYSTEM_CI";

    private static Path TMP_DIR;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException, TimeoutException {
        TMP_DIR = Files.createTempDirectory("junit");
        installCLI(TMP_DIR, getJbangMavenRepoLocal());
    }

    private static String getJbangMavenRepoLocal() {
        return System.getProperty("maven.repo.local", System.getProperty("user.home", "~") + "/.m2/repository") + "/io/quarkus/quarkus-cli/999-SNAPSHOT/quarkus-cli-999-SNAPSHOT-runner.jar";
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/update-tests.csv", numLinesToSkip = 1)
    void testMaven(String currentStream, String updateStream) throws IOException, InterruptedException, TimeoutException {
        final Path tmpDir = TMP_DIR.resolve(currentStream + "-to-" + updateStream);
        testUpdateMaven(tmpDir, currentStream, updateStream);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/update-tests.csv", numLinesToSkip = 1)
    void testGradle(String currentStream, String updateStream) throws IOException, InterruptedException, TimeoutException {
        final Path tmpDir = TMP_DIR.resolve(currentStream + "-to-" + updateStream);
        testUpdateGradle(tmpDir, currentStream, updateStream);
    }

}
