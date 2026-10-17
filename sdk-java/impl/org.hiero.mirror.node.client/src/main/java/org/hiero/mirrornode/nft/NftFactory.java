package org.hiero.mirrornode.nft;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultNftRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.nft`.
public final class NftFactory {

    public static NftRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultNftRepository(MirrorNodeRest.create(mirrorNode));
    }

    private NftFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
