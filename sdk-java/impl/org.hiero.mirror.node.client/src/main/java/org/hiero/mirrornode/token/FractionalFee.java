package org.hiero.mirrornode.token;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

public record FractionalFee(
        long numeratorAmount,
        long denominatorAmount,
        @Nullable AccountId collectorAccountId,
        @Nullable Address denominatingTokenId) {

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

        /// Creates the `FractionalFee` of the values set.
        ///
        /// @return the new `FractionalFee`
        /// @throws NullPointerException if a required value is not set
        public FractionalFee build() {
            return new FractionalFee(
                    Objects.requireNonNull(numeratorAmount, "numeratorAmount must not be null"),
                    Objects.requireNonNull(denominatorAmount, "denominatorAmount must not be null"),
                    collectorAccountId,
                    denominatingTokenId);
        }
    }
}
