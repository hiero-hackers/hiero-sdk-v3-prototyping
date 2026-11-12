package org.hiero.consensusnode.clpr.queries;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.consensusnode.clpr.ClprThrottles;
import org.jspecify.annotations.Nullable;

/// The CLPR configuration of a ledger, with the state proof that a peer ledger passes to its verifier contract.
///
/// @param protocolVersion the CLPR protocol version of the ledger, set at genesis
/// @param chainId the CAIP-2 id of the ledger, e.g. "hedera:mainnet"; set at genesis
/// @param serviceAddress the address of the CLPR service of the ledger
/// @param timestamp when the configuration was last changed
/// @param throttles the capacity limits of the CLPR service
/// @param initialTrustAnchor the trust anchor a peer starts to verify this ledger with
/// @param initialTrustAnchorId the id of the initial trust anchor
/// @param configurationStateProof The opaque state proof of the configuration from a signed snapshot; pass it unchanged to the peer ledger. Empty
///   when no usable snapshot exists.
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public record ClprLedgerConfiguration(
        long protocolVersion,
        String chainId,
        byte[] serviceAddress,
        ZonedDateTime timestamp,
        ClprThrottles throttles,
        byte[] initialTrustAnchor,
        byte[] initialTrustAnchorId,
        byte[] configurationStateProof) {

    /// Creates a new `ClprLedgerConfiguration`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ClprLedgerConfiguration {
        if (protocolVersion < 0L || protocolVersion > 4294967295L) {
            throw new IllegalArgumentException("protocolVersion must be between 0 and 4294967295");
        }
        Objects.requireNonNull(chainId, "chainId must not be null");
        Objects.requireNonNull(serviceAddress, "serviceAddress must not be null");
        serviceAddress = serviceAddress.clone();
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(throttles, "throttles must not be null");
        Objects.requireNonNull(initialTrustAnchor, "initialTrustAnchor must not be null");
        initialTrustAnchor = initialTrustAnchor.clone();
        Objects.requireNonNull(initialTrustAnchorId, "initialTrustAnchorId must not be null");
        initialTrustAnchorId = initialTrustAnchorId.clone();
        Objects.requireNonNull(configurationStateProof, "configurationStateProof must not be null");
        configurationStateProof = configurationStateProof.clone();
    }

    /// the address of the CLPR service of the ledger
    ///
    /// Returns a copy of the `serviceAddress` array.
    @Override
    public byte[] serviceAddress() {
        return serviceAddress.clone();
    }

    /// the trust anchor a peer starts to verify this ledger with
    ///
    /// Returns a copy of the `initialTrustAnchor` array.
    @Override
    public byte[] initialTrustAnchor() {
        return initialTrustAnchor.clone();
    }

    /// the id of the initial trust anchor
    ///
    /// Returns a copy of the `initialTrustAnchorId` array.
    @Override
    public byte[] initialTrustAnchorId() {
        return initialTrustAnchorId.clone();
    }

    /// The opaque state proof of the configuration from a signed snapshot; pass it unchanged to the peer ledger. Empty
    /// when no usable snapshot exists.
    ///
    /// Returns a copy of the `configurationStateProof` array.
    @Override
    public byte[] configurationStateProof() {
        return configurationStateProof.clone();
    }

    /// Compares `serviceAddress`, `initialTrustAnchor`, `initialTrustAnchorId` and `configurationStateProof` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ClprLedgerConfiguration other
            && protocolVersion == other.protocolVersion
            && Objects.equals(chainId, other.chainId)
            && Arrays.equals(serviceAddress, other.serviceAddress)
            && Objects.equals(timestamp, other.timestamp)
            && Objects.equals(throttles, other.throttles)
            && Arrays.equals(initialTrustAnchor, other.initialTrustAnchor)
            && Arrays.equals(initialTrustAnchorId, other.initialTrustAnchorId)
            && Arrays.equals(configurationStateProof, other.configurationStateProof);
    }

    /// Hashes `serviceAddress`, `initialTrustAnchor`, `initialTrustAnchorId` and `configurationStateProof` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, chainId, Arrays.hashCode(serviceAddress), timestamp, throttles, Arrays.hashCode(initialTrustAnchor), Arrays.hashCode(initialTrustAnchorId), Arrays.hashCode(configurationStateProof));
    }

    /// Shows only the length of `serviceAddress`, `initialTrustAnchor`, `initialTrustAnchorId` and `configurationStateProof` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ClprLedgerConfiguration[protocolVersion=" + protocolVersion + ", chainId=" + chainId + ", serviceAddress=" + "byte[" + serviceAddress.length + "]" + ", timestamp=" + timestamp + ", throttles=" + throttles + ", initialTrustAnchor=" + "byte[" + initialTrustAnchor.length + "]" + ", initialTrustAnchorId=" + "byte[" + initialTrustAnchorId.length + "]" + ", configurationStateProof=" + "byte[" + configurationStateProof.length + "]" + "]";
    }

    /// Returns a builder for a new `ClprLedgerConfiguration`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ClprLedgerConfiguration`.
    ///
    /// `build()` creates the `ClprLedgerConfiguration` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long protocolVersion;
        private @Nullable String chainId;
        private byte @Nullable [] serviceAddress;
        private @Nullable ZonedDateTime timestamp;
        private @Nullable ClprThrottles throttles;
        private byte @Nullable [] initialTrustAnchor;
        private byte @Nullable [] initialTrustAnchorId;
        private byte @Nullable [] configurationStateProof;

        private Builder() {
            // created by ClprLedgerConfiguration.builder()
        }

        /// Sets the `protocolVersion`.
        ///
        /// the CLPR protocol version of the ledger, set at genesis
        ///
        /// @param protocolVersion the value
        /// @return this builder
        public Builder protocolVersion(final long protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        /// Sets the `chainId`.
        ///
        /// the CAIP-2 id of the ledger, e.g. "hedera:mainnet"; set at genesis
        ///
        /// @param chainId the value
        /// @return this builder
        public Builder chainId(final String chainId) {
            this.chainId = chainId;
            return this;
        }

        /// Sets the `serviceAddress`.
        ///
        /// the address of the CLPR service of the ledger
        ///
        /// @param serviceAddress the value
        /// @return this builder
        public Builder serviceAddress(final byte[] serviceAddress) {
            this.serviceAddress = serviceAddress;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// when the configuration was last changed
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Sets the `throttles`.
        ///
        /// the capacity limits of the CLPR service
        ///
        /// @param throttles the value
        /// @return this builder
        public Builder throttles(final ClprThrottles throttles) {
            this.throttles = throttles;
            return this;
        }

        /// Sets the `initialTrustAnchor`.
        ///
        /// the trust anchor a peer starts to verify this ledger with
        ///
        /// @param initialTrustAnchor the value
        /// @return this builder
        public Builder initialTrustAnchor(final byte[] initialTrustAnchor) {
            this.initialTrustAnchor = initialTrustAnchor;
            return this;
        }

        /// Sets the `initialTrustAnchorId`.
        ///
        /// the id of the initial trust anchor
        ///
        /// @param initialTrustAnchorId the value
        /// @return this builder
        public Builder initialTrustAnchorId(final byte[] initialTrustAnchorId) {
            this.initialTrustAnchorId = initialTrustAnchorId;
            return this;
        }

        /// Sets the `configurationStateProof`.
        ///
        /// The opaque state proof of the configuration from a signed snapshot; pass it unchanged to the peer ledger. Empty
        /// when no usable snapshot exists.
        ///
        /// @param configurationStateProof the value
        /// @return this builder
        public Builder configurationStateProof(final byte[] configurationStateProof) {
            this.configurationStateProof = configurationStateProof;
            return this;
        }

        /// Creates the `ClprLedgerConfiguration` of the values set.
        ///
        /// @return the new `ClprLedgerConfiguration`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ClprLedgerConfiguration build() {
            return new ClprLedgerConfiguration(
                    Objects.requireNonNull(protocolVersion, "protocolVersion must not be null"),
                    Objects.requireNonNull(chainId, "chainId must not be null"),
                    Objects.requireNonNull(serviceAddress, "serviceAddress must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"),
                    Objects.requireNonNull(throttles, "throttles must not be null"),
                    Objects.requireNonNull(initialTrustAnchor, "initialTrustAnchor must not be null"),
                    Objects.requireNonNull(initialTrustAnchorId, "initialTrustAnchorId must not be null"),
                    Objects.requireNonNull(configurationStateProof, "configurationStateProof must not be null"));
        }
    }
}
