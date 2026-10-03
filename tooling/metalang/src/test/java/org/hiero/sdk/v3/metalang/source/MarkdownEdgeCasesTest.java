package org.hiero.sdk.v3.metalang.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor.CodeBlock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Markdown boundary cases (CommonMark fenced code blocks and ATX headings) for the schema extractor.
 */
class MarkdownEdgeCasesTest {

    private final MarkdownSchemaExtractor extractor = new MarkdownSchemaExtractor();

    private List<String> ruleIds(final String markdown) {
        return extractor.extract("a.md", markdown).diagnostics().stream().map(Diagnostic::ruleId).toList();
    }

    @Nested
    class Fences {

        @Test
        void shouldSupportLongerFencesThatContainShorterOnes() {
            // GIVEN a four-backtick fence whose content contains a three-backtick line
            final String markdown = "````\nnamespace a\n```\n// inside\n````\n";

            // WHEN
            final List<CodeBlock> blocks = extractor.codeBlocks(markdown);

            // THEN
            assertThat(blocks).singleElement().satisfies(b -> {
                assertThat(b.content()).isEqualTo("namespace a\n```\n// inside\n");
                assertThat(b.closed()).isTrue();
            });
        }

        @Test
        void shouldCloseWithLongerFenceButNotWithShorterOrDifferentCharacter() {
            assertThat(extractor.codeBlocks("```\na\n`````\n")).singleElement()
                    .satisfies(b -> assertThat(b.content()).isEqualTo("a\n"));
            assertThat(extractor.codeBlocks("````\na\n```\n~~~~\n")).singleElement()
                    .satisfies(b -> assertThat(b.closed()).isFalse());
        }

        @Test
        void shouldAllowUpToThreeSpacesOfIndentationForFences() {
            assertThat(extractor.codeBlocks("   ```\na\n   ```\n")).singleElement()
                    .satisfies(b -> assertThat(b.closed()).isTrue());
            // four spaces: an indented code block in CommonMark, not a fence
            assertThat(extractor.codeBlocks("    ```\na\n    ```\n")).isEmpty();
            // a four-space indented closing line is content, so the block stays open
            assertThat(extractor.codeBlocks("```\na\n    ```\n")).singleElement()
                    .satisfies(b -> assertThat(b.closed()).isFalse());
        }

        @Test
        void shouldNotCloseFenceWhenTextFollowsIt() {
            assertThat(extractor.codeBlocks("```\na\n``` text\n```\n")).singleElement()
                    .satisfies(b -> assertThat(b.content()).isEqualTo("a\n``` text\n"));
        }

        @Test
        void shouldCaptureInfoStringAndRejectBacktickFenceWithBacktickInInfo() {
            assertThat(extractor.codeBlocks("```java title\nx\n```\n")).singleElement()
                    .satisfies(b -> assertThat(b.info()).isEqualTo("java title"));
            assertThat(extractor.codeBlocks("``` a`b\nx\n")).isEmpty();
            assertThat(extractor.codeBlocks("~~~ a`b\nx\n~~~\n")).singleElement()
                    .satisfies(b -> assertThat(b.info()).isEqualTo("a`b"));
        }

        @Test
        void shouldUseSchemaBlockWithInfoString() {
            final ExtractionResult result = extractor.extract("a.md", "## API Schema\n```text\nnamespace a\n```\n");
            assertThat(result.source().text()).isEqualTo("namespace a\n");
        }

        @Test
        void shouldReportUnclosedFenceAtOpeningLine() {
            final ExtractionResult result = extractor.extract("a.md", "# T\n\n## API Schema\n```\nnamespace a\n");
            assertThat(result.diagnostics()).filteredOn(d -> d.ruleId().equals("doc.unclosed-fence"))
                    .singleElement().satisfies(d -> assertThat(d.location().line()).isEqualTo(4));
        }

        @Test
        void shouldExposeSectionAndLinesOfEveryBlock() {
            // WHEN
            final List<CodeBlock> blocks = extractor.codeBlocks("```\na\n```\n## API Schema\n\n~~~\nb\n~~~\n");

            // THEN
            assertThat(blocks).hasSize(2);
            assertThat(blocks.getFirst().section()).isNull();
            assertThat(blocks.getFirst().sectionLine()).isZero();
            assertThat(blocks.get(1).section()).isEqualTo("API Schema");
            assertThat(blocks.get(1).sectionLine()).isEqualTo(4);
            assertThat(blocks.get(1).openingLine()).isEqualTo(6);
            assertThat(blocks.get(1).firstContentLine()).isEqualTo(7);
        }
    }

