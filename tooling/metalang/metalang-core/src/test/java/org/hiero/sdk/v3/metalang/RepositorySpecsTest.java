package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Runs the tooling against the real content of this repository: every spec must be syntactically
 * valid (guideline examples are covered by {@link GuidelineExamplesTest}).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositorySpecsTest {

    private static final Path SPEC_ROOT = Path.of(System.getProperty("spec.root", "../../../spec"));

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
}
