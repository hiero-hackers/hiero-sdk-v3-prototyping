package org.hiero.http;

import java.util.Objects;
import org.hiero.sdk.annotation.ThreadSafe;

/// Owns the right to cancel. Kept by whoever starts a call; only the Cancellation is handed out.
public record CancellationSource(Cancellation cancellation) {

    /// Creates a new `CancellationSource`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public CancellationSource {
        Objects.requireNonNull(cancellation, "cancellation must not be null");
    }

    /// Requests cancellation. Idempotent; a second call does nothing. Runs the registered
    /// callbacks synchronously on the calling thread.
    @ThreadSafe(group = "cancellation")
    public void cancel() {
        cancellation.fire();
    }

    /// Creates a source with a new cancellation that has not fired.
    public static CancellationSource create() {
        return new CancellationSource(new Cancellation());
    }
}
