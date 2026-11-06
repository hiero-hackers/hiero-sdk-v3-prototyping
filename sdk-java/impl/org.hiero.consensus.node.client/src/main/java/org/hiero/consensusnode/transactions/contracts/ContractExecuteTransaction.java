package org.hiero.consensusnode.transactions.contracts;

import org.hiero.consensusnode.client.internal.ContractKinds;
import org.hiero.consensusnode.client.internal.Packing;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Calls a function of a contract. The result, the gas used and the logs are part of the record of the transaction.
public final class ContractExecuteTransaction extends Transaction<ContractExecuteReceipt, ContractExecuteTransaction> {

    private final ContractId contractId;
    private final long gas;
    private final @Nullable NativeToken<?, ?> payableAmount;
    private final byte[] callData;

    /// Creates a new `ContractExecuteTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractExecuteTransaction(
            final ContractId contractId,
            final long gas,
            final @Nullable NativeToken<?, ?> payableAmount,
            final byte[] callData) {
        super();
        Objects.requireNonNull(contractId, "contractId must not be null");
        this.contractId = contractId;
        if (gas < 0L) {
            throw new IllegalArgumentException("gas must be at least 0");
        }
        this.gas = gas;
        this.payableAmount = payableAmount;
        Objects.requireNonNull(callData, "callData must not be null");
        this.callData = callData.clone();
    }

    /// Creates a new `ContractExecuteTransaction` with the default value of `callData`.
    public ContractExecuteTransaction(
            final ContractId contractId,
            final long gas,
            final @Nullable NativeToken<?, ?> payableAmount) {
        this(contractId, gas, payableAmount, new byte[0]);
    }

    /// the contract that is called
    public ContractId contractId() {
        return contractId;
    }

    /// the maximum gas the call may use
    public long gas() {
        return gas;
    }

    /// Amount transferred from the payer to the contract. The function must be payable if it is set.
    public @Nullable NativeToken<?, ?> payableAmount() {
        return payableAmount;
    }

    /// The selector of the function followed by its ABI-encoded arguments (see `AbiFunction.encodeCall`). Empty
    /// calls the receive or fallback function of the contract.
    public byte[] callData() {
        return callData.clone();
    }

    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ContractExecuteReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).signWithOperatorAndSubmit(this, client);
    }

    /// Packs this transaction as the *inner* transaction of a BatchTransaction (HIP-551), without
    /// signing it. Unlike pack(payer, nodes), this produces a single TransactionBody addressed to no
    /// consensus node (nodeAccountID = 0.0.0) instead of one body per target node — an inner batch
    /// transaction is never submitted to a node on its own; it is embedded into
    /// BatchTransaction.innerTransactions and executed by the network as part of the batch. The
    /// returned PackedTransaction still carries its own TransactionId (payer + validStart); further
    /// signatures use PackedTransaction.sign(...) / signableBodies().
    ///
    /// batchKey names the Authority that must sign the *outer* BatchTransaction for this inner
    /// transaction to execute — the inner author's controlled opt-in to being batched. Whether a
    /// transaction type may be batched at all is decided by the network.
    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_EXECUTE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `ContractExecuteTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractExecuteTransaction`.
    ///
    /// `build()` creates the `ContractExecuteTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ContractId contractId;
        private @Nullable Long gas;
        private @Nullable NativeToken<?, ?> payableAmount;
        private byte[] callData = new byte[0];

        private Builder() {
            // created by ContractExecuteTransaction.builder()
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
        /// the maximum gas the call may use
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `payableAmount`.
        ///
        /// Amount transferred from the payer to the contract. The function must be payable if it is set.
        ///
        /// @param payableAmount the value
        /// @return this builder
        public Builder payableAmount(final @Nullable NativeToken<?, ?> payableAmount) {
            this.payableAmount = payableAmount;
            return this;
        }

        /// Sets the `callData`.
        ///
        /// The selector of the function followed by its ABI-encoded arguments (see `AbiFunction.encodeCall`). Empty
        /// calls the receive or fallback function of the contract.
        ///
        /// @param callData the value
        /// @return this builder
        public Builder callData(final byte[] callData) {
            this.callData = callData;
            return this;
        }

        /// Creates the `ContractExecuteTransaction` of the values set.
        ///
        /// @return the new `ContractExecuteTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractExecuteTransaction build() {
            return new ContractExecuteTransaction(
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    Objects.requireNonNull(gas, "gas must not be null"),
                    payableAmount,
                    callData);
        }
    }

    @Override
    public String toString() {
        return "ContractExecuteTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", contractId=" + contractId()
                + ", gas=" + gas()
                + ", payableAmount=" + payableAmount()
                + ", callData=" + "byte[" + callData().length + "]"
                + "]";
    }
}
