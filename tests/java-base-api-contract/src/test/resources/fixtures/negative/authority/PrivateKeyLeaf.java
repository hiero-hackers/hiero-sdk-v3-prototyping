package org.hiero.sdk.v3.fixture.negative;

import org.hiero.sdk.v3.authority.PublicKeyAuthority;
import org.hiero.sdk.v3.keys.PrivateKey;

final class PrivateKeyLeaf {
    PublicKeyAuthority invalid(PrivateKey privateKey) { return new PublicKeyAuthority(privateKey); }
}
