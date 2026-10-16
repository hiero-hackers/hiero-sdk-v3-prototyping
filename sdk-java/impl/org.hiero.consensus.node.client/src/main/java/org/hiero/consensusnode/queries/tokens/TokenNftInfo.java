package org.hiero.consensusnode.queries.tokens;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// Metadata of a single NFT serial. Returned by `TokenNftInfoQuery`.
///
/// @param tokenId the NFT collection
/// @param serial serial number within the collection
/// @param accountId current owner of this serial
/// @param creationTime consensus time at which this serial was minted
/// @param metadata opaque per-serial metadata set at mint (e.g. IPFS CID)
/// @param spenderId account granted an allowance to transfer this serial (HIP-376); absent if none
public record TokenNftInfo(
        Address tokenId,
        long serial,
        AccountId accountId,
        ZonedDateTime creationTime,
        byte[] metadata,
        @Nullable AccountId spenderId) {

    /// Creates a new `TokenNftInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenNftInfo {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        if (serial < 1L) {
            throw new IllegalArgumentException("serial must be at least 1");
        }
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(creationTime, "creationTime must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        metadata = metadata.clone();
    }

    /// Creates a new `TokenNftInfo` with the default value of `metadata`.
    public TokenNftInfo(
            final Address tokenId,
            final long serial,
            final AccountId accountId,
            final ZonedDateTime creationTime,
            final @Nullable AccountId spenderId) {
        this(tokenId, serial, accountId, creationTime, new byte[0], spenderId);
    }

    /// opaque per-serial metadata set at mint (e.g. IPFS CID)
    ///
    /// Returns a copy of the `metadata` array.
    @Override
    public byte[] metadata() {
        return metadata.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TokenNftInfo other
            && Objects.equals(tokenId, other.tokenId)
            && serial == other.serial
            && Objects.equals(accountId, other.accountId)
            && Objects.equals(creationTime, other.creationTime)
            && Arrays.equals(metadata, other.metadata)
            && Objects.equals(spenderId, other.spenderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId, serial, accountId, creationTime, Arrays.hashCode(metadata), spenderId);
    }

    @Override
    public String toString() {
        return "TokenNftInfo[tokenId=" + tokenId + ", serial=" + serial + ", accountId=" + accountId + ", creationTime=" + creationTime + ", metadata=" + "byte[" + metadata.length + "]" + ", spenderId=" + spenderId + "]";
    }

    /// Returns a builder for a new `TokenNftInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenNftInfo`.
    ///
    /// `build()` creates the `TokenNftInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable Long serial;
        private @Nullable AccountId accountId;
        private @Nullable ZonedDateTime creationTime;
        private byte[] metadata = new byte[0];
        private @Nullable AccountId spenderId;

        private Builder() {
            // created by TokenNftInfo.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// the NFT collection
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `serial`.
        ///
        /// serial number within the collection
        ///
        /// @param serial the value
        /// @return this builder
        public Builder serial(final long serial) {
            this.serial = serial;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// current owner of this serial
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `creationTime`.
        ///
        /// consensus time at which this serial was minted
        ///
        /// @param creationTime the value
        /// @return this builder
        public Builder creationTime(final ZonedDateTime creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        /// Sets the `metadata`.
        ///
        /// opaque per-serial metadata set at mint (e.g. IPFS CID)
        ///
        /// @param metadata the value
        /// @return this builder
        public Builder metadata(final byte[] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `spenderId`.
        ///
        /// account granted an allowance to transfer this serial (HIP-376); absent if none
        ///
        /// @param spenderId the value
        /// @return this builder
        public Builder spenderId(final @Nullable AccountId spenderId) {
            this.spenderId = spenderId;
            return this;
        }

        /// Creates the `TokenNftInfo` of the values set.
        ///
        /// @return the new `TokenNftInfo`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public TokenNftInfo build() {
            return new TokenNftInfo(
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(serial, "serial must not be null"),
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    Objects.requireNonNull(creationTime, "creationTime must not be null"),
                    metadata,
                    spenderId);
        }
    }
}
