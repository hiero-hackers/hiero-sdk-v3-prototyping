package org.hiero.enterprise.service.topic;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TopicMessage`, generated from the specs.
@DisplayName("TopicMessage")
class TopicMessageTest {

    @Test
    @DisplayName("creates a TopicMessage and returns the values")
    @Tag("testId:enterprise.service.topic.TopicMessage/create")
    void shouldCreateAndReturnTheValues() {
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final String message = messageValue();
        final AccountId payerAccountId = payerAccountIdValue();
        final long sequenceNumber = sequenceNumberValue();
        final Address topicId = topicIdValue();
        final TopicMessage subject = new TopicMessage(consensusTimestamp, message, payerAccountId, sequenceNumber, topicId);
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(message, subject.message());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(sequenceNumber, subject.sequenceNumber());
        assertValue(topicId, subject.topicId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:enterprise.service.topic.TopicMessage/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final String message = messageValue();
        final AccountId payerAccountId = payerAccountIdValue();
        final long sequenceNumber = sequenceNumberValue();
        final Address topicId = topicIdValue();
        final TopicMessage subject = TopicMessage.builder()
                .consensusTimestamp(consensusTimestamp)
                .message(message)
                .payerAccountId(payerAccountId)
                .sequenceNumber(sequenceNumber)
                .topicId(topicId)
                .build();
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(message, subject.message());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(sequenceNumber, subject.sequenceNumber());
        assertValue(topicId, subject.topicId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:enterprise.service.topic.TopicMessage/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TopicMessage subject = TopicMessage.builder()
                .consensusTimestamp(consensusTimestampValue())
                .message(messageValue())
                .payerAccountId(payerAccountIdValue())
                .sequenceNumber(sequenceNumberValue())
                .topicId(topicIdValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:enterprise.service.topic.TopicMessage/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TopicMessage.builder().build());
    }

    @Test
    @DisplayName("rejects null for consensusTimestamp")
    @Tag("testId:enterprise.service.topic.TopicMessage/consensusTimestamp/rejects-null")
    void shouldRejectNullConsensusTimestamp() {
        assertThrows(NullPointerException.class, () -> new TopicMessage((ZonedDateTime) null, messageValue(), payerAccountIdValue(), sequenceNumberValue(), topicIdValue()));
    }

    @Test
    @DisplayName("rejects null for message")
    @Tag("testId:enterprise.service.topic.TopicMessage/message/rejects-null")
    void shouldRejectNullMessage() {
        assertThrows(NullPointerException.class, () -> new TopicMessage(consensusTimestampValue(), (String) null, payerAccountIdValue(), sequenceNumberValue(), topicIdValue()));
    }

    @Test
    @DisplayName("rejects null for payerAccountId")
    @Tag("testId:enterprise.service.topic.TopicMessage/payerAccountId/rejects-null")
    void shouldRejectNullPayerAccountId() {
        assertThrows(NullPointerException.class, () -> new TopicMessage(consensusTimestampValue(), messageValue(), (AccountId) null, sequenceNumberValue(), topicIdValue()));
    }

    @Test
    @DisplayName("sequenceNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:enterprise.service.topic.TopicMessage/sequenceNumber/accepts-at-minimum")
    void shouldAcceptSequenceNumberAtMinimum() {
        final long value = -9223372036854775808L;
        final TopicMessage subject = new TopicMessage(consensusTimestampValue(), messageValue(), payerAccountIdValue(), value, topicIdValue());
        assertValue(value, subject.sequenceNumber());
    }

    @Test
    @DisplayName("sequenceNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:enterprise.service.topic.TopicMessage/sequenceNumber/accepts-at-maximum")
    void shouldAcceptSequenceNumberAtMaximum() {
        final long value = 9223372036854775807L;
        final TopicMessage subject = new TopicMessage(consensusTimestampValue(), messageValue(), payerAccountIdValue(), value, topicIdValue());
        assertValue(value, subject.sequenceNumber());
    }

    @Test
    @DisplayName("rejects null for topicId")
    @Tag("testId:enterprise.service.topic.TopicMessage/topicId/rejects-null")
    void shouldRejectNullTopicId() {
        assertThrows(NullPointerException.class, () -> new TopicMessage(consensusTimestampValue(), messageValue(), payerAccountIdValue(), sequenceNumberValue(), (Address) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:enterprise.service.topic.TopicMessage/value-semantics")
    void shouldCompareByValue() {
        final TopicMessage subject = create();
        final TopicMessage other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ZonedDateTime consensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String messageValue() {
        return "value";
    }

    private static AccountId payerAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long sequenceNumberValue() {
        return 1L;
    }

    private static Address topicIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TopicMessage create() {
        return new TopicMessage(consensusTimestampValue(), messageValue(), payerAccountIdValue(), sequenceNumberValue(), topicIdValue());
    }
}
