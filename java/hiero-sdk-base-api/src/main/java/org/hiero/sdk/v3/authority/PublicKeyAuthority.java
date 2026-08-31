// Generated from spec/base/authority.md (authority#PublicKeyAuthority); DO NOT EDIT.

package org.hiero.sdk.v3.authority;

import java.util.Objects;
import org.hiero.sdk.v3.keys.PublicKey;

/** Public-key authorization leaf. */
public record PublicKeyAuthority(PublicKey publicKey) implements Authority {
    /** Validates the public key. */ public PublicKeyAuthority { Objects.requireNonNull(publicKey, "publicKey"); }
}
