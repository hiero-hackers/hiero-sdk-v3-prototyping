// Source: spec/base/keys.md (keys.Key).

package org.hiero.sdk.v3.keys;

import java.util.Objects;

/** Provider base for defensively owned key material. */
public abstract class Key {
    private final byte[] bytes;
    private final KeyAlgorithm algorithm;
    private final KeyType type;

    /** Creates a key base and copies its raw bytes. */
    protected Key(byte[] bytes, KeyAlgorithm algorithm, KeyType type) {
        this.bytes = Objects.requireNonNull(bytes, "bytes").clone();
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm");
        this.type = Objects.requireNonNull(type, "type");
    }

    /** Returns a defensive copy of raw key bytes. */ public final byte[] bytes() { return bytes.clone(); }
    /** Returns the key algorithm. */ public final KeyAlgorithm algorithm() { return algorithm; }
    /** Returns the public/private classification. */ public final KeyType type() { return type; }
    /** Exports raw bytes according to provider policy. */ public abstract byte[] toRawBytes();
    /** Exports bytes using a container format. */ public abstract byte[] toBytes(KeyFormat container);
    /** Exports text using a container format. */ public abstract String toString(KeyFormat container);
    /** Returns a secret-safe diagnostic containing no key bytes. */
    @Override public final String toString() { return getClass().getSimpleName() + "[algorithm=" + algorithm + ", type=" + type + "]"; }
}
