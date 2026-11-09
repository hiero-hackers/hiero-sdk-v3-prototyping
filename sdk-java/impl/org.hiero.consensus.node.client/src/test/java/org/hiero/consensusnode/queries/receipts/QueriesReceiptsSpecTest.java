package org.hiero.consensusnode.queries.receipts;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-receipts`, generated from the `tests` blocks of its `## Testing` section.
///
/// Not generated:
/// - `an untyped receipt with an account id is typed as the receipt of an account creation`: no value of nativeToken.ExchangeRate
/// - `an untyped receipt without an account id is not the receipt of an account creation`: no value of nativeToken.ExchangeRate
/// - `an untyped receipt is typed with the id of the entity it created`: no value of nativeToken.ExchangeRate
/// - `an untyped receipt is typed with the schedule, the supply and the serials`: no value of nativeToken.ExchangeRate
/// - `an untyped receipt is typed only when nothing required is missing`: no value of nativeToken.ExchangeRate
@DisplayName("queries-receipts")
class QueriesReceiptsSpecTest {

    /// `consensusnode.queries.receipts/a-receipt-query-asks-only-for-the-receipt-unless-it-names-duplicates-children-or-a-node`
    ///
    /// Source: this spec (@@default(false) of includeDuplicates and includeChildren, @@nullable nodeAccountId: any node of
    /// the client); the Java SDK (TransactionReceiptQueryTest: setIncludeDuplicates, setIncludeChildren) and the Python SDK
    /// (transaction_receipt_query_test.py, test_include_children_and_duplicates).
    @Test
    @DisplayName("a receipt query asks only for the receipt unless it names duplicates, children or a node")
    @Tag("testId:consensusnode.queries.receipts/a-receipt-query-asks-only-for-the-receipt-unless-it-names-duplicates-children-or-a-node")
    void aReceiptQueryAsksOnlyForTheReceiptUnlessItNamesDuplicatesChildrenOrANode() {
        // given
        final TransactionId id = TransactionId.fromString("0.0.1001@1554158542.000000000");
        final AccountId node = AccountId.fromString("0.0.3");

        // when
        final TransactionReceiptQuery plain = new TransactionReceiptQuery(id, false, false, null);
        final TransactionReceiptQuery all = new TransactionReceiptQuery(id, true, true, node);

        // then
        assertEquals(id, plain.transactionId());
        assertFalse(plain.includeDuplicates());
        assertFalse(plain.includeChildren());
        assertNull(plain.nodeAccountId());
        assertTrue(all.includeDuplicates());
        assertTrue(all.includeChildren());
        assertEquals(node, all.nodeAccountId());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionId defaultTransactionId() {
        return TransactionId.generateTransactionId(defaultAddress());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TransactionStatus defaultTransactionStatus() {
        return BasicTransactionStatus.OK;
    }
}
