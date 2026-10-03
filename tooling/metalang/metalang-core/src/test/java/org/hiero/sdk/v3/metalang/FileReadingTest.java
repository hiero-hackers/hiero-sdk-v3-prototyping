package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Reading spec files from disk: encodings, byte order marks, file selection and relative names.
 */
class FileReadingTest {

    @TempDir
    Path root;

    @Test
    void shouldReportInvalidUtf8AndContinueWithTheOtherFiles() throws IOException {
        // GIVEN one ISO-8859-1 file and one valid spec
        Files.write(root.resolve("latin1.md"), "# T\n// Größe\n".getBytes(StandardCharsets.ISO_8859_1));
        Files.writeString(root.resolve("ok.md"), TestSpecs.markdown("namespace a\n"));

        // WHEN
        final ValidationReport report = new MetaLang().validate(root);

        // THEN
        assertThat(report.diagnostics()).extracting(Diagnostic::toString)
                .containsExactly("latin1.md:1:1: ERROR [doc.invalid-encoding] File is not valid UTF-8; it is skipped");
        assertThat(report.specCount()).isEqualTo(1);
        assertThat(report.model().namespaceNames()).containsExactly("a");
    }

    @Test
    void shouldReportInvalidUtf8ForASingleFile() throws IOException {
        final Path file = root.resolve("latin1.md");
        Files.write(file, new byte[] {'#', ' ', (byte) 0xF6});
        assertThat(new MetaLang().validate(file).byRule("doc.invalid-encoding")).hasSize(1);
    }

    @Test
    void shouldIgnoreLeadingByteOrderMark() throws IOException {
        // GIVEN a spec that starts with a UTF-8 BOM directly in front of the first heading
        Files.writeString(root.resolve("bom.md"), "﻿## API Schema\n```\nnamespace a\n```\n");

        // WHEN
        final ValidationReport report = new MetaLang().validate(root);

        // THEN the heading is recognized (no 'schema outside section')
        assertThat(report.diagnostics()).extracting(Diagnostic::ruleId).doesNotContain("doc.schema-outside-section");
        assertThat(report.specCount()).isEqualTo(1);
    }

    @Test
    void shouldOnlyReadMarkdownFilesAndUseRelativeSlashSeparatedNames() throws IOException {
        // GIVEN
        Files.createDirectories(root.resolve("sub/dir"));
        Files.writeString(root.resolve("sub/dir/x.md"), TestSpecs.markdown("namespace a\nX {}\n"));
        Files.writeString(root.resolve("sub/notes.txt"), "namespace b\n");
        Files.createDirectories(root.resolve("folder.md"));

        // WHEN
        final ValidationReport report = new MetaLang().validate(root);

        // THEN
        assertThat(report.model().files()).extracting(f -> f.file()).containsExactly("sub/dir/x.md");
    }
}
