package org.hiero.sdk.v3.metalang;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Result of validating a set of spec files.
 *
 * @param model       the semantic model of all specs that could be parsed
 * @param diagnostics all findings (document structure, syntax, semantics) in deterministic order
 * @param specCount   number of files that contained a schema
 */
public record ValidationReport(SpecModel model, List<Diagnostic> diagnostics, int specCount) {

    /**
     * Creates a report.
     *
     * @param model       the model
     * @param diagnostics the findings
     * @param specCount   the number of spec files
     */
    public ValidationReport {
        Objects.requireNonNull(model, "model must not be null");
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics must not be null"));
    }

    /**
     * Returns whether at least one finding has severity {@link Severity#ERROR}.
     *
     * @return {@code true} if there are errors
     */
    public boolean hasErrors() {
        return count(Severity.ERROR) > 0;
    }

    /**
     * Counts the findings of a severity.
     *
     * @param severity the severity
     * @return the number of findings
     */
    public long count(final Severity severity) {
        return diagnostics.stream().filter(d -> d.severity() == severity).count();
    }

    /**
     * Returns the number of findings per rule id, sorted by rule id.
     *
     * @return rule id to count
     */
    public SortedMap<String, Long> countByRule() {
        final SortedMap<String, Long> result = new TreeMap<>();
        for (final Diagnostic diagnostic : diagnostics) {
            result.merge(diagnostic.ruleId(), 1L, Long::sum);
        }
        return result;
    }

    /**
     * Returns the findings of a single rule.
     *
     * @param ruleId the rule id
     * @return the findings in report order
     */
    public List<Diagnostic> byRule(final String ruleId) {
        return diagnostics.stream().filter(d -> d.ruleId().equals(ruleId)).toList();
    }

    /**
     * Returns the severity of every rule that occurs in the report.
     *
     * @return rule id to severity
     */
    public Map<String, Severity> severities() {
        final Map<String, Severity> result = new TreeMap<>();
        diagnostics.forEach(d -> result.putIfAbsent(d.ruleId(), d.severity()));
        return result;
    }
}
