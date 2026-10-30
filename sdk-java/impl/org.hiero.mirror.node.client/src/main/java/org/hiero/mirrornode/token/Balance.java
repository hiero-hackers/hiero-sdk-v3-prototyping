package org.hiero.mirrornode.token;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// The balance of a token held by one account, in the token's smallest unit.
///
/// @param decimals the number of decimals of the token
public record Balance(AccountId account, long balance, @Nullable Long decimals) {

    /// Creates a new `Balance`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Balance {
        Objects.requireNonNull(account, "account must not be null");
    }
}
