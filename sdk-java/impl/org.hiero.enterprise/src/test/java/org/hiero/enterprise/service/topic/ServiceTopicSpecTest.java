package org.hiero.enterprise.service.topic;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `service-topic`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("service-topic")
class ServiceTopicSpecTest {

    /// `enterprise.service.topic/topic-parameters-without-values-describe-a-public-topic-that-cannot-be-changed`
    ///
    /// Source: this spec (TopicCreateParams: "Nothing is required: without values the topic is public and cannot be
    /// changed"); the Python SDK (topic_create_transaction_test.py, test_constructor_default_values: no admin and no submit
    /// key).
    @Test
    @DisplayName("topic parameters without values describe a public topic that cannot be changed")
    void topicParametersWithoutValuesDescribeAPublicTopicThatCannotBeChanged() {
        // when
        final TopicCreateParams params = defaultTopicCreateParams();

        // then
        assertNull(params.adminAuthority());
        assertNull(params.submitAuthority());
        assertNull(params.topicMemo());
        assertNull(params.autoRenewPeriod());
        assertNull(params.autoRenewAccount());
    }

    /// `enterprise.service.topic/topic-parameters-keep-the-values-they-are-created-with`
    ///
    /// Source: the TopicCreateTransaction unit tests of the Java (TopicCreateTransactionTest), Go
    /// (topic_create_transaction_unit_test.go), C++ (TopicCreateTransactionUnitTests), Swift
    /// (TopicCreateTransactionUnitTests) and Python (topic_create_transaction_test.py, test_constructor) SDKs: every value
    /// that is set is read back unchanged. The auto-renew period is the one of the Java test (Duration.ofHours(24), which
    /// is exactly 86400 seconds).
    @Test
    @DisplayName("topic parameters keep the values they are created with")
    void topicParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final Authority authority = defaultAuthority();
        final AccountId autoRenewAccount = defaultAccountId();
        final Duration autoRenewPeriod = toSeconds(86400L);

        // when
        final TopicCreateParams params = new TopicCreateParams(authority, authority, "memo", autoRenewPeriod, autoRenewAccount);

