// Generated from spec/base/keys.md (keys#PrivateKey); DO NOT EDIT.

package org.hiero.sdk.v3.keys;

/** Private-key signing contract. Implementations must not expose key material in diagnostics. */
public interface PrivateKey extends Key {
    /** Signs a message. */ byte[] sign(byte[] message);
    /** Derives a new public-key instance. */ PublicKey createPublicKey();
}
