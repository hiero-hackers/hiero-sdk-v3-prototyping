package org.hiero.mirrornode.block;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultBlockRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.block`.
public final class BlockFactory {

    public static BlockRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultBlockRepository(MirrorNodeRest.create(mirrorNode));
    }

    private BlockFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
