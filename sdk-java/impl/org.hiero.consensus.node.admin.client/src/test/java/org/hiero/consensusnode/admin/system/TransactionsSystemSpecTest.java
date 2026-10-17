package org.hiero.consensusnode.admin.system;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZonedDateTime;
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

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }
}
