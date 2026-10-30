package org.hiero.mirrornode.token;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// A fee that charges a fraction of the NFT exchange value, with an optional fixed fallback fee if no value is
/// exchanged.
///
/// @param fallbackFeeAmount the amount of the fallback fee; null without one
/// @param denominatingTokenId the token of the fallback fee; null if it is charged in HBAR or there is none
/// @param allCollectorsAreExempt whether all fee collectors are exempt from the fee
public record RoyaltyFee(
        long numeratorAmount,
        long denominatorAmount,
        @Nullable Long fallbackFeeAmount,
        @Nullable AccountId collectorAccountId,
        @Nullable Address denominatingTokenId,
        boolean allCollectorsAreExempt) {

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
        private @Nullable Boolean allCollectorsAreExempt;

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
        /// the amount of the fallback fee; null without one
        ///
        /// @param fallbackFeeAmount the value
        /// @return this builder
        public Builder fallbackFeeAmount(final @Nullable Long fallbackFeeAmount) {
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
        /// the token of the fallback fee; null if it is charged in HBAR or there is none
        ///
        /// @param denominatingTokenId the value
        /// @return this builder
        public Builder denominatingTokenId(final @Nullable Address denominatingTokenId) {
            this.denominatingTokenId = denominatingTokenId;
            return this;
        }

        /// Sets the `allCollectorsAreExempt`.
        ///
        /// whether all fee collectors are exempt from the fee
        ///
        /// @param allCollectorsAreExempt the value
        /// @return this builder
        public Builder allCollectorsAreExempt(final boolean allCollectorsAreExempt) {
            this.allCollectorsAreExempt = allCollectorsAreExempt;
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
                    fallbackFeeAmount,
                    collectorAccountId,
                    denominatingTokenId,
                    Objects.requireNonNull(allCollectorsAreExempt, "allCollectorsAreExempt must not be null"));
        }
    }
}
