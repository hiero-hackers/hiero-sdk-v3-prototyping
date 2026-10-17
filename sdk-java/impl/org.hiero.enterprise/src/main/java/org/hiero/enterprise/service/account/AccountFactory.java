package org.hiero.enterprise.service.account;

import org.hiero.enterprise.service.Session;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.enterprise.service.internal.DefaultAccountService;

/// Factory methods of the package `org.hiero.enterprise.service.account`.
public final class AccountFactory {

    /// Creates the service for the given session. With a framework integration the service is usually obtained via
    /// dependency injection instead.
    public static AccountService createService(final Session session) {
        return new DefaultAccountService(DefaultSession.of(session));
    }

    private AccountFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
