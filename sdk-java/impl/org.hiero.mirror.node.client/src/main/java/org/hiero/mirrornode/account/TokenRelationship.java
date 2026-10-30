package org.hiero.mirrornode.account;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// The relationship of an account with a token it is associated with. GET /accounts/{id}/tokens.
///
/// @param automaticAssociation true if the token was associated automatically
/// @param balance balance in the token's smallest unit
/// @param createdTimestamp when the association was created
/// @param decimals the decimals of the token
public record TokenRelationship(
        @Nullable Boolean automaticAssociation,
        long balance,
        @Nullable ZonedDateTime createdTimestamp,
        @Nullable Long decimals,
        @Nullable TokenFreezeStatus freezeStatus,
        TokenKycStatus kycStatus,
        Address tokenId) {

    /// Creates a new `TokenRelationship`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenRelationship {
        Objects.requireNonNull(kycStatus, "kycStatus must not be null");
        Objects.requireNonNull(tokenId, "tokenId must not be null");
    }

    /// Returns a builder for a new `TokenRelationship`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenRelationship`.
    ///
    /// `build()` creates the `TokenRelationship` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Boolean automaticAssociation;
        private @Nullable Long balance;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable Long decimals;
        private @Nullable TokenFreezeStatus freezeStatus;
        private @Nullable TokenKycStatus kycStatus;
        private @Nullable Address tokenId;

        private Builder() {
            // created by TokenRelationship.builder()
        }

        /// Sets the `automaticAssociation`.
        ///
        /// true if the token was associated automatically
        ///
        /// @param automaticAssociation the value
        /// @return this builder
        public Builder automaticAssociation(final @Nullable Boolean automaticAssociation) {
            this.automaticAssociation = automaticAssociation;
            return this;
        }

        /// Sets the `balance`.
        ///
        /// balance in the token's smallest unit
        ///
        /// @param balance the value
        /// @return this builder
        public Builder balance(final long balance) {
            this.balance = balance;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// when the association was created
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `decimals`.
        ///
        /// the decimals of the token
        ///
        /// @param decimals the value
        /// @return this builder
        public Builder decimals(final @Nullable Long decimals) {
            this.decimals = decimals;
            return this;
        }

        /// Sets the `freezeStatus`.
        ///
        /// @param freezeStatus the value
        /// @return this builder
        public Builder freezeStatus(final @Nullable TokenFreezeStatus freezeStatus) {
            this.freezeStatus = freezeStatus;
            return this;
        }

        /// Sets the `kycStatus`.
        ///
        /// @param kycStatus the value
        /// @return this builder
        public Builder kycStatus(final TokenKycStatus kycStatus) {
            this.kycStatus = kycStatus;
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

        /// Creates the `TokenRelationship` of the values set.
        ///
        /// @return the new `TokenRelationship`
        /// @throws NullPointerException if a required value is not set
        public TokenRelationship build() {
            return new TokenRelationship(
                    automaticAssociation,
                    Objects.requireNonNull(balance, "balance must not be null"),
                    createdTimestamp,
                    decimals,
                    freezeStatus,
                    Objects.requireNonNull(kycStatus, "kycStatus must not be null"),
                    Objects.requireNonNull(tokenId, "tokenId must not be null"));
        }
    }
}
