package org.hiero.consensusnode.transactions;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.transactions.utility.PrngReceipt;
import org.hiero.consensusnode.transactions.utility.PrngTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions")
class TransactionsSpecTest {

    /// `consensusnode.transactions/packing-gives-one-body-per-target-node-in-the-order-of-the-nodes`
    ///
    /// Source: HAPI (one TransactionBody per target node, each with its nodeAccountID); the Java, Go, JavaScript and
    /// Python SDKs.
    @Test
    @DisplayName("packing gives one body per target node, in the order of the nodes")
    @Tag("testId:consensusnode.transactions/packing-gives-one-body-per-target-node-in-the-order-of-the-nodes")
    void packingGivesOneBodyPerTargetNodeInTheOrderOfTheNodes() {
        // given
        final Account payer = defaultAccount();
        final AccountId node3 = AccountId.fromString("0.0.3");
        final AccountId node4 = AccountId.fromString("0.0.4");

        // when
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(null).pack(payer, List.of(node3, node4));

        // then
        final List<NodeBody> bodies = packed.signableBodies();
        assertEquals(2L, length(bodies));
        assertEquals(node3, get(bodies, 0L).node());
        assertEquals(node4, get(bodies, 1L).node());
        assertNotEquals(get(bodies, 0L).bytes(), get(bodies, 1L).bytes(), "each body names its node");
        assertEquals(payer.accountId(), packed.transactionId().accountId());
        assertEquals(0L, length(packed.nodeSignatures()));
    }

