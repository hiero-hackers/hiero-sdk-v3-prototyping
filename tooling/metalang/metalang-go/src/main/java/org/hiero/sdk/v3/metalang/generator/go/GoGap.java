package org.hiero.sdk.v3.metalang.generator.go;

/**
 * Signals that a declaration has no Go form yet. The generator catches it, leaves the declaration out and reports
 * it as deferred, rather than emitting code that does not compile.
 */
final class GoGap extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a gap.
     *
     * @param reason why the declaration cannot be generated
     */
    GoGap(final String reason) {
        super(reason);
    }
}
