package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Every rule of the {@link Rule} catalog has a fixture directory {@code rule-fixtures/<rule-id>/} that triggers it.
 *
 * <ul>
 *   <li>{@code *.ml} files contain a plain schema and are wrapped into the canonical spec skeleton,</li>
 *   <li>{@code *.md} files are used verbatim (for document-structure rules),</li>
 *   <li>an optional {@code also.txt} lists further rule ids the fixture inevitably triggers.</li>
 * </ul>
 *
 * <p>A fixture must produce exactly the expected set of rule ids — no more, no less — so a rule that starts to fire
 * in unrelated situations is caught as well as a rule that stops firing.
 */
class RuleFixturesTest {

    private static final Path FIXTURES = fixtureRoot();

    private static Path fixtureRoot() {
        try {
            return Path.of(RuleFixturesTest.class.getResource("/rule-fixtures").toURI());
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, String> documents(final Path directory) throws IOException {
        final Map<String, String> documents = new TreeMap<>();
        try (Stream<Path> files = Files.list(directory)) {
            for (final Path file : files.sorted().toList()) {
                final String name = file.getFileName().toString();
                final String content = Files.readString(file, StandardCharsets.UTF_8);
                if (name.endsWith(".ml")) {
                    documents.put(name.replace(".ml", ".md"), TestSpecs.markdown(content));
                } else if (name.endsWith(".md")) {
                    documents.put(name, content);
                }
            }
        }
        return documents;
    }

    private static Set<String> expected(final Path directory, final String ruleId) throws IOException {
        final Set<String> expected = new TreeSet<>(Set.of(ruleId));
        final Path also = directory.resolve("also.txt");
        if (Files.exists(also)) {
            Files.readAllLines(also).stream().map(String::strip).filter(l -> !l.isEmpty()).forEach(expected::add);
        }
        return expected;
    }

    @TestFactory
    Stream<DynamicTest> everyRuleShouldBeTriggeredExactlyByItsFixture() {
        return Arrays.stream(Rule.values()).map(rule -> DynamicTest.dynamicTest(rule.id(), () -> {
            // GIVEN
            final Path directory = FIXTURES.resolve(rule.id());
            assertThat(directory).as("fixture directory for " + rule.id()).isDirectory();

            // WHEN fixtures without .ml files are read from disk (this also covers file decoding)
            final boolean rawOnly;
            try (Stream<Path> files = Files.list(directory)) {
                rawOnly = files.noneMatch(f -> f.getFileName().toString().endsWith(".ml"));
            }
            final ValidationReport report = rawOnly
                    ? new MetaLang().validate(directory)
                    : new MetaLang().validate(documents(directory));

            // THEN
            final Set<String> actual = report.diagnostics().stream().map(Diagnostic::ruleId)
                    .collect(Collectors.toCollection(TreeSet::new));
            assertThat(actual).as(report.diagnostics().toString()).isEqualTo(expected(directory, rule.id()));
            assertThat(report.diagnostics()).filteredOn(d -> d.ruleId().equals(rule.id()))
                    .allSatisfy(d -> assertThat(d.severity()).isEqualTo(rule.severity()));
        }));
    }

    @Test
    void thereShouldBeNoFixtureForAnUnknownRule() throws IOException {
        final List<String> unknown;
        try (Stream<Path> directories = Files.list(FIXTURES)) {
            unknown = directories.map(p -> p.getFileName().toString())
                    .filter(id -> Rule.byId(id).isEmpty())
                    .toList();
        }
        assertThat(unknown).as("fixtures of removed or renamed rules").isEmpty();
    }

    @Test
    void validatingAFixtureDirectoryFromDiskShouldGiveTheSameResult() {
        try {
            // GIVEN a fixture with .md files only, read through the directory API
            final Path directory = FIXTURES.resolve("doc.section-order");

            // WHEN
            final ValidationReport fromDisk = new MetaLang().validate(directory);
            final ValidationReport fromMap = new MetaLang().validate(documents(directory));

            // THEN
            assertThat(fromDisk.diagnostics()).isEqualTo(fromMap.diagnostics());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
