package org.hiero.consensusnode.transactions.schedule;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.transactions.NodeBody;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.accounts.HbarTransfer;
import org.hiero.consensusnode.transactions.accounts.TransferTransaction;
import org.hiero.consensusnode.transactions.utility.PrngTransaction;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-schedule`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-schedule")
class TransactionsScheduleSpecTest {

    /// The expiration time of the schedule create tests of the Java SDK (ScheduleCreateTransactionTest: validStart =
    /// Instant.ofEpochSecond(1554158542)), the Swift SDK (ScheduleCreateTransactionUnitTests: TestConstants.validStart =
    /// Timestamp(seconds: 1_554_158_542, subSecondNanos: 0)) and the Rust SDK (schedule_create_transaction.rs:
    /// EXPIRATION_TIME = VALID_START), whole seconds.
    private static ZonedDateTime SDK_TEST_EXPIRATION_TIME() {
        return toZonedDateTime("2019-04-01T22:42:22Z");
    }

    /// `consensusnode.transactions.schedule/a-schedule-create-transaction-leaves-out-what-is-not-given`
    ///
    /// Source: this spec (@@nullable, @@default(false) of waitForExpiry); the Python SDK (test_constructor_default_values
    /// of ScheduleCreateTransaction).
    @Test
    @DisplayName("a schedule create transaction leaves out what is not given")
    @Tag("testId:consensusnode.transactions.schedule/a-schedule-create-transaction-leaves-out-what-is-not-given")
    void aScheduleCreateTransactionLeavesOutWhatIsNotGiven() {
        // given
        final PrngTransaction inner = new PrngTransaction(null);

        // when
        final ScheduleCreateTransaction transaction = new ScheduleCreateTransaction(inner, null, null, null, null, false);

        // then
        assertNull(transaction.adminKey());
        assertNull(transaction.payerAccountId());
        assertNull(transaction.scheduleMemo());
        assertNull(transaction.expirationTime());
        assertFalse(transaction.waitForExpiry());
    }

    /// `consensusnode.transactions.schedule/a-schedule-create-transaction-keeps-its-inner-transaction-and-attributes`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters of ScheduleCreateTransaction: payer 0.0.123, "test memo",
    /// waitForExpiry true, a transfer as inner transaction) and the Swift SDK (ScheduleCreateTransactionUnitTests
    /// .test_GetSetPayerAccountId, test_GetSetAdminKey and test_GetSetExpirationTime: TestConstants.validStart =
    /// 2019-04-01T22:42:22Z).
    @Test
    @DisplayName("a schedule create transaction keeps its inner transaction and attributes")
    @Tag("testId:consensusnode.transactions.schedule/a-schedule-create-transaction-keeps-its-inner-transaction-and-attributes")
    void aScheduleCreateTransactionKeepsItsInnerTransactionAndAttributes() {
        // given
        final Transaction<?, ?> inner = new TransferTransaction(List.of(new HbarTransfer(AccountId.fromString("0.0.1001"), new Hbar(-1L, HbarUnit.HBAR), false), new HbarTransfer(AccountId.fromString("0.0.1002"), new Hbar(1L, HbarUnit.HBAR), false)), List.of(), List.of());

        // when
        final ScheduleCreateTransaction transaction = new ScheduleCreateTransaction(inner, defaultAuthority(), AccountId.fromString("0.0.123"), "test memo", SDK_TEST_EXPIRATION_TIME(), true);

        // then
        assertEquals(inner, transaction.scheduledTransaction());
        assertEquals(defaultAuthority(), transaction.adminKey());
        assertEquals(AccountId.fromString("0.0.123"), transaction.payerAccountId());
        assertEquals("test memo", transaction.scheduleMemo());
        assertEquals(SDK_TEST_EXPIRATION_TIME(), transaction.expirationTime());
        assertTrue(transaction.waitForExpiry());
    }

    /// `consensusnode.transactions.schedule/a-schedule-create-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (ScheduleCreateTransactionTest.shouldBytes and shouldBytesNoSetters; expiration time validStart
    /// = Instant.ofEpochSecond(1554158542) = 2019-04-01T22:42:22Z) and the Swift SDK
    /// (ScheduleCreateTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a schedule create transaction survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.schedule/a-schedule-create-transaction-survives-tobytes-and-frombytes")
    void aScheduleCreateTransactionSurvivesToBytesAndFromBytes() {
        // given
        final Transaction<?, ?> inner = new TransferTransaction(List.of(new HbarTransfer(AccountId.fromString("0.0.1001"), new Hbar(-1L, HbarUnit.HBAR), false), new HbarTransfer(AccountId.fromString("0.0.1002"), new Hbar(1L, HbarUnit.HBAR), false)), List.of(), List.of());
        final PackedTransaction<ScheduleCreateReceipt, ScheduleCreateTransaction> packed = new ScheduleCreateTransaction(inner, defaultAuthority(), AccountId.fromString("0.0.123"), "test memo", SDK_TEST_EXPIRATION_TIME(), true).pack(defaultAccount(), List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4")));

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.schedule/a-schedule-sign-transaction-keeps-its-schedule-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_constructor_with_schedule_id of ScheduleSignTransaction), the Java SDK
    /// (ScheduleSignTransactionTest.shouldBytes) and the Swift SDK (ScheduleSignTransactionUnitTests.test_GetSetScheduleId
    /// and test_ToFromBytes).
    @Test
    @DisplayName("a schedule sign transaction keeps its schedule and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.schedule/a-schedule-sign-transaction-keeps-its-schedule-and-survives-tobytes-and-frombytes")
    void aScheduleSignTransactionKeepsItsScheduleAndSurvivesToBytesAndFromBytes() {
        // given
        final Address scheduleId = Address.fromString("0.0.5005");
        final ScheduleSignTransaction transaction = new ScheduleSignTransaction(scheduleId);

        // when
        final PackedTransaction<ScheduleSignReceipt, ScheduleSignTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(scheduleId, transaction.scheduleId());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.schedule/every-key-that-signs-a-schedule-sign-transaction-signs-each-body`
    ///
    /// Source: this spec (a ScheduleSign carries only the scheduleId; the signatures on it are credited to the schedule);
    /// the Python SDK (test_sign_transaction of ScheduleSignTransaction). The second key is the secp256k1 key of the tests
    /// of keys.md.
    @Test
    @DisplayName("every key that signs a schedule sign transaction signs each body")
    @Tag("testId:consensusnode.transactions.schedule/every-key-that-signs-a-schedule-sign-transaction-signs-each-body")
    void everyKeyThatSignsAScheduleSignTransactionSignsEachBody() {
        // given
        final Account payer = defaultAccount();
        final Account second = new Account(AccountId.fromString("0.0.1002"), KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048"));
        final PackedTransaction<ScheduleSignReceipt, ScheduleSignTransaction> packed = new ScheduleSignTransaction(Address.fromString("0.0.5005")).sign(payer, List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4")));

        // when
        final PackedTransaction<ScheduleSignReceipt, ScheduleSignTransaction> signed = packed.sign(second);

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

    /// `consensusnode.transactions.schedule/a-schedule-delete-transaction-keeps-its-schedule-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters of ScheduleDeleteTransaction), the Java SDK
    /// (ScheduleDeleteTransactionTest.shouldBytes) and the Swift SDK (ScheduleDeleteTransactionUnitTests
    /// .test_GetSetScheduleId and test_ToFromBytes).
    @Test
    @DisplayName("a schedule delete transaction keeps its schedule and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.schedule/a-schedule-delete-transaction-keeps-its-schedule-and-survives-tobytes-and-frombytes")
    void aScheduleDeleteTransactionKeepsItsScheduleAndSurvivesToBytesAndFromBytes() {
        // given
        final Address scheduleId = Address.fromString("0.0.5005");
        final ScheduleDeleteTransaction transaction = new ScheduleDeleteTransaction(scheduleId);

        // when
        final PackedTransaction<ScheduleDeleteReceipt, ScheduleDeleteTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(scheduleId, transaction.scheduleId());
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
}
