// Source: spec/base/keys.md (keys.PublicKey).

package org.hiero.sdk.v3.keys;

/** Public-key verification contract with structurally owned key bytes. */
public abstract class PublicKey extends Key {
    /** Creates a public-key base and copies its bytes. */
    protected PublicKey(byte[] bytes, KeyAlgorithm algorithm) { super(bytes, algorithm, KeyType.PUBLIC); }
    /** Verifies a signature over a message. */ public abstract boolean verify(byte[] message, byte[] signature);
}
