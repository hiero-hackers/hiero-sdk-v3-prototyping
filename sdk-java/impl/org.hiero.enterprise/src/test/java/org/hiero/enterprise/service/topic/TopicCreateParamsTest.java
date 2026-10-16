package org.hiero.enterprise.service.topic;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `TopicCreateParams`, generated from the specs.
@DisplayName("TopicCreateParams")
class TopicCreateParamsTest {

    @Test
    @DisplayName("creates a TopicCreateParams and returns the values")
    void shouldCreateAndReturnTheValues() {
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority submitAuthority = submitAuthorityValue();
        final @Nullable String topicMemo = topicMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final TopicCreateParams subject = new TopicCreateParams(adminAuthority, submitAuthority, topicMemo, autoRenewPeriod, autoRenewAccount);
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(submitAuthority, subject.submitAuthority());
        assertValue(topicMemo, subject.topicMemo());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority submitAuthority = submitAuthorityValue();
        final @Nullable String topicMemo = topicMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final TopicCreateParams subject = TopicCreateParams.builder()
                .adminAuthority(adminAuthority)
                .submitAuthority(submitAuthority)
                .topicMemo(topicMemo)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .build();
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(submitAuthority, subject.submitAuthority());
        assertValue(topicMemo, subject.topicMemo());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final TopicCreateParams subject = TopicCreateParams.builder()
                .build();
        assertNull(subject.adminAuthority());
        assertNull(subject.submitAuthority());
        assertNull(subject.topicMemo());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
    }

    @Test
    @DisplayName("topicMemo: accepts a value with the maximum length")
    void shouldAcceptTopicMemoAtMaximumLength() {
        final @Nullable String value = "valueaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        final TopicCreateParams subject = new TopicCreateParams(adminAuthorityValue(), submitAuthorityValue(), value, autoRenewPeriodValue(), autoRenewAccountValue());
        assertValue(value, subject.topicMemo());
    }

    @Test
    @DisplayName("topicMemo: rejects a value longer than the maximum length")
    void shouldRejectTopicMemoAboveMaximumLength() {
        assertThrows(IllegalArgumentException.class, () -> new TopicCreateParams(adminAuthorityValue(), submitAuthorityValue(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", autoRenewPeriodValue(), autoRenewAccountValue()));
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority submitAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String topicMemoValue() {
        return "value";
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TopicCreateParams create() {
        return new TopicCreateParams(adminAuthorityValue(), submitAuthorityValue(), topicMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue());
    }
}
