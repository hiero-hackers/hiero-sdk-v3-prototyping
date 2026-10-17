package org.hiero.enterprise.service.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hiero.sdk.common.HieroStream;

/// A stream that fails with a terminal error as soon as it is iterated.
///
/// @param <T> the type of the items
public final class FailedStream<T> implements HieroStream<T> {

    private final RuntimeException error;

    private final AtomicBoolean iterated = new AtomicBoolean();

    /// Creates a stream that fails with the given error.
    ///
    /// @param error the terminal error
    public FailedStream(final RuntimeException error) {
        this.error = Objects.requireNonNull(error, "error must not be null");
    }

    @Override
    public Iterator<T> iterator() {
        if (!iterated.compareAndSet(false, true)) {
            throw new IllegalStateException("The stream can only be iterated once");
        }
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                throw error;
            }

            @Override
            public T next() {
                throw new NoSuchElementException(error.getMessage());
            }
        };
    }

    @Override
    public void close() {
        // nothing to release
    }
}
