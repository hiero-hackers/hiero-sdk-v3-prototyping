package org.hiero.mirrornode.nft;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.transaction.TransactionType;
import org.jspecify.annotations.Nullable;

/// One transfer of an NFT (including its mint and its burn) in the transfer history of the NFT.
///
/// @param isApproval whether the transfer used an allowance
/// @param receiverAccountId null for a burn
/// @param senderAccountId null for a mint
public record NftTransactionTransfer(
        ZonedDateTime consensusTimestamp,
        boolean isApproval,
        int nonce,
        @Nullable AccountId receiverAccountId,
        @Nullable AccountId senderAccountId,
        TransactionId transactionId,
        TransactionType type) {

    /// Creates a new `NftTransactionTransfer`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public NftTransactionTransfer {
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        if (nonce < 0) {
            throw new IllegalArgumentException("nonce must be at least 0");
        }
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    /// Returns a builder for a new `NftTransactionTransfer`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NftTransactionTransfer`.
    ///
    /// `build()` creates the `NftTransactionTransfer` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ZonedDateTime consensusTimestamp;
        private @Nullable Boolean isApproval;
        private @Nullable Integer nonce;
        private @Nullable AccountId receiverAccountId;
        private @Nullable AccountId senderAccountId;
        private @Nullable TransactionId transactionId;
        private @Nullable TransactionType type;

        private Builder() {
            // created by NftTransactionTransfer.builder()
        }

        /// Sets the `consensusTimestamp`.
        ///
        /// @param consensusTimestamp the value
        /// @return this builder
        public Builder consensusTimestamp(final ZonedDateTime consensusTimestamp) {
            this.consensusTimestamp = consensusTimestamp;
            return this;
        }

        /// Sets the `isApproval`.
        ///
        /// whether the transfer used an allowance
        ///
        /// @param isApproval the value
        /// @return this builder
        public Builder isApproval(final boolean isApproval) {
            this.isApproval = isApproval;
            return this;
        }

        /// Sets the `nonce`.
        ///
        /// @param nonce the value
        /// @return this builder
        public Builder nonce(final int nonce) {
            this.nonce = nonce;
            return this;
        }

        /// Sets the `receiverAccountId`.
        ///
        /// null for a burn
        ///
        /// @param receiverAccountId the value
        /// @return this builder
        public Builder receiverAccountId(final @Nullable AccountId receiverAccountId) {
            this.receiverAccountId = receiverAccountId;
            return this;
        }

        /// Sets the `senderAccountId`.
        ///
        /// null for a mint
        ///
        /// @param senderAccountId the value
        /// @return this builder
        public Builder senderAccountId(final @Nullable AccountId senderAccountId) {
            this.senderAccountId = senderAccountId;
            return this;
        }

        /// Sets the `transactionId`.
        ///
        /// @param transactionId the value
        /// @return this builder
        public Builder transactionId(final TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /// Sets the `type`.
        ///
        /// @param type the value
        /// @return this builder
        public Builder type(final TransactionType type) {
            this.type = type;
            return this;
        }

        /// Creates the `NftTransactionTransfer` of the values set.
        ///
        /// @return the new `NftTransactionTransfer`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public NftTransactionTransfer build() {
            return new NftTransactionTransfer(
                    Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null"),
                    Objects.requireNonNull(isApproval, "isApproval must not be null"),
                    Objects.requireNonNull(nonce, "nonce must not be null"),
                    receiverAccountId,
                    senderAccountId,
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(type, "type must not be null"));
        }
    }
}
