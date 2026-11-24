package org.hiero.enterprise.service;

import org.hiero.consensusnode.client.Account;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.ledger.config.NetworkSetting;

/// Factory methods of the package `org.hiero.enterprise.service`.
public final class ServiceFactory {

    /// Creates a session that pays for and signs every transaction with the operator account.
    public static Session createSession(final NetworkSetting networkSettings, final Account operatorAccount) {
        return new DefaultSession(networkSettings, operatorAccount);
    }

    private ServiceFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
