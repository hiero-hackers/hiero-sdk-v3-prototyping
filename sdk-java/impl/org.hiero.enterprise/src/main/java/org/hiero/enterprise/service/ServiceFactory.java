package org.hiero.enterprise.service;

import java.util.Objects;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.ledger.config.NetworkSetting;

/// Factory methods of the package `org.hiero.enterprise.service`.
public final class ServiceFactory {

    /// Default factory: the session signs every transaction with the operator account's private key.
    public static Session createSession(final NetworkSetting networkSettings, final Account operatorAccount) {
        return new DefaultSession(networkSettings, operatorAccount, null);
    }

    /// Factory for setups where signing happens out of process (HSM, KMS, wallet).
    public static Session createSession(final NetworkSetting networkSettings, final Account operatorAccount, final TransactionSigner transactionSigner) {
        Objects.requireNonNull(transactionSigner, "transactionSigner must not be null");
        return new DefaultSession(networkSettings, operatorAccount, transactionSigner);
    }

    private ServiceFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
