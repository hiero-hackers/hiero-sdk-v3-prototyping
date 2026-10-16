package org.hiero.consensusnode.transactions.batch;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.NodeBody;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.utility.PrngReceipt;
import org.hiero.consensusnode.transactions.utility.PrngTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-batch`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-batch")
class TransactionsBatchSpecTest {

    /// `consensusnode.transactions.batch/an-inner-transaction-signed-for-a-batch-has-one-body-for-node-0-0-0-and-its-payer-s-signature`
    ///
    /// Source: HIP-551 (an inner transaction has one body for node 0.0.0, signed by its payer); the Python SDK
    /// (test_batchify_sets_required_fields).
    @Test
    @DisplayName("an inner transaction signed for a batch has one body for node 0.0.0 and its payer's signature")
    void anInnerTransactionSignedForABatchHasOneBodyForNode000AndItsPayerSSignature() {
        // given
        final Account payer = defaultAccount();

        // when
        final PackedTransaction<PrngReceipt, PrngTransaction> inner = new PrngTransaction(null).signForBatch(payer, defaultAuthority());

        // then
        final List<NodeBody> bodies = inner.signableBodies();
        assertEquals(1L, length(bodies));
        assertEquals(AccountId.fromString("0.0.0"), get(bodies, 0L).node());
        assertEquals(payer.accountId(), inner.transactionId().accountId());
        assertEquals(1L, length(inner.nodeSignatures()));
        assertTrue(get(inner.nodeSignatures(), 0L).publicKey().verify(get(bodies, 0L).bytes(), get(inner.nodeSignatures(), 0L).signature()));
    }

    /// `consensusnode.transactions.batch/a-batch-keeps-its-inner-transactions-in-their-order-each-with-its-own-payer`
    ///
    /// Source: this spec (innerTransactions is executed in list order); the Java SDK (BatchTransactionTest
    /// .shouldPreserveTransactionOrder and getInnerTransactionIdsShouldReturnCorrectIds) and the Python SDK
    /// (test_get_inner_transactions_ids_returns_transaction_ids).
    @Test
    @DisplayName("a batch keeps its inner transactions in their order, each with its own payer")
    void aBatchKeepsItsInnerTransactionsInTheirOrderEachWithItsOwnPayer() {
        // given
        final Account first = defaultAccount();
        final Account second = new Account(AccountId.fromString("0.0.1002"), first.privateKey());
        final PackedTransaction<PrngReceipt, PrngTransaction> innerA = new PrngTransaction(null).signForBatch(first, defaultAuthority());
        final PackedTransaction<PrngReceipt, PrngTransaction> innerB = new PrngTransaction(10).signForBatch(second, defaultAuthority());

        // when
        final BatchTransaction batch = new BatchTransaction(List.of(innerA, innerB));

        // then
        assertEquals(2L, length(batch.innerTransactions()));
        assertEquals(innerA.toBytes(), get(batch.innerTransactions(), 0L).toBytes());
        assertEquals(innerB.toBytes(), get(batch.innerTransactions(), 1L).toBytes());
        assertEquals(first.accountId(), get(batch.innerTransactions(), 0L).transactionId().accountId());
        assertEquals(second.accountId(), get(batch.innerTransactions(), 1L).transactionId().accountId());
    }

    /// `consensusnode.transactions.batch/a-signed-batch-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_round_trip_to_bytes_and_back_preserves_inner_transactions), the Java SDK
    /// (BatchTransactionTest.shouldBytes) and the Swift SDK (BatchTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a signed batch survives toBytes and fromBytes")
    void aSignedBatchSurvivesToBytesAndFromBytes() {
        // given
        final Account payer = defaultAccount();
        final PackedTransaction<PrngReceipt, PrngTransaction> inner = new PrngTransaction(null).signForBatch(payer, defaultAuthority());
        final PackedTransaction<BatchReceipt, BatchTransaction> packed = new BatchTransaction(List.of(inner)).sign(payer, List.of(AccountId.fromString("0.0.3")));

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.transactionId().accountId(), restored.transactionId().accountId());
        assertEquals(packed.signableBodies(), restored.signableBodies());
        assertEquals(1L, length(restored.nodeSignatures()));
    }

    /// `consensusnode.transactions.batch/the-body-of-a-batch-contains-the-bytes-of-its-inner-transactions`
    ///
    /// Source: HIP-551 (AtomicBatchTransactionBody.transactions holds the signed inner transactions); the Python SDK
    /// (test_build_batch_transaction_body).
    @Test
    @DisplayName("the body of a batch contains the bytes of its inner transactions")
    void theBodyOfABatchContainsTheBytesOfItsInnerTransactions() {
        // given
        final Account payer = defaultAccount();
        final PackedTransaction<PrngReceipt, PrngTransaction> inner = new PrngTransaction(10).signForBatch(payer, defaultAuthority());

        // when
        final PackedTransaction<BatchReceipt, BatchTransaction> packed = new BatchTransaction(List.of(inner)).pack(payer, List.of(AccountId.fromString("0.0.3")));

        // then
        assertTrue(contains(get(packed.signableBodies(), 0L).bytes(), get(inner.signableBodies(), 0L).bytes()));
    }

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }
}
