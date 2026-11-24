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
import org.hiero.consensusnode.transactions.ContractFunctionResult;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query that calls a function of a contract on the answering node, without consensus. The call must not change
/// state; it creates no transaction and no record. The query is charged for the full `gas`, not for the gas used.
public final class ContractCallQuery extends PaidQuery<ContractFunctionResult> {

    private final ContractId contractId;
    private final long gas;
    private final byte[] callData;
    private final @Nullable AccountId senderId;

    /// Creates a new `ContractCallQuery`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractCallQuery(
            final ContractId contractId,
            final long gas,
            final byte[] callData,
            final @Nullable AccountId senderId) {
        super();
        Objects.requireNonNull(contractId, "contractId must not be null");
        this.contractId = contractId;
        if (gas < 0L) {
            throw new IllegalArgumentException("gas must be at least 0");
        }
        this.gas = gas;
        Objects.requireNonNull(callData, "callData must not be null");
        this.callData = callData.clone();
        this.senderId = senderId;
    }

    /// Creates a new `ContractCallQuery` with the default value of `callData`.
    public ContractCallQuery(
            final ContractId contractId,
            final long gas,
            final @Nullable AccountId senderId) {
        this(contractId, gas, new byte[0], senderId);
    }

    /// the contract that is called
    public ContractId contractId() {
        return contractId;
    }

    /// the gas the call may use; all of it is charged
    public long gas() {
        return gas;
    }

    /// The selector of the function followed by its ABI-encoded arguments (see `AbiFunction.encodeCall`). Empty calls
    /// the receive or fallback function of the contract.
    public byte[] callData() {
        return callData.clone();
    }

    /// The account the call is executed as (`msg.sender`); the operator account of the client if unset.
    public @Nullable AccountId senderId() {
        return senderId;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public ContractCallQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public ContractCallQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public ContractCallQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
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
    public CompletionStage<PaidQueryResponse<ContractFunctionResult>> submit(final HieroClient<?> client) {
        return Querying.paid(this, this, maxQueryPayment(), ContractQueries.CONTRACT_CALL, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        return Querying.cost(this, this, ContractQueries.CONTRACT_CALL, client);
    }

    /// Returns a builder for a new `ContractCallQuery`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractCallQuery`.
    ///
    /// `build()` creates the `ContractCallQuery` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ContractId contractId;
        private @Nullable Long gas;
        private byte[] callData = new byte[0];
        private @Nullable AccountId senderId;

        private Builder() {
            // created by ContractCallQuery.builder()
        }

        /// Sets the `contractId`.
        ///
        /// the contract that is called
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// the gas the call may use; all of it is charged
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `callData`.
        ///
        /// The selector of the function followed by its ABI-encoded arguments (see `AbiFunction.encodeCall`). Empty calls
        /// the receive or fallback function of the contract.
        ///
        /// @param callData the value
        /// @return this builder
        public Builder callData(final byte[] callData) {
            this.callData = callData;
            return this;
        }

        /// Sets the `senderId`.
        ///
        /// The account the call is executed as (`msg.sender`); the operator account of the client if unset.
        ///
        /// @param senderId the value
        /// @return this builder
        public Builder senderId(final @Nullable AccountId senderId) {
            this.senderId = senderId;
            return this;
        }

        /// Creates the `ContractCallQuery` of the values set.
        ///
        /// @return the new `ContractCallQuery`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractCallQuery build() {
            return new ContractCallQuery(
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    Objects.requireNonNull(gas, "gas must not be null"),
                    callData,
                    senderId);
        }
    }

    @Override
    public String toString() {
        return "ContractCallQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + ", contractId=" + contractId()
                + ", gas=" + gas()
                + ", callData=" + "byte[" + callData().length + "]"
                + ", senderId=" + senderId()
                + "]";
    }
}
