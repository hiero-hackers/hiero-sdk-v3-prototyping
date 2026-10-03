package org.hiero.sdk.v3.metalang.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mutable, single-threaded collector for {@link Diagnostic}s.
 */
public final class DiagnosticCollector {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    /**
     * Adds a finding of the given rule with the rule's severity.
     *
     * @param rule     the rule
     * @param message  human-readable message
     * @param location where the finding applies
     */
    public void report(final Rule rule, final String message, final SourceLocation location) {
        Objects.requireNonNull(rule, "rule must not be null");
        diagnostics.add(new Diagnostic(rule.severity(), rule.id(), message, location));
    }

    /**
     * Adds all given diagnostics.
     *
     * @param others diagnostics to add
     */
    public void addAll(final List<Diagnostic> others) {
        Objects.requireNonNull(others, "others must not be null");
        diagnostics.addAll(others);
    }

    /**
     * Returns all collected diagnostics in deterministic order.
     *
     * @return sorted, unmodifiable list of diagnostics
     */
    public List<Diagnostic> sorted() {
        return diagnostics.stream().sorted().distinct().toList();
    }
}
