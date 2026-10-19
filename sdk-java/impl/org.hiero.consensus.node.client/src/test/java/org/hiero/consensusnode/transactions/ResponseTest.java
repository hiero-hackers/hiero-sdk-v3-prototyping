package org.hiero.consensusnode.transactions;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;

/// Tests of `Response`, generated from the specs.
///
/// Not generated:
/// - no valid value for `transactionType` of Response: constructor, attribute and method tests
@DisplayName("Response")
class ResponseTest {

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }
}
