package org.hiero.consensusnode.admin.system;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZonedDateTime;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-system`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-system")
class TransactionsSystemSpecTest {

    /// `consensusnode.admin.system/a-system-delete-of-a-file-keeps-the-file-and-the-expiration-time`
    ///
    /// Source: the Java (getSetFileId, getSetExpirationTime, constructSystemDeleteTransactionFromTransactionBodyProtobuf),
    /// JavaScript ("should carry the file id and leave the contract id null"), C++ (GetSetFileId, GetSetExpirationTime),
    /// Go and Swift SystemDeleteTransaction unit tests.
    @Test
    @DisplayName("a system delete of a file keeps the file and the expiration time")
    void aSystemDeleteOfAFileKeepsTheFileAndTheExpirationTime() {
        // given
        final Address file = defaultAddress();
        final ZonedDateTime expirationTime = defaultTransactionId().validStart();

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

    /// `consensusnode.admin.system/a-system-undelete-of-a-file-keeps-the-file`
    ///
    /// Source: the Java (shouldSerializeFile), C++, Go and Swift (test_GetSetFileId) SystemUndeleteTransaction unit tests.
    @Test
    @DisplayName("a system undelete of a file keeps the file")
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
    void aSystemUndeleteOfAContractKeepsTheContract() {
        // given
        final ContractId contract = defaultContractId();

        // when
        final SystemUndeleteTransaction transaction = new SystemUndeleteTransaction(null, contract);

        // then
        assertNull(transaction.fileId());
        assertEquals(contract, transaction.contractId());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TransactionId defaultTransactionId() {
        return TransactionId.generateTransactionId(defaultAddress());
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }
}
