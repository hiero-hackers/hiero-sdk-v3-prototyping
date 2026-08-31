// Generated from spec/base/keys.md (keys#Key); DO NOT EDIT.

package org.hiero.sdk.v3.keys;

/** Provider-owned key contract. Implementations must copy raw bytes and redact diagnostics. */
public interface Key {
    /** Returns a defensive copy of raw key bytes. */ byte[] bytes();
    /** Returns the key algorithm. */ KeyAlgorithm algorithm();
    /** Returns the public/private classification. */ KeyType type();
    /** Exports raw bytes. */ byte[] toRawBytes();
    /** Exports bytes using a container format. */ byte[] toBytes(KeyFormat container);
    /** Exports text using a container format. */ String toString(KeyFormat container);
}
