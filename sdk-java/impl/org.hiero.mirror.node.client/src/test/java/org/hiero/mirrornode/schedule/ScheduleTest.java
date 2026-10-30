package org.hiero.mirrornode.schedule;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Schedule`, generated from the specs.
@DisplayName("Schedule")
class ScheduleTest {

    @Test
    @DisplayName("creates a Schedule and returns the values")
    @Tag("testId:mirrornode.schedule.Schedule/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable Authority adminKey = adminKeyValue();
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final AccountId creatorAccountId = creatorAccountIdValue();
        final boolean deleted = deletedValue();
        final @Nullable ZonedDateTime executedTimestamp = executedTimestampValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final String memo = memoValue();
        final AccountId payerAccountId = payerAccountIdValue();
        final Address scheduleId = scheduleIdValue();
        final List<ScheduleSignature> signatures = signaturesValue();
        final byte[] transactionBody = transactionBodyValue();
        final boolean waitForExpiry = waitForExpiryValue();
        final Schedule subject = new Schedule(adminKey, consensusTimestamp, creatorAccountId, deleted, executedTimestamp, expirationTime, memo, payerAccountId, scheduleId, signatures, transactionBody, waitForExpiry);
        assertValue(adminKey, subject.adminKey());
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(creatorAccountId, subject.creatorAccountId());
        assertValue(deleted, subject.deleted());
        assertValue(executedTimestamp, subject.executedTimestamp());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(memo, subject.memo());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(scheduleId, subject.scheduleId());
        assertValue(signatures, subject.signatures());
        assertValue(transactionBody, subject.transactionBody());
        assertValue(waitForExpiry, subject.waitForExpiry());
    }

    @Test
    @DisplayName("creates a Schedule with the default values")
    @Tag("testId:mirrornode.schedule.Schedule/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final Schedule subject = new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), transactionBodyValue(), waitForExpiryValue());
        assertValue(List.of(), subject.signatures());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.schedule.Schedule/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable Authority adminKey = adminKeyValue();
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final AccountId creatorAccountId = creatorAccountIdValue();
        final boolean deleted = deletedValue();
        final @Nullable ZonedDateTime executedTimestamp = executedTimestampValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final String memo = memoValue();
        final AccountId payerAccountId = payerAccountIdValue();
        final Address scheduleId = scheduleIdValue();
        final List<ScheduleSignature> signatures = signaturesValue();
        final byte[] transactionBody = transactionBodyValue();
        final boolean waitForExpiry = waitForExpiryValue();
        final Schedule subject = Schedule.builder()
                .adminKey(adminKey)
                .consensusTimestamp(consensusTimestamp)
                .creatorAccountId(creatorAccountId)
                .deleted(deleted)
                .executedTimestamp(executedTimestamp)
                .expirationTime(expirationTime)
                .memo(memo)
                .payerAccountId(payerAccountId)
                .scheduleId(scheduleId)
                .signatures(signatures)
                .transactionBody(transactionBody)
                .waitForExpiry(waitForExpiry)
                .build();
        assertValue(adminKey, subject.adminKey());
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(creatorAccountId, subject.creatorAccountId());
        assertValue(deleted, subject.deleted());
        assertValue(executedTimestamp, subject.executedTimestamp());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(memo, subject.memo());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(scheduleId, subject.scheduleId());
        assertValue(signatures, subject.signatures());
        assertValue(transactionBody, subject.transactionBody());
        assertValue(waitForExpiry, subject.waitForExpiry());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.schedule.Schedule/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Schedule subject = Schedule.builder()
                .consensusTimestamp(consensusTimestampValue())
                .creatorAccountId(creatorAccountIdValue())
                .deleted(deletedValue())
                .memo(memoValue())
                .payerAccountId(payerAccountIdValue())
                .scheduleId(scheduleIdValue())
                .transactionBody(transactionBodyValue())
                .waitForExpiry(waitForExpiryValue())
                .build();
        assertNull(subject.adminKey());
        assertNull(subject.executedTimestamp());
        assertNull(subject.expirationTime());
        assertValue(List.of(), subject.signatures());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.schedule.Schedule/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Schedule.builder().build());
    }

    @Test
    @DisplayName("rejects null for consensusTimestamp")
    @Tag("testId:mirrornode.schedule.Schedule/consensusTimestamp/rejects-null")
    void shouldRejectNullConsensusTimestamp() {
        assertThrows(NullPointerException.class, () -> new Schedule(adminKeyValue(), (ZonedDateTime) null, creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), signaturesValue(), transactionBodyValue(), waitForExpiryValue()));
    }

    @Test
    @DisplayName("rejects null for creatorAccountId")
    @Tag("testId:mirrornode.schedule.Schedule/creatorAccountId/rejects-null")
    void shouldRejectNullCreatorAccountId() {
        assertThrows(NullPointerException.class, () -> new Schedule(adminKeyValue(), consensusTimestampValue(), (AccountId) null, deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), signaturesValue(), transactionBodyValue(), waitForExpiryValue()));
    }

    @Test
    @DisplayName("rejects null for memo")
    @Tag("testId:mirrornode.schedule.Schedule/memo/rejects-null")
    void shouldRejectNullMemo() {
        assertThrows(NullPointerException.class, () -> new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), (String) null, payerAccountIdValue(), scheduleIdValue(), signaturesValue(), transactionBodyValue(), waitForExpiryValue()));
    }

    @Test
    @DisplayName("rejects null for payerAccountId")
    @Tag("testId:mirrornode.schedule.Schedule/payerAccountId/rejects-null")
    void shouldRejectNullPayerAccountId() {
        assertThrows(NullPointerException.class, () -> new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), (AccountId) null, scheduleIdValue(), signaturesValue(), transactionBodyValue(), waitForExpiryValue()));
    }

    @Test
    @DisplayName("rejects null for scheduleId")
    @Tag("testId:mirrornode.schedule.Schedule/scheduleId/rejects-null")
    void shouldRejectNullScheduleId() {
        assertThrows(NullPointerException.class, () -> new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), (Address) null, signaturesValue(), transactionBodyValue(), waitForExpiryValue()));
    }

    @Test
    @DisplayName("rejects null for signatures")
    @Tag("testId:mirrornode.schedule.Schedule/signatures/rejects-null")
    void shouldRejectNullSignatures() {
        assertThrows(NullPointerException.class, () -> new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), (List<ScheduleSignature>) null, transactionBodyValue(), waitForExpiryValue()));
    }

    @Test
    @DisplayName("the constructor copies signatures; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.schedule.Schedule/signatures/copies-in")
    void shouldCopySignaturesIn() {
        final List<ScheduleSignature> value = new ArrayList<>(signaturesValue());
        final List<ScheduleSignature> expected = List.copyOf(value);
        final Schedule subject = new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), value, transactionBodyValue(), waitForExpiryValue());
        value.clear();
        assertValue(expected, subject.signatures());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.signatures()).clear());
    }

    @Test
    @DisplayName("rejects null for transactionBody")
    @Tag("testId:mirrornode.schedule.Schedule/transactionBody/rejects-null")
    void shouldRejectNullTransactionBody() {
        assertThrows(NullPointerException.class, () -> new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), signaturesValue(), (byte[]) null, waitForExpiryValue()));
    }

    @Test
    @DisplayName("the constructor copies transactionBody; the accessor returns a copy")
    @Tag("testId:mirrornode.schedule.Schedule/transactionBody/copies-in")
    void shouldCopyTransactionBodyIn() {
        final byte[] value = transactionBodyValue();
        final byte[] expected = value.clone();
        final Schedule subject = new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), signaturesValue(), value, waitForExpiryValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.transactionBody());
        final byte[] returned = subject.transactionBody();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.transactionBody());
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static ZonedDateTime consensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static AccountId creatorAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static ZonedDateTime executedTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String memoValue() {
        return "value";
    }

    private static AccountId payerAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address scheduleIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static List<ScheduleSignature> signaturesValue() {
        return List.of(new ScheduleSignature(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new byte[] {1, 2, 3}, new byte[] {1, 2, 3}, SignatureType.CONTRACT));
    }

    private static byte[] transactionBodyValue() {
        return new byte[] {1, 2, 3};
    }

    private static boolean waitForExpiryValue() {
        return true;
    }

    private static Schedule create() {
        return new Schedule(adminKeyValue(), consensusTimestampValue(), creatorAccountIdValue(), deletedValue(), executedTimestampValue(), expirationTimeValue(), memoValue(), payerAccountIdValue(), scheduleIdValue(), signaturesValue(), transactionBodyValue(), waitForExpiryValue());
    }
}