        // then
        assertEquals(authority, params.adminAuthority());
        assertEquals(authority, params.submitAuthority());
        assertEquals("memo", params.topicMemo());
        assertEquals(autoRenewPeriod, params.autoRenewPeriod());
        assertEquals(autoRenewAccount, params.autoRenewAccount());
    }

    /// `enterprise.service.topic/a-topic-memo-of-100-characters-is-accepted`
    ///
    /// Source: this spec (@@maxBytes(100) of topicMemo); HAPI limits a memo to 100 bytes, which ASCII text of 100
    /// characters is.
    @Test
    @DisplayName("a topic memo of 100 characters is accepted")
    void aTopicMemoOf100CharactersIsAccepted() {
        // given
        final String memo = repeat("a", 100L);

        // when
        final TopicCreateParams params = new TopicCreateParams(null, null, memo, null, null);

        // then
        assertEquals(memo, params.topicMemo());
    }

    /// `enterprise.service.topic/a-topic-memo-of-101-characters-is-rejected`
    ///
    /// Source: this spec (@@maxBytes(100) of topicMemo); HAPI limits a memo to 100 bytes.
    @Test
    @DisplayName("a topic memo of 101 characters is rejected")
    void aTopicMemoOf101CharactersIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TopicCreateParams(null, null, repeat("a", 101L), null, null));
    }

    /// `enterprise.service.topic/a-topic-keeps-the-values-it-is-created-with`
    ///
    /// Source: hiero-enterprise-java (Topic, and the Mirror Node fixture topic.json of MirrorNodeJsonConverterTest: topic
    /// 0.0.1926 with an admin key, a submit key and the memo "1706816668096 Mirror node created test topic"). The created
    /// timestamp of the fixture has nanosecond precision, so the test uses some valid point in time instead.
    @Test
    @DisplayName("a topic keeps the values it is created with")
    void aTopicKeepsTheValuesItIsCreatedWith() {
        // given
        final Address topicId = new Address(0L, 0L, "", 1926L);
        final Authority authority = defaultAuthority();
        final ZonedDateTime createdTimestamp = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        // when
        final Topic topic = new Topic(topicId, authority, authority, createdTimestamp, "1706816668096 Mirror node created test topic");

        // then
        assertEquals(topicId, topic.topicId());
        assertEquals(authority, topic.adminAuthority());
        assertEquals(authority, topic.submitAuthority());
        assertEquals(createdTimestamp, topic.createdTimestamp());
        assertEquals("1706816668096 Mirror node created test topic", topic.memo());
    }

    /// `enterprise.service.topic/a-topic-without-authorities-has-neither-an-admin-nor-a-submit-authority`
    ///
    /// Source: this spec (Description: "A topic without a submit authority is public"; TopicCreateParams.adminAuthority:
    /// "without it the topic cannot be changed").
    @Test
    @DisplayName("a topic without authorities has neither an admin nor a submit authority")
    void aTopicWithoutAuthoritiesHasNeitherAnAdminNorASubmitAuthority() {
        // when
        final Topic topic = new Topic(defaultAddress(), null, null, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), "");

        // then
        assertNull(topic.adminAuthority());
        assertNull(topic.submitAuthority());
    }

    /// `enterprise.service.topic/topics-created-at-different-times-are-not-equal`
    ///
    /// Source: this spec (Topic is a value of its attributes); the points in time are the fixed timestamps of the Java
    /// SDK tests (TopicMessageTest: Instant.ofEpochSecond(1554158542), FileCreateTransactionTest:
    /// Instant.ofEpochSecond(1554158728); converted exactly to 2019-04-01T22:42:22Z and 2019-04-01T22:45:28Z).
    @Test
    @DisplayName("topics created at different times are not equal")
    void topicsCreatedAtDifferentTimesAreNotEqual() {
        // given
        final ZonedDateTime created = toZonedDateTime("2019-04-01T22:42:22Z");
        final ZonedDateTime createdLater = toZonedDateTime("2019-04-01T22:45:28Z");

        // when
        final Topic topic = new Topic(defaultAddress(), null, null, created, "memo");
        final Topic sameTopic = new Topic(defaultAddress(), null, null, created, "memo");
        final Topic laterTopic = new Topic(defaultAddress(), null, null, createdLater, "memo");

        // then
        assertEquals(topic, sameTopic);
        assertNotEquals(topic, laterTopic);
        assertTrue(topic.createdTimestamp().compareTo(laterTopic.createdTimestamp()) < 0);
    }

    /// `enterprise.service.topic/a-topic-message-keeps-the-values-it-is-created-with`
    ///
    /// Source: the Java SDK (TopicMessageTest, constructWithArgs: consensus timestamp Instant.ofEpochSecond(1554158542),
    /// converted exactly to 2019-04-01T22:42:22Z, sequence number 7, payer 0.0.1 of the transaction id); every value that
    /// is set is read back unchanged.
    @Test
    @DisplayName("a topic message keeps the values it is created with")
    void aTopicMessageKeepsTheValuesItIsCreatedWith() {
        // given
        final ZonedDateTime consensusTimestamp = toZonedDateTime("2019-04-01T22:42:22Z");
        final AccountId payer = new AccountId(0L, 0L, "", 1L, null, null);
        final Address topicId = defaultAddress();

        // when
        final TopicMessage message = new TopicMessage(consensusTimestamp, "Hello, Hiero!", payer, 7L, topicId);

        // then
        assertEquals(consensusTimestamp, message.consensusTimestamp());
        assertEquals("Hello, Hiero!", message.message());
        assertEquals(payer, message.payerAccountId());
        assertEquals(7L, message.sequenceNumber());
        assertEquals(topicId, message.topicId());
    }

    private static TopicCreateParams defaultTopicCreateParams() {
        return new TopicCreateParams(null, null, null, null, null);
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
