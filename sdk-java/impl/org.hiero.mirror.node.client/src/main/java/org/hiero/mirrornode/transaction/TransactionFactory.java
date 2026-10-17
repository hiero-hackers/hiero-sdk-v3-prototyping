package org.hiero.mirrornode.transaction;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultTransactionRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.transaction`.
public final class TransactionFactory {

    public static TransactionRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultTransactionRepository(MirrorNodeRest.create(mirrorNode));
    }

    private TransactionFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
