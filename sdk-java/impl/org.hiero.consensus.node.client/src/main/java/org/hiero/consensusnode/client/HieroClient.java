package org.hiero.consensusnode.client;

import java.util.Objects;
import org.hiero.ledger.Network;
import org.hiero.nativeToken.NativeTokenUnit;

/// A connection to one network with one operator account; the entry point for all queries and transactions.
///
/// @param operatorAccount the account that pays for and signs requests
/// @param network the network to connect to
/// @param executionSettings how requests are executed; by default `ExecutionSettings.createDefault()`
public record HieroClient<Unit extends NativeTokenUnit>(
        Account operatorAccount,
        Network<Unit> network,
        ExecutionSettings executionSettings) {

    /// Creates a new `HieroClient`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public HieroClient {
        Objects.requireNonNull(operatorAccount, "operatorAccount must not be null");
        Objects.requireNonNull(network, "network must not be null");
        Objects.requireNonNull(executionSettings, "executionSettings must not be null");
    }
}
