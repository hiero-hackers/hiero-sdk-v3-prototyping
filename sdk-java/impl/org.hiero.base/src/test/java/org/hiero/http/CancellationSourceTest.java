package org.hiero.http;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `CancellationSource`, generated from the specs.
@DisplayName("CancellationSource")
class CancellationSourceTest {

    @Test
    @DisplayName("creates a CancellationSource and returns the values")
    void shouldCreateAndReturnTheValues() {
        final Cancellation cancellation = cancellationValue();
        final CancellationSource subject = new CancellationSource(cancellation);
        assertValue(cancellation, subject.cancellation());
    }

    @Test
    @DisplayName("rejects null for cancellation")
    void shouldRejectNullCancellation() {
        assertThrows(NullPointerException.class, () -> new CancellationSource((Cancellation) null));
    }

    @Test
    @DisplayName("cancel() can be called")
    void shouldCallCancel() {
        create().cancel();
    }

    @Test
    @DisplayName("create() can be called and returns a value")
    void shouldCallCreate() {
        assertNotNull(CancellationSource.create());
    }

    private static Cancellation cancellationValue() {
        return new Cancellation();
    }

    private static CancellationSource create() {
        return new CancellationSource(cancellationValue());
    }
}
