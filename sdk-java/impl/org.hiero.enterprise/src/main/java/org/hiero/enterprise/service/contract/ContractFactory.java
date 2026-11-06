package org.hiero.enterprise.service.contract;

import org.hiero.enterprise.service.Session;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.enterprise.service.internal.DefaultSmartContractService;

/// Factory methods of the package `org.hiero.enterprise.service.contract`.
public final class ContractFactory {

    /// Creates the service for the given session. With a framework integration the service is usually obtained via
    /// dependency injection instead.
    public static SmartContractService createService(final Session session) {
        return new DefaultSmartContractService(DefaultSession.of(session));
    }

    private ContractFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
