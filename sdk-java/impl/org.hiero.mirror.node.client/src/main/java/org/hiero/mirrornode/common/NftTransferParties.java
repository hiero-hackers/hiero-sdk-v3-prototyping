package org.hiero.mirrornode.common;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// Grouping of sender, receiver, and token for an NFT transfer
///
/// @param senderAccountId absent for a mint
/// @param receiverAccountId absent for a burn
public record NftTransferParties(
        @Nullable AccountId senderAccountId,
        @Nullable AccountId receiverAccountId,
        Address tokenId) {

    /// Creates a new `NftTransferParties`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NftTransferParties {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
    }

    /// Returns a builder for a new `NftTransferParties`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NftTransferParties`.
    ///
    /// `build()` creates the `NftTransferParties` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId senderAccountId;
        private @Nullable AccountId receiverAccountId;
        private @Nullable Address tokenId;

        private Builder() {
            // created by NftTransferParties.builder()
        }

        /// Sets the `senderAccountId`.
        ///
        /// absent for a mint
        ///
        /// @param senderAccountId the value
        /// @return this builder
        public Builder senderAccountId(final @Nullable AccountId senderAccountId) {
            this.senderAccountId = senderAccountId;
            return this;
        }

        /// Sets the `receiverAccountId`.
        ///
        /// absent for a burn
        ///
        /// @param receiverAccountId the value
        /// @return this builder
        public Builder receiverAccountId(final @Nullable AccountId receiverAccountId) {
            this.receiverAccountId = receiverAccountId;
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

        /// Creates the `NftTransferParties` of the values set.
        ///
        /// @return the new `NftTransferParties`
        /// @throws NullPointerException if a required value is not set
        public NftTransferParties build() {
            return new NftTransferParties(
                    senderAccountId,
                    receiverAccountId,
                    Objects.requireNonNull(tokenId, "tokenId must not be null"));
        }
    }
}
