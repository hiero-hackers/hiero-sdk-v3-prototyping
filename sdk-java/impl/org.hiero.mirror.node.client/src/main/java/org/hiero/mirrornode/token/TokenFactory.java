package org.hiero.mirrornode.token;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultTokenRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.token`.
public final class TokenFactory {

    public static TokenRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultTokenRepository(MirrorNodeRest.create(mirrorNode));
    }

    private TokenFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
