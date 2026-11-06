package org.hiero.consensusnode.transactions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.evm.EvmAddress;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The result of executing a contract: what the function or constructor returned, the gas it used and the logs it
/// emitted. The result and the logs are encoded in the contract ABI; decode them with `AbiFunction.decodeResult` and
/// `AbiEvent.decodeLog` of the namespace `evm`.
///
/// @param contractId the contract that was called or created
/// @param evmAddress the EVM address of a created contract; absent for a call
/// @param result the data the function returned, ABI-encoded
/// @param errorMessage why the execution failed or reverted; absent on success
/// @param bloom the 256-byte bloom filter of all logs
/// @param gasUsed the gas the execution used
/// @param gas the gas the execution was allowed to use
/// @param amount the amount transferred to the contract
/// @param callData the call data: the selector and the ABI-encoded arguments
/// @param senderId the account that sent the call; absent if it is the payer
/// @param logs the logs the execution emitted, in their order
/// @param contractNonces the nonces of the contracts the execution changed
/// @param signerNonce the nonce of the signer of an Ethereum transaction
public record ContractFunctionResult(
        ContractId contractId,
        @Nullable EvmAddress evmAddress,
        byte[] result,
        @Nullable String errorMessage,
        byte[] bloom,
        long gasUsed,
        long gas,
        NativeToken<?, ?> amount,
        byte[] callData,
        @Nullable AccountId senderId,
        List<ContractLog> logs,
        List<ContractNonce> contractNonces,
        @Nullable Long signerNonce) {

    /// Creates a new `ContractFunctionResult`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractFunctionResult {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(result, "result must not be null");
        result = result.clone();
        Objects.requireNonNull(bloom, "bloom must not be null");
        bloom = bloom.clone();
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(callData, "callData must not be null");
        callData = callData.clone();
        Objects.requireNonNull(logs, "logs must not be null");
        logs = List.copyOf(logs);
        Objects.requireNonNull(contractNonces, "contractNonces must not be null");
        contractNonces = List.copyOf(contractNonces);
    }

    /// Creates a new `ContractFunctionResult` with the default value of `logs`, `contractNonces`.
    public ContractFunctionResult(
            final ContractId contractId,
            final @Nullable EvmAddress evmAddress,
            final byte[] result,
            final @Nullable String errorMessage,
            final byte[] bloom,
            final long gasUsed,
            final long gas,
            final NativeToken<?, ?> amount,
            final byte[] callData,
            final @Nullable AccountId senderId,
            final @Nullable Long signerNonce) {
        this(contractId, evmAddress, result, errorMessage, bloom, gasUsed, gas, amount, callData, senderId, List.of(), List.of(), signerNonce);
    }

    /// the data the function returned, ABI-encoded
    ///
    /// Returns a copy of the `result` array.
    @Override
    public byte[] result() {
        return result.clone();
    }

    /// the 256-byte bloom filter of all logs
    ///
    /// Returns a copy of the `bloom` array.
    @Override
    public byte[] bloom() {
        return bloom.clone();
    }

    /// the call data: the selector and the ABI-encoded arguments
    ///
    /// Returns a copy of the `callData` array.
    @Override
    public byte[] callData() {
        return callData.clone();
    }

    /// Compares `result`, `bloom` and `callData` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ContractFunctionResult other
            && Objects.equals(contractId, other.contractId)
            && Objects.equals(evmAddress, other.evmAddress)
            && Arrays.equals(result, other.result)
            && Objects.equals(errorMessage, other.errorMessage)
            && Arrays.equals(bloom, other.bloom)
            && gasUsed == other.gasUsed
            && gas == other.gas
            && Objects.equals(amount, other.amount)
            && Arrays.equals(callData, other.callData)
            && Objects.equals(senderId, other.senderId)
            && Objects.equals(logs, other.logs)
            && Objects.equals(contractNonces, other.contractNonces)
            && Objects.equals(signerNonce, other.signerNonce);
    }

    /// Hashes `result`, `bloom` and `callData` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(contractId, evmAddress, Arrays.hashCode(result), errorMessage, Arrays.hashCode(bloom), gasUsed, gas, amount, Arrays.hashCode(callData), senderId, logs, contractNonces, signerNonce);
    }

    /// Shows only the length of `result`, `bloom` and `callData` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ContractFunctionResult[contractId=" + contractId + ", evmAddress=" + evmAddress + ", result=" + "byte[" + result.length + "]" + ", errorMessage=" + errorMessage + ", bloom=" + "byte[" + bloom.length + "]" + ", gasUsed=" + gasUsed + ", gas=" + gas + ", amount=" + amount + ", callData=" + "byte[" + callData.length + "]" + ", senderId=" + senderId + ", logs=" + logs + ", contractNonces=" + contractNonces + ", signerNonce=" + signerNonce + "]";
    }

    /// Returns a builder for a new `ContractFunctionResult`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractFunctionResult`.
    ///
    /// `build()` creates the `ContractFunctionResult` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ContractId contractId;
        private @Nullable EvmAddress evmAddress;
        private byte @Nullable [] result;
        private @Nullable String errorMessage;
        private byte @Nullable [] bloom;
        private @Nullable Long gasUsed;
        private @Nullable Long gas;
        private @Nullable NativeToken<?, ?> amount;
        private byte @Nullable [] callData;
        private @Nullable AccountId senderId;
        private List<ContractLog> logs = List.of();
        private List<ContractNonce> contractNonces = List.of();
        private @Nullable Long signerNonce;

        private Builder() {
            // created by ContractFunctionResult.builder()
        }

        /// Sets the `contractId`.
        ///
        /// the contract that was called or created
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `evmAddress`.
        ///
        /// the EVM address of a created contract; absent for a call
        ///
        /// @param evmAddress the value
        /// @return this builder
        public Builder evmAddress(final @Nullable EvmAddress evmAddress) {
            this.evmAddress = evmAddress;
            return this;
        }

        /// Sets the `result`.
        ///
        /// the data the function returned, ABI-encoded
        ///
        /// @param result the value
        /// @return this builder
        public Builder result(final byte[] result) {
            this.result = result;
            return this;
        }

        /// Sets the `errorMessage`.
        ///
        /// why the execution failed or reverted; absent on success
        ///
        /// @param errorMessage the value
        /// @return this builder
        public Builder errorMessage(final @Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /// Sets the `bloom`.
        ///
        /// the 256-byte bloom filter of all logs
        ///
        /// @param bloom the value
        /// @return this builder
        public Builder bloom(final byte[] bloom) {
            this.bloom = bloom;
            return this;
        }

        /// Sets the `gasUsed`.
        ///
        /// the gas the execution used
        ///
        /// @param gasUsed the value
        /// @return this builder
        public Builder gasUsed(final long gasUsed) {
            this.gasUsed = gasUsed;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// the gas the execution was allowed to use
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `amount`.
        ///
        /// the amount transferred to the contract
        ///
        /// @param amount the value
        /// @return this builder
        public Builder amount(final NativeToken<?, ?> amount) {
            this.amount = amount;
            return this;
        }

        /// Sets the `callData`.
        ///
        /// the call data: the selector and the ABI-encoded arguments
        ///
        /// @param callData the value
        /// @return this builder
        public Builder callData(final byte[] callData) {
            this.callData = callData;
            return this;
        }

        /// Sets the `senderId`.
        ///
        /// the account that sent the call; absent if it is the payer
        ///
        /// @param senderId the value
        /// @return this builder
        public Builder senderId(final @Nullable AccountId senderId) {
            this.senderId = senderId;
            return this;
        }

        /// Sets the `logs`.
        ///
        /// the logs the execution emitted, in their order
        ///
        /// @param logs the value
        /// @return this builder
        public Builder logs(final List<ContractLog> logs) {
            this.logs = logs;
            return this;
        }

        /// Sets the `contractNonces`.
        ///
        /// the nonces of the contracts the execution changed
        ///
        /// @param contractNonces the value
        /// @return this builder
        public Builder contractNonces(final List<ContractNonce> contractNonces) {
            this.contractNonces = contractNonces;
            return this;
        }

        /// Sets the `signerNonce`.
        ///
        /// the nonce of the signer of an Ethereum transaction
        ///
        /// @param signerNonce the value
        /// @return this builder
        public Builder signerNonce(final @Nullable Long signerNonce) {
            this.signerNonce = signerNonce;
            return this;
        }

        /// Creates the `ContractFunctionResult` of the values set.
        ///
        /// @return the new `ContractFunctionResult`
        /// @throws NullPointerException if a required value is not set
        public ContractFunctionResult build() {
            return new ContractFunctionResult(
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    evmAddress,
                    Objects.requireNonNull(result, "result must not be null"),
                    errorMessage,
                    Objects.requireNonNull(bloom, "bloom must not be null"),
                    Objects.requireNonNull(gasUsed, "gasUsed must not be null"),
                    Objects.requireNonNull(gas, "gas must not be null"),
                    Objects.requireNonNull(amount, "amount must not be null"),
                    Objects.requireNonNull(callData, "callData must not be null"),
                    senderId,
                    logs,
                    contractNonces,
                    signerNonce);
        }
    }
}
