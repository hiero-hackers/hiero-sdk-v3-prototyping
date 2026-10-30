package org.hiero.mirrornode.account;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// A pending airdrop (HIP-904) the account sent (outstanding) or is to receive (pending).
/// Exactly one of `amount` (fungible) / `serialNumber` (NFT) is set.
///
/// @param amount fungible amount (smallest unit); null for an NFT airdrop
/// @param serialNumber NFT serial; null for a fungible airdrop
/// @param timestamp the period this version of the airdrop is valid for
public record TokenAirdrop(
        AccountId senderId,
        AccountId receiverId,
        Address tokenId,
        @Nullable Long amount,
        @Nullable Long serialNumber,
        TimestampRange timestamp) {

    /// Creates a new `TokenAirdrop`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenAirdrop {
        Objects.requireNonNull(senderId, "senderId must not be null");
        Objects.requireNonNull(receiverId, "receiverId must not be null");
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Returns a builder for a new `TokenAirdrop`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenAirdrop`.
    ///
    /// `build()` creates the `TokenAirdrop` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId senderId;
        private @Nullable AccountId receiverId;
        private @Nullable Address tokenId;
        private @Nullable Long amount;
        private @Nullable Long serialNumber;
        private @Nullable TimestampRange timestamp;

        private Builder() {
            // created by TokenAirdrop.builder()
        }

        /// Sets the `senderId`.
        ///
        /// @param senderId the value
        /// @return this builder
        public Builder senderId(final AccountId senderId) {
            this.senderId = senderId;
            return this;
        }

        /// Sets the `receiverId`.
        ///
        /// @param receiverId the value
        /// @return this builder
        public Builder receiverId(final AccountId receiverId) {
            this.receiverId = receiverId;
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

        /// Sets the `amount`.
        ///
        /// fungible amount (smallest unit); null for an NFT airdrop
        ///
        /// @param amount the value
        /// @return this builder
        public Builder amount(final @Nullable Long amount) {
            this.amount = amount;
            return this;
        }

        /// Sets the `serialNumber`.
        ///
        /// NFT serial; null for a fungible airdrop
        ///
        /// @param serialNumber the value
        /// @return this builder
        public Builder serialNumber(final @Nullable Long serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the period this version of the airdrop is valid for
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final TimestampRange timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `TokenAirdrop` of the values set.
        ///
        /// @return the new `TokenAirdrop`
        /// @throws NullPointerException if a required value is not set
        public TokenAirdrop build() {
            return new TokenAirdrop(
                    Objects.requireNonNull(senderId, "senderId must not be null"),
                    Objects.requireNonNull(receiverId, "receiverId must not be null"),
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    amount,
                    serialNumber,
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
