package org.hiero.mirrornode.contract;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// The result of the execution of a contract function, a contract creation or a wrapped Ethereum transaction.
///
/// @param accessList the access list of a wrapped Ethereum transaction
/// @param address the EVM address of the contract
/// @param authorizationList the authorizations of a wrapped EIP-7702 Ethereum transaction
/// @param amount the amount sent to the function; in tinybars, in weibars if requested with hbar = false
/// @param blockGasUsed the gas used by the whole block
/// @param blockHash the hash of the block in hex
/// @param bloom the bloom filter of the result in hex
/// @param callResult the value returned by the function in hex
/// @param chainId the chain id of a wrapped Ethereum transaction in hex
/// @param createdContractIds the contracts created by the execution
/// @param errorMessage the message of an error during the execution
/// @param failedInitcode the init code of a failed contract creation in hex
/// @param from the sender
/// @param functionParameters the parameters passed to the function in hex
/// @param gasConsumed the gas consumed by the EVM to execute the contract
/// @param gasLimit the maximum gas allowed for the execution
/// @param gasPrice the gas price of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
/// @param gasUsed the gas used to execute the contract
/// @param hash the hash of the transaction in hex
/// @param maxFeePerGas of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
/// @param maxPriorityFeePerGas of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
/// @param nonce the nonce of a wrapped Ethereum transaction
/// @param r the signature value r of a wrapped Ethereum transaction in hex
/// @param result the status of the transaction, e.g. SUCCESS
/// @param s the signature value s of a wrapped Ethereum transaction in hex
/// @param status 0x1 for a successful transaction, 0x0 otherwise
/// @param timestamp the consensus time of the execution
/// @param to the recipient
/// @param transactionIndex the position of the transaction in the block
/// @param type the type of a wrapped Ethereum transaction: 0 (before EIP-1559) or 2 (EIP-1559)
/// @param v the recovery id of a wrapped Ethereum transaction
/// @param logs only filled by the lookups of a single result
/// @param stateChanges only filled by the lookups of a single result
public record ContractResult(
        List<AccessListEntry> accessList,
        @Nullable EvmAddress address,
        List<AuthorizationListEntry> authorizationList,
        @Nullable BigInteger amount,
        @Nullable Long blockGasUsed,
        @Nullable String blockHash,
        @Nullable Long blockNumber,
        @Nullable String bloom,
        @Nullable String callResult,
        @Nullable String chainId,
        @Nullable ContractId contractId,
        List<ContractId> createdContractIds,
        @Nullable String errorMessage,
        @Nullable String failedInitcode,
        @Nullable EvmAddress from,
        @Nullable String functionParameters,
        @Nullable Long gasConsumed,
        long gasLimit,
        @Nullable String gasPrice,
        @Nullable Long gasUsed,
        String hash,
        @Nullable String maxFeePerGas,
        @Nullable String maxPriorityFeePerGas,
        @Nullable Long nonce,
        @Nullable String r,
        String result,
        @Nullable String s,
        String status,
        ZonedDateTime timestamp,
        @Nullable EvmAddress to,
        @Nullable Long transactionIndex,
        @Nullable Integer type,
        @Nullable Integer v,
        List<ContractLog> logs,
        List<ContractStateChange> stateChanges) {

    private static final BigInteger AMOUNT_MIN = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
    private static final BigInteger AMOUNT_MAX = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");

    /// Creates a new `ContractResult`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractResult {
        Objects.requireNonNull(accessList, "accessList must not be null");
        accessList = List.copyOf(accessList);
        Objects.requireNonNull(authorizationList, "authorizationList must not be null");
        authorizationList = List.copyOf(authorizationList);
        if (amount != null && (amount.compareTo(AMOUNT_MIN) < 0 || amount.compareTo(AMOUNT_MAX) > 0)) {
            throw new IllegalArgumentException("amount must be between -57896044618658097711785492504343953926634992332820282019728792003956564819968 and 57896044618658097711785492504343953926634992332820282019728792003956564819967");
        }
        Objects.requireNonNull(createdContractIds, "createdContractIds must not be null");
        createdContractIds = List.copyOf(createdContractIds);
        Objects.requireNonNull(hash, "hash must not be null");
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(logs, "logs must not be null");
        logs = List.copyOf(logs);
        Objects.requireNonNull(stateChanges, "stateChanges must not be null");
        stateChanges = List.copyOf(stateChanges);
    }

    /// Creates a new `ContractResult` with the default value of `accessList`, `authorizationList`, `createdContractIds`, `logs`, `stateChanges`.
    public ContractResult(
            final @Nullable EvmAddress address,
            final @Nullable BigInteger amount,
            final @Nullable Long blockGasUsed,
            final @Nullable String blockHash,
            final @Nullable Long blockNumber,
            final @Nullable String bloom,
            final @Nullable String callResult,
            final @Nullable String chainId,
            final @Nullable ContractId contractId,
            final @Nullable String errorMessage,
            final @Nullable String failedInitcode,
            final @Nullable EvmAddress from,
            final @Nullable String functionParameters,
            final @Nullable Long gasConsumed,
            final long gasLimit,
            final @Nullable String gasPrice,
            final @Nullable Long gasUsed,
            final String hash,
            final @Nullable String maxFeePerGas,
            final @Nullable String maxPriorityFeePerGas,
            final @Nullable Long nonce,
            final @Nullable String r,
            final String result,
            final @Nullable String s,
            final String status,
            final ZonedDateTime timestamp,
            final @Nullable EvmAddress to,
            final @Nullable Long transactionIndex,
            final @Nullable Integer type,
            final @Nullable Integer v) {
        this(List.of(), address, List.of(), amount, blockGasUsed, blockHash, blockNumber, bloom, callResult, chainId, contractId, List.of(), errorMessage, failedInitcode, from, functionParameters, gasConsumed, gasLimit, gasPrice, gasUsed, hash, maxFeePerGas, maxPriorityFeePerGas, nonce, r, result, s, status, timestamp, to, transactionIndex, type, v, List.of(), List.of());
    }

    /// Returns a builder for a new `ContractResult`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractResult`.
    ///
    /// `build()` creates the `ContractResult` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private List<AccessListEntry> accessList = List.of();
        private @Nullable EvmAddress address;
        private List<AuthorizationListEntry> authorizationList = List.of();
        private @Nullable BigInteger amount;
        private @Nullable Long blockGasUsed;
        private @Nullable String blockHash;
        private @Nullable Long blockNumber;
        private @Nullable String bloom;
        private @Nullable String callResult;
        private @Nullable String chainId;
        private @Nullable ContractId contractId;
        private List<ContractId> createdContractIds = List.of();
        private @Nullable String errorMessage;
        private @Nullable String failedInitcode;
        private @Nullable EvmAddress from;
        private @Nullable String functionParameters;
        private @Nullable Long gasConsumed;
        private @Nullable Long gasLimit;
        private @Nullable String gasPrice;
        private @Nullable Long gasUsed;
        private @Nullable String hash;
        private @Nullable String maxFeePerGas;
        private @Nullable String maxPriorityFeePerGas;
        private @Nullable Long nonce;
        private @Nullable String r;
        private @Nullable String result;
        private @Nullable String s;
        private @Nullable String status;
        private @Nullable ZonedDateTime timestamp;
        private @Nullable EvmAddress to;
        private @Nullable Long transactionIndex;
        private @Nullable Integer type;
        private @Nullable Integer v;
        private List<ContractLog> logs = List.of();
        private List<ContractStateChange> stateChanges = List.of();

        private Builder() {
            // created by ContractResult.builder()
        }

        /// Sets the `accessList`.
        ///
        /// the access list of a wrapped Ethereum transaction
        ///
        /// @param accessList the value
        /// @return this builder
        public Builder accessList(final List<AccessListEntry> accessList) {
            this.accessList = accessList;
            return this;
        }

        /// Sets the `address`.
        ///
        /// the EVM address of the contract
        ///
        /// @param address the value
        /// @return this builder
        public Builder address(final @Nullable EvmAddress address) {
            this.address = address;
            return this;
        }

        /// Sets the `authorizationList`.
        ///
        /// the authorizations of a wrapped EIP-7702 Ethereum transaction
        ///
        /// @param authorizationList the value
        /// @return this builder
        public Builder authorizationList(final List<AuthorizationListEntry> authorizationList) {
            this.authorizationList = authorizationList;
            return this;
        }

        /// Sets the `amount`.
        ///
        /// the amount sent to the function; in tinybars, in weibars if requested with hbar = false
        ///
        /// @param amount the value
        /// @return this builder
        public Builder amount(final @Nullable BigInteger amount) {
            this.amount = amount;
            return this;
        }

        /// Sets the `blockGasUsed`.
        ///
        /// the gas used by the whole block
        ///
        /// @param blockGasUsed the value
        /// @return this builder
        public Builder blockGasUsed(final @Nullable Long blockGasUsed) {
            this.blockGasUsed = blockGasUsed;
            return this;
        }

        /// Sets the `blockHash`.
        ///
        /// the hash of the block in hex
        ///
        /// @param blockHash the value
        /// @return this builder
        public Builder blockHash(final @Nullable String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        /// Sets the `blockNumber`.
        ///
        /// @param blockNumber the value
        /// @return this builder
        public Builder blockNumber(final @Nullable Long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        /// Sets the `bloom`.
        ///
        /// the bloom filter of the result in hex
        ///
        /// @param bloom the value
        /// @return this builder
        public Builder bloom(final @Nullable String bloom) {
            this.bloom = bloom;
            return this;
        }

        /// Sets the `callResult`.
        ///
        /// the value returned by the function in hex
        ///
        /// @param callResult the value
        /// @return this builder
        public Builder callResult(final @Nullable String callResult) {
            this.callResult = callResult;
            return this;
        }

        /// Sets the `chainId`.
        ///
        /// the chain id of a wrapped Ethereum transaction in hex
        ///
        /// @param chainId the value
        /// @return this builder
        public Builder chainId(final @Nullable String chainId) {
            this.chainId = chainId;
            return this;
        }

        /// Sets the `contractId`.
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final @Nullable ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `createdContractIds`.
        ///
        /// the contracts created by the execution
        ///
        /// @param createdContractIds the value
        /// @return this builder
        public Builder createdContractIds(final List<ContractId> createdContractIds) {
            this.createdContractIds = createdContractIds;
            return this;
        }

        /// Sets the `errorMessage`.
        ///
        /// the message of an error during the execution
        ///
        /// @param errorMessage the value
        /// @return this builder
        public Builder errorMessage(final @Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /// Sets the `failedInitcode`.
        ///
        /// the init code of a failed contract creation in hex
        ///
        /// @param failedInitcode the value
        /// @return this builder
        public Builder failedInitcode(final @Nullable String failedInitcode) {
            this.failedInitcode = failedInitcode;
            return this;
        }

        /// Sets the `from`.
        ///
        /// the sender
        ///
        /// @param from the value
        /// @return this builder
        public Builder from(final @Nullable EvmAddress from) {
            this.from = from;
            return this;
        }

        /// Sets the `functionParameters`.
        ///
        /// the parameters passed to the function in hex
        ///
        /// @param functionParameters the value
        /// @return this builder
        public Builder functionParameters(final @Nullable String functionParameters) {
            this.functionParameters = functionParameters;
            return this;
        }

        /// Sets the `gasConsumed`.
        ///
        /// the gas consumed by the EVM to execute the contract
        ///
        /// @param gasConsumed the value
        /// @return this builder
        public Builder gasConsumed(final @Nullable Long gasConsumed) {
            this.gasConsumed = gasConsumed;
            return this;
        }

        /// Sets the `gasLimit`.
        ///
        /// the maximum gas allowed for the execution
        ///
        /// @param gasLimit the value
        /// @return this builder
        public Builder gasLimit(final long gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        /// Sets the `gasPrice`.
        ///
        /// the gas price of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
        ///
        /// @param gasPrice the value
        /// @return this builder
        public Builder gasPrice(final @Nullable String gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        /// Sets the `gasUsed`.
        ///
        /// the gas used to execute the contract
        ///
        /// @param gasUsed the value
        /// @return this builder
        public Builder gasUsed(final @Nullable Long gasUsed) {
            this.gasUsed = gasUsed;
            return this;
        }

        /// Sets the `hash`.
        ///
        /// the hash of the transaction in hex
        ///
        /// @param hash the value
        /// @return this builder
        public Builder hash(final String hash) {
            this.hash = hash;
            return this;
        }

        /// Sets the `maxFeePerGas`.
        ///
        /// of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
        ///
        /// @param maxFeePerGas the value
        /// @return this builder
        public Builder maxFeePerGas(final @Nullable String maxFeePerGas) {
            this.maxFeePerGas = maxFeePerGas;
            return this;
        }

        /// Sets the `maxPriorityFeePerGas`.
        ///
        /// of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
        ///
        /// @param maxPriorityFeePerGas the value
        /// @return this builder
        public Builder maxPriorityFeePerGas(final @Nullable String maxPriorityFeePerGas) {
            this.maxPriorityFeePerGas = maxPriorityFeePerGas;
            return this;
        }

        /// Sets the `nonce`.
        ///
        /// the nonce of a wrapped Ethereum transaction
        ///
        /// @param nonce the value
        /// @return this builder
        public Builder nonce(final @Nullable Long nonce) {
            this.nonce = nonce;
            return this;
        }

        /// Sets the `r`.
        ///
        /// the signature value r of a wrapped Ethereum transaction in hex
        ///
        /// @param r the value
        /// @return this builder
        public Builder r(final @Nullable String r) {
            this.r = r;
            return this;
        }

        /// Sets the `result`.
        ///
        /// the status of the transaction, e.g. SUCCESS
        ///
        /// @param result the value
        /// @return this builder
        public Builder result(final String result) {
            this.result = result;
            return this;
        }

        /// Sets the `s`.
        ///
        /// the signature value s of a wrapped Ethereum transaction in hex
        ///
        /// @param s the value
        /// @return this builder
        public Builder s(final @Nullable String s) {
            this.s = s;
            return this;
        }

        /// Sets the `status`.
        ///
        /// 0x1 for a successful transaction, 0x0 otherwise
        ///
        /// @param status the value
        /// @return this builder
        public Builder status(final String status) {
            this.status = status;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the consensus time of the execution
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Sets the `to`.
        ///
        /// the recipient
        ///
        /// @param to the value
        /// @return this builder
        public Builder to(final @Nullable EvmAddress to) {
            this.to = to;
            return this;
        }

        /// Sets the `transactionIndex`.
        ///
        /// the position of the transaction in the block
        ///
        /// @param transactionIndex the value
        /// @return this builder
        public Builder transactionIndex(final @Nullable Long transactionIndex) {
            this.transactionIndex = transactionIndex;
            return this;
        }

        /// Sets the `type`.
        ///
        /// the type of a wrapped Ethereum transaction: 0 (before EIP-1559) or 2 (EIP-1559)
        ///
        /// @param type the value
        /// @return this builder
        public Builder type(final @Nullable Integer type) {
            this.type = type;
            return this;
        }

        /// Sets the `v`.
        ///
        /// the recovery id of a wrapped Ethereum transaction
        ///
        /// @param v the value
        /// @return this builder
        public Builder v(final @Nullable Integer v) {
            this.v = v;
            return this;
        }

        /// Sets the `logs`.
        ///
        /// only filled by the lookups of a single result
        ///
        /// @param logs the value
        /// @return this builder
        public Builder logs(final List<ContractLog> logs) {
            this.logs = logs;
            return this;
        }

        /// Sets the `stateChanges`.
        ///
        /// only filled by the lookups of a single result
        ///
        /// @param stateChanges the value
        /// @return this builder
        public Builder stateChanges(final List<ContractStateChange> stateChanges) {
            this.stateChanges = stateChanges;
            return this;
        }

        /// Creates the `ContractResult` of the values set.
        ///
        /// @return the new `ContractResult`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractResult build() {
            return new ContractResult(
                    accessList,
                    address,
                    authorizationList,
                    amount,
                    blockGasUsed,
                    blockHash,
                    blockNumber,
                    bloom,
                    callResult,
                    chainId,
                    contractId,
                    createdContractIds,
                    errorMessage,
                    failedInitcode,
                    from,
                    functionParameters,
                    gasConsumed,
                    Objects.requireNonNull(gasLimit, "gasLimit must not be null"),
                    gasPrice,
                    gasUsed,
                    Objects.requireNonNull(hash, "hash must not be null"),
                    maxFeePerGas,
                    maxPriorityFeePerGas,
                    nonce,
                    r,
                    Objects.requireNonNull(result, "result must not be null"),
                    s,
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"),
                    to,
                    transactionIndex,
                    type,
                    v,
                    logs,
                    stateChanges);
        }
    }
}
