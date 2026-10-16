package org.hiero.enterprise.service.topic;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
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
    /// that is set is read back unchanged.
    @Test
    @DisplayName("topic parameters keep the values they are created with")
    void topicParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final Authority authority = defaultAuthority();
        final AccountId autoRenewAccount = defaultAccountId();

        // when
        final TopicCreateParams params = new TopicCreateParams(authority, authority, "memo", null, autoRenewAccount);

        // then
        assertEquals(authority, params.adminAuthority());
        assertEquals(authority, params.submitAuthority());
        assertEquals("memo", params.topicMemo());
        assertEquals(autoRenewAccount, params.autoRenewAccount());
    }

    /// `enterprise.service.topic/a-topic-memo-of-100-characters-is-accepted`
    ///
    /// Source: this spec (@@maxLength(100) of topicMemo); HAPI limits a memo to 100 bytes, which ASCII text of 100
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
}
