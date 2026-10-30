package org.hiero.mirrornode.topic;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
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
import org.hiero.mirrornode.common.FixedFee;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Topic`, generated from the specs.
@DisplayName("Topic")
class TopicTest {

    @Test
    @DisplayName("creates a Topic and returns the values")
    @Tag("testId:mirrornode.topic.Topic/create")
    void shouldCreateAndReturnTheValues() {
        final Address topicId = topicIdValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority submitAuthority = submitAuthorityValue();
        final @Nullable Authority feeScheduleAuthority = feeScheduleAuthorityValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable ZonedDateTime customFeesCreatedTimestamp = customFeesCreatedTimestampValue();
        final @Nullable Boolean deleted = deletedValue();
        final String memo = memoValue();
        final List<FixedFee> fixedFees = fixedFeesValue();
        final List<Authority> feeExemptAuthorities = feeExemptAuthoritiesValue();
        final TimestampRange timestamp = timestampValue();
        final Topic subject = new Topic(topicId, adminAuthority, submitAuthority, feeScheduleAuthority, autoRenewAccount, autoRenewPeriod, createdTimestamp, customFeesCreatedTimestamp, deleted, memo, fixedFees, feeExemptAuthorities, timestamp);
        assertValue(topicId, subject.topicId());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(submitAuthority, subject.submitAuthority());
        assertValue(feeScheduleAuthority, subject.feeScheduleAuthority());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(customFeesCreatedTimestamp, subject.customFeesCreatedTimestamp());
        assertValue(deleted, subject.deleted());
        assertValue(memo, subject.memo());
        assertValue(fixedFees, subject.fixedFees());
        assertValue(feeExemptAuthorities, subject.feeExemptAuthorities());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("creates a Topic with the default values")
    @Tag("testId:mirrornode.topic.Topic/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final Topic subject = new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), timestampValue());
        assertValue(List.of(), subject.fixedFees());
        assertValue(List.of(), subject.feeExemptAuthorities());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.topic.Topic/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address topicId = topicIdValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority submitAuthority = submitAuthorityValue();
        final @Nullable Authority feeScheduleAuthority = feeScheduleAuthorityValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable ZonedDateTime customFeesCreatedTimestamp = customFeesCreatedTimestampValue();
        final @Nullable Boolean deleted = deletedValue();
        final String memo = memoValue();
        final List<FixedFee> fixedFees = fixedFeesValue();
        final List<Authority> feeExemptAuthorities = feeExemptAuthoritiesValue();
        final TimestampRange timestamp = timestampValue();
        final Topic subject = Topic.builder()
                .topicId(topicId)
                .adminAuthority(adminAuthority)
                .submitAuthority(submitAuthority)
                .feeScheduleAuthority(feeScheduleAuthority)
                .autoRenewAccount(autoRenewAccount)
                .autoRenewPeriod(autoRenewPeriod)
                .createdTimestamp(createdTimestamp)
                .customFeesCreatedTimestamp(customFeesCreatedTimestamp)
                .deleted(deleted)
                .memo(memo)
                .fixedFees(fixedFees)
                .feeExemptAuthorities(feeExemptAuthorities)
                .timestamp(timestamp)
                .build();
        assertValue(topicId, subject.topicId());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(submitAuthority, subject.submitAuthority());
        assertValue(feeScheduleAuthority, subject.feeScheduleAuthority());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(customFeesCreatedTimestamp, subject.customFeesCreatedTimestamp());
        assertValue(deleted, subject.deleted());
        assertValue(memo, subject.memo());
        assertValue(fixedFees, subject.fixedFees());
        assertValue(feeExemptAuthorities, subject.feeExemptAuthorities());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.topic.Topic/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Topic subject = Topic.builder()
                .topicId(topicIdValue())
                .memo(memoValue())
                .timestamp(timestampValue())
                .build();
        assertNull(subject.adminAuthority());
        assertNull(subject.submitAuthority());
        assertNull(subject.feeScheduleAuthority());
        assertNull(subject.autoRenewAccount());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.createdTimestamp());
        assertNull(subject.customFeesCreatedTimestamp());
        assertNull(subject.deleted());
        assertValue(List.of(), subject.fixedFees());
        assertValue(List.of(), subject.feeExemptAuthorities());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.topic.Topic/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Topic.builder().build());
    }

    @Test
    @DisplayName("rejects null for topicId")
    @Tag("testId:mirrornode.topic.Topic/topicId/rejects-null")
    void shouldRejectNullTopicId() {
        assertThrows(NullPointerException.class, () -> new Topic((Address) null, adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), fixedFeesValue(), feeExemptAuthoritiesValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for memo")
    @Tag("testId:mirrornode.topic.Topic/memo/rejects-null")
    void shouldRejectNullMemo() {
        assertThrows(NullPointerException.class, () -> new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), (String) null, fixedFeesValue(), feeExemptAuthoritiesValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for fixedFees")
    @Tag("testId:mirrornode.topic.Topic/fixedFees/rejects-null")
    void shouldRejectNullFixedFees() {
        assertThrows(NullPointerException.class, () -> new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), (List<FixedFee>) null, feeExemptAuthoritiesValue(), timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies fixedFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.topic.Topic/fixedFees/copies-in")
    void shouldCopyFixedFeesIn() {
        final List<FixedFee> value = new ArrayList<>(fixedFeesValue());
        final List<FixedFee> expected = List.copyOf(value);
        final Topic subject = new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), value, feeExemptAuthoritiesValue(), timestampValue());
        value.clear();
        assertValue(expected, subject.fixedFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.fixedFees()).clear());
    }

    @Test
    @DisplayName("rejects null for feeExemptAuthorities")
    @Tag("testId:mirrornode.topic.Topic/feeExemptAuthorities/rejects-null")
    void shouldRejectNullFeeExemptAuthorities() {
        assertThrows(NullPointerException.class, () -> new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), fixedFeesValue(), (List<Authority>) null, timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies feeExemptAuthorities; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.topic.Topic/feeExemptAuthorities/copies-in")
    void shouldCopyFeeExemptAuthoritiesIn() {
        final List<Authority> value = new ArrayList<>(feeExemptAuthoritiesValue());
        final List<Authority> expected = List.copyOf(value);
        final Topic subject = new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), fixedFeesValue(), value, timestampValue());
        value.clear();
        assertValue(expected, subject.feeExemptAuthorities());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.feeExemptAuthorities()).clear());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.topic.Topic/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), fixedFeesValue(), feeExemptAuthoritiesValue(), (TimestampRange) null));
    }

    private static Address topicIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority submitAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority feeScheduleAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime customFeesCreatedTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static String memoValue() {
        return "value";
    }

    private static List<FixedFee> fixedFeesValue() {
        return List.of(new FixedFee(true, 1L, null, null));
    }

    private static List<Authority> feeExemptAuthoritiesValue() {
        return List.of(new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey()));
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static Topic create() {
        return new Topic(topicIdValue(), adminAuthorityValue(), submitAuthorityValue(), feeScheduleAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), customFeesCreatedTimestampValue(), deletedValue(), memoValue(), fixedFeesValue(), feeExemptAuthoritiesValue(), timestampValue());
    }
}
