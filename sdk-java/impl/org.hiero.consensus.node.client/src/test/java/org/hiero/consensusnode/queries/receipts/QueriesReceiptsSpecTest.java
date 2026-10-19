package org.hiero.consensusnode.queries.receipts;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.transactions.BasicTransactionStatus;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;

/// Tests of the spec `queries-receipts`, generated from the `tests` blocks of its `## Testing` section.
///
/// Not generated:
/// - `an untyped receipt with an account id is typed as the receipt of an account creation`: no value of nativeToken.ExchangeRate
/// - `an untyped receipt without an account id is not the receipt of an account creation`: no value of nativeToken.ExchangeRate
@DisplayName("queries-receipts")
class QueriesReceiptsSpecTest {

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
