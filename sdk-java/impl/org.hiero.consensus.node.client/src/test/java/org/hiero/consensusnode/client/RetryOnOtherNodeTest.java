package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `RetryOnOtherNode`, generated from the specs.
@DisplayName("RetryOnOtherNode")
class RetryOnOtherNodeTest {

    @Test
    @DisplayName("creates a RetryOnOtherNode and returns the values")
    @Tag("testId:consensusnode.client.RetryOnOtherNode/create")
    void shouldCreateAndReturnTheValues() {
        final Duration delay = delayValue();
        final RetryOnOtherNode subject = new RetryOnOtherNode(delay);
        assertValue(delay, subject.delay());
    }

    @Test
    @DisplayName("rejects null for delay")
    @Tag("testId:consensusnode.client.RetryOnOtherNode/delay/rejects-null")
    void shouldRejectNullDelay() {
        assertThrows(NullPointerException.class, () -> new RetryOnOtherNode((Duration) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.client.RetryOnOtherNode/value-semantics")
    void shouldCompareByValue() {
        final RetryOnOtherNode subject = create();
        final RetryOnOtherNode other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Duration delayValue() {
        return Duration.ofSeconds(1L);
    }

    private static RetryOnOtherNode create() {
        return new RetryOnOtherNode(delayValue());
    }
}
