package org.hiero.consensusnode.clpr.transactions;

import org.hiero.consensusnode.clpr.internal.Ownership;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.jspecify.annotations.Nullable;

/// The proof that the registrant of a Channel owns the key of its ownership commitment: the Channel ID, the public key
/// of the commitment and the signature of keccak256(channelId) with the matching private key.
///
/// @param channelId the Channel ID, the same on both ledgers
/// @param publicKey the public key of the ownership commitment
/// @param signature the signature of keccak256(channelId): r || s for ECDSA
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public record ClprChannelOwnershipProof(byte[] channelId, PublicKey publicKey, byte[] signature) {

    /// Creates a new `ClprChannelOwnershipProof`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ClprChannelOwnershipProof {
        Objects.requireNonNull(channelId, "channelId must not be null");
        channelId = channelId.clone();
        if (channelId.length < 32) {
            throw new IllegalArgumentException("channelId must contain at least 32 element(s)");
        }
        if (channelId.length > 32) {
            throw new IllegalArgumentException("channelId must contain at most 32 element(s)");
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

    /// the Channel ID, the same on both ledgers
    ///
    /// Returns a copy of the `channelId` array.
    @Override
    public byte[] channelId() {
        return channelId.clone();
    }

    /// the signature of keccak256(channelId): r || s for ECDSA
    ///
    /// Returns a copy of the `signature` array.
    @Override
    public byte[] signature() {
        return signature.clone();
    }

    /// Compares `channelId` and `signature` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ClprChannelOwnershipProof other
            && Arrays.equals(channelId, other.channelId)
            && Objects.equals(publicKey, other.publicKey)
            && Arrays.equals(signature, other.signature);
    }

    /// Hashes `channelId` and `signature` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(channelId), publicKey, Arrays.hashCode(signature));
    }

    /// Shows only the length of `channelId` and `signature` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ClprChannelOwnershipProof[channelId=" + "byte[" + channelId.length + "]" + ", publicKey=" + publicKey + ", signature=" + "byte[" + signature.length + "]" + "]";
    }

    /// Creates the proof for the Channel with the private key of the ownership commitment. The private key is not kept.
    public static ClprChannelOwnershipProof create(final byte[] channelId, final PrivateKey privateKey) {
        return new ClprChannelOwnershipProof(channelId, privateKey.createPublicKey(),
                Ownership.signChannel(channelId, privateKey));
    }
}