    @Nested
    class Headings {

        @Test
        void shouldRecognizeIndentedHeadingsAndClosingHashes() {
            assertThat(extractor.extract("a.md", "   ## API Schema ##\n```\nnamespace a\n```\n").schema()).isPresent();
        }

        @Test
        void shouldIgnoreTextThatIsNotALevelTwoHeading() {
            // "##API" (no space), "### API Schema" (level 3) and a 4-space indented line are no level-2 headings
            for (final String heading : List.of("##API Schema", "### API Schema", "    ## API Schema")) {
                assertThat(ruleIds(heading + "\n```\nnamespace a\n```\n")).as(heading)
                        .contains("doc.schema-outside-section");
            }
        }

        @Test
        void shouldKeepBlocksBelowSubheadingsInTheSchemaSection() {
            final ExtractionResult result = extractor.extract("a.md", "## API Schema\n### Part 1\n```\nnamespace a\n```\n");
            assertThat(result.source().text()).isEqualTo("namespace a\n");
            assertThat(result.diagnostics()).extracting(Diagnostic::ruleId).doesNotContain("doc.schema-outside-section");
        }
    }

    @Nested
    class Description {

        @Test
        void shouldCaptureTheDescriptionSectionIncludingCodeBlocksAndSubheadings() {
            // GIVEN a description with a code block that contains a "## " line and a level-3 heading
            final String markdown = """
                    # Title

                    ## Description

                    First paragraph.

                    ```
                    ## not a heading
                    ```

                    ### Details
                    More.

                    ## API Schema

                    ```
                    namespace a
                    ```
                    """;

            // WHEN
            final SchemaSource source = extractor.extract("a.md", markdown).source();

            // THEN
            assertThat(source.description()).isEqualTo(
                    "First paragraph.\n\n```\n## not a heading\n```\n\n### Details\nMore.");
        }

        @Test
        void shouldUseEmptyDescriptionIfTheSectionIsMissingOrLast() {
            assertThat(extractor.extract("a.md", "## API Schema\n```\nnamespace a\n```\n").source().description())
                    .isEmpty();
            assertThat(extractor.extract("a.md", "## API Schema\n```\nnamespace a\n```\n## Description\n\nLast.\n")
                    .source().description()).isEqualTo("Last.");
            assertThat(extractor.extract("a.md", "## Description\n## API Schema\n```\nnamespace a\n```\n")
                    .source().descriptionLine()).isZero();
        }

        @Test
        void shouldTrackTheLineOfTheFirstDescriptionLine() {
            // GIVEN a description that starts after two blank lines (Markdown line 5)
            final String markdown = "# T\n## Description\n\n\nFirst.\nSecond.\n## API Schema\n```\nnamespace a\n```\n";

            // WHEN
            final SchemaSource source = extractor.extract("a.md", markdown).source();

            // THEN
            assertThat(source.description()).isEqualTo("First.\nSecond.");
            assertThat(source.descriptionLine()).isEqualTo(5);
        }

        @Test
        void shouldAcceptDesignNotesBetweenDescriptionAndSchema() {
            final String markdown = "# T\n## Description\nA.\n## Design Notes\nWhy.\n## API Schema\n```\nnamespace a\n```\n"
                    + "## Testing\nNone.\n## Questions & Comments\nNone.\n";
            assertThat(ruleIds(markdown)).isEmpty();
            assertThat(extractor.extract("a.md", markdown).source().description()).isEqualTo("A.");
            assertThat(ruleIds(markdown.replace("## Design Notes\nWhy.\n", "")
                    .replace("## Testing", "## Design Notes\nWhy.\n## Testing"))).contains("doc.section-order");
        }
    }

    @Nested
    class LineEndings {

        @Test
        void shouldHandleCrlfAndKeepLineNumbers() {
            // GIVEN
            final String markdown = "# T\r\n\r\n## API Schema\r\n```\r\nnamespace a\r\nX {}\r\n```\r\n";

            // WHEN
            final SchemaSource source = extractor.extract("a.md", markdown).source();

            // THEN
            assertThat(source.text()).isEqualTo("namespace a\nX {}\n");
            assertThat(source.lineOffset()).isEqualTo(4);
        }

        @Test
        void shouldHandleMissingTrailingNewlineAndUnicode() {
            final SchemaSource source = extractor.extract("a.md", "## API Schema\n```\n// tℏ ✓\nnamespace a\n```").source();
            assertThat(source.text()).isEqualTo("// tℏ ✓\nnamespace a\n");
        }
    }
}
