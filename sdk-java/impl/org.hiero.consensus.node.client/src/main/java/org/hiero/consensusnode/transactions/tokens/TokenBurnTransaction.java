package org.hiero.consensusnode.transactions.tokens;

import java.time.Duration;
import java.util.Objects;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Burns existing units of a token from the treasury account. Requires the token's supplyAuthority
/// to sign; which of the two burn transactions applies follows from the token's type.
///
/// Burning NFT serials held by a non-treasury account is not possible via this transaction —
/// use the wipe transactions (gated by `wipeAuthority`) instead.
public abstract sealed class TokenBurnTransaction<Self extends TokenBurnTransaction<Self>> extends Transaction<TokenBurnReceipt, Self> permits TokenBurnFungibleTransaction, TokenBurnNftTransaction {

    private final Address tokenId;

    /// Creates a new `TokenBurnTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected TokenBurnTransaction(final Address tokenId) {
        super();
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        this.tokenId = tokenId;
    }

    /// the token to burn from
    public Address tokenId() {
        return tokenId;
    }
}
