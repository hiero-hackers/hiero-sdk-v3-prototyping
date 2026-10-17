package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NftTransfer`, generated from the specs.
@DisplayName("NftTransfer")
class NftTransferTest {

    @Test
    @DisplayName("creates a NftTransfer and returns the values")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final long serial = serialValue();
        final AccountId sender = senderValue();
        final AccountId receiver = receiverValue();
        final boolean approved = approvedValue();
        final NftTransfer subject = new NftTransfer(tokenId, serial, sender, receiver, approved);
        assertValue(tokenId, subject.tokenId());
        assertValue(serial, subject.serial());
        assertValue(sender, subject.sender());
        assertValue(receiver, subject.receiver());
        assertValue(approved, subject.approved());
    }

    @Test
    @DisplayName("creates a NftTransfer with the default values")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final NftTransfer subject = new NftTransfer(tokenIdValue(), serialValue(), senderValue(), receiverValue());
        assertValue(false, subject.approved());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address tokenId = tokenIdValue();
        final long serial = serialValue();
        final AccountId sender = senderValue();
        final AccountId receiver = receiverValue();
        final boolean approved = approvedValue();
        final NftTransfer subject = NftTransfer.builder()
                .tokenId(tokenId)
                .serial(serial)
                .sender(sender)
                .receiver(receiver)
                .approved(approved)
                .build();
        assertValue(tokenId, subject.tokenId());
        assertValue(serial, subject.serial());
        assertValue(sender, subject.sender());
        assertValue(receiver, subject.receiver());
        assertValue(approved, subject.approved());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NftTransfer subject = NftTransfer.builder()
                .tokenId(tokenIdValue())
                .serial(serialValue())
                .sender(senderValue())
                .receiver(receiverValue())
                .build();
        assertValue(false, subject.approved());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NftTransfer.builder().build());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new NftTransfer((Address) null, serialValue(), senderValue(), receiverValue(), approvedValue()));
    }

    @Test
    @DisplayName("serial: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/serial/accepts-at-minimum")
    void shouldAcceptSerialAtMinimum() {
        final long value = -9223372036854775808L;
        final NftTransfer subject = new NftTransfer(tokenIdValue(), value, senderValue(), receiverValue(), approvedValue());
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("serial: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/serial/accepts-at-maximum")
    void shouldAcceptSerialAtMaximum() {
        final long value = 9223372036854775807L;
        final NftTransfer subject = new NftTransfer(tokenIdValue(), value, senderValue(), receiverValue(), approvedValue());
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("rejects null for sender")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/sender/rejects-null")
    void shouldRejectNullSender() {
        assertThrows(NullPointerException.class, () -> new NftTransfer(tokenIdValue(), serialValue(), (AccountId) null, receiverValue(), approvedValue()));
    }

    @Test
    @DisplayName("rejects null for receiver")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/receiver/rejects-null")
    void shouldRejectNullReceiver() {
        assertThrows(NullPointerException.class, () -> new NftTransfer(tokenIdValue(), serialValue(), senderValue(), (AccountId) null, approvedValue()));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.transactions.accounts.NftTransfer/value-semantics")
    void shouldCompareByValue() {
        final NftTransfer subject = create();
        final NftTransfer other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long serialValue() {
        return 1L;
    }

    private static AccountId senderValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId receiverValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static boolean approvedValue() {
        return true;
    }

    private static NftTransfer create() {
        return new NftTransfer(tokenIdValue(), serialValue(), senderValue(), receiverValue(), approvedValue());
    }
}
