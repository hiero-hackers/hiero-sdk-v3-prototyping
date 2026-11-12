package org.hiero.blocknode;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.blocknode.status.SemanticVersion;
import org.jspecify.annotations.Nullable;

/// The header of a block.
///
/// @param number the number of the block; one more than its predecessor's
/// @param timestamp the consensus time of the block
/// @param hapiProtoVersion the version of the HAPI protobuf definitions of the block
/// @param softwareVersion the version of the consensus node software that produced it
/// @param hashAlgorithm the hash algorithm of the block
public record BlockHeader(
        long number,
        ZonedDateTime timestamp,
        SemanticVersion hapiProtoVersion,
        SemanticVersion softwareVersion,
        BlockHashAlgorithm hashAlgorithm) {

    /// Creates a new `BlockHeader`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public BlockHeader {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(hapiProtoVersion, "hapiProtoVersion must not be null");
        Objects.requireNonNull(softwareVersion, "softwareVersion must not be null");
        Objects.requireNonNull(hashAlgorithm, "hashAlgorithm must not be null");
    }

    /// Returns a builder for a new `BlockHeader`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `BlockHeader`.
    ///
    /// `build()` creates the `BlockHeader` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long number;
        private @Nullable ZonedDateTime timestamp;
        private @Nullable SemanticVersion hapiProtoVersion;
        private @Nullable SemanticVersion softwareVersion;
        private @Nullable BlockHashAlgorithm hashAlgorithm;

        private Builder() {
            // created by BlockHeader.builder()
        }

        /// Sets the `number`.
        ///
        /// the number of the block; one more than its predecessor's
        ///
        /// @param number the value
        /// @return this builder
        public Builder number(final long number) {
            this.number = number;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the consensus time of the block
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Sets the `hapiProtoVersion`.
        ///
        /// the version of the HAPI protobuf definitions of the block
        ///
        /// @param hapiProtoVersion the value
        /// @return this builder
        public Builder hapiProtoVersion(final SemanticVersion hapiProtoVersion) {
            this.hapiProtoVersion = hapiProtoVersion;
            return this;
        }

        /// Sets the `softwareVersion`.
        ///
        /// the version of the consensus node software that produced it
        ///
        /// @param softwareVersion the value
        /// @return this builder
        public Builder softwareVersion(final SemanticVersion softwareVersion) {
            this.softwareVersion = softwareVersion;
            return this;
        }

        /// Sets the `hashAlgorithm`.
        ///
        /// the hash algorithm of the block
        ///
        /// @param hashAlgorithm the value
        /// @return this builder
        public Builder hashAlgorithm(final BlockHashAlgorithm hashAlgorithm) {
            this.hashAlgorithm = hashAlgorithm;
            return this;
        }

        /// Creates the `BlockHeader` of the values set.
        ///
        /// @return the new `BlockHeader`
        /// @throws NullPointerException if a required value is not set
        public BlockHeader build() {
            return new BlockHeader(
                    Objects.requireNonNull(number, "number must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"),
                    Objects.requireNonNull(hapiProtoVersion, "hapiProtoVersion must not be null"),
                    Objects.requireNonNull(softwareVersion, "softwareVersion must not be null"),
                    Objects.requireNonNull(hashAlgorithm, "hashAlgorithm must not be null"));
        }
    }
}
