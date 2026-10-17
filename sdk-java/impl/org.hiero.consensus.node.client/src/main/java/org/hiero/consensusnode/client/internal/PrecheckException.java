package org.hiero.consensusnode.client.internal;

/// A node rejected a transaction or a query before consensus (precheck). The specs have no error type that carries
/// the status yet (see the `## Questions & Comments` of `transactions.md`), so the status is part of the message in
/// a fixed form, `precheck failed: <STATUS>`, which callers outside the SDK (the TCK runtime) can read.
public final class PrecheckException extends IllegalStateException {

    /// The beginning of the message, followed by the name of the status.
    public static final String PREFIX = "precheck failed: ";

    private static final long serialVersionUID = 1L;

    /// The status.
    private final transient HapiTransactionStatus status;

    /// Creates the error.
    ///
    /// @param status the status the node answered with
    public PrecheckException(final HapiTransactionStatus status) {
        super(PREFIX + status.name());
        this.status = status;
    }

    /// The status the node answered with.
    ///
    /// @return the status
    public HapiTransactionStatus status() {
        return status;
    }
}
