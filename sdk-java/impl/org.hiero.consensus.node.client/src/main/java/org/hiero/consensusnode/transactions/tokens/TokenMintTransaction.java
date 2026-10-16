package org.hiero.consensusnode.transactions.tokens;

import java.time.Duration;
import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Mints new units of a token, crediting them to the treasury account. Requires the token's
/// supplyAuthority to sign. Which of the two mint transactions applies follows from the token's
/// type: TokenMintFungibleTransaction for FUNGIBLE_COMMON, TokenMintNftTransaction for
/// NON_FUNGIBLE_UNIQUE.
public abstract sealed class TokenMintTransaction<ReceiptT extends Receipt, Self extends TokenMintTransaction<ReceiptT, Self>> extends Transaction<ReceiptT, Self> permits TokenMintFungibleTransaction, TokenMintNftTransaction {

    private final Address tokenId;

    /// Creates a new `TokenMintTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected TokenMintTransaction(final Address tokenId) {
        super();
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        this.tokenId = tokenId;
    }

    /// the token to mint
    public Address tokenId() {
        return tokenId;
    }
}
