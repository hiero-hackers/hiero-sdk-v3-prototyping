package org.hiero.consensusnode.clpr.internal;

import com.hederahashgraph.api.proto.java.ClprSignatureScheme;
import java.util.Arrays;
import java.util.Objects;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;

/// The ownership commitments, identities and proofs of the CLPR protocol: Keccak-256 over the concatenated bytes, with
/// a public key in the encoding of the protocol (64 bytes `x || y` for secp256k1 without the `0x04` prefix, 32 bytes
/// for Ed25519).
public final class Ownership {

    /// The length of a Channel ID, a Connector ID, a salt and a commitment.
    public static final int ID_LENGTH = 32;

    /// The 20-byte address of the CLPR service (`0x…016e`), part of the message a Connector proof signs.
    private static final byte[] SERVICE_ADDRESS = new byte[20];

    private static final X9ECParameters SECP256K1 = CustomNamedCurves.getByName("secp256k1");

    static {
        SERVICE_ADDRESS[18] = 0x01;
        SERVICE_ADDRESS[19] = 0x6e;
    }

    private Ownership() {
    }

    /// `keccak256(channelId || publicKey)`, the ownership commitment of a Channel.
    ///
    /// @param channelId the Channel ID (32 bytes)
    /// @param publicKey the public key of the owner
    /// @return the commitment
    public static byte[] channelCommitment(final byte[] channelId, final PublicKey publicKey) {
        return keccak256(id(channelId, "channelId"), encode(publicKey));
    }

    /// `keccak256(channelId || publicKey || salt)`, the ID of a Connector.
    ///
    /// @param channelId the Channel ID (32 bytes)
    /// @param publicKey the public key of the owner
    /// @param salt      the salt (32 bytes)
    /// @return the Connector ID
    public static byte[] connectorId(final byte[] channelId, final PublicKey publicKey, final byte[] salt) {
        return keccak256(id(channelId, "channelId"), encode(publicKey), id(salt, "salt"));
    }

    /// `keccak256(connectorId || publicKey)`, the ownership commitment of a Connector.
    ///
    /// @param connectorId the Connector ID (32 bytes)
    /// @param publicKey   the public key of the owner
    /// @return the commitment
    public static byte[] connectorCommitment(final byte[] connectorId, final PublicKey publicKey) {
        return keccak256(id(connectorId, "connectorId"), encode(publicKey));
    }

    /// The signature of a Channel proof: over `keccak256(channelId)`.
    ///
    /// @param channelId  the Channel ID (32 bytes)
    /// @param privateKey the private key of the owner
    /// @return the 64-byte signature
    public static byte[] signChannel(final byte[] channelId, final PrivateKey privateKey) {
        return sign(privateKey, id(channelId, "channelId"));
    }

    /// The signature of a Connector proof: over `keccak256(connectorId || clprServiceAddress)`.
    ///
    /// @param connectorId the Connector ID (32 bytes)
    /// @param privateKey  the private key of the owner
    /// @return the 64-byte signature
    public static byte[] signConnector(final byte[] connectorId, final PrivateKey privateKey) {
        return sign(privateKey, concat(id(connectorId, "connectorId"), SERVICE_ADDRESS));
    }

    /// The public key in the encoding of the protocol.
    ///
    /// @param publicKey the public key
    /// @return 64 bytes `x || y` for secp256k1, 32 bytes for Ed25519
    public static byte[] encode(final PublicKey publicKey) {
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        final byte[] raw = publicKey.toRawBytes();
        if (publicKey.algorithm() == KeyAlgorithm.ED25519) {
            return raw;
        }
        final byte[] uncompressed = SECP256K1.getCurve().decodePoint(raw).getEncoded(false);
        return Arrays.copyOfRange(uncompressed, 1, uncompressed.length);
    }

    /// The signature scheme of the wire for the algorithm of a public key.
    ///
    /// @param publicKey the public key
    /// @return the scheme
    public static ClprSignatureScheme scheme(final PublicKey publicKey) {
        return publicKey.algorithm() == KeyAlgorithm.ED25519 ? ClprSignatureScheme.ED25519
                : ClprSignatureScheme.ECDSA_SECP256K1;
    }

    /// Signs the Keccak-256 digest of a message. An ECDSA `PrivateKey.sign` hashes its message with Keccak-256 itself,
    /// an Ed25519 one signs its message as it is, so the ECDSA key gets the message and the Ed25519 key the digest.
    private static byte[] sign(final PrivateKey privateKey, final byte[] message) {
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        return privateKey.algorithm() == KeyAlgorithm.ED25519 ? privateKey.sign(keccak256(message))
                : privateKey.sign(message);
    }

    /// Checks that an ID has 32 bytes.
    private static byte[] id(final byte[] value, final String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.length != ID_LENGTH) {
            throw new IllegalArgumentException(name + " must have " + ID_LENGTH + " bytes, not " + value.length);
        }
        return value;
    }

    private static byte[] keccak256(final byte[]... parts) {
        final KeccakDigest digest = new KeccakDigest(256);
        for (final byte[] part : parts) {
            digest.update(part, 0, part.length);
        }
        final byte[] hash = new byte[32];
        digest.doFinal(hash, 0);
        return hash;
    }

    private static byte[] concat(final byte[] first, final byte[] second) {
        final byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
