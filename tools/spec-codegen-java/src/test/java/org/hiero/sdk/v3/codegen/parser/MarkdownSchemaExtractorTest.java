package org.hiero.sdk.v3.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkdownSchemaExtractorTest {

    private final MarkdownSchemaExtractor extractor = new MarkdownSchemaExtractor();

    @Test
    void extractsTheOnlyNamespaceFenceAndIgnoresOtherFences() {
        var markdown = """
                # Diagram
                ```mermaid
                classDiagram
                ```
                ## API
                ```api
                namespace ledger
                abstraction Address {}
                ```
                """;

        var extracted = extractor.extract("spec/base/ledger.md", markdown);

        assertEquals("spec/base/ledger.md", extracted.sourcePath());
        assertEquals(7, extracted.schemaStartLine());
        assertEquals("namespace ledger\nabstraction Address {}\n", extracted.content());
    }

    @Test
    void preservesUtf8AndReportsRepositoryRelativeLocations() {
        var extracted = extractor.extract("spec/base/hedera.md", """
                # HBAR
                ```
                namespace hedera
                constant SYMBOL:string = "ℏ"
                ```
                """);

        assertTrue(extracted.content().contains("ℏ"));
        assertEquals("spec/base/hedera.md:3:1", extracted.location().display());
    }

    @Test
    void rejectsMissingDuplicateAndUnclosedSchemaFences() {
        var missing = assertThrows(
                SchemaException.class,
                () -> extractor.extract("spec/base/missing.md", "```\nclassDiagram\n```\n"));
        assertEquals("SCHEMA_FENCE_MISSING", missing.code());

        var duplicate = assertThrows(
                SchemaException.class,
                () -> extractor.extract(
                        "spec/base/duplicate.md",
                        "```\nnamespace first\n```\n```\nnamespace second\n```\n"));
        assertEquals("SCHEMA_FENCE_DUPLICATE", duplicate.code());
        assertTrue(duplicate.getMessage().startsWith("spec/base/duplicate.md:5:1"));

        var unclosed = assertThrows(
                SchemaException.class,
                () -> extractor.extract("spec/base/unclosed.md", "# API\n```\nnamespace broken\n"));
        assertEquals("SCHEMA_FENCE_UNCLOSED", unclosed.code());
        assertTrue(unclosed.getMessage().startsWith("spec/base/unclosed.md:3:1"));
    }

    @Test
    void rejectsPathsThatAreNotRepositoryRelative() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> extractor.extract("/tmp/spec.md", "```\nnamespace unsafe\n```\n"));
        assertTrue(exception.getMessage().contains("repository-relative"));
    }

    @Test
    void readingAFileDoesNotModifyIt(@TempDir Path temporaryDirectory) throws Exception {
        var repository = temporaryDirectory.resolve("repo");
        var source = repository.resolve("spec/base/common.md");
        java.nio.file.Files.createDirectories(source.getParent());
        java.nio.file.Files.writeString(source, "```\nnamespace common\n```\n");
        var before = java.nio.file.Files.readAllBytes(source);

        var extracted = extractor.extract(repository, source);

        assertEquals("spec/base/common.md", extracted.sourcePath());
        assertFalse(extracted.content().isBlank());
        assertTrue(java.util.Arrays.equals(before, java.nio.file.Files.readAllBytes(source)));
    }
}
