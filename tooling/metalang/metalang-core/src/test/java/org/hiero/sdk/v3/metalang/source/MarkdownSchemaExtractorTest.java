package org.hiero.sdk.v3.metalang.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MarkdownSchemaExtractorTest {

    private final MarkdownSchemaExtractor extractor = new MarkdownSchemaExtractor();

    private static List<String> ruleIds(final ExtractionResult result) {
        return result.diagnostics().stream().map(Diagnostic::ruleId).toList();
    }

    @Nested
    class SchemaSelection {

        @Test
        void shouldExtractFirstCodeBlockOfApiSchemaSection() {
            // GIVEN
            final String markdown = TestSpecs.markdown("namespace a\n");

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.schema()).isPresent();
            assertThat(result.source().text()).isEqualTo("namespace a\n");
            assertThat(result.diagnostics()).as("canonical skeleton has no findings").isEmpty();
        }

        @Test
        void shouldComputeLineOffsetOfSchemaBlock() {
            // GIVEN
            final String markdown = "# T\n## API Schema\n\n```\nnamespace a\n```\n";

            // WHEN
            final SchemaSource source = extractor.extract("a.md", markdown).source();

            // THEN line 1 of the schema is line 5 of the Markdown file
            assertThat(source.lineOffset()).isEqualTo(4);
        }

        @Test
        void shouldAcceptSuffixedSchemaHeadingAndTildeFences() {
            // GIVEN
            final String markdown = "## API Schema — Abstraction\n~~~\nnamespace a\n~~~\n";

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.source().text()).isEqualTo("namespace a\n");
        }

        @Test
        void shouldIgnoreHeadingsInsideCodeBlocks() {
            // GIVEN
            final String markdown = "## Description\n```\n## API Schema\n```\n## API Schema\n```\nnamespace b\n```\n";

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.source().text()).isEqualTo("namespace b\n");
        }

        @Test
        void shouldWarnAboutAdditionalBlocksInSchemaSection() {
            // GIVEN
            final String markdown = "## API Schema\n```\nnamespace a\n```\n```\nnamespace b\n```\n";

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.source().text()).isEqualTo("namespace a\n");
            assertThat(ruleIds(result)).contains("doc.multiple-schema-blocks");
        }

        @Test
        void shouldReportEmptySchemaSection() {
            // WHEN
            final ExtractionResult result = extractor.extract("a.md", "## API Schema\ntext only\n");

            // THEN
            assertThat(result.schema()).isEmpty();
            assertThat(ruleIds(result)).containsExactly("doc.empty-schema-section");
        }

        @Test
        void shouldFallBackToNamespaceBlockOutsideSection() {
            // GIVEN
            final String markdown = "## Description\n```\n// comment\n\nnamespace grpc\n```\n";

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.source().text()).startsWith("// comment");
            assertThat(ruleIds(result)).contains("doc.schema-outside-section");
        }

        @Test
        void shouldTreatFileWithoutSchemaAsNonSpec() {
            // WHEN
            final ExtractionResult result = extractor.extract("a.md", "# Doc\n```mermaid\nflowchart\n```\n");

            // THEN
            assertThat(result.schema()).isEmpty();
            assertThat(ruleIds(result)).containsExactly("doc.no-schema");
        }

        @Test
        void shouldReportUnclosedFenceAndStillUseItsContent() {
            // WHEN
            final ExtractionResult result = extractor.extract("a.md", "## API Schema\n```\nnamespace a\n");

            // THEN
            assertThat(ruleIds(result)).contains("doc.unclosed-fence");
            assertThat(result.source().text()).isEqualTo("namespace a\n");
        }

        @Test
        void shouldRejectNullArguments() {
            assertThatThrownBy(() -> extractor.extract(null, "")).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> extractor.extract("a", null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Skeleton {

        @Test
        void shouldReportSectionsOutOfOrder() {
            // GIVEN
            final String markdown = "## Description\n## API Schema\n```\nnamespace a\n```\n"
                    + "## Questions & Comments\n## Testing\n";

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(ruleIds(result)).containsExactly("doc.section-order");
        }

        @Test
        void shouldReportNonCanonicalSectionName() {
            // GIVEN
            final String markdown = "## Description\n## API Schema\n```\nnamespace a\n```\n"
                    + "## Example\n## Testing\n## Questions & Comments\n";

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(ruleIds(result)).containsExactly("doc.section-name");
        }

        @Test
        void shouldReportMissingSectionsAndIgnoreUnknownOnes() {
            // GIVEN
            final String markdown = "## Custom\n## API Schema\n```\nnamespace a\n```\n";

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.diagnostics()).extracting(Diagnostic::message).containsExactly(
                    "Section '## Description' is missing",
                    "Section '## Questions & Comments' is missing",
                    "Section '## Testing' is missing");
        }
    }

    @Nested
    class SchemaSourceRecord {

        @Test
        void shouldRejectNegativeOffset() {
            assertThatThrownBy(() -> new SchemaSource("a", "", -1)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldCreatePlainTextSourceWithoutOffset() {
            assertThat(SchemaSource.ofPlainText("a", "x").lineOffset()).isZero();
        }
    }

    @Nested
    class BehaviorRules {

        @Test
        void shouldReadTheRulesOfTheDescriptionOutsideOfCodeBlocks() {
            // GIVEN markers at the start of a paragraph, of a numbered and of a nested item and of a bold text; a
            // marker in a code block, one in the middle of a sentence and one that a line break moved to the start of a
            // continuation line define no rule
            final String markdown = """
                    # T

                    ## Description

                    `rule:first` A paragraph.

                    1. `rule:second` An item.
                       2. `rule:third` A nested item.
                    - **`rule:fourth` Bold.**
                    See `rule:first` for details, and also
                    `rule:second` for more.

                    ```
                    `rule:in-a-code-block`
                    ```

                    ## API Schema

                    ```
                    namespace a
                    ```

                    ## Testing

                    ## Questions & Comments
                    """;

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.rules()).extracting(org.hiero.sdk.v3.metalang.ast.BehaviorRule::id)
                    .containsExactly("first", "second", "third", "fourth");
            assertThat(result.rules().get(1).location().line()).isEqualTo(7);
            assertThat(result.rules().get(1).location().column()).isEqualTo(4);
            assertThat(result.diagnostics()).isEmpty();
        }

        @Test
        void shouldIgnoreAndReportARuleOutsideOfTheDescription() {
            // GIVEN
            final String markdown = TestSpecs.markdown("namespace a\n").replace("## Questions & Comments",
                    "## Questions & Comments\n\n- `rule:open` Is this a rule?");

            // WHEN
            final ExtractionResult result = extractor.extract("a.md", markdown);

            // THEN
            assertThat(result.rules()).isEmpty();
            assertThat(ruleIds(result)).containsExactly("rule.outside-description");
        }
    }
}
