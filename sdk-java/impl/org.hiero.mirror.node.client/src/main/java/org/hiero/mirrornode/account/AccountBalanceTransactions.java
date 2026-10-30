package org.hiero.mirrornode.account;

import java.util.List;
import java.util.Objects;
import org.hiero.mirrornode.transaction.TransactionInfo;

/// An account together with its most recent transactions. GET /accounts/{id}.
///
/// @param transactions the most recent transactions of the account
public record AccountBalanceTransactions(AccountInfo account, List<TransactionInfo> transactions) {

    /// Creates a new `AccountBalanceTransactions`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AccountBalanceTransactions {
        Objects.requireNonNull(account, "account must not be null");
        Objects.requireNonNull(transactions, "transactions must not be null");
        transactions = List.copyOf(transactions);
    }

    /// Creates a new `AccountBalanceTransactions` with the default value of `transactions`.
    public AccountBalanceTransactions(final AccountInfo account) {
        this(account, List.of());
    }
}
