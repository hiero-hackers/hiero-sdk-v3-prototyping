package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TssRosterEntry`, generated from the specs.
@DisplayName("TssRosterEntry")
class TssRosterEntryTest {

    @Test
    @DisplayName("creates a TssRosterEntry and returns the values")
    @Tag("testId:blocknode.status.TssRosterEntry/create")
    void shouldCreateAndReturnTheValues() {
        final long nodeId = nodeIdValue();
        final long weight = weightValue();
        final byte[] schnorrPublicKey = schnorrPublicKeyValue();
        final TssRosterEntry subject = new TssRosterEntry(nodeId, weight, schnorrPublicKey);
        assertValue(nodeId, subject.nodeId());
        assertValue(weight, subject.weight());
        assertValue(schnorrPublicKey, subject.schnorrPublicKey());
    }

    @Test
    @DisplayName("nodeId: accepts the minimum (0)")
    @Tag("testId:blocknode.status.TssRosterEntry/nodeId/accepts-at-minimum")
    void shouldAcceptNodeIdAtMinimum() {
        final long value = 0L;
        final TssRosterEntry subject = new TssRosterEntry(value, weightValue(), schnorrPublicKeyValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.TssRosterEntry/nodeId/accepts-at-maximum")
    void shouldAcceptNodeIdAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final TssRosterEntry subject = new TssRosterEntry(value, weightValue(), schnorrPublicKeyValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("weight: accepts the minimum (0)")
    @Tag("testId:blocknode.status.TssRosterEntry/weight/accepts-at-minimum")
    void shouldAcceptWeightAtMinimum() {
        final long value = 0L;
        final TssRosterEntry subject = new TssRosterEntry(nodeIdValue(), value, schnorrPublicKeyValue());
        assertValue(value, subject.weight());
    }

    @Test
    @DisplayName("weight: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.TssRosterEntry/weight/accepts-at-maximum")
    void shouldAcceptWeightAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final TssRosterEntry subject = new TssRosterEntry(nodeIdValue(), value, schnorrPublicKeyValue());
        assertValue(value, subject.weight());
    }

    @Test
    @DisplayName("rejects null for schnorrPublicKey")
    @Tag("testId:blocknode.status.TssRosterEntry/schnorrPublicKey/rejects-null")
    void shouldRejectNullSchnorrPublicKey() {
        assertThrows(NullPointerException.class, () -> new TssRosterEntry(nodeIdValue(), weightValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies schnorrPublicKey; the accessor returns a copy")
    @Tag("testId:blocknode.status.TssRosterEntry/schnorrPublicKey/copies-in")
    void shouldCopySchnorrPublicKeyIn() {
        final byte[] value = schnorrPublicKeyValue();
        final byte[] expected = value.clone();
        final TssRosterEntry subject = new TssRosterEntry(nodeIdValue(), weightValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.schnorrPublicKey());
        final byte[] returned = subject.schnorrPublicKey();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.schnorrPublicKey());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.TssRosterEntry/value-semantics")
    void shouldCompareByValue() {
        final TssRosterEntry subject = create();
        final TssRosterEntry other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long nodeIdValue() {
        return 1L;
    }

    private static long weightValue() {
        return 1L;
    }

    private static byte[] schnorrPublicKeyValue() {
        return new byte[] {1, 2, 3};
    }

    private static TssRosterEntry create() {
        return new TssRosterEntry(nodeIdValue(), weightValue(), schnorrPublicKeyValue());
    }
}
