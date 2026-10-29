package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TransportFailure`, generated from the specs.
@DisplayName("TransportFailure")
class TransportFailureTest {

    @Test
    @DisplayName("creates a TransportFailure and returns the values")
    @Tag("testId:consensusnode.client.TransportFailure/create")
    void shouldCreateAndReturnTheValues() {
        final TransportFailureKind kind = kindValue();
        final String message = messageValue();
        final TransportFailure subject = new TransportFailure(kind, message);
        assertValue(kind, subject.kind());
        assertValue(message, subject.message());
    }

    @Test
    @DisplayName("rejects null for kind")
    @Tag("testId:consensusnode.client.TransportFailure/kind/rejects-null")
    void shouldRejectNullKind() {
        assertThrows(NullPointerException.class, () -> new TransportFailure((TransportFailureKind) null, messageValue()));
    }

    @Test
    @DisplayName("rejects null for message")
    @Tag("testId:consensusnode.client.TransportFailure/message/rejects-null")
    void shouldRejectNullMessage() {
        assertThrows(NullPointerException.class, () -> new TransportFailure(kindValue(), (String) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.client.TransportFailure/value-semantics")
    void shouldCompareByValue() {
        final TransportFailure subject = create();
        final TransportFailure other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static TransportFailureKind kindValue() {
        return TransportFailureKind.CONNECTION;
    }

    private static String messageValue() {
        return "value";
    }

    private static TransportFailure create() {
        return new TransportFailure(kindValue(), messageValue());
    }
}
