package org.hiero.mirrornode.network;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultNetworkRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.network`.
public final class NetworkFactory {

    public static NetworkRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultNetworkRepository(MirrorNodeRest.create(mirrorNode));
    }

    private NetworkFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
