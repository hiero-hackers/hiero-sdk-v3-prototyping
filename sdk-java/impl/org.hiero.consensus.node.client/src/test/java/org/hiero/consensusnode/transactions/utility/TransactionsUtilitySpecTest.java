package org.hiero.consensusnode.transactions.utility;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-utility`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-utility")
class TransactionsUtilitySpecTest {

    /// `consensusnode.transactions.utility/a-prng-transaction-without-a-range-asks-for-random-bytes`
    ///
    /// Source: this spec (range is @@nullable); the Python SDK (test_constructor_default_values).
    @Test
    @DisplayName("a PRNG transaction without a range asks for random bytes")
    void aPRNGTransactionWithoutARangeAsksForRandomBytes() {
        // when
        final PrngTransaction transaction = new PrngTransaction(null);

        // then
        assertNull(transaction.range());
    }

    /// `consensusnode.transactions.utility/a-prng-transaction-keeps-its-range`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters, range 1000) and the Go SDK (TestUnitPrngTransactionSetRange).
    @Test
    @DisplayName("a PRNG transaction keeps its range")
    void aPRNGTransactionKeepsItsRange() {
        // when
        final PrngTransaction transaction = new PrngTransaction(1000);

        // then
        assertEquals(1000, transaction.range());
    }

    /// `consensusnode.transactions.utility/a-prng-transaction-with-a-range-survives-tobytes-and-frombytes`
    ///
    /// Source: the JavaScript SDK ("should return range when create transaction from Bytes", range 100) and the Go SDK
    /// (TestUnitPrngTransactionFromToBytes).
    @Test
    @DisplayName("a PRNG transaction with a range survives toBytes and fromBytes")
    void aPRNGTransactionWithARangeSurvivesToBytesAndFromBytes() {
        // given
        final PackedTransaction<PrngReceipt, PrngTransaction> packed = new PrngTransaction(100).pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final byte[] bytes = packed.toBytes();

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(bytes);

        // then
        assertEquals(bytes, restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
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
