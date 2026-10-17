package org.hiero.mirrornode.topic;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-topic`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-topic")
class MirrorNodeTopicSpecTest {

    /// `mirrornode.topic/chunk-info-keeps-the-initial-transaction-id-the-chunk-number-and-the-total`
    ///
    /// Source: this spec (a message submitted in several chunks carries the chunk's number and the total number of
    /// chunks, and the id of the transaction of its first chunk).
    @Test
    @DisplayName("chunk info keeps the initial transaction id, the chunk number and the total")
    @Tag("testId:mirrornode.topic/chunk-info-keeps-the-initial-transaction-id-the-chunk-number-and-the-total")
    void chunkInfoKeepsTheInitialTransactionIdTheChunkNumberAndTheTotal() {
        // given
        final TransactionId initial = defaultTransactionId();

        // when
        final ChunkInfo chunk = new ChunkInfo(initial, 0, 2, 3, false);

        // then
        assertEquals(initial, chunk.initialTransactionId());
        assertEquals(2, chunk.number());
        assertEquals(3, chunk.total());
        assertFalse(chunk.scheduled());
    }

    /// `mirrornode.topic/a-topic-keeps-its-auto-renew-period-and-its-creation-time`
    ///
    /// Source: the Hiero Enterprise Java test fixture topic.json (topic 0.0.1926, auto-renew period 7776000 seconds, created
    /// "1706816673.106110015", timestamp range from the creation, memo "1706816668096 Mirror node created test topic", no
    /// custom fees, no fee-exempt keys); millisecond precision. The fixture has no end of the timestamp range, which this
    /// spec requires, so the test uses some valid time for it.
    @Test
    @DisplayName("a topic keeps its auto-renew period and its creation time")
    @Tag("testId:mirrornode.topic/a-topic-keeps-its-auto-renew-period-and-its-creation-time")
    void aTopicKeepsItsAutoRenewPeriodAndItsCreationTime() {
        // given
        final ZonedDateTime created = toZonedDateTime("2024-02-01T19:44:33.106Z");

        // when
        final Topic topic = new Topic(Address.fromString("0.0.1926"), null, null, null, null, toSeconds(7776000L), created, false, "1706816668096 Mirror node created test topic", List.of(), List.of(), created, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));

        // then
        assertEquals(Address.fromString("0.0.1926"), topic.topicId());
        assertEquals(toSeconds(7776000L), topic.autoRenewPeriod());
        assertEquals(toZonedDateTime("2024-02-01T19:44:33.106Z"), topic.createdTimestamp());
        assertEquals(created, topic.fromTimestamp());
        assertEquals("1706816668096 Mirror node created test topic", topic.memo());
        assertEquals(0L, length(topic.fixedFees()));
        assertEquals(0L, length(topic.feeExemptAuthorities()));
        assertNull(topic.adminAuthority());
        assertNull(topic.autoRenewAccount());
    }

    /// `mirrornode.topic/a-topic-message-keeps-its-consensus-timestamp-payer-running-hash-and-sequence-number`
    ///
    /// Source: the Hiero Enterprise Java test fixture topic-message.json (topic 0.0.9411905, payer 0.0.6105114, consensus
    /// timestamp "1783075674.960707349", running hash version 3, sequence number 3, chunk 3 of 14); millisecond precision.
    /// A running hash of version 3 is a SHA-384 hash of 48 bytes (HAPI, TransactionReceipt.topicRunningHashVersion).
    @Test
    @DisplayName("a topic message keeps its consensus timestamp, payer, running hash and sequence number")
    @Tag("testId:mirrornode.topic/a-topic-message-keeps-its-consensus-timestamp-payer-running-hash-and-sequence-number")
    void aTopicMessageKeepsItsConsensusTimestampPayerRunningHashAndSequenceNumber() {
        // given
        final ZonedDateTime consensus = toZonedDateTime("2026-07-03T10:47:54.960Z");
        final byte[] runningHash = base64("qAIp0612V63kRt90Jwnmg3TmOn9WYZ50hzqGeiAACy569p0XYSEzqp5u6b1e4Nmd");
        final ChunkInfo chunk = new ChunkInfo(defaultTransactionId(), 0, 3, 14, false);

        // when
        final TopicMessage message = new TopicMessage(chunk, consensus, "message", AccountId.fromString("0.0.6105114"), runningHash, 3, 3L, Address.fromString("0.0.9411905"));

        // then
        assertEquals(toZonedDateTime("2026-07-03T10:47:54.960Z"), message.consensusTimestamp());
        assertEquals(AccountId.fromString("0.0.6105114"), message.payerAccountId());
        assertEquals(runningHash, message.runningHash());
        assertEquals(48L, length(message.runningHash()));
        assertEquals(3, message.runningHashVersion());
        assertEquals(3L, message.sequenceNumber());
        assertEquals(Address.fromString("0.0.9411905"), message.topicId());
        assertEquals(chunk, message.chunkInfo());
    }

    /// `mirrornode.topic/a-topic-message-submitted-in-one-piece-has-no-chunk-info`
    ///
    /// Source: this spec (`chunkInfo` is nullable: only a message submitted in several chunks carries it; a topic message is
    /// a value, two messages that differ only in their consensus timestamp are different).
    @Test
    @DisplayName("a topic message submitted in one piece has no chunk info")
    @Tag("testId:mirrornode.topic/a-topic-message-submitted-in-one-piece-has-no-chunk-info")
    void aTopicMessageSubmittedInOnePieceHasNoChunkInfo() {
        // given
        final Address topic = Address.fromString("0.0.9411905");
        final AccountId payer = AccountId.fromString("0.0.6105114");
        final byte[] runningHash = base64("qAIp0612V63kRt90Jwnmg3TmOn9WYZ50hzqGeiAACy569p0XYSEzqp5u6b1e4Nmd");

        // when
        final TopicMessage message = new TopicMessage(null, toZonedDateTime("2026-07-03T10:47:54.960Z"), "message", payer, runningHash, 3, 1L, topic);
        final TopicMessage later = new TopicMessage(null, toZonedDateTime("2026-07-03T10:47:54.961Z"), "message", payer, runningHash, 3, 1L, topic);

        // then
        assertNull(message.chunkInfo());
        assertNotEquals(message, later);
    }

    private static TransactionId defaultTransactionId() {
        return TransactionId.generateTransactionId(defaultAddress());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
