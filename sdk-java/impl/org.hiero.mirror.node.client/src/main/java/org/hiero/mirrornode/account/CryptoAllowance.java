package org.hiero.mirrornode.account;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// An HBAR spending allowance the account granted. GET /accounts/{id}/allowances/crypto.
///
/// @param amount remaining approved tinybars
/// @param amountGranted originally approved tinybars
/// @param timestamp when the allowance was granted / last modified
public record CryptoAllowance(
        AccountId owner,
        AccountId spender,
        long amount,
        long amountGranted,
        ZonedDateTime timestamp) {

    /// Creates a new `CryptoAllowance`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public CryptoAllowance {
        Objects.requireNonNull(owner, "owner must not be null");
        Objects.requireNonNull(spender, "spender must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Returns a builder for a new `CryptoAllowance`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `CryptoAllowance`.
    ///
    /// `build()` creates the `CryptoAllowance` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId owner;
        private @Nullable AccountId spender;
        private @Nullable Long amount;
        private @Nullable Long amountGranted;
        private @Nullable ZonedDateTime timestamp;

        private Builder() {
            // created by CryptoAllowance.builder()
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

        /// Sets the `amount`.
        ///
        /// remaining approved tinybars
        ///
        /// @param amount the value
        /// @return this builder
        public Builder amount(final long amount) {
            this.amount = amount;
            return this;
        }

        /// Sets the `amountGranted`.
        ///
        /// originally approved tinybars
        ///
        /// @param amountGranted the value
        /// @return this builder
        public Builder amountGranted(final long amountGranted) {
            this.amountGranted = amountGranted;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// when the allowance was granted / last modified
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `CryptoAllowance` of the values set.
        ///
        /// @return the new `CryptoAllowance`
        /// @throws NullPointerException if a required value is not set
        public CryptoAllowance build() {
            return new CryptoAllowance(
                    Objects.requireNonNull(owner, "owner must not be null"),
                    Objects.requireNonNull(spender, "spender must not be null"),
                    Objects.requireNonNull(amount, "amount must not be null"),
                    Objects.requireNonNull(amountGranted, "amountGranted must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
