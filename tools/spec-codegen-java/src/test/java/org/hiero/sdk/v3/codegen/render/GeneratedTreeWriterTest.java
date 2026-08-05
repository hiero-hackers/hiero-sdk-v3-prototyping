package org.hiero.sdk.v3.codegen.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GeneratedTreeWriterTest {

    @Test
    void atomicallyReplacesCompleteTreeAndRemovesStaleFiles(@TempDir Path temporaryDirectory) throws Exception {
        var output = temporaryDirectory.resolve("generated");
        var staging = temporaryDirectory.resolve("staging");
        Files.createDirectories(output);
        Files.writeString(output.resolve("Stale.java"), "stale");

        new GeneratedTreeWriter().replace(
                output,
                staging,
                List.of(new RenderedFile(
                        Path.of("org/example/Current.java"),
                        "current\n".getBytes(StandardCharsets.UTF_8))));

        assertFalse(Files.exists(output.resolve("Stale.java")));
        assertEquals("current\n", Files.readString(output.resolve("org/example/Current.java")));
        assertFalse(Files.exists(staging));
    }
}
