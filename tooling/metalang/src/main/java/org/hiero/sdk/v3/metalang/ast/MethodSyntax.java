package org.hiero.sdk.v3.metalang.ast;

/**
 * The syntactic form a method was declared in.
 */
public enum MethodSyntax {
    /** {@code ReturnType name(params)} — the form defined by the guideline. */
    CLASSIC,
    /** {@code name(params): ReturnType} — non-standard variant. */
    TRAILING_RETURN,
    /** {@code name(params)} without a return type — non-standard variant; treated as {@code void}. */
    MISSING_RETURN
}
