package org.hiero.consensusnode.transactions.accounts;

import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// One leg of a fungible-token transfer. The sum of `amount` per `tokenId` across all
/// `TransferTransaction.tokenTransfers` must equal zero.
///
/// @param tokenId the fungible token being moved
/// @param accountId the account being debited or credited
/// @param amount signed amount in the token's smallest unit: negative = debit, positive = credit
/// @param expectedDecimals optional protocol-side decimals check; if set and the token's actual decimals differ, the network rejects the transaction
/// @param approved when true, the debit is drawn from an allowance granted to the transaction payer; the owner (accountId) need not sign
public record TokenTransfer(
        Address tokenId,
        AccountId accountId,
        long amount,
        @Nullable Integer expectedDecimals,
        boolean approved) {

    /// Creates a new `TokenTransfer`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenTransfer {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
    }

    /// Creates a new `TokenTransfer` with the default value of `approved`.
    public TokenTransfer(
            final Address tokenId,
            final AccountId accountId,
            final long amount,
            final @Nullable Integer expectedDecimals) {
        this(tokenId, accountId, amount, expectedDecimals, false);
    }

    /// Returns a builder for a new `TokenTransfer`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenTransfer`.
    ///
    /// `build()` creates the `TokenTransfer` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable AccountId accountId;
        private @Nullable Long amount;
        private @Nullable Integer expectedDecimals;
        private boolean approved = false;

        private Builder() {
            // created by TokenTransfer.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// the fungible token being moved
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// the account being debited or credited
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `amount`.
        ///
        /// signed amount in the token's smallest unit: negative = debit, positive = credit
        ///
        /// @param amount the value
        /// @return this builder
        public Builder amount(final long amount) {
            this.amount = amount;
            return this;
        }

        /// Sets the `expectedDecimals`.
        ///
        /// optional protocol-side decimals check; if set and the token's actual decimals differ, the network rejects the transaction
        ///
        /// @param expectedDecimals the value
        /// @return this builder
        public Builder expectedDecimals(final @Nullable Integer expectedDecimals) {
            this.expectedDecimals = expectedDecimals;
            return this;
        }

        /// Sets the `approved`.
        ///
        /// when true, the debit is drawn from an allowance granted to the transaction payer; the owner (accountId) need not sign
        ///
        /// @param approved the value
        /// @return this builder
        public Builder approved(final boolean approved) {
            this.approved = approved;
            return this;
        }

        /// Creates the `TokenTransfer` of the values set.
        ///
        /// @return the new `TokenTransfer`
        /// @throws NullPointerException if a required value is not set
        public TokenTransfer build() {
            return new TokenTransfer(
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    Objects.requireNonNull(amount, "amount must not be null"),
                    expectedDecimals,
                    approved);
        }
    }
}
