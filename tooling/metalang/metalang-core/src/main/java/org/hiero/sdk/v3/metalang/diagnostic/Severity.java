package org.hiero.sdk.v3.metalang.diagnostic;

/**
 * Severity of a {@link Diagnostic}.
 */
public enum Severity {
    /** The spec violates the meta-language or one of its rules; tooling must not continue with it. */
    ERROR,
    /** The spec deviates from a guideline recommendation or uses a non-standard syntax variant. */
    WARNING,
    /** Informational finding, e.g. a construct the guideline does not (yet) describe. */
    INFO
}
