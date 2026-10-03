package org.hiero.sdk.v3.metalang.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GeneratedOutputTest {

    private static final String HEADER = "// Generated. Do not edit.\n";

    @TempDir
    Path output;

    private static GeneratedFile file(final String path, final String body) {
        return new GeneratedFile(path, HEADER + body);
    }

    @Test
    void shouldWriteNewFilesAndCreateDirectories() throws Exception {
        // WHEN
        final GeneratedOutput.Result result = GeneratedOutput.write(output.resolve("new/dir"),
                List.of(file("a/A.java", "class A {}\n"), file("B.java", "class B {}\n")), HEADER);

        // THEN
        assertThat(result.written()).isEqualTo(2);
        assertThat(result.removed()).isEmpty();
        assertThat(output.resolve("new/dir/a/A.java")).hasContent(HEADER + "class A {}");
    }

    @Test
    void shouldOnlyRewriteFilesWhoseContentChanged() throws Exception {
        // GIVEN
        GeneratedOutput.write(output, List.of(file("A.java", "class A {}\n"), file("B.java", "class B {}\n")), HEADER);
        final FileTime old = FileTime.from(Instant.parse("2020-01-01T00:00:00Z"));
        Files.setLastModifiedTime(output.resolve("A.java"), old);

        // WHEN
        final GeneratedOutput.Result result = GeneratedOutput.write(output,
                List.of(file("A.java", "class A {}\n"), file("B.java", "class B { int x; }\n")), HEADER);

        // THEN the unchanged file keeps its timestamp
        assertThat(result.written()).isEqualTo(1);
        assertThat(Files.getLastModifiedTime(output.resolve("A.java"))).isEqualTo(old);
        assertThat(output.resolve("B.java")).hasContent(HEADER + "class B { int x; }");
    }

    @Test
    void shouldDeleteOnlyStaleGeneratedFilesAndTheDirectoriesTheyLeaveEmpty() throws Exception {
        // GIVEN generated files, a hand-written file, a binary file and an empty directory that already exists
        GeneratedOutput.write(output, List.of(file("keep/Keep.java", "class Keep {}\n"),
                file("gone/deep/Gone.java", "class Gone {}\n"), file("mixed/Old.java", "class Old {}\n")), HEADER);
        Files.writeString(output.resolve("mixed/Manual.java"), "// written by hand\nclass Manual {}\n");
        Files.write(output.resolve("mixed/data.bin"), new byte[] {(byte) 0xff, (byte) 0xfe, 0x00});
        Files.createDirectories(output.resolve("empty"));

        // WHEN the generator no longer produces Gone and Old
        final GeneratedOutput.Result result = GeneratedOutput.write(output,
                List.of(file("keep/Keep.java", "class Keep {}\n")), HEADER);

        // THEN
        assertThat(result.written()).isZero();
        assertThat(result.removed()).containsExactly(Path.of("gone/deep/Gone.java"), Path.of("mixed/Old.java"));
        assertThat(output.resolve("gone")).doesNotExist();
        assertThat(output.resolve("mixed/Manual.java")).exists();
        assertThat(output.resolve("mixed/data.bin")).exists();
        assertThat(output.resolve("empty")).isDirectory();
        assertThat(Files.readString(output.resolve("keep/Keep.java"), StandardCharsets.UTF_8))
                .startsWith(HEADER);
    }
}
