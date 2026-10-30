package org.hiero.mirrornode.transaction;

import java.util.List;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// A custom fee that was charged for a transaction.
///
/// @param amount in tinybars or in the smallest unit of the token
/// @param effectivePayerAccountIds the accounts that paid the fee
/// @param tokenId the token the fee was charged in; absent for HBAR
public record AssessedCustomFee(
        long amount,
        @Nullable AccountId collectorAccountId,
        List<AccountId> effectivePayerAccountIds,
        @Nullable Address tokenId) {

    /// Creates a new `AssessedCustomFee`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AssessedCustomFee {
        Objects.requireNonNull(effectivePayerAccountIds, "effectivePayerAccountIds must not be null");
        effectivePayerAccountIds = List.copyOf(effectivePayerAccountIds);
    }

    /// Creates a new `AssessedCustomFee` with the default value of `effectivePayerAccountIds`.
    public AssessedCustomFee(
            final long amount,
            final @Nullable AccountId collectorAccountId,
            final @Nullable Address tokenId) {
        this(amount, collectorAccountId, List.of(), tokenId);
    }

    /// Returns a builder for a new `AssessedCustomFee`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AssessedCustomFee`.
    ///
    /// `build()` creates the `AssessedCustomFee` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long amount;
        private @Nullable AccountId collectorAccountId;
        private List<AccountId> effectivePayerAccountIds = List.of();
        private @Nullable Address tokenId;

        private Builder() {
            // created by AssessedCustomFee.builder()
        }

        /// Sets the `amount`.
        ///
        /// in tinybars or in the smallest unit of the token
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

        /// Sets the `effectivePayerAccountIds`.
        ///
        /// the accounts that paid the fee
        ///
        /// @param effectivePayerAccountIds the value
        /// @return this builder
        public Builder effectivePayerAccountIds(final List<AccountId> effectivePayerAccountIds) {
            this.effectivePayerAccountIds = effectivePayerAccountIds;
            return this;
        }

        /// Sets the `tokenId`.
        ///
        /// the token the fee was charged in; absent for HBAR
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final @Nullable Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Creates the `AssessedCustomFee` of the values set.
        ///
        /// @return the new `AssessedCustomFee`
        /// @throws NullPointerException if a required value is not set
        public AssessedCustomFee build() {
            return new AssessedCustomFee(
                    Objects.requireNonNull(amount, "amount must not be null"),
                    collectorAccountId,
                    effectivePayerAccountIds,
                    tokenId);
        }
    }
}
