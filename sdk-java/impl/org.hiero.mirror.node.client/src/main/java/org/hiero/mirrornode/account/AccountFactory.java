package org.hiero.mirrornode.account;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultAccountRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.account`.
public final class AccountFactory {

    public static AccountRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultAccountRepository(MirrorNodeRest.create(mirrorNode));
    }

    private AccountFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
