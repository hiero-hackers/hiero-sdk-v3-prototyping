package org.hiero.sdk.v3.codegen.manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.hiero.sdk.v3.codegen.model.MappingDecision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerationManifestTest {

    private final ManifestWriter writer = new ManifestWriter();

    @Test
    void recordsCanonicalProvenanceHashesAndMappings(@TempDir Path temporaryDirectory) throws Exception {
        var repository = temporaryDirectory.resolve("repository");
        var input = repository.resolve("spec/base/common.md");
        var output = repository.resolve("java/generated/Page.java");
        Files.createDirectories(input.getParent());
        Files.createDirectories(output.getParent());
        Files.writeString(input, "namespace common\n");
        Files.writeString(output, "interface Page {}\n");

        var manifest = new GenerationManifest(
                "abc123",
                new GeneratorIdentity("org.hiero.sdk.v3", "spec-codegen-java", "0.1.0-SNAPSHOT"),
                Sha256.digest("config"),
                21,
                "org.hiero.sdk.v3",
                List.of(FileDigest.from(repository, input)),
                List.of(MappingDecision.direct("common#Page", "org.hiero.sdk.v3.common.Page", "DIRECT", "FR-003")),
                List.of(new DeferredEnforcement(
                        "common#Page.data", "DE-002", "immutable snapshot", "provider TCK")),
                List.of(FileDigest.from(repository, output)));

        var json = writer.toJson(manifest);
        var repeated = writer.toJson(manifest);

        assertEquals(json, repeated);
        assertTrue(json.contains("\"sourceRevision\": \"abc123\""));
        assertTrue(json.contains(Sha256.digest("namespace common\n")));
        assertTrue(json.contains("common#Page"));
        assertTrue(json.contains("DE-002"));
        assertFalse(json.contains(temporaryDirectory.toString()));
        assertFalse(json.contains(System.getProperty("user.name")));
    }

    @Test
    void rejectsAbsolutePathsAndSecretLikeValues() {
        assertThrows(IllegalArgumentException.class, () -> new FileDigest("/tmp/input.md", Sha256.digest("x")));
        assertThrows(
                IllegalArgumentException.class,
                () -> writer.toJson(new GenerationManifest(
                        "abc123",
                        new GeneratorIdentity("org.example", "generator", "1"),
                        Sha256.digest("config"),
                        21,
                        "org.example",
                        List.of(),
                        List.of(),
                        List.of(new DeferredEnforcement("keys#Key", "DE-003", "password=hunter2", "TCK")),
                        List.of())));
    }
}
