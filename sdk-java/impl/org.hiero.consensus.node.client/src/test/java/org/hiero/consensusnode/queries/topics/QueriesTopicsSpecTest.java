package org.hiero.consensusnode.queries.topics;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-topics`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-topics")
class QueriesTopicsSpecTest {

    /// `consensusnode.queries.topics/a-topic-info-query-keeps-the-id-of-its-topic`
    ///
    /// Source: the Python SDK (topic_info_query_test.py, test_topic_info_query) and the Swift SDK
    /// (TopicInfoQueryUnitTests, the get / set test).
    @Test
    @DisplayName("a topic info query keeps the id of its topic")
    void aTopicInfoQueryKeepsTheIdOfItsTopic() {
        // given
        final Address topic = defaultAddress();

        // when
        final TopicInfoQuery query = new TopicInfoQuery(topic);

        // then
        assertEquals(topic, query.topicId());
    }

    /// `consensusnode.queries.topics/a-topic-info-of-a-public-immutable-topic-has-no-authorities`
    ///
    /// Source: this spec (an absent admin authority means an immutable topic, an absent submit authority a public one); the
    /// Python SDK (topic_info_test.py, test_topic_info_initialization_with_none_values).
    @Test
    @DisplayName("a topic info of a public immutable topic has no authorities")
    void aTopicInfoOfAPublicImmutableTopicHasNoAuthorities() {
        // given
        final byte[] runningHash = repeat(hex("00"), 48L);

        // when
        final TopicInfo info = new TopicInfo(defaultAddress(), null, runningHash, 0L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, null, null, null);

        // then
        assertEquals(runningHash, info.runningHash());
        assertEquals(0L, info.sequenceNumber());
        assertNull(info.topicMemo());
        assertNull(info.adminAuthority());
        assertNull(info.submitAuthority());
        assertNull(info.autoRenewPeriod());
        assertNull(info.autoRenewAccount());
    }

    /// `consensusnode.queries.topics/a-topic-info-keeps-its-expiration-time-and-auto-renew-period`
    ///
    /// Source: the Java SDK (TopicInfoTest, the topic info of its fixture): expiration time Instant.ofEpochMilli(4), i.e.
    /// 1970-01-01T00:00:00.004Z, and auto-renew period Duration.ofDays(5), i.e. 432000 seconds. Millisecond precision.
    @Test
    @DisplayName("a topic info keeps its expiration time and auto-renew period")
    void aTopicInfoKeepsItsExpirationTimeAndAutoRenewPeriod() {
        // given
        final ZonedDateTime expirationTime = toZonedDateTime("1970-01-01T00:00:00.004Z");
        final byte[] runningHash = repeat(hex("00"), 48L);

        // when
        final TopicInfo info = new TopicInfo(defaultAddress(), null, runningHash, 0L, expirationTime, null, null, toSeconds(432000L), null);

        // then
        assertEquals(expirationTime, info.expirationTime());
        assertEquals(toSeconds(432000L), info.autoRenewPeriod());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