    /// `consensusnode.transactions/signing-adds-one-signature-per-node-that-verifies-over-the-body-of-that-node`
    ///
    /// Source: HAPI (a signature covers the bodyBytes of its SignedTransaction); the Java, Python, C++ and Go SDKs.
    @Test
    @DisplayName("signing adds one signature per node that verifies over the body of that node")
    @Tag("testId:consensusnode.transactions/signing-adds-one-signature-per-node-that-verifies-over-the-body-of-that-node")
    void signingAddsOneSignaturePerNodeThatVerifiesOverTheBodyOfThatNode() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));

        // when
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(null).sign(payer, nodes);

        // then
        assertEquals(2L, length(packed.nodeSignatures()));
        for (final NodeSignature signature : packed.nodeSignatures()) {
            assertEquals(payer.privateKey().createPublicKey(), signature.publicKey());
            assertEquals(64L, length(signature.signature()));
            for (final NodeBody body : packed.signableBodies()) {
                if (equal(body.node(), signature.node())) {
                    assertTrue(signature.publicKey().verify(body.bytes(), signature.signature()));
                }
            }
        }
    }

    /// `consensusnode.transactions/signing-again-with-the-same-key-adds-no-signature`
    ///
    /// Source: the Python, Java and Go SDKs (a key in a SignatureMap only once).
    @Test
    @DisplayName("signing again with the same key adds no signature")
    @Tag("testId:consensusnode.transactions/signing-again-with-the-same-key-adds-no-signature")
    void signingAgainWithTheSameKeyAddsNoSignature() {
        // given
        final Account payer = defaultAccount();
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(null).sign(payer, List.of(AccountId.fromString("0.0.3")));

        // when
        final PackedTransaction<PrngReceipt, PrngTransaction> again = packed.sign(payer);

        // then
        assertEquals(1L, length(again.nodeSignatures()));
    }

    /// `consensusnode.transactions/an-ed25519-and-a-secp256k1-key-both-sign-every-body`
    ///
    /// Source: the JavaScript and Python SDKs (Ed25519 and ECDSA signatures on one transaction). The second key is the
    /// secp256k1 key of the tests of keys.md.
    @Test
    @DisplayName("an Ed25519 and a secp256k1 key both sign every body")
    @Tag("testId:consensusnode.transactions/an-ed25519-and-a-secp256k1-key-both-sign-every-body")
    void anEd25519AndASecp256k1KeyBothSignEveryBody() {
        // given
        final Account payer = defaultAccount();
        final Account second = new Account(AccountId.fromString("0.0.1002"), KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048"));
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(null).sign(payer, List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4")));

        // when
        final PackedTransaction<PrngReceipt, PrngTransaction> signed = packed.sign(second);

        // then
        assertEquals(4L, length(signed.nodeSignatures()));
        for (final NodeSignature signature : signed.nodeSignatures()) {
            for (final NodeBody body : signed.signableBodies()) {
                if (equal(body.node(), signature.node())) {
                    assertTrue(signature.publicKey().verify(body.bytes(), signature.signature()));
                }
            }
        }
    }

    /// `consensusnode.transactions/a-signed-transaction-survives-tobytes-and-frombytes-unchanged`
    ///
    /// Source: the Rust, Python, JavaScript, Go and Swift SDKs (the body bytes stay byte-exact, otherwise the signatures
    /// break).
    @Test
    @DisplayName("a signed transaction survives toBytes and fromBytes unchanged")
    @Tag("testId:consensusnode.transactions/a-signed-transaction-survives-tobytes-and-frombytes-unchanged")
    void aSignedTransactionSurvivesToBytesAndFromBytesUnchanged() {
        // given
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(null).sign(defaultAccount(), List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4")));
        final byte[] bytes = packed.toBytes();

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(bytes);

        // then
        assertEquals(bytes, restored.toBytes());
        assertEquals(packed.transactionId().accountId(), restored.transactionId().accountId());
        assertEquals(packed.signableBodies(), restored.signableBodies());
        assertEquals(length(packed.nodeSignatures()), length(restored.nodeSignatures()));
    }

    /// `consensusnode.transactions/a-restored-transaction-can-be-signed-by-another-party`
    ///
    /// Source: the multi-party flow of the Python SDK: a transaction is packed by one party and signed by another.
    @Test
    @DisplayName("a restored transaction can be signed by another party")
    @Tag("testId:consensusnode.transactions/a-restored-transaction-can-be-signed-by-another-party")
    void aRestoredTransactionCanBeSignedByAnotherParty() {
        // given
        final Account payer = defaultAccount();
        final byte[] bytes = new PrngTransaction(null).sign(payer, List.of(AccountId.fromString("0.0.3"))).toBytes();
        final Account other = new Account(AccountId.fromString("0.0.1002"), KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048"));

        // when
        final PackedTransaction<?, ?> signed = PackedTransaction.fromBytes(bytes).sign(other);

        // then
        assertEquals(2L, length(signed.nodeSignatures()));
        for (final NodeSignature signature : signed.nodeSignatures()) {
            assertTrue(signature.publicKey().verify(get(signed.signableBodies(), 0L).bytes(), signature.signature()));
        }
    }

    /// `consensusnode.transactions/signatures-made-outside-the-sdk-are-attached-to-their-bodies`
    ///
    /// Source: the out-of-process signing of the Rust SDK and the JavaScript SDK (sign the body bytes, attach the
    /// signatures).
    @Test
    @DisplayName("signatures made outside the SDK are attached to their bodies")
    @Tag("testId:consensusnode.transactions/signatures-made-outside-the-sdk-are-attached-to-their-bodies")
    void signaturesMadeOutsideTheSDKAreAttachedToTheirBodies() {
        // given
        final Account payer = defaultAccount();
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(null).pack(payer, List.of(AccountId.fromString("0.0.3")));
        final NodeBody body = get(packed.signableBodies(), 0L);
        final NodeSignature external = new NodeSignature(body.node(), payer.privateKey().createPublicKey(), payer.privateKey().sign(body.bytes()));

        // when
        final PackedTransaction<PrngReceipt, PrngTransaction> signed = packed.sign(List.of(external));

        // then
        assertEquals(List.of(external), signed.nodeSignatures());
        assertEquals(packed.transactionId().accountId(), signed.transactionId().accountId());
    }

    /// `consensusnode.transactions/a-transaction-packed-for-a-batch-has-one-body-for-node-0-0-0`
    ///
    /// Source: HIP-551 (an inner batch transaction is addressed to node 0.0.0); the Python SDK.
    @Test
    @DisplayName("a transaction packed for a batch has one body for node 0.0.0")
    @Tag("testId:consensusnode.transactions/a-transaction-packed-for-a-batch-has-one-body-for-node-0-0-0")
    void aTransactionPackedForABatchHasOneBodyForNode000() {
        // given
        final Account payer = defaultAccount();
        final PublicKeyAuthority batchKey = new PublicKeyAuthority(payer.privateKey().createPublicKey());

        // when
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(null).packForBatch(payer, batchKey);

        // then
        assertEquals(1L, length(packed.signableBodies()));
        assertEquals(AccountId.fromString("0.0.0"), get(packed.signableBodies(), 0L).node());
        assertEquals(payer.accountId(), packed.transactionId().accountId());
    }

    /// `consensusnode.transactions/a-transaction-of-another-sdk-is-read-with-a-valid-start-in-2125`
    ///
    /// Source: a fixture of the JavaScript SDK (a TransactionList with one body for node 0.0.3, payer 0.0.1234, valid
    /// start 4891363200 = 2125-01-01, memo "Transaction with validStart in 2125"); a regression test for valid starts
    /// beyond 32-bit seconds.
    @Test
    @DisplayName("a transaction of another SDK is read with a valid start in 2125")
    @Tag("testId:consensusnode.transactions/a-transaction-of-another-sdk-is-read-with-a-valid-start-in-2125")
    void aTransactionOfAnotherSDKIsReadWithAValidStartIn2125() {
        // given
        final byte[] bytes = hex((("0acc012ac9010a5f0a0d0a0608808fb19c12120318d209120218031880c2d72f2202087832235472616e73616374" + "696f6e20776974682076616c6964537461727420696e2032313235721c0a1a0a0b0a0318d20910ffa7d6b9070a0b0a0318ae2c1080") + "a8d6b90712660a640a205afd59dfc4401b63272c050535457495841ba2d414891cb8609549d3f08ef6671a40a82b1257b5476a98c6") + "5fe73e802150e9526eed77c11b377da642893e0004db096c258636a47327d986084a5044409220d68dde2ea34b1b1b097b39129d83d302");

        // when
        final PackedTransaction<?, ?> transaction = PackedTransaction.fromBytes(bytes);

        // then
        assertEquals(AccountId.fromString("0.0.1234"), transaction.transactionId().accountId());
        assertTrue(startsWith(transaction.transactionId().toString(), "0.0.1234@4891363200."));
        assertEquals(1L, length(transaction.signableBodies()));
        assertEquals(AccountId.fromString("0.0.3"), get(transaction.signableBodies(), 0L).node());
        assertEquals(1L, length(transaction.nodeSignatures()));
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
}
