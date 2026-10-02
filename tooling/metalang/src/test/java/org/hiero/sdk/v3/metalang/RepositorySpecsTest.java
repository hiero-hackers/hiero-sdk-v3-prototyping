package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.parser.ParseResult;
import org.hiero.sdk.v3.metalang.parser.SchemaParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Runs the tooling against the real content of this repository: every spec must be syntactically
 * valid, and every complete example in the guideline must parse.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositorySpecsTest {

    private static final Path SPEC_ROOT = Path.of(System.getProperty("spec.root", "../../spec"));
    private static final Path GUIDELINE = SPEC_ROOT.resolve("../guidelines/api-guideline.md");

    private final ValidationReport report = new MetaLang().validate(SPEC_ROOT);

    @Test
    void everySpecShouldBeSyntacticallyValid() {
        assertThat(report.byRule("syntax.error")).as("syntax errors in spec/").isEmpty();
        assertThat(report.specCount()).isGreaterThan(40);
        assertThat(report.model().files()).hasSize(report.specCount());
    }

    @Test
    void reportShouldBeDeterministic() {
        final ValidationReport second = new MetaLang().validate(SPEC_ROOT);
        assertThat(second.diagnostics()).isEqualTo(report.diagnostics());
    }

    @Test
    void everyDiagnosticShouldPointIntoTheMarkdownFile() throws IOException {
        for (final Diagnostic diagnostic : report.diagnostics()) {
            final List<String> lines = Files.readAllLines(SPEC_ROOT.resolve(diagnostic.location().file()));
            assertThat(diagnostic.location().line()).as(diagnostic.toString()).isBetween(1, lines.size());
        }
    }

    @Test
    void everyNamespaceExampleOfTheGuidelineShouldParse() throws IOException {
        // GIVEN all fenced blocks of the guideline that start with a namespace declaration
        final String guideline = Files.readString(GUIDELINE);
        final Matcher matcher = Pattern.compile("```\\n(namespace [\\s\\S]*?)```").matcher(guideline);
        final List<String> examples = new ArrayList<>();
        while (matcher.find()) {
            // the guideline uses "..." as an ellipsis for omitted content
            examples.add(matcher.group(1).replace("\n...\n", "\n"));
        }

        // WHEN / THEN
        assertThat(examples).hasSizeGreaterThanOrEqualTo(5);
        final SchemaParser parser = new SchemaParser();
        for (final String example : examples) {
            final ParseResult result = parser.parse("api-guideline.md", example);
            assertThat(result.diagnostics()).as(example).isEmpty();
        }
    }
}
