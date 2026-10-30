package org.hiero.mirrornode.topic;

import java.util.Objects;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// The chunk of a message that was submitted in several chunks.
///
/// @param initialTransactionId the transaction of the first chunk
/// @param nonce the nonce of the initial transaction
/// @param number the number of this chunk, starting at 1
/// @param total the number of chunks of the message
/// @param scheduled whether the initial transaction was scheduled
public record ChunkInfo(
        TransactionId initialTransactionId,
        int nonce,
        int number,
        int total,
        boolean scheduled) {

    /// Creates a new `ChunkInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ChunkInfo {
        Objects.requireNonNull(initialTransactionId, "initialTransactionId must not be null");
    }

    /// Returns a builder for a new `ChunkInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ChunkInfo`.
    ///
    /// `build()` creates the `ChunkInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId initialTransactionId;
        private @Nullable Integer nonce;
        private @Nullable Integer number;
        private @Nullable Integer total;
        private @Nullable Boolean scheduled;

        private Builder() {
            // created by ChunkInfo.builder()
        }

        /// Sets the `initialTransactionId`.
        ///
        /// the transaction of the first chunk
        ///
        /// @param initialTransactionId the value
        /// @return this builder
        public Builder initialTransactionId(final TransactionId initialTransactionId) {
            this.initialTransactionId = initialTransactionId;
            return this;
        }

        /// Sets the `nonce`.
        ///
        /// the nonce of the initial transaction
        ///
        /// @param nonce the value
        /// @return this builder
        public Builder nonce(final int nonce) {
            this.nonce = nonce;
            return this;
        }

        /// Sets the `number`.
        ///
        /// the number of this chunk, starting at 1
        ///
        /// @param number the value
        /// @return this builder
        public Builder number(final int number) {
            this.number = number;
            return this;
        }

        /// Sets the `total`.
        ///
        /// the number of chunks of the message
        ///
        /// @param total the value
        /// @return this builder
        public Builder total(final int total) {
            this.total = total;
            return this;
        }

        /// Sets the `scheduled`.
        ///
        /// whether the initial transaction was scheduled
        ///
        /// @param scheduled the value
        /// @return this builder
        public Builder scheduled(final boolean scheduled) {
            this.scheduled = scheduled;
            return this;
        }

        /// Creates the `ChunkInfo` of the values set.
        ///
        /// @return the new `ChunkInfo`
        /// @throws NullPointerException if a required value is not set
        public ChunkInfo build() {
            return new ChunkInfo(
                    Objects.requireNonNull(initialTransactionId, "initialTransactionId must not be null"),
                    Objects.requireNonNull(nonce, "nonce must not be null"),
                    Objects.requireNonNull(number, "number must not be null"),
                    Objects.requireNonNull(total, "total must not be null"),
                    Objects.requireNonNull(scheduled, "scheduled must not be null"));
        }
    }
}
