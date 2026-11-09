package org.hiero.consensusnode.admin.system;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-system`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-system")
class TransactionsSystemSpecTest {

    /// `consensusnode.admin.system/a-system-delete-of-a-file-keeps-the-file-and-the-expiration-time`
    ///
    /// Source: the Java (getSetFileId, getSetExpirationTime, constructSystemDeleteTransactionFromTransactionBodyProtobuf),
    /// JavaScript ("should carry the file id and leave the contract id null"), C++ (GetSetFileId, GetSetExpirationTime),
    /// Go and Swift SystemDeleteTransaction unit tests. The expiration time is the one of the Java test
    /// (Instant.ofEpochSecond(1554158542), converted exactly to 2019-04-01T22:42:22Z); whole seconds, as HAPI carries it
    /// (see Questions & Comments).
    @Test
    @DisplayName("a system delete of a file keeps the file and the expiration time")
    @Tag("testId:consensusnode.admin.system/a-system-delete-of-a-file-keeps-the-file-and-the-expiration-time")
    void aSystemDeleteOfAFileKeepsTheFileAndTheExpirationTime() {
        // given
        final Address file = defaultAddress();
        final ZonedDateTime expirationTime = toZonedDateTime("2019-04-01T22:42:22Z");

        // when
        final SystemDeleteTransaction transaction = new SystemDeleteTransaction(file, null, expirationTime);

        // then
        assertEquals(file, transaction.fileId());
        assertNull(transaction.contractId());
        assertEquals(expirationTime, transaction.expirationTime());
    }

    /// `consensusnode.admin.system/a-system-delete-of-a-contract-keeps-the-contract-and-needs-no-expiration-time`
    ///
    /// Source: the Java (getSetContractId), JavaScript ("should carry the contract id and leave the file id null"), C++
    /// (GetSetContractId), Go and Swift SystemDeleteTransaction unit tests; the API schema of this spec (expirationTime is
    /// ignored for contracts).
    @Test
    @DisplayName("a system delete of a contract keeps the contract and needs no expiration time")
    @Tag("testId:consensusnode.admin.system/a-system-delete-of-a-contract-keeps-the-contract-and-needs-no-expiration-time")
    void aSystemDeleteOfAContractKeepsTheContractAndNeedsNoExpirationTime() {
        // given
        final ContractId contract = defaultContractId();

        // when
        final SystemDeleteTransaction transaction = new SystemDeleteTransaction(null, contract, null);

        // then
        assertNull(transaction.fileId());
        assertEquals(contract, transaction.contractId());
        assertNull(transaction.expirationTime());
    }

    /// `consensusnode.admin.system/a-system-delete-of-both-a-file-and-a-contract-is-rejected`
    ///
    /// Source: the JavaScript SystemDeleteTransaction unit tests ("should never encode both ids at once": fileID and
    /// contractID are a protobuf oneof); `@@oneOf(fileId, contractId)` of this spec.
    @Test
    @DisplayName("a system delete of both a file and a contract is rejected")
    @Tag("testId:consensusnode.admin.system/a-system-delete-of-both-a-file-and-a-contract-is-rejected")
    void aSystemDeleteOfBothAFileAndAContractIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new SystemDeleteTransaction(defaultAddress(), defaultContractId(), toZonedDateTime("2019-04-01T22:42:22Z")));
    }

    /// `consensusnode.admin.system/a-system-delete-without-a-file-and-without-a-contract-is-rejected`
    ///
    /// Source: this spec (`@@oneOf(fileId, contractId)`: exactly one target).
    @Test
    @DisplayName("a system delete without a file and without a contract is rejected")
    @Tag("testId:consensusnode.admin.system/a-system-delete-without-a-file-and-without-a-contract-is-rejected")
    void aSystemDeleteWithoutAFileAndWithoutAContractIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new SystemDeleteTransaction(null, null, null));
    }

    /// `consensusnode.admin.system/a-system-undelete-of-a-file-keeps-the-file`
    ///
    /// Source: the Java (shouldSerializeFile), C++, Go and Swift (test_GetSetFileId) SystemUndeleteTransaction unit tests.
    @Test
    @DisplayName("a system undelete of a file keeps the file")
    @Tag("testId:consensusnode.admin.system/a-system-undelete-of-a-file-keeps-the-file")
    void aSystemUndeleteOfAFileKeepsTheFile() {
        // given
        final Address file = defaultAddress();

        // when
        final SystemUndeleteTransaction transaction = new SystemUndeleteTransaction(file, null);

        // then
        assertEquals(file, transaction.fileId());
        assertNull(transaction.contractId());
    }

    /// `consensusnode.admin.system/a-system-undelete-of-a-contract-keeps-the-contract`
    ///
    /// Source: the Java (shouldSerializeContract), C++, Go and Swift (test_GetSetContractId) SystemUndeleteTransaction unit
    /// tests.
    @Test
    @DisplayName("a system undelete of a contract keeps the contract")
    @Tag("testId:consensusnode.admin.system/a-system-undelete-of-a-contract-keeps-the-contract")
    void aSystemUndeleteOfAContractKeepsTheContract() {
        // given
        final ContractId contract = defaultContractId();

        // when
        final SystemUndeleteTransaction transaction = new SystemUndeleteTransaction(null, contract);

        // then
        assertNull(transaction.fileId());
        assertEquals(contract, transaction.contractId());
    }

    /// `consensusnode.admin.system/a-system-undelete-of-both-a-file-and-a-contract-is-rejected`
    ///
    /// Source: HAPI system_undelete.proto (fileID and contractID are a oneof); `@@oneOf(fileId, contractId)` of this spec.
    @Test
    @DisplayName("a system undelete of both a file and a contract is rejected")
    @Tag("testId:consensusnode.admin.system/a-system-undelete-of-both-a-file-and-a-contract-is-rejected")
    void aSystemUndeleteOfBothAFileAndAContractIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new SystemUndeleteTransaction(defaultAddress(), defaultContractId()));
    }

    /// `consensusnode.admin.system/a-system-undelete-without-a-file-and-without-a-contract-is-rejected`
    ///
    /// Source: this spec (`@@oneOf(fileId, contractId)`: exactly one target).
    @Test
    @DisplayName("a system undelete without a file and without a contract is rejected")
    @Tag("testId:consensusnode.admin.system/a-system-undelete-without-a-file-and-without-a-contract-is-rejected")
    void aSystemUndeleteWithoutAFileAndWithoutAContractIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new SystemUndeleteTransaction(null, null));
    }

    /// `consensusnode.admin.system/system-delete-and-undelete-survive-tobytes-and-frombytes`
    ///
    /// Source: the Java (SystemDeleteTransactionTest.shouldBytesFile, shouldBytesContract,
    /// SystemUndeleteTransactionTest.shouldBytesFile, shouldBytesContract), JavaScript, Go and Swift unit tests (to and from
    /// bytes). The expiration time is the one of the Java test (Instant.ofEpochSecond(1554158542) = 2019-04-01T22:42:22Z).
    @Test
    @DisplayName("system delete and undelete survive toBytes and fromBytes")
    @Tag("testId:consensusnode.admin.system/system-delete-and-undelete-survive-tobytes-and-frombytes")
    void systemDeleteAndUndeleteSurviveToBytesAndFromBytes() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));
        final SystemDeleteTransaction deleteFile = new SystemDeleteTransaction(defaultAddress(), null, toZonedDateTime("2019-04-01T22:42:22Z"));
        final SystemDeleteTransaction deleteContract = new SystemDeleteTransaction(null, defaultContractId(), null);
        final SystemUndeleteTransaction undeleteFile = new SystemUndeleteTransaction(defaultAddress(), null);
        final SystemUndeleteTransaction undeleteContract = new SystemUndeleteTransaction(null, defaultContractId());
        for (final SystemDeleteTransaction transaction : List.of(deleteFile, deleteContract)) {
            // when
            final PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> packed = transaction.pack(payer, nodes);
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

            // then
            assertEquals(packed.toBytes(), restored.toBytes());
            assertEquals(packed.signableBodies(), restored.signableBodies());
        }
        for (final SystemUndeleteTransaction undelete : List.of(undeleteFile, undeleteContract)) {
            // when
            final PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> packedUndelete = undelete.pack(payer, nodes);
            final PackedTransaction<?, ?> restoredUndelete = PackedTransaction.fromBytes(packedUndelete.toBytes());

            // then
            assertEquals(packedUndelete.toBytes(), restoredUndelete.toBytes());
            assertEquals(packedUndelete.signableBodies(), restoredUndelete.signableBodies());
        }
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
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
