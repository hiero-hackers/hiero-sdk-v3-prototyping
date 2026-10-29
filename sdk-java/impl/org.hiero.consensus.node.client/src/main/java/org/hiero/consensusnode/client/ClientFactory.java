package org.hiero.consensusnode.client;

import java.util.Objects;
import org.hiero.consensusnode.client.internal.ClientRuntime;
import org.hiero.ledger.config.NetworkSetting;

/// Factory methods of the package `org.hiero.consensusnode.client`.
public final class ClientFactory {

    /// Creates a client for the given network that uses the operator account to pay for and sign requests.
    public static HieroClient<?> createClient(final NetworkSetting networkSettings, final Account operatorAccount) {
        return createClient(networkSettings, operatorAccount, (bytes, node) -> new NodeSignature(node,
                operatorAccount.privateKey().createPublicKey(), operatorAccount.privateKey().sign(bytes)));
    }

    /// Creates a client for the given network that uses the operator account to pay for requests and the given
    /// signer to sign transactions.
    public static HieroClient<?> createClient(final NetworkSetting networkSettings, final Account operatorAccount, final TransactionSigner transactionSigner) {
        return createClient(networkSettings, operatorAccount, transactionSigner, ExecutionSettings.createDefault());
    }

    /// Creates a client for the given network that uses the operator account to pay for requests, the given signer to
    /// sign transactions and the given settings to execute requests.
    public static HieroClient<?> createClient(final NetworkSetting networkSettings, final Account operatorAccount,
                                              final TransactionSigner transactionSigner,
                                              final ExecutionSettings executionSettings) {
        Objects.requireNonNull(networkSettings, "networkSettings must not be null");
        final HieroClient<?> client = new HieroClient<>(operatorAccount, networkSettings.network(), transactionSigner,
                executionSettings);
        ClientRuntime.register(client, networkSettings);
        return client;
    }

    private ClientFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
