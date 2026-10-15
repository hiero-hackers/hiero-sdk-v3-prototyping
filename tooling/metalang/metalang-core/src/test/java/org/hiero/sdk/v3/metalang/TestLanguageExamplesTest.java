package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.metalang.ast.TestCase;
import org.hiero.sdk.v3.metalang.parser.SchemaParser;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor.CodeBlock;
import org.hiero.sdk.v3.metalang.source.SchemaSource;
import org.junit.jupiter.api.Test;

/**
 * Every {@code tests} block of {@code guidelines/test-language.md} must be accepted by the grammar, including the
 * blocks inside the guideline's Markdown examples.
 */
class TestLanguageExamplesTest {

    private static final Path GUIDELINE = Path.of(System.getProperty("spec.root", "../../../spec"))
            .resolve("../guidelines/test-language.md");

    private final MarkdownSchemaExtractor extractor = new MarkdownSchemaExtractor();

    /** The {@code tests} blocks of a Markdown text, also those of nested {@code markdown} blocks. */
    private List<CodeBlock> testsBlocks(final String markdown) {
        final List<CodeBlock> result = new ArrayList<>();
        for (final CodeBlock block : extractor.codeBlocks(markdown)) {
            if (block.info().equals(MarkdownSchemaExtractor.TESTS_INFO)) {
                result.add(block);
            } else if (block.info().equals("markdown")) {
                result.addAll(testsBlocks(block.content()));
            }
        }
        return result;
    }

    @Test
    void everyTestsBlockOfTheGuidelineShouldParse() throws IOException {
        final List<CodeBlock> blocks = testsBlocks(Files.readString(GUIDELINE));
        assertThat(blocks).hasSizeGreaterThanOrEqualTo(2);
        final List<TestCase> tests = new ArrayList<>();
        for (final CodeBlock block : blocks) {
            final SchemaParser.TestsResult result = new SchemaParser().parseTests(
                    new SchemaSource("test-language.md", block.content(), block.firstContentLine() - 1));
            assertThat(result.diagnostics()).as("tests block at line " + block.openingLine()).isEmpty();
            tests.addAll(result.tests());
        }
        // the example of spec/base/keys.md
        assertThat(tests).extracting(TestCase::name).contains("a key survives every format that supports its type");
    }
}
