package org.hiero.mirrornode.common;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// @param allCollectorsAreExempt whether all fee collectors of the token are exempt from the fee
/// @param denominatingTokenId token in which the fee is charged; absent if the fee is charged in HBAR
public record FixedFee(
        boolean allCollectorsAreExempt,
        long amount,
        @Nullable AccountId collectorAccountId,
        @Nullable Address denominatingTokenId) {

    /// Creates a new `FixedFee` with the default value of `allCollectorsAreExempt`.
    public FixedFee(
            final long amount,
            final @Nullable AccountId collectorAccountId,
            final @Nullable Address denominatingTokenId) {
        this(false, amount, collectorAccountId, denominatingTokenId);
    }

    /// Returns a builder for a new `FixedFee`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FixedFee`.
    ///
    /// `build()` creates the `FixedFee` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private boolean allCollectorsAreExempt = false;
        private @Nullable Long amount;
        private @Nullable AccountId collectorAccountId;
        private @Nullable Address denominatingTokenId;

        private Builder() {
            // created by FixedFee.builder()
        }

        /// Sets the `allCollectorsAreExempt`.
        ///
        /// whether all fee collectors of the token are exempt from the fee
        ///
        /// @param allCollectorsAreExempt the value
        /// @return this builder
        public Builder allCollectorsAreExempt(final boolean allCollectorsAreExempt) {
            this.allCollectorsAreExempt = allCollectorsAreExempt;
            return this;
        }

        /// Sets the `amount`.
        ///
        /// @param amount the value
        /// @return this builder
        public Builder amount(final long amount) {
            this.amount = amount;
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
        /// token in which the fee is charged; absent if the fee is charged in HBAR
        ///
        /// @param denominatingTokenId the value
        /// @return this builder
        public Builder denominatingTokenId(final @Nullable Address denominatingTokenId) {
            this.denominatingTokenId = denominatingTokenId;
            return this;
        }

        /// Creates the `FixedFee` of the values set.
        ///
        /// @return the new `FixedFee`
        /// @throws NullPointerException if a required value is not set
        public FixedFee build() {
            return new FixedFee(
                    allCollectorsAreExempt,
                    Objects.requireNonNull(amount, "amount must not be null"),
                    collectorAccountId,
                    denominatingTokenId);
        }
    }
}
