package org.hiero.mirrornode.account;

import java.util.Objects;
import org.hiero.ledger.Address;

/// One token balance line of an account.
///
/// @param balance balance in the token's smallest unit
public record TokenBalance(Address tokenId, long balance) {

    /// Creates a new `TokenBalance`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenBalance {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
    }
}
