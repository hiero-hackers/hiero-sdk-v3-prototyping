// Generated from spec/base/keys.md (keys#PublicKey); DO NOT EDIT.

package org.hiero.sdk.v3.keys;

/** Public-key verification contract. */
public interface PublicKey extends Key {
    /** Verifies a signature over a message. */ boolean verify(byte[] message, byte[] signature);
}
