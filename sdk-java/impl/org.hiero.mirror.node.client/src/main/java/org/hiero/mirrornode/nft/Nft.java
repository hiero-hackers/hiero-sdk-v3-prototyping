package org.hiero.mirrornode.nft;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// @param accountId the owner; null for a burned NFT
/// @param delegatingSpender the account that granted the spender its allowance
/// @param spender the account allowed to transfer the NFT
/// @param deleted whether the NFT or its token has been deleted
public record Nft(
        Address tokenId,
        long serialNumber,
        @Nullable AccountId accountId,
        byte @Nullable [] metadata,
        @Nullable ZonedDateTime createdTimestamp,
        @Nullable ZonedDateTime modifiedTimestamp,
        @Nullable AccountId delegatingSpender,
        @Nullable AccountId spender,
        boolean deleted) {

    /// Creates a new `Nft`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Nft {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        metadata = metadata == null ? null : metadata.clone();
    }

    /// Returns a copy of the `metadata` array.
    @Override
    public byte @Nullable [] metadata() {
        return metadata == null ? null : metadata.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof Nft other
            && Objects.equals(tokenId, other.tokenId)
            && serialNumber == other.serialNumber
            && Objects.equals(accountId, other.accountId)
            && Arrays.equals(metadata, other.metadata)
            && Objects.equals(createdTimestamp, other.createdTimestamp)
            && Objects.equals(modifiedTimestamp, other.modifiedTimestamp)
            && Objects.equals(delegatingSpender, other.delegatingSpender)
            && Objects.equals(spender, other.spender)
            && deleted == other.deleted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId, serialNumber, accountId, Arrays.hashCode(metadata), createdTimestamp, modifiedTimestamp, delegatingSpender, spender, deleted);
    }

    @Override
    public String toString() {
        return "Nft[tokenId=" + tokenId + ", serialNumber=" + serialNumber + ", accountId=" + accountId + ", metadata=" + (metadata == null ? "null" : "byte[" + metadata.length + "]") + ", createdTimestamp=" + createdTimestamp + ", modifiedTimestamp=" + modifiedTimestamp + ", delegatingSpender=" + delegatingSpender + ", spender=" + spender + ", deleted=" + deleted + "]";
    }

    /// Returns a builder for a new `Nft`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Nft`.
    ///
    /// `build()` creates the `Nft` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable Long serialNumber;
        private @Nullable AccountId accountId;
        private byte @Nullable [] metadata;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable ZonedDateTime modifiedTimestamp;
        private @Nullable AccountId delegatingSpender;
        private @Nullable AccountId spender;
        private @Nullable Boolean deleted;

        private Builder() {
            // created by Nft.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `serialNumber`.
        ///
        /// @param serialNumber the value
        /// @return this builder
        public Builder serialNumber(final long serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// the owner; null for a burned NFT
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final @Nullable AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `metadata`.
        ///
        /// @param metadata the value
        /// @return this builder
        public Builder metadata(final byte @Nullable [] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `modifiedTimestamp`.
        ///
        /// @param modifiedTimestamp the value
        /// @return this builder
        public Builder modifiedTimestamp(final @Nullable ZonedDateTime modifiedTimestamp) {
            this.modifiedTimestamp = modifiedTimestamp;
            return this;
        }

        /// Sets the `delegatingSpender`.
        ///
        /// the account that granted the spender its allowance
        ///
        /// @param delegatingSpender the value
        /// @return this builder
        public Builder delegatingSpender(final @Nullable AccountId delegatingSpender) {
            this.delegatingSpender = delegatingSpender;
            return this;
        }

        /// Sets the `spender`.
        ///
        /// the account allowed to transfer the NFT
        ///
        /// @param spender the value
        /// @return this builder
        public Builder spender(final @Nullable AccountId spender) {
            this.spender = spender;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// whether the NFT or its token has been deleted
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /// Creates the `Nft` of the values set.
        ///
        /// @return the new `Nft`
        /// @throws NullPointerException if a required value is not set
        public Nft build() {
            return new Nft(
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(serialNumber, "serialNumber must not be null"),
                    accountId,
                    metadata,
                    createdTimestamp,
                    modifiedTimestamp,
                    delegatingSpender,
                    spender,
                    Objects.requireNonNull(deleted, "deleted must not be null"));
        }
    }
}
