package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `TokenReference`, generated from the specs.
@DisplayName("TokenReference")
class TokenReferenceTest {

    @Test
    @DisplayName("creates a TokenReference and returns the values")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final @Nullable Long serial = serialValue();
        final TokenReference subject = new TokenReference(tokenId, serial);
        assertValue(tokenId, subject.tokenId());
        assertValue(serial, subject.serial());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenReference((Address) null, serialValue()));
    }

    @Test
    @DisplayName("serial: accepts the minimum (-9223372036854775808)")
    void shouldAcceptSerialAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final TokenReference subject = new TokenReference(tokenIdValue(), value);
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("serial: accepts the maximum (9223372036854775807)")
    void shouldAcceptSerialAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenReference subject = new TokenReference(tokenIdValue(), value);
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final TokenReference subject = create();
        final TokenReference other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long serialValue() {
        return 1L;
    }

    private static TokenReference create() {
        return new TokenReference(tokenIdValue(), serialValue());
    }
}
