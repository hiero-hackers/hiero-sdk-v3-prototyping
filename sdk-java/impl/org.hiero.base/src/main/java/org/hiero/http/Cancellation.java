package org.hiero.http;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.hiero.sdk.annotation.ThreadSafe;

/// A caller's request to end an exchange early. Only observed, never fired: a client that receives
/// one cannot cancel the caller's operation. Obtain one from a CancellationSource, or none() when
/// there is nothing to cancel.
public final class Cancellation {

    private static final Cancellation NONE = new Cancellation(false);

    private final boolean cancellable;
    private final Object lock = new Object();
    private final Set<Runnable> callbacks = new LinkedHashSet<>();
    private volatile boolean cancelled;

    /// Creates a new `Cancellation`.
    public Cancellation() {
        this(true);
    }

    private Cancellation(final boolean cancellable) {
        this.cancellable = cancellable;
    }

    /// Whether cancellation has been requested. True before any onCancel callback runs.
    @ThreadSafe(group = "cancellation")
    public boolean isCancelled() {
        return cancelled;
    }

    /// Registers a callback that runs when cancellation is requested — immediately if it already
    /// has been. Release the registration when the exchange ends, or the callback leaks.
    @ThreadSafe(group = "cancellation")
    public CancellationRegistration onCancel(final Runnable callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        if (!cancellable) {
            return new CancellationRegistration();
        }
        // a wrapper per registration, so that the same Runnable can be registered and released twice
        final Runnable registered = callback::run;
        synchronized (lock) {
            if (!cancelled) {
                callbacks.add(registered);
                return new CancellationRegistration(() -> {
                    synchronized (lock) {
                        callbacks.remove(registered);
                    }
                });
            }
        }
        runQuietly(callback);
        return new CancellationRegistration();
    }

    /// The cancellation that never fires, for a call nobody intends to cancel.
    public static Cancellation none() {
        return NONE;
    }

    /// Fires this cancellation: marks it cancelled and runs the registered callbacks synchronously on the calling
    /// thread. Idempotent; the cancellation of `none()` never fires.
    void fire() {
        if (!cancellable) {
            return;
        }
        final List<Runnable> toRun;
        synchronized (lock) {
            if (cancelled) {
                return;
            }
            cancelled = true;
            toRun = new ArrayList<>(callbacks);
            callbacks.clear();
        }
        toRun.forEach(Cancellation::runQuietly);
    }

    private static void runQuietly(final Runnable callback) {
        try {
            callback.run();
        } catch (final RuntimeException e) {
            // a callback that throws neither stops the others nor propagates to the caller of cancel()
            final Thread thread = Thread.currentThread();
            thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
        }
    }
}
