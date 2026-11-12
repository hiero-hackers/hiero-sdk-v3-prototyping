package org.hiero.blocknode;

import org.hiero.blocknode.internal.DefaultBlockNodeClient;
import org.hiero.ledger.BlockNode;

/// Factory methods of the package `org.hiero.blocknode`.
public final class BlocknodeFactory {

    /// Creates a client for the given block node. Creating the client sends no request.
    public static BlockNodeClient createBlockNodeClient(final BlockNode blockNode) {
        return new DefaultBlockNodeClient(blockNode);
    }

    private BlocknodeFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
