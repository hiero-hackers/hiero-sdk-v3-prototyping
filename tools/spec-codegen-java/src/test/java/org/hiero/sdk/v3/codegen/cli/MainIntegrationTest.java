package org.hiero.sdk.v3.codegen.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MainIntegrationTest {

    @Test
    void validatesAllConfiguredSpecificationsWithoutWritingOutput() {
        var root = repositoryRoot();
        var outBytes = new ByteArrayOutputStream();
        var errorBytes = new ByteArrayOutputStream();
        var output = root.resolve("java/hiero-sdk-base-api/src/main/java");
        var existedBefore = Files.exists(output);

        var status = Main.run(
                new String[] {"validate", "--repository-root", root.toString()},
                new PrintStream(outBytes, true, StandardCharsets.UTF_8),
                new PrintStream(errorBytes, true, StandardCharsets.UTF_8));

        assertEquals(0, status, errorBytes.toString(StandardCharsets.UTF_8));
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).contains("Validated 11 specification namespaces."));
        assertEquals(existedBefore, Files.exists(output));
    }

    @Test
    void inventoryIsReadOnlyAndContainsMappedSourceIdentities() {
        var root = repositoryRoot();
        var output = new ByteArrayOutputStream();

        var status = Main.run(
                new String[] {"inventory", "--repository-root", root.toString()},
                new PrintStream(output, true, StandardCharsets.UTF_8),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));

        assertEquals(0, status);
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("authority#Authority"));
    }

    @Test
    void rejectsPhysicalOutputOutsideRepository() {
        var root = repositoryRoot();
        var errors = new ByteArrayOutputStream();

        var status = Main.run(
                new String[] {
                    "generate",
                    "--repository-root",
                    root.toString(),
                    "--physical-source-root",
                    root.getParent().resolve("outside-generated-api").toString()
                },
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8),
                new PrintStream(errors, true, StandardCharsets.UTF_8));

        assertEquals(2, status);
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("must remain within the repository root"));
    }

    private static Path repositoryRoot() {
        var current = Path.of("").toAbsolutePath().normalize();
        while (current != null && !Files.isRegularFile(current.resolve("codegen/java-base.yml"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("repository root not found");
        }
        return current;
    }
}
