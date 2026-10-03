package org.hiero.sdk.v3.metalang.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleTest {

    @Test
    void ruleIdsShouldBeUniqueAndWellFormed() {
        final List<String> ids = Arrays.stream(Rule.values()).map(Rule::id).toList();
        assertThat(ids).doesNotHaveDuplicates();
        assertThat(ids).allMatch(id -> id.matches("[a-z]+\\.[a-z]+(-[a-z]+)*"), "category.kebab-name");
    }

    @Test
    void everyRuleShouldBeDocumented() {
        for (final Rule rule : Rule.values()) {
            assertThat(rule.description()).as(rule.id()).isNotBlank();
            assertThat(rule.reference()).as(rule.id()).contains(".md");
            assertThat(rule.severity()).isNotNull();
        }
    }

    @Test
    void shouldLookUpRulesById() {
        assertThat(Rule.byId("naming.type")).contains(Rule.NAMING_TYPE);
        assertThat(Rule.byId("nope")).isEmpty();
        assertThatThrownBy(() -> Rule.byId(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void diagnosticsShouldOrderByLocationThenRuleThenMessage() {
        // GIVEN
        final SourceLocation a1 = new SourceLocation("a.md", 1, 1);
        final Diagnostic first = new Diagnostic(Severity.INFO, "a.rule", "m", a1);
        final Diagnostic second = new Diagnostic(Severity.ERROR, "b.rule", "a", a1);
        final Diagnostic third = new Diagnostic(Severity.ERROR, "b.rule", "b", a1);
        final Diagnostic fourth = new Diagnostic(Severity.ERROR, "a.rule", "m", new SourceLocation("a.md", 1, 2));
        final Diagnostic fifth = new Diagnostic(Severity.ERROR, "a.rule", "m", new SourceLocation("a.md", 2, 1));
        final Diagnostic sixth = new Diagnostic(Severity.ERROR, "a.rule", "m", new SourceLocation("b.md", 1, 1));
        final DiagnosticCollector collector = new DiagnosticCollector();
        collector.addAll(List.of(sixth, fifth, fourth, third, second, first, first));

        // WHEN / THEN duplicates are removed
        assertThat(collector.sorted()).containsExactly(first, second, third, fourth, fifth, sixth);
        assertThat(first.toString()).isEqualTo("a.md:1:1: INFO [a.rule] m");
    }

    @Test
    void shouldRejectNulls() {
        final SourceLocation location = new SourceLocation("a", 1, 1);
        assertThatThrownBy(() -> new SourceLocation(null, 1, 1)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Diagnostic(null, "r", "m", location)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DiagnosticCollector().report(null, "m", location))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DiagnosticCollector().addAll(null)).isInstanceOf(NullPointerException.class);
    }
}
