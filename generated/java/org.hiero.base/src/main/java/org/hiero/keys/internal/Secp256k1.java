package org.hiero.keys.internal;

import java.math.BigInteger;
import java.util.Arrays;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

/// The secp256k1 curve and the primitives Hiero uses with it: a Keccak-256 pre-hash and a
/// deterministic nonce (RFC 6979 with HMAC-SHA-256).
public final class Secp256k1 {

    /// The secp256k1 domain parameters.
    public static final ECDomainParameters DOMAIN;

    static {
        final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");
        DOMAIN = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
    }

    private Secp256k1() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /// Returns a signer with the deterministic nonce of RFC 6979.
    ///
    /// @return the signer, not yet initialised
    public static ECDSASigner deterministicSigner() {
        return new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    }

    /// Hashes a message with Keccak-256, the digest Hiero signs over.
    ///
    /// @param message the message
    /// @return the 32-byte digest
    public static byte[] keccak256(final byte[] message) {
        final KeccakDigest digest = new KeccakDigest(256);
        digest.update(message, 0, message.length);
        final byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

    /// Returns the big-endian bytes of a non-negative value, left-padded or trimmed to a fixed length.
    ///
    /// @param value  the value
    /// @param length the number of bytes
    /// @return the bytes
    /// @throws IllegalArgumentException if the value does not fit into that many bytes
    public static byte[] toFixedLength(final BigInteger value, final int length) {
        final byte[] magnitude = value.toByteArray();
        if (magnitude.length == length) {
            return magnitude;
        }
        if (magnitude.length > length) {
            final int excess = magnitude.length - length;
            for (int i = 0; i < excess; i++) {
                if (magnitude[i] != 0) {
                    throw new IllegalArgumentException("The value does not fit into " + length + " bytes");
                }
            }
            return Arrays.copyOfRange(magnitude, excess, magnitude.length);
        }
        final byte[] padded = new byte[length];
        System.arraycopy(magnitude, 0, padded, length - magnitude.length, magnitude.length);
        return padded;
    }
}
