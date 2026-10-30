package org.hiero.mirrornode.transaction;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// The maximum custom fee the payer of a transaction was willing to pay in one unit (HIP-991).
///
/// @param denominatingTokenId the token of the limit; absent for HBAR
public record CustomFeeLimit(
        @Nullable AccountId accountId,
        long amount,
        @Nullable Address denominatingTokenId) {

    /// Returns a builder for a new `CustomFeeLimit`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `CustomFeeLimit`.
    ///
    /// `build()` creates the `CustomFeeLimit` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId accountId;
        private @Nullable Long amount;
        private @Nullable Address denominatingTokenId;

        private Builder() {
            // created by CustomFeeLimit.builder()
        }

        /// Sets the `accountId`.
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final @Nullable AccountId accountId) {
            this.accountId = accountId;
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

        /// Sets the `denominatingTokenId`.
        ///
        /// the token of the limit; absent for HBAR
        ///
        /// @param denominatingTokenId the value
        /// @return this builder
        public Builder denominatingTokenId(final @Nullable Address denominatingTokenId) {
            this.denominatingTokenId = denominatingTokenId;
            return this;
        }

        /// Creates the `CustomFeeLimit` of the values set.
        ///
        /// @return the new `CustomFeeLimit`
        /// @throws NullPointerException if a required value is not set
        public CustomFeeLimit build() {
            return new CustomFeeLimit(
                    accountId,
                    Objects.requireNonNull(amount, "amount must not be null"),
                    denominatingTokenId);
        }
    }
}
