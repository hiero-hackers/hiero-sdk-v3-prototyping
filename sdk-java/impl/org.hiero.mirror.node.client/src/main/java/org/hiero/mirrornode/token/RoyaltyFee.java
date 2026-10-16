package org.hiero.mirrornode.token;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

public record RoyaltyFee(
        long numeratorAmount,
        long denominatorAmount,
        long fallbackFeeAmount,
        @Nullable AccountId collectorAccountId,
        @Nullable Address denominatingTokenId) {

    /// Returns a builder for a new `RoyaltyFee`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `RoyaltyFee`.
    ///
    /// `build()` creates the `RoyaltyFee` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long numeratorAmount;
        private @Nullable Long denominatorAmount;
        private @Nullable Long fallbackFeeAmount;
        private @Nullable AccountId collectorAccountId;
        private @Nullable Address denominatingTokenId;

        private Builder() {
            // created by RoyaltyFee.builder()
        }

        /// Sets the `numeratorAmount`.
        ///
        /// @param numeratorAmount the value
        /// @return this builder
        public Builder numeratorAmount(final long numeratorAmount) {
            this.numeratorAmount = numeratorAmount;
            return this;
        }

        /// Sets the `denominatorAmount`.
        ///
        /// @param denominatorAmount the value
        /// @return this builder
        public Builder denominatorAmount(final long denominatorAmount) {
            this.denominatorAmount = denominatorAmount;
            return this;
        }

        /// Sets the `fallbackFeeAmount`.
        ///
        /// @param fallbackFeeAmount the value
        /// @return this builder
        public Builder fallbackFeeAmount(final long fallbackFeeAmount) {
            this.fallbackFeeAmount = fallbackFeeAmount;
            return this;
        }

        /// Sets the `collectorAccountId`.
        ///
        /// @param collectorAccountId the value
        /// @return this builder
        public Builder collectorAccountId(final @Nullable AccountId collectorAccountId) {
            this.collectorAccountId = collectorAccountId;
            return this;
        }

        /// Sets the `denominatingTokenId`.
        ///
        /// @param denominatingTokenId the value
        /// @return this builder
        public Builder denominatingTokenId(final @Nullable Address denominatingTokenId) {
            this.denominatingTokenId = denominatingTokenId;
            return this;
        }

        /// Creates the `RoyaltyFee` of the values set.
        ///
        /// @return the new `RoyaltyFee`
        /// @throws NullPointerException if a required value is not set
        public RoyaltyFee build() {
            return new RoyaltyFee(
                    Objects.requireNonNull(numeratorAmount, "numeratorAmount must not be null"),
                    Objects.requireNonNull(denominatorAmount, "denominatorAmount must not be null"),
                    Objects.requireNonNull(fallbackFeeAmount, "fallbackFeeAmount must not be null"),
                    collectorAccountId,
                    denominatingTokenId);
        }
    }
}
