package org.hiero.sdk.v3.metalang.diagnostic;

import java.util.Comparator;
import java.util.Objects;

/**
 * A single finding produced by the extractor, parser, resolver or validator.
 *
 * <p>Diagnostics are value objects with a total, deterministic order (location, then rule id, then
 * message) so that reports are byte-for-byte reproducible.
 *
 * @param severity the severity
 * @param ruleId   stable identifier of the rule that produced the finding (e.g. {@code naming.type})
 * @param message  human-readable message
 * @param location where the finding applies
 */
public record Diagnostic(Severity severity, String ruleId, String message, SourceLocation location)
        implements Comparable<Diagnostic> {

    private static final Comparator<Diagnostic> ORDER = Comparator
            .comparing(Diagnostic::location)
            .thenComparing(Diagnostic::ruleId)
            .thenComparing(Diagnostic::message);

    /**
     * Creates a new diagnostic.
     *
     * @param severity the severity
     * @param ruleId   stable rule identifier
     * @param message  human-readable message
     * @param location where the finding applies
     */
    public Diagnostic {
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(ruleId, "ruleId must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    @Override
    public int compareTo(final Diagnostic other) {
        return ORDER.compare(this, other);
    }

    @Override
    public String toString() {
        return location + ": " + severity + " [" + ruleId + "] " + message;
    }
}
