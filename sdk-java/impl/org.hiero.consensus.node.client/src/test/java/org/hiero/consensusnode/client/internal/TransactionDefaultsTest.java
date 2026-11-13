package org.hiero.consensusnode.client.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hederahashgraph.api.proto.java.TransactionBody;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.utility.PrngTransaction;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of what the SDK fills in when it packs a transaction: the defaults of `maxTransactionFee` and
/// `validDuration` and the valid start of the transaction id (rules default-max-transaction-fee, default-valid-duration
/// and valid-start of `transactions.md`). The packed body is read from `signableBodies()`.
@DisplayName("Transaction defaults")
class TransactionDefaultsTest {

    private static final Account PAYER = new Account(new AccountId(0L, 0L, "", 1001L, null, null),
            KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX,
                    "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    private static final AccountId NODE = new AccountId(0L, 0L, "", 3L, null, null);

    private static TransactionBody body(final PrngTransaction transaction) throws Exception {
        return TransactionBody.parseFrom(transaction.pack(PAYER, List.of(NODE)).signableBodies().getFirst().bytes());
    }

    @Test
    @DisplayName("default-max-transaction-fee: a transaction without a maximum fee is packed with 200,000,000 base units")
    @Tag("testId:impl/transaction-defaults/a-transaction-without-a-maximum-fee-is-packed-with-2-hbar")
    @Tag("rule:consensusnode.transactions/default-max-transaction-fee")
    void aTransactionWithoutAMaximumFeeIsPackedWith2Hbar() throws Exception {
        // when
        final TransactionBody body = body(new PrngTransaction(null));

        // then
        assertEquals(200_000_000L, body.getTransactionFee());
    }

    @Test
    @DisplayName("default-valid-duration: a transaction without a valid duration is packed with 120 seconds")
    @Tag("testId:impl/transaction-defaults/a-transaction-without-a-valid-duration-is-packed-with-120-seconds")
    @Tag("rule:consensusnode.transactions/default-valid-duration")
    void aTransactionWithoutAValidDurationIsPackedWith120Seconds() throws Exception {
        // when
        final TransactionBody body = body(new PrngTransaction(null));

        // then
        assertEquals(120L, body.getTransactionValidDuration().getSeconds());
    }

    @Test
    @DisplayName("a maximum fee and a valid duration that are set are packed as they are, also above the default")
    @Tag("testId:impl/transaction-defaults/values-that-are-set-are-packed-as-they-are")
    @Tag("rule:consensusnode.transactions/default-max-transaction-fee")
    @Tag("rule:consensusnode.transactions/default-valid-duration")
    void valuesThatAreSetArePackedAsTheyAre() throws Exception {
        // given
        final PrngTransaction transaction = new PrngTransaction(null)
                .setMaxTransactionFee(new Hbar(5, HbarUnit.HBAR))
                .setValidDuration(Duration.ofSeconds(30));

        // when
        final TransactionBody body = body(transaction);

        // then
        assertEquals(500_000_000L, body.getTransactionFee());
        assertEquals(30L, body.getTransactionValidDuration().getSeconds());
    }

    @Test
    @DisplayName("valid-start: a generated transaction id starts 10 seconds in the past, and later ids start later")
    @Tag("testId:impl/transaction-defaults/a-generated-transaction-id-starts-10-seconds-in-the-past")
    @Tag("rule:consensusnode.transactions/valid-start")
    void aGeneratedTransactionIdStarts10SecondsInThePast() {
        // given
        final Address payer = new Address(0L, 0L, "", 1001L);
        final Instant before = Instant.now();

        // when
        final TransactionId first = TransactionId.generateTransactionId(payer);
        final TransactionId second = TransactionId.generateTransactionId(payer);
        final Instant after = Instant.now();

        // then: now minus 10 s plus less than 1 µs, with the clock of the test around the call
        final Instant start = first.validStart().toInstant();
        assertTrue(!start.isBefore(before.minusSeconds(10).minusMillis(1)), start + " is before " + before);
        assertTrue(!start.isAfter(after.minusSeconds(10).plusMillis(1)), start + " is after " + after);
        assertTrue(second.validStart().toInstant().isAfter(start), "the second id does not start later");
    }
}
