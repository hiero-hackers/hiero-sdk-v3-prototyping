package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `RetryOnSameNode`, generated from the specs.
@DisplayName("RetryOnSameNode")
class RetryOnSameNodeTest {

    @Test
    @DisplayName("creates a RetryOnSameNode and returns the values")
    @Tag("testId:consensusnode.client.RetryOnSameNode/create")
    void shouldCreateAndReturnTheValues() {
        final Duration delay = delayValue();
        final RetryOnSameNode subject = new RetryOnSameNode(delay);
        assertValue(delay, subject.delay());
    }

    @Test
    @DisplayName("rejects null for delay")
    @Tag("testId:consensusnode.client.RetryOnSameNode/delay/rejects-null")
    void shouldRejectNullDelay() {
        assertThrows(NullPointerException.class, () -> new RetryOnSameNode((Duration) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.client.RetryOnSameNode/value-semantics")
    void shouldCompareByValue() {
        final RetryOnSameNode subject = create();
        final RetryOnSameNode other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Duration delayValue() {
        return Duration.ofSeconds(1L);
    }

    private static RetryOnSameNode create() {
        return new RetryOnSameNode(delayValue());
    }
}
