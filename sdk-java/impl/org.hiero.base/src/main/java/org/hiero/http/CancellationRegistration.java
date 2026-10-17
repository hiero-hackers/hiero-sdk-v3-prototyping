package org.hiero.http;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hiero.sdk.annotation.ThreadSafe;

/// The handle of a registered callback, so that an observer can stop observing.
public final class CancellationRegistration {

    private final Runnable remover;
    private final AtomicBoolean released = new AtomicBoolean();

    /// Creates a new `CancellationRegistration`.
    public CancellationRegistration() {
        this(() -> { });
    }

    /// Creates a registration that runs `remover` on its first release.
    CancellationRegistration(final Runnable remover) {
        this.remover = Objects.requireNonNull(remover, "remover must not be null");
    }

    /// Removes the callback. Idempotent.
    @ThreadSafe(group = "cancellation")
    public void release() {
        if (released.compareAndSet(false, true)) {
            remover.run();
        }
    }
}
