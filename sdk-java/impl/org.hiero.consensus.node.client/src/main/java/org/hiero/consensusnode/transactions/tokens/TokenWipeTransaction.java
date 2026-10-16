package org.hiero.consensusnode.transactions.tokens;

import java.time.Duration;
import java.util.Objects;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Removes (burns) units of a token from a NON-treasury account. Requires the token's
/// wipeAuthority. Which of the two wipe transactions applies follows from the token's type:
/// TokenWipeFungibleTransaction for FUNGIBLE_COMMON, TokenWipeNftTransaction for
/// NON_FUNGIBLE_UNIQUE.
public abstract sealed class TokenWipeTransaction<Self extends TokenWipeTransaction<Self>> extends Transaction<TokenWipeReceipt, Self> permits TokenWipeFungibleTransaction, TokenWipeNftTransaction {

    private final Address tokenId;
    private final AccountId accountId;

    /// Creates a new `TokenWipeTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected TokenWipeTransaction(final Address tokenId, final AccountId accountId) {
        super();
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        this.tokenId = tokenId;
        Objects.requireNonNull(accountId, "accountId must not be null");
        this.accountId = accountId;
    }

    /// the token to wipe
    public Address tokenId() {
        return tokenId;
    }

    /// the non-treasury account to wipe from (MUST NOT be the treasury)
    public AccountId accountId() {
        return accountId;
    }
}
