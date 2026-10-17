package org.hiero.mirrornode.common;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NftTransfer`, generated from the specs.
@DisplayName("NftTransfer")
class NftTransferTest {

    @Test
    @DisplayName("creates a NftTransfer and returns the values")
    @Tag("testId:mirrornode.common.NftTransfer/create")
    void shouldCreateAndReturnTheValues() {
        final boolean isApproval = isApprovalValue();
        final @Nullable NftTransferParties parties = partiesValue();
        final long serialNumber = serialNumberValue();
        final NftTransfer subject = new NftTransfer(isApproval, parties, serialNumber);
        assertValue(isApproval, subject.isApproval());
        assertValue(parties, subject.parties());
        assertValue(serialNumber, subject.serialNumber());
    }

    @Test
    @DisplayName("serialNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.common.NftTransfer/serialNumber/accepts-at-minimum")
    void shouldAcceptSerialNumberAtMinimum() {
        final long value = -9223372036854775808L;
        final NftTransfer subject = new NftTransfer(isApprovalValue(), partiesValue(), value);
        assertValue(value, subject.serialNumber());
    }

    @Test
    @DisplayName("serialNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.common.NftTransfer/serialNumber/accepts-at-maximum")
    void shouldAcceptSerialNumberAtMaximum() {
        final long value = 9223372036854775807L;
        final NftTransfer subject = new NftTransfer(isApprovalValue(), partiesValue(), value);
        assertValue(value, subject.serialNumber());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.common.NftTransfer/value-semantics")
    void shouldCompareByValue() {
        final NftTransfer subject = create();
        final NftTransfer other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static boolean isApprovalValue() {
        return true;
    }

    private static NftTransferParties partiesValue() {
        return new NftTransferParties(new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), new Address(0L, 0L, "", 1001L));
    }

    private static long serialNumberValue() {
        return 1L;
    }

    private static NftTransfer create() {
        return new NftTransfer(isApprovalValue(), partiesValue(), serialNumberValue());
    }
}
