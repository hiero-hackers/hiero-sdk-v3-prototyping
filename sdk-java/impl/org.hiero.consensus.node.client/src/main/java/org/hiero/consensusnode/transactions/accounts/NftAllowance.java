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

    /// Returns a builder for a new `NftAllowance`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NftAllowance`.
    ///
    /// `build()` creates the `NftAllowance` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable AccountId ownerAccountId;
        private @Nullable AccountId spenderAccountId;
        private @Nullable NftGrant grant;
        private @Nullable AccountId delegatingSpender;

        private Builder() {
            // created by NftAllowance.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `ownerAccountId`.
        ///
        /// @param ownerAccountId the value
        /// @return this builder
        public Builder ownerAccountId(final AccountId ownerAccountId) {
            this.ownerAccountId = ownerAccountId;
            return this;
        }

        /// Sets the `spenderAccountId`.
        ///
        /// @param spenderAccountId the value
        /// @return this builder
        public Builder spenderAccountId(final AccountId spenderAccountId) {
            this.spenderAccountId = spenderAccountId;
            return this;
        }

        /// Sets the `grant`.
        ///
        /// @param grant the value
        /// @return this builder
        public Builder grant(final NftGrant grant) {
            this.grant = grant;
            return this;
        }

        /// Sets the `delegatingSpender`.
        ///
        /// @param delegatingSpender the value
        /// @return this builder
        public Builder delegatingSpender(final @Nullable AccountId delegatingSpender) {
            this.delegatingSpender = delegatingSpender;
            return this;
        }

        /// Creates the `NftAllowance` of the values set.
        ///
        /// @return the new `NftAllowance`
        /// @throws NullPointerException if a required value is not set
        public NftAllowance build() {
            return new NftAllowance(
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(ownerAccountId, "ownerAccountId must not be null"),
                    Objects.requireNonNull(spenderAccountId, "spenderAccountId must not be null"),
                    Objects.requireNonNull(grant, "grant must not be null"),
                    delegatingSpender);
        }
    }
}
