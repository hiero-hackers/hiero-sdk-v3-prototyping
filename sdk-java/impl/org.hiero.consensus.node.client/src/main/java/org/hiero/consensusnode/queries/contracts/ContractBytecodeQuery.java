package org.hiero.consensusnode.queries.contracts;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.ContractQueries;
import org.hiero.consensusnode.client.internal.Querying;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query for the runtime bytecode of a contract.
public final class ContractBytecodeQuery extends PaidQuery<ContractBytecode> {

    private final ContractId contractId;

    /// Creates a new `ContractBytecodeQuery`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractBytecodeQuery(final ContractId contractId) {
        super();
        Objects.requireNonNull(contractId, "contractId must not be null");
        this.contractId = contractId;
    }

    /// Returns the `contractId`.
    public ContractId contractId() {
        return contractId;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public ContractBytecodeQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public ContractBytecodeQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public ContractBytecodeQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
        super.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up, and with
    /// `unknown-status-error` when a node answers with a status code that the SDK cannot name.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` or `UnknownStatusException` if the operation fails.
    @Override
    public CompletionStage<PaidQueryResponse<ContractBytecode>> submit(final HieroClient<?> client) {
        return Querying.paid(this, this, maxQueryPayment(), ContractQueries.CONTRACT_BYTECODE, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        return Querying.cost(this, this, ContractQueries.CONTRACT_BYTECODE, client);
    }

    @Override
    public String toString() {
        return "ContractBytecodeQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + ", contractId=" + contractId()
                + "]";
    }
}
