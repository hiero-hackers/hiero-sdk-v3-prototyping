package org.hiero.mirrornode.block;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// A block of the network.
///
/// @param count number of transactions in the block
/// @param gasUsed gas used by the EVM transactions of the block
/// @param hapiVersion version of the services API the block was created with
/// @param hash hash of the block
/// @param logsBloom bloom filter of the EVM logs of the block (256 bytes)
/// @param name name of the record file of the block
/// @param number number of the block
/// @param previousHash hash of the previous block
/// @param size size of the block in bytes
/// @param timestamp consensus time range of the transactions of the block
public record Block(
        long count,
        @Nullable Long gasUsed,
        @Nullable String hapiVersion,
        byte[] hash,
        byte @Nullable [] logsBloom,
        String name,
        long number,
        byte[] previousHash,
        @Nullable Long size,
        TimestampRange timestamp) {

    /// Creates a new `Block`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public Block {
        if (count < 0L) {
            throw new IllegalArgumentException("count must be at least 0");
        }
        if (gasUsed != null && gasUsed < 0L) {
            throw new IllegalArgumentException("gasUsed must be at least 0");
        }
        Objects.requireNonNull(hash, "hash must not be null");
        hash = hash.clone();
        logsBloom = logsBloom == null ? null : logsBloom.clone();
        Objects.requireNonNull(name, "name must not be null");
        if (number < 0L) {
            throw new IllegalArgumentException("number must be at least 0");
        }
        Objects.requireNonNull(previousHash, "previousHash must not be null");
        previousHash = previousHash.clone();
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// hash of the block
    ///
    /// Returns a copy of the `hash` array.
    @Override
    public byte[] hash() {
        return hash.clone();
    }

    /// bloom filter of the EVM logs of the block (256 bytes)
    ///
    /// Returns a copy of the `logsBloom` array.
    @Override
    public byte @Nullable [] logsBloom() {
        return logsBloom == null ? null : logsBloom.clone();
    }

    /// hash of the previous block
    ///
    /// Returns a copy of the `previousHash` array.
    @Override
    public byte[] previousHash() {
        return previousHash.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof Block other
            && count == other.count
            && Objects.equals(gasUsed, other.gasUsed)
            && Objects.equals(hapiVersion, other.hapiVersion)
            && Arrays.equals(hash, other.hash)
            && Arrays.equals(logsBloom, other.logsBloom)
            && Objects.equals(name, other.name)
            && number == other.number
            && Arrays.equals(previousHash, other.previousHash)
            && Objects.equals(size, other.size)
            && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, gasUsed, hapiVersion, Arrays.hashCode(hash), Arrays.hashCode(logsBloom), name, number, Arrays.hashCode(previousHash), size, timestamp);
    }

    @Override
    public String toString() {
        return "Block[count=" + count + ", gasUsed=" + gasUsed + ", hapiVersion=" + hapiVersion + ", hash=" + "byte[" + hash.length + "]" + ", logsBloom=" + (logsBloom == null ? "null" : "byte[" + logsBloom.length + "]") + ", name=" + name + ", number=" + number + ", previousHash=" + "byte[" + previousHash.length + "]" + ", size=" + size + ", timestamp=" + timestamp + "]";
    }

    /// Returns a builder for a new `Block`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Block`.
    ///
    /// `build()` creates the `Block` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long count;
        private @Nullable Long gasUsed;
        private @Nullable String hapiVersion;
        private byte @Nullable [] hash;
        private byte @Nullable [] logsBloom;
        private @Nullable String name;
        private @Nullable Long number;
        private byte @Nullable [] previousHash;
        private @Nullable Long size;
        private @Nullable TimestampRange timestamp;

        private Builder() {
            // created by Block.builder()
        }

        /// Sets the `count`.
        ///
        /// number of transactions in the block
        ///
        /// @param count the value
        /// @return this builder
        public Builder count(final long count) {
            this.count = count;
            return this;
        }

        /// Sets the `gasUsed`.
        ///
        /// gas used by the EVM transactions of the block
        ///
        /// @param gasUsed the value
        /// @return this builder
        public Builder gasUsed(final @Nullable Long gasUsed) {
            this.gasUsed = gasUsed;
            return this;
        }

        /// Sets the `hapiVersion`.
        ///
        /// version of the services API the block was created with
        ///
        /// @param hapiVersion the value
        /// @return this builder
        public Builder hapiVersion(final @Nullable String hapiVersion) {
            this.hapiVersion = hapiVersion;
            return this;
        }

        /// Sets the `hash`.
        ///
        /// hash of the block
        ///
        /// @param hash the value
        /// @return this builder
        public Builder hash(final byte[] hash) {
            this.hash = hash;
            return this;
        }

        /// Sets the `logsBloom`.
        ///
        /// bloom filter of the EVM logs of the block (256 bytes)
        ///
        /// @param logsBloom the value
        /// @return this builder
        public Builder logsBloom(final byte @Nullable [] logsBloom) {
            this.logsBloom = logsBloom;
            return this;
        }

        /// Sets the `name`.
        ///
        /// name of the record file of the block
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /// Sets the `number`.
        ///
        /// number of the block
        ///
        /// @param number the value
        /// @return this builder
        public Builder number(final long number) {
            this.number = number;
            return this;
        }

        /// Sets the `previousHash`.
        ///
        /// hash of the previous block
        ///
        /// @param previousHash the value
        /// @return this builder
        public Builder previousHash(final byte[] previousHash) {
            this.previousHash = previousHash;
            return this;
        }

        /// Sets the `size`.
        ///
        /// size of the block in bytes
        ///
        /// @param size the value
        /// @return this builder
        public Builder size(final @Nullable Long size) {
            this.size = size;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// consensus time range of the transactions of the block
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final TimestampRange timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `Block` of the values set.
        ///
        /// @return the new `Block`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public Block build() {
            return new Block(
                    Objects.requireNonNull(count, "count must not be null"),
                    gasUsed,
                    hapiVersion,
                    Objects.requireNonNull(hash, "hash must not be null"),
                    logsBloom,
                    Objects.requireNonNull(name, "name must not be null"),
                    Objects.requireNonNull(number, "number must not be null"),
                    Objects.requireNonNull(previousHash, "previousHash must not be null"),
                    size,
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
