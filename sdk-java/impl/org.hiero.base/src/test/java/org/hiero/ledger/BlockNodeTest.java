package org.hiero.ledger;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockNode`, generated from the specs.
@DisplayName("BlockNode")
class BlockNodeTest {

    @Test
    @DisplayName("creates a BlockNode and returns the values")
    @Tag("testId:ledger.BlockNode/create")
    void shouldCreateAndReturnTheValues() {
        final String host = hostValue();
        final int port = portValue();
        final boolean requiresTls = requiresTlsValue();
        final BlockNode subject = new BlockNode(host, port, requiresTls);
        assertValue(host, subject.host());
        assertValue(port, subject.port());
        assertValue(requiresTls, subject.requiresTls());
    }

    @Test
    @DisplayName("rejects null for host")
    @Tag("testId:ledger.BlockNode/host/rejects-null")
    void shouldRejectNullHost() {
        assertThrows(NullPointerException.class, () -> new BlockNode((String) null, portValue(), requiresTlsValue()));
    }

    @Test
    @DisplayName("host: accepts a value with the minimum length")
    @Tag("testId:ledger.BlockNode/host/accepts-at-minimum-length")
    void shouldAcceptHostAtMinimumLength() {
        final String value = "v";
        final BlockNode subject = new BlockNode(value, portValue(), requiresTlsValue());
        assertValue(value, subject.host());
    }

    @Test
    @DisplayName("host: rejects a value shorter than the minimum length")
    @Tag("testId:ledger.BlockNode/host/rejects-below-minimum-length")
    void shouldRejectHostBelowMinimumLength() {
        assertThrows(IllegalArgumentException.class, () -> new BlockNode("", portValue(), requiresTlsValue()));
    }

    @Test
    @DisplayName("port: accepts the minimum (0)")
    @Tag("testId:ledger.BlockNode/port/accepts-at-minimum")
    void shouldAcceptPortAtMinimum() {
        final int value = 0;
        final BlockNode subject = new BlockNode(hostValue(), value, requiresTlsValue());
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: accepts the maximum (65535)")
    @Tag("testId:ledger.BlockNode/port/accepts-at-maximum")
    void shouldAcceptPortAtMaximum() {
        final int value = 65535;
        final BlockNode subject = new BlockNode(hostValue(), value, requiresTlsValue());
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: rejects a value below the minimum (-1)")
    @Tag("testId:ledger.BlockNode/port/rejects-below-minimum")
    void shouldRejectPortBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new BlockNode(hostValue(), -1, requiresTlsValue()));
    }

    @Test
    @DisplayName("port: rejects a value above the maximum (65536)")
    @Tag("testId:ledger.BlockNode/port/rejects-above-maximum")
    void shouldRejectPortAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new BlockNode(hostValue(), 65536, requiresTlsValue()));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:ledger.BlockNode/value-semantics")
    void shouldCompareByValue() {
        final BlockNode subject = create();
        final BlockNode other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static String hostValue() {
        return "value";
    }

    private static int portValue() {
        return 1;
    }

    private static boolean requiresTlsValue() {
        return true;
    }

    private static BlockNode create() {
        return new BlockNode(hostValue(), portValue(), requiresTlsValue());
    }

    private static BlockNode defaultSubject() {
        return new BlockNode("127.0.0.1", 40840, false);
    }
}
