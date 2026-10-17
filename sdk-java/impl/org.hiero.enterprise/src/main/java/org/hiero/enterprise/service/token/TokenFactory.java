package org.hiero.enterprise.service.token;

import org.hiero.enterprise.service.Session;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.enterprise.service.internal.DefaultFungibleTokenService;

/// Factory methods of the package `org.hiero.enterprise.service.token`.
public final class TokenFactory {

    /// Creates the service for the given session. With a framework integration the service is usually obtained via
    /// dependency injection instead.
    public static FungibleTokenService createService(final Session session) {
        return new DefaultFungibleTokenService(DefaultSession.of(session));
    }

    private TokenFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
