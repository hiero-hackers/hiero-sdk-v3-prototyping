package org.hiero.sdk.v3.metalang.generator.python;

/**
 * Signals that a declaration has no Python form yet. The declaration is left out and reported as deferred, so the
 * generated package always imports and type-checks.
 */
final class PyGap extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the gap.
     *
     * @param reason why the declaration cannot be generated
     */
    PyGap(final String reason) {
        super(reason);
    }
}
