package org.hiero.consensusnode.queries.schedule;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceApproveTransaction;
import org.hiero.consensusnode.transactions.accounts.HbarAllowance;
import org.hiero.consensusnode.transactions.accounts.NftAllowance;
import org.hiero.consensusnode.transactions.accounts.NftCollectionGrant;
import org.hiero.consensusnode.transactions.accounts.TokenAllowance;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `ScheduleInfo`, generated from the specs.
@DisplayName("ScheduleInfo")
class ScheduleInfoTest {

    @Test
    @DisplayName("creates a ScheduleInfo and returns the values")
    void shouldCreateAndReturnTheValues() {
        final Address scheduleId = scheduleIdValue();
        final AccountId creatorAccountId = creatorAccountIdValue();
        final AccountId payerAccountId = payerAccountIdValue();
        final Transaction<?, ?> scheduledTransaction = scheduledTransactionValue();
        final TransactionId scheduledTransactionId = scheduledTransactionIdValue();
        final List<PublicKey> signers = signersValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable String scheduleMemo = scheduleMemoValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final boolean waitForExpiry = waitForExpiryValue();
        final @Nullable ZonedDateTime executionTime = executionTimeValue();
        final @Nullable ZonedDateTime deletionTime = (ZonedDateTime) null;
        final ScheduleInfo subject = new ScheduleInfo(scheduleId, creatorAccountId, payerAccountId, scheduledTransaction, scheduledTransactionId, signers, adminKey, scheduleMemo, expirationTime, waitForExpiry, executionTime, deletionTime);
        assertValue(scheduleId, subject.scheduleId());
        assertValue(creatorAccountId, subject.creatorAccountId());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(scheduledTransaction, subject.scheduledTransaction());
        assertValue(scheduledTransactionId, subject.scheduledTransactionId());
        assertValue(signers, subject.signers());
        assertValue(adminKey, subject.adminKey());
        assertValue(scheduleMemo, subject.scheduleMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(waitForExpiry, subject.waitForExpiry());
        assertValue(executionTime, subject.executionTime());
        assertValue(deletionTime, subject.deletionTime());
    }

    @Test
    @DisplayName("creates a ScheduleInfo with the default values")
    void shouldCreateWithTheDefaultValues() {
        final ScheduleInfo subject = new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), executionTimeValue(), (ZonedDateTime) null);
        assertValue(List.of(), subject.signers());
        assertValue(false, subject.waitForExpiry());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address scheduleId = scheduleIdValue();
        final AccountId creatorAccountId = creatorAccountIdValue();
        final AccountId payerAccountId = payerAccountIdValue();
        final Transaction<?, ?> scheduledTransaction = scheduledTransactionValue();
        final TransactionId scheduledTransactionId = scheduledTransactionIdValue();
        final List<PublicKey> signers = signersValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable String scheduleMemo = scheduleMemoValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final boolean waitForExpiry = waitForExpiryValue();
        final @Nullable ZonedDateTime executionTime = executionTimeValue();
        final @Nullable ZonedDateTime deletionTime = (ZonedDateTime) null;
        final ScheduleInfo subject = ScheduleInfo.builder()
                .scheduleId(scheduleId)
                .creatorAccountId(creatorAccountId)
                .payerAccountId(payerAccountId)
                .scheduledTransaction(scheduledTransaction)
                .scheduledTransactionId(scheduledTransactionId)
                .signers(signers)
                .adminKey(adminKey)
                .scheduleMemo(scheduleMemo)
                .expirationTime(expirationTime)
                .waitForExpiry(waitForExpiry)
                .executionTime(executionTime)
                .deletionTime(deletionTime)
                .build();
        assertValue(scheduleId, subject.scheduleId());
        assertValue(creatorAccountId, subject.creatorAccountId());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(scheduledTransaction, subject.scheduledTransaction());
        assertValue(scheduledTransactionId, subject.scheduledTransactionId());
        assertValue(signers, subject.signers());
        assertValue(adminKey, subject.adminKey());
        assertValue(scheduleMemo, subject.scheduleMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(waitForExpiry, subject.waitForExpiry());
        assertValue(executionTime, subject.executionTime());
        assertValue(deletionTime, subject.deletionTime());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final ScheduleInfo subject = ScheduleInfo.builder()
                .scheduleId(scheduleIdValue())
                .creatorAccountId(creatorAccountIdValue())
                .payerAccountId(payerAccountIdValue())
                .scheduledTransaction(scheduledTransactionValue())
                .scheduledTransactionId(scheduledTransactionIdValue())
                .expirationTime(expirationTimeValue())
                .executionTime(executionTimeValue())
                .build();
        assertValue(List.of(), subject.signers());
        assertNull(subject.adminKey());
        assertNull(subject.scheduleMemo());
        assertValue(false, subject.waitForExpiry());
        assertValue(executionTimeValue(), subject.executionTime());
        assertNull(subject.deletionTime());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ScheduleInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for scheduleId")
    void shouldRejectNullScheduleId() {
        assertThrows(NullPointerException.class, () -> new ScheduleInfo((Address) null, creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), signersValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("rejects null for creatorAccountId")
    void shouldRejectNullCreatorAccountId() {
        assertThrows(NullPointerException.class, () -> new ScheduleInfo(scheduleIdValue(), (AccountId) null, payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), signersValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("rejects null for payerAccountId")
    void shouldRejectNullPayerAccountId() {
        assertThrows(NullPointerException.class, () -> new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), (AccountId) null, scheduledTransactionValue(), scheduledTransactionIdValue(), signersValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("rejects null for scheduledTransaction")
    void shouldRejectNullScheduledTransaction() {
        assertThrows(NullPointerException.class, () -> new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), (Transaction<?, ?>) null, scheduledTransactionIdValue(), signersValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("rejects null for scheduledTransactionId")
    void shouldRejectNullScheduledTransactionId() {
        assertThrows(NullPointerException.class, () -> new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), (TransactionId) null, signersValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("rejects null for signers")
    void shouldRejectNullSigners() {
        assertThrows(NullPointerException.class, () -> new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), (List<PublicKey>) null, adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("the constructor copies signers; the accessor returns an unmodifiable collection")
    void shouldCopySignersIn() {
        final List<PublicKey> value = new ArrayList<>(signersValue());
        final List<PublicKey> expected = List.copyOf(value);
        final ScheduleInfo subject = new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), value, adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null);
        value.clear();
        assertValue(expected, subject.signers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.signers()).clear());
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), signersValue(), adminKeyValue(), scheduleMemoValue(), (ZonedDateTime) null, waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("rejects executionTime and deletionTime together (@@oneOrNoneOf)")
    void shouldRejectTwoOfExecutionTimeOrDeletionTime() {
        assertThrows(IllegalArgumentException.class, () -> new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), signersValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), deletionTimeValue()));
    }

    private static Address scheduleIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AccountId creatorAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId payerAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Transaction<?, ?> scheduledTransactionValue() {
        return new AccountAllowanceApproveTransaction(List.of(new HbarAllowance(new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), new Hbar(1L, HbarUnit.TINYBAR))), List.of(new TokenAllowance(new Address(0L, 0L, "", 1001L), new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), 1L)), List.of(new NftAllowance(new Address(0L, 0L, "", 1001L), new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), new NftCollectionGrant(true), null)));
    }

    private static TransactionId scheduledTransactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static List<PublicKey> signersValue() {
        return List.of(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String scheduleMemoValue() {
        return "value";
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean waitForExpiryValue() {
        return true;
    }

    private static ZonedDateTime executionTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime deletionTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ScheduleInfo create() {
        return new ScheduleInfo(scheduleIdValue(), creatorAccountIdValue(), payerAccountIdValue(), scheduledTransactionValue(), scheduledTransactionIdValue(), signersValue(), adminKeyValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue(), executionTimeValue(), (ZonedDateTime) null);
    }
}
