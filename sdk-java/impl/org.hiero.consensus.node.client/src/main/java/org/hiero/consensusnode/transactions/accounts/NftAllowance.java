package org.hiero.consensusnode.transactions.accounts;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// Allowance for an NFT collection, granting either named serials or the whole collection.
///
/// `delegatingSpender` supports HIP-336: a spender that already holds an `NftCollectionGrant` may
/// delegate per-serial rights onward without involving the original owner. When set, the
/// transaction must be signed by `delegatingSpender`, not by `ownerAccountId`.
public record NftAllowance(
        Address tokenId,
        AccountId ownerAccountId,
        AccountId spenderAccountId,
        NftGrant grant,
        @Nullable AccountId delegatingSpender) {

    /// Creates a new `NftAllowance`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NftAllowance {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId must not be null");
        Objects.requireNonNull(spenderAccountId, "spenderAccountId must not be null");
        Objects.requireNonNull(grant, "grant must not be null");
    }
}
