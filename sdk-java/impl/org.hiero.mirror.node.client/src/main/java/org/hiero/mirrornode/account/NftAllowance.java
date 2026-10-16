package org.hiero.mirrornode.account;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// An NFT approved-for-all allowance the account granted. GET /accounts/{id}/allowances/nfts.
///
/// @param approvedForAll true → spender may transfer every serial of the collection
public record NftAllowance(
        AccountId owner,
        AccountId spender,
        Address tokenId,
        boolean approvedForAll,
        ZonedDateTime timestamp) {

    /// Creates a new `NftAllowance`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NftAllowance {
        Objects.requireNonNull(owner, "owner must not be null");
        Objects.requireNonNull(spender, "spender must not be null");
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
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

        private @Nullable AccountId owner;
        private @Nullable AccountId spender;
        private @Nullable Address tokenId;
        private @Nullable Boolean approvedForAll;
        private @Nullable ZonedDateTime timestamp;

        private Builder() {
            // created by NftAllowance.builder()
        }

        /// Sets the `owner`.
        ///
        /// @param owner the value
        /// @return this builder
        public Builder owner(final AccountId owner) {
            this.owner = owner;
            return this;
        }

        /// Sets the `spender`.
        ///
        /// @param spender the value
        /// @return this builder
        public Builder spender(final AccountId spender) {
            this.spender = spender;
            return this;
        }

        /// Sets the `tokenId`.
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `approvedForAll`.
        ///
        /// true → spender may transfer every serial of the collection
        ///
        /// @param approvedForAll the value
        /// @return this builder
        public Builder approvedForAll(final boolean approvedForAll) {
            this.approvedForAll = approvedForAll;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `NftAllowance` of the values set.
        ///
        /// @return the new `NftAllowance`
        /// @throws NullPointerException if a required value is not set
        public NftAllowance build() {
            return new NftAllowance(
                    Objects.requireNonNull(owner, "owner must not be null"),
                    Objects.requireNonNull(spender, "spender must not be null"),
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(approvedForAll, "approvedForAll must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
