package org.hiero.mirrornode.token;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// A fee that charges a fraction of the transferred units of a fungible token.
///
/// @param allCollectorsAreExempt whether all fee collectors are exempt from the fee
/// @param maximum the maximum fee; null if there is none
/// @param minimum the minimum fee
/// @param netOfTransfers whether the fee is charged to the receiver instead of the sender
public record FractionalFee(
        long numeratorAmount,
        long denominatorAmount,
        @Nullable AccountId collectorAccountId,
        @Nullable Address denominatingTokenId,
        boolean allCollectorsAreExempt,
        @Nullable Long maximum,
        long minimum,
        boolean netOfTransfers) {

    /// Returns a builder for a new `FractionalFee`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FractionalFee`.
    ///
    /// `build()` creates the `FractionalFee` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long numeratorAmount;
        private @Nullable Long denominatorAmount;
        private @Nullable AccountId collectorAccountId;
        private @Nullable Address denominatingTokenId;
        private @Nullable Boolean allCollectorsAreExempt;
        private @Nullable Long maximum;
        private @Nullable Long minimum;
        private @Nullable Boolean netOfTransfers;

        private Builder() {
            // created by FractionalFee.builder()
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

        /// Sets the `maximum`.
        ///
        /// the maximum fee; null if there is none
        ///
        /// @param maximum the value
        /// @return this builder
        public Builder maximum(final @Nullable Long maximum) {
            this.maximum = maximum;
            return this;
        }

        /// Sets the `minimum`.
        ///
        /// the minimum fee
        ///
        /// @param minimum the value
        /// @return this builder
        public Builder minimum(final long minimum) {
            this.minimum = minimum;
            return this;
        }

        /// Sets the `netOfTransfers`.
        ///
        /// whether the fee is charged to the receiver instead of the sender
        ///
        /// @param netOfTransfers the value
        /// @return this builder
        public Builder netOfTransfers(final boolean netOfTransfers) {
            this.netOfTransfers = netOfTransfers;
            return this;
        }

        /// Creates the `FractionalFee` of the values set.
        ///
        /// @return the new `FractionalFee`
        /// @throws NullPointerException if a required value is not set
        public FractionalFee build() {
            return new FractionalFee(
                    Objects.requireNonNull(numeratorAmount, "numeratorAmount must not be null"),
                    Objects.requireNonNull(denominatorAmount, "denominatorAmount must not be null"),
                    collectorAccountId,
                    denominatingTokenId,
                    Objects.requireNonNull(allCollectorsAreExempt, "allCollectorsAreExempt must not be null"),
                    maximum,
                    Objects.requireNonNull(minimum, "minimum must not be null"),
                    Objects.requireNonNull(netOfTransfers, "netOfTransfers must not be null"));
        }
    }
}
