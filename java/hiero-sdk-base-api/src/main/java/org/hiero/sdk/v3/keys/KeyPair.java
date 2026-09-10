// Source: spec/base/keys.md (keys#KeyPair).

package org.hiero.sdk.v3.keys;

import java.util.Objects;

/** Immutable public/private key pair. */
public record KeyPair(PublicKey publicKey, PrivateKey privateKey) {
    /** Validates both pair components. */
    public KeyPair { Objects.requireNonNull(publicKey, "publicKey"); Objects.requireNonNull(privateKey, "privateKey"); }
}
