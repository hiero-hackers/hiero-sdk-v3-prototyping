package org.hiero.consensusnode.transactions.topics;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-topics`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-topics")
class TransactionsTopicsSpecTest {

    /// `consensusnode.transactions.topics/a-topic-create-transaction-without-attributes-creates-a-public-immutable-topic`
    ///
    /// Source: this spec (adminAuthority unset: an immutable topic, submitAuthority unset: a public topic); the Python SDK
    /// (test_constructor_default_values of TopicCreateTransaction).
    @Test
    @DisplayName("a topic create transaction without attributes creates a public, immutable topic")
    void aTopicCreateTransactionWithoutAttributesCreatesAPublicImmutableTopic() {
        // when
        final TopicCreateTransaction transaction = new TopicCreateTransaction(null, null, null, null, null);

        // then
        assertNull(transaction.adminAuthority());
        assertNull(transaction.submitAuthority());
        assertNull(transaction.topicMemo());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.autoRenewAccount());
    }

    /// `consensusnode.transactions.topics/a-topic-create-transaction-keeps-its-authorities-memo-and-auto-renew-account`
    ///
    /// Source: the Python SDK (test_constructor of TopicCreateTransaction: "Test Topic", an admin and a submit key) and the
    /// Swift SDK (TopicCreateTransactionUnitTests.test_GetSetSubmitKey, test_GetSetAdminKey and
    /// test_GetSetAutoRenewAccountId).
    @Test
    @DisplayName("a topic create transaction keeps its authorities, memo and auto-renew account")
    void aTopicCreateTransactionKeepsItsAuthoritiesMemoAndAutoRenewAccount() {
        // given
        final Authority submitAuthority = new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048").createPublicKey());

        // when
        final TopicCreateTransaction transaction = new TopicCreateTransaction(defaultAuthority(), submitAuthority, "Test Topic", null, AccountId.fromString("0.0.5678"));

        // then
        assertEquals(defaultAuthority(), transaction.adminAuthority());
        assertEquals(submitAuthority, transaction.submitAuthority());
        assertEquals("Test Topic", transaction.topicMemo());
        assertEquals(AccountId.fromString("0.0.5678"), transaction.autoRenewAccount());
    }

    /// `consensusnode.transactions.topics/a-topic-create-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (TopicCreateTransactionTest.shouldBytes and shouldBytesNoSetters) and the Swift SDK
    /// (TopicCreateTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a topic create transaction survives toBytes and fromBytes")
    void aTopicCreateTransactionSurvivesToBytesAndFromBytes() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));
        final TopicCreateTransaction withAttributes = new TopicCreateTransaction(defaultAuthority(), defaultAuthority(), "Test Topic", null, AccountId.fromString("0.0.5678"));
        for (final TopicCreateTransaction transaction : List.of(withAttributes, new TopicCreateTransaction(null, null, null, null, null))) {
            // given
            final PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> packed = transaction.pack(payer, nodes);

            // when
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

            // then
            assertEquals(packed.toBytes(), restored.toBytes());
            assertEquals(packed.signableBodies(), restored.signableBodies());
        }
    }

    /// `consensusnode.transactions.topics/a-topic-update-transaction-changes-nothing-but-what-it-names`
    ///
    /// Source: this spec (every nullable field of an update is "leave unchanged"); the Python SDK
    /// (test_auto_renew_period_omitted_when_unset, test_topic_memo_serialization_distinguishes_unset_and_empty).
    @Test
    @DisplayName("a topic update transaction changes nothing but what it names")
    void aTopicUpdateTransactionChangesNothingButWhatItNames() {
        // when
        final TopicUpdateTransaction transaction = new TopicUpdateTransaction(defaultAddress(), null, null, null, null, null, null);

        // then
        assertEquals(defaultAddress(), transaction.topicId());
        assertNull(transaction.adminAuthority());
        assertNull(transaction.submitAuthority());
        assertNull(transaction.topicMemo());
        assertNull(transaction.expirationTime());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.autoRenewAccount());
    }

    /// `consensusnode.transactions.topics/a-topic-update-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_topic_update_transaction_with_all_fields: "Comprehensive update", auto-renew account
    /// 0.0.5678), the Java SDK (TopicUpdateTransactionTest.shouldBytes) and the Swift SDK
    /// (TopicUpdateTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a topic update transaction survives toBytes and fromBytes")
    void aTopicUpdateTransactionSurvivesToBytesAndFromBytes() {
        // given
        final PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> packed = new TopicUpdateTransaction(defaultAddress(), defaultAuthority(), defaultAuthority(), "Comprehensive update", null, null, AccountId.fromString("0.0.5678")).pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.topics/a-topic-delete-transaction-keeps-its-topic-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (TopicDeleteTransactionTest.shouldBytes) and the Swift SDK (TopicDeleteTransactionUnitTests
    /// .test_GetSetTopicId and test_ToFromBytes).
    @Test
    @DisplayName("a topic delete transaction keeps its topic and survives toBytes and fromBytes")
    void aTopicDeleteTransactionKeepsItsTopicAndSurvivesToBytesAndFromBytes() {
        // given
        final TopicDeleteTransaction transaction = new TopicDeleteTransaction(defaultAddress());

        // when
        final PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(defaultAddress(), transaction.topicId());
        assertEquals(packed.signableBodies(), restored.signableBodies());
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

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
