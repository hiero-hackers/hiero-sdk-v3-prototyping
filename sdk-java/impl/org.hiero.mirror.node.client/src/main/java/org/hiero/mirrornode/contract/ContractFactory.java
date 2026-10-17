package org.hiero.mirrornode.contract;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultContractRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.contract`.
public final class ContractFactory {

    public static ContractRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultContractRepository(MirrorNodeRest.create(mirrorNode));
    }

    private ContractFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
