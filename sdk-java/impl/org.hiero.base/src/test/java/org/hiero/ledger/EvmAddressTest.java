package org.hiero.ledger;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `EvmAddress`, generated from the specs.
@DisplayName("EvmAddress")
class EvmAddressTest {

    @Test
    @DisplayName("creates an EvmAddress and returns the values")
    @Tag("testId:ledger.EvmAddress/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] bytes = bytesValue();
        final EvmAddress subject = new EvmAddress(bytes);
        assertValue(bytes, subject.bytes());
    }

    @Test
    @DisplayName("rejects null for bytes")
    @Tag("testId:ledger.EvmAddress/bytes/rejects-null")
    void shouldRejectNullBytes() {
        assertThrows(NullPointerException.class, () -> new EvmAddress((byte[]) null));
    }

    @Test
    @DisplayName("bytes: accepts the minimum number of elements")
    @Tag("testId:ledger.EvmAddress/bytes/accepts-at-minimum-size")
    void shouldAcceptBytesAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        final EvmAddress subject = new EvmAddress(value);
        assertValue(value, subject.bytes());
    }

    @Test
    @DisplayName("bytes: rejects fewer elements than the minimum")
    @Tag("testId:ledger.EvmAddress/bytes/rejects-below-minimum-size")
    void shouldRejectBytesBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}));
    }

    @Test
    @DisplayName("bytes: rejects more elements than the maximum")
    @Tag("testId:ledger.EvmAddress/bytes/rejects-above-maximum-size")
    void shouldRejectBytesAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21}));
    }

    @Test
    @DisplayName("the constructor copies bytes; the accessor returns a copy")
    @Tag("testId:ledger.EvmAddress/bytes/copies-in")
    void shouldCopyBytesIn() {
        final byte[] value = bytesValue();
        final byte[] expected = value.clone();
        final EvmAddress subject = new EvmAddress(value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytes());
        final byte[] returned = subject.bytes();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytes());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:ledger.EvmAddress/value-semantics")
    void shouldCompareByValue() {
        final EvmAddress subject = create();
        final EvmAddress other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:ledger.EvmAddress/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    @Test
    @DisplayName("fromString(value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:ledger.EvmAddress/fromString/call")
    void shouldCallFromString() throws Exception {
        try {
            assertNotNull(EvmAddress.fromString(valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("fromBytes(value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:ledger.EvmAddress/fromBytes/call")
    void shouldCallFromBytes() throws Exception {
        try {
            assertNotNull(EvmAddress.fromBytes(valueValue2()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static byte[] bytesValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    }

    private static EvmAddress create() {
        return new EvmAddress(bytesValue());
    }

    private static String valueValue() {
        return "value";
    }

    private static byte[] valueValue2() {
        return new byte[] {1, 2, 3};
    }
}
