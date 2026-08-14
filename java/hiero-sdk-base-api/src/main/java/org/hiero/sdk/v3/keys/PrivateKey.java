// Source: spec/base/keys.md (keys.PrivateKey).

package org.hiero.sdk.v3.keys;

/** Private-key signing contract with secret-safe diagnostics. */
public abstract class PrivateKey extends Key {
    /** Creates a private-key base and copies its bytes. */
    protected PrivateKey(byte[] bytes, KeyAlgorithm algorithm) { super(bytes, algorithm, KeyType.PRIVATE); }
    /** Signs a message. */ public abstract byte[] sign(byte[] message);
    /** Derives a new public-key instance. */ public abstract PublicKey createPublicKey();
}
