package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TssData`, generated from the specs.
@DisplayName("TssData")
class TssDataTest {

    @Test
    @DisplayName("creates a TssData and returns the values")
    @Tag("testId:blocknode.status.TssData/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] ledgerId = ledgerIdValue();
        final byte[] wrapsVerificationKey = wrapsVerificationKeyValue();
        final List<TssRosterEntry> currentRoster = currentRosterValue();
        final long validFromBlock = validFromBlockValue();
        final TssData subject = new TssData(ledgerId, wrapsVerificationKey, currentRoster, validFromBlock);
        assertValue(ledgerId, subject.ledgerId());
        assertValue(wrapsVerificationKey, subject.wrapsVerificationKey());
        assertValue(currentRoster, subject.currentRoster());
        assertValue(validFromBlock, subject.validFromBlock());
    }

    @Test
    @DisplayName("rejects null for ledgerId")
    @Tag("testId:blocknode.status.TssData/ledgerId/rejects-null")
    void shouldRejectNullLedgerId() {
        assertThrows(NullPointerException.class, () -> new TssData((byte[]) null, wrapsVerificationKeyValue(), currentRosterValue(), validFromBlockValue()));
    }

    @Test
    @DisplayName("the constructor copies ledgerId; the accessor returns a copy")
    @Tag("testId:blocknode.status.TssData/ledgerId/copies-in")
    void shouldCopyLedgerIdIn() {
        final byte[] value = ledgerIdValue();
        final byte[] expected = value.clone();
        final TssData subject = new TssData(value, wrapsVerificationKeyValue(), currentRosterValue(), validFromBlockValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.ledgerId());
        final byte[] returned = subject.ledgerId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.ledgerId());
    }

    @Test
    @DisplayName("rejects null for wrapsVerificationKey")
    @Tag("testId:blocknode.status.TssData/wrapsVerificationKey/rejects-null")
    void shouldRejectNullWrapsVerificationKey() {
        assertThrows(NullPointerException.class, () -> new TssData(ledgerIdValue(), (byte[]) null, currentRosterValue(), validFromBlockValue()));
    }

    @Test
    @DisplayName("the constructor copies wrapsVerificationKey; the accessor returns a copy")
    @Tag("testId:blocknode.status.TssData/wrapsVerificationKey/copies-in")
    void shouldCopyWrapsVerificationKeyIn() {
        final byte[] value = wrapsVerificationKeyValue();
        final byte[] expected = value.clone();
        final TssData subject = new TssData(ledgerIdValue(), value, currentRosterValue(), validFromBlockValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.wrapsVerificationKey());
        final byte[] returned = subject.wrapsVerificationKey();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.wrapsVerificationKey());
    }

    @Test
    @DisplayName("rejects null for currentRoster")
    @Tag("testId:blocknode.status.TssData/currentRoster/rejects-null")
    void shouldRejectNullCurrentRoster() {
        assertThrows(NullPointerException.class, () -> new TssData(ledgerIdValue(), wrapsVerificationKeyValue(), (List<TssRosterEntry>) null, validFromBlockValue()));
    }

    @Test
    @DisplayName("the constructor copies currentRoster; the accessor returns an unmodifiable collection")
    @Tag("testId:blocknode.status.TssData/currentRoster/copies-in")
    void shouldCopyCurrentRosterIn() {
        final List<TssRosterEntry> value = new ArrayList<>(currentRosterValue());
        final List<TssRosterEntry> expected = List.copyOf(value);
        final TssData subject = new TssData(ledgerIdValue(), wrapsVerificationKeyValue(), value, validFromBlockValue());
        value.clear();
        assertValue(expected, subject.currentRoster());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.currentRoster()).clear());
    }

    @Test
    @DisplayName("validFromBlock: accepts the minimum (0)")
    @Tag("testId:blocknode.status.TssData/validFromBlock/accepts-at-minimum")
    void shouldAcceptValidFromBlockAtMinimum() {
        final long value = 0L;
        final TssData subject = new TssData(ledgerIdValue(), wrapsVerificationKeyValue(), currentRosterValue(), value);
        assertValue(value, subject.validFromBlock());
    }

    @Test
    @DisplayName("validFromBlock: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.TssData/validFromBlock/accepts-at-maximum")
    void shouldAcceptValidFromBlockAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final TssData subject = new TssData(ledgerIdValue(), wrapsVerificationKeyValue(), currentRosterValue(), value);
        assertValue(value, subject.validFromBlock());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.TssData/value-semantics")
    void shouldCompareByValue() {
        final TssData subject = create();
        final TssData other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static byte[] ledgerIdValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] wrapsVerificationKeyValue() {
        return new byte[] {1, 2, 3};
    }

    private static List<TssRosterEntry> currentRosterValue() {
        return List.of(new TssRosterEntry(1L, 1L, new byte[] {1, 2, 3}));
    }

    private static long validFromBlockValue() {
        return 1L;
    }

    private static TssData create() {
        return new TssData(ledgerIdValue(), wrapsVerificationKeyValue(), currentRosterValue(), validFromBlockValue());
    }
}
