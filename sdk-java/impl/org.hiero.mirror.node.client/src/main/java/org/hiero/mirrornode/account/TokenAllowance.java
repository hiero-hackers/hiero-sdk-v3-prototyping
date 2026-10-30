package org.hiero.mirrornode.account;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// A fungible-token spending allowance the account granted. GET /accounts/{id}/allowances/tokens.
///
/// @param amount remaining approved amount (smallest unit)
/// @param amountGranted originally approved amount
/// @param timestamp the period this version of the allowance is valid for
public record TokenAllowance(
        AccountId owner,
        AccountId spender,
        Address tokenId,
        long amount,
        long amountGranted,
        TimestampRange timestamp) {

    /// Creates a new `TokenAllowance`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenAllowance {
        Objects.requireNonNull(owner, "owner must not be null");
        Objects.requireNonNull(spender, "spender must not be null");
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Returns a builder for a new `TokenAllowance`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenAllowance`.
    ///
    /// `build()` creates the `TokenAllowance` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId owner;
        private @Nullable AccountId spender;
        private @Nullable Address tokenId;
        private @Nullable Long amount;
        private @Nullable Long amountGranted;
        private @Nullable TimestampRange timestamp;

        private Builder() {
            // created by TokenAllowance.builder()
        }

        /// Sets the `owner`.
        ///
        /// @param owner the value
        /// @return this builder
        public Builder owner(final AccountId owner) {
            this.owner = owner;
            return this;
        }

        /// Sets the `spender`.
        ///
        /// @param spender the value
        /// @return this builder
        public Builder spender(final AccountId spender) {
            this.spender = spender;
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
        /// remaining approved amount (smallest unit)
        ///
        /// @param amount the value
        /// @return this builder
        public Builder amount(final long amount) {
            this.amount = amount;
            return this;
        }

        /// Sets the `amountGranted`.
        ///
        /// originally approved amount
        ///
        /// @param amountGranted the value
        /// @return this builder
        public Builder amountGranted(final long amountGranted) {
            this.amountGranted = amountGranted;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the period this version of the allowance is valid for
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final TimestampRange timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `TokenAllowance` of the values set.
        ///
        /// @return the new `TokenAllowance`
        /// @throws NullPointerException if a required value is not set
        public TokenAllowance build() {
            return new TokenAllowance(
                    Objects.requireNonNull(owner, "owner must not be null"),
                    Objects.requireNonNull(spender, "spender must not be null"),
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(amount, "amount must not be null"),
                    Objects.requireNonNull(amountGranted, "amountGranted must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
