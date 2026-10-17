package org.hiero.enterprise.service.nft;

import org.hiero.enterprise.service.Session;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.enterprise.service.internal.DefaultNftService;

/// Factory methods of the package `org.hiero.enterprise.service.nft`.
public final class NftFactory {

    /// Creates the service for the given session. With a framework integration the service is usually obtained via
    /// dependency injection instead.
    public static NftService createService(final Session session) {
        return new DefaultNftService(DefaultSession.of(session));
    }

    private NftFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
