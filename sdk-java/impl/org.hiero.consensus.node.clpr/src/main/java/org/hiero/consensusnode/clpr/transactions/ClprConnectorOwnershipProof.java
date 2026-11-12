package org.hiero.consensusnode.clpr.transactions;

import org.hiero.consensusnode.clpr.internal.Ownership;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.jspecify.annotations.Nullable;

/// The proof that the registrant of a Connector owns the key of its commitment: the Channel ID, the salt and the
/// Connector ID derived from them, the public key and the signature of keccak256(connectorId || clprServiceAddress)
/// with the matching private key.
///
/// @param channelId the Channel of the Connector
/// @param salt the salt of the Connector ID
/// @param connectorId keccak256(channelId || publicKey || salt)
/// @param publicKey the public key of the commitment
/// @param signature the signature of keccak256(connectorId || clprServiceAddress)
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public record ClprConnectorOwnershipProof(
        byte[] channelId,
        byte[] salt,
        byte[] connectorId,
        PublicKey publicKey,
        byte[] signature) {

    /// Creates a new `ClprConnectorOwnershipProof`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ClprConnectorOwnershipProof {
        Objects.requireNonNull(channelId, "channelId must not be null");
        channelId = channelId.clone();
        if (channelId.length < 32) {
            throw new IllegalArgumentException("channelId must contain at least 32 element(s)");
        }
        if (channelId.length > 32) {
            throw new IllegalArgumentException("channelId must contain at most 32 element(s)");
        }
        Objects.requireNonNull(salt, "salt must not be null");
        salt = salt.clone();
        if (salt.length < 32) {
            throw new IllegalArgumentException("salt must contain at least 32 element(s)");
        }
        if (salt.length > 32) {
            throw new IllegalArgumentException("salt must contain at most 32 element(s)");
        }
        Objects.requireNonNull(connectorId, "connectorId must not be null");
        connectorId = connectorId.clone();
        if (connectorId.length < 32) {
            throw new IllegalArgumentException("connectorId must contain at least 32 element(s)");
        }
        if (connectorId.length > 32) {
            throw new IllegalArgumentException("connectorId must contain at most 32 element(s)");
        }
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        Objects.requireNonNull(signature, "signature must not be null");
        signature = signature.clone();
        if (signature.length < 64) {
            throw new IllegalArgumentException("signature must contain at least 64 element(s)");
        }
        if (signature.length > 64) {
            throw new IllegalArgumentException("signature must contain at most 64 element(s)");
        }
    }

    /// the Channel of the Connector
    ///
    /// Returns a copy of the `channelId` array.
    @Override
    public byte[] channelId() {
        return channelId.clone();
    }

    /// the salt of the Connector ID
    ///
    /// Returns a copy of the `salt` array.
    @Override
    public byte[] salt() {
        return salt.clone();
    }

    /// keccak256(channelId || publicKey || salt)
    ///
    /// Returns a copy of the `connectorId` array.
    @Override
    public byte[] connectorId() {
        return connectorId.clone();
    }

    /// the signature of keccak256(connectorId || clprServiceAddress)
    ///
    /// Returns a copy of the `signature` array.
    @Override
    public byte[] signature() {
        return signature.clone();
    }

    /// Compares `channelId`, `salt`, `connectorId` and `signature` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ClprConnectorOwnershipProof other
            && Arrays.equals(channelId, other.channelId)
            && Arrays.equals(salt, other.salt)
            && Arrays.equals(connectorId, other.connectorId)
            && Objects.equals(publicKey, other.publicKey)
            && Arrays.equals(signature, other.signature);
    }

    /// Hashes `channelId`, `salt`, `connectorId` and `signature` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(channelId), Arrays.hashCode(salt), Arrays.hashCode(connectorId), publicKey, Arrays.hashCode(signature));
    }

    /// Shows only the length of `channelId`, `salt`, `connectorId` and `signature` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ClprConnectorOwnershipProof[channelId=" + "byte[" + channelId.length + "]" + ", salt=" + "byte[" + salt.length + "]" + ", connectorId=" + "byte[" + connectorId.length + "]" + ", publicKey=" + publicKey + ", signature=" + "byte[" + signature.length + "]" + "]";
    }

    /// Creates the proof for the Connector with the private key of the commitment: derives the Connector ID from the
    /// Channel ID, the public key and the salt, and signs it. The private key is not kept.
    public static ClprConnectorOwnershipProof create(final byte[] channelId, final byte[] salt, final PrivateKey privateKey) {
        final PublicKey publicKey = privateKey.createPublicKey();
        final byte[] connectorId = Ownership.connectorId(channelId, publicKey, salt);
        return new ClprConnectorOwnershipProof(channelId, salt, connectorId, publicKey,
                Ownership.signConnector(connectorId, privateKey));
    }

    /// Returns a builder for a new `ClprConnectorOwnershipProof`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ClprConnectorOwnershipProof`.
    ///
    /// `build()` creates the `ClprConnectorOwnershipProof` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private byte @Nullable [] channelId;
        private byte @Nullable [] salt;
        private byte @Nullable [] connectorId;
        private @Nullable PublicKey publicKey;
        private byte @Nullable [] signature;

        private Builder() {
            // created by ClprConnectorOwnershipProof.builder()
        }

        /// Sets the `channelId`.
        ///
        /// the Channel of the Connector
        ///
        /// @param channelId the value
        /// @return this builder
        public Builder channelId(final byte[] channelId) {
            this.channelId = channelId;
            return this;
        }

        /// Sets the `salt`.
        ///
        /// the salt of the Connector ID
        ///
        /// @param salt the value
        /// @return this builder
        public Builder salt(final byte[] salt) {
            this.salt = salt;
            return this;
        }

        /// Sets the `connectorId`.
        ///
        /// keccak256(channelId || publicKey || salt)
        ///
        /// @param connectorId the value
        /// @return this builder
        public Builder connectorId(final byte[] connectorId) {
            this.connectorId = connectorId;
            return this;
        }

        /// Sets the `publicKey`.
        ///
        /// the public key of the commitment
        ///
        /// @param publicKey the value
        /// @return this builder
        public Builder publicKey(final PublicKey publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        /// Sets the `signature`.
        ///
        /// the signature of keccak256(connectorId || clprServiceAddress)
        ///
        /// @param signature the value
        /// @return this builder
        public Builder signature(final byte[] signature) {
            this.signature = signature;
            return this;
        }

        /// Creates the `ClprConnectorOwnershipProof` of the values set.
        ///
        /// @return the new `ClprConnectorOwnershipProof`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ClprConnectorOwnershipProof build() {
            return new ClprConnectorOwnershipProof(
                    Objects.requireNonNull(channelId, "channelId must not be null"),
                    Objects.requireNonNull(salt, "salt must not be null"),
                    Objects.requireNonNull(connectorId, "connectorId must not be null"),
                    Objects.requireNonNull(publicKey, "publicKey must not be null"),
                    Objects.requireNonNull(signature, "signature must not be null"));
        }
    }
}
