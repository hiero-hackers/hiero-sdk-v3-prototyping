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
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Submits a transaction signed by an Ethereum account. The fee of this transaction is paid by the payer (the relay);
/// the gas is paid by the Ethereum account and, up to `maxGasAllowance`, by the payer.
public final class EthereumTransaction extends Transaction<EthereumTransactionReceipt, EthereumTransaction> {

    private final byte[] ethereumData;
    private final @Nullable Address callDataFileId;
    private final @Nullable NativeToken<?, ?> maxGasAllowance;

    /// Creates a new `EthereumTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public EthereumTransaction(
            final byte[] ethereumData,
            final @Nullable Address callDataFileId,
            final @Nullable NativeToken<?, ?> maxGasAllowance) {
        super();
        Objects.requireNonNull(ethereumData, "ethereumData must not be null");
        if (ethereumData.length < 1) {
            throw new IllegalArgumentException("ethereumData must contain at least 1 element(s)");
        }
        this.ethereumData = ethereumData.clone();
        this.callDataFileId = callDataFileId;
        this.maxGasAllowance = maxGasAllowance;
    }

    /// The signed Ethereum transaction, RLP-encoded: a legacy transaction or a typed transaction of EIP-2930 or
    /// EIP-1559. If `callDataFileId` is set, its call data must be empty.
    public byte[] ethereumData() {
        return ethereumData.clone();
    }

    /// The file that holds the call data, as hex text, for call data that does not fit into one transaction.
    public @Nullable Address callDataFileId() {
        return callDataFileId;
    }

    /// The most the payer pays for the gas that the Ethereum account does not pay itself; nothing if unset.
    public @Nullable NativeToken<?, ?> maxGasAllowance() {
        return maxGasAllowance;
    }

    @Override
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.ETHEREUM).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.ETHEREUM).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.ETHEREUM).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.ETHEREUM).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<EthereumTransactionReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.ETHEREUM).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.ETHEREUM).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ContractKinds.ETHEREUM).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.ETHEREUM).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ContractKinds.ETHEREUM).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `EthereumTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `EthereumTransaction`.
    ///
    /// `build()` creates the `EthereumTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private byte @Nullable [] ethereumData;
        private @Nullable Address callDataFileId;
        private @Nullable NativeToken<?, ?> maxGasAllowance;

        private Builder() {
            // created by EthereumTransaction.builder()
        }

        /// Sets the `ethereumData`.
        ///
        /// The signed Ethereum transaction, RLP-encoded: a legacy transaction or a typed transaction of EIP-2930 or
        /// EIP-1559. If `callDataFileId` is set, its call data must be empty.
        ///
        /// @param ethereumData the value
        /// @return this builder
        public Builder ethereumData(final byte[] ethereumData) {
            this.ethereumData = ethereumData;
            return this;
        }

        /// Sets the `callDataFileId`.
        ///
        /// The file that holds the call data, as hex text, for call data that does not fit into one transaction.
        ///
        /// @param callDataFileId the value
        /// @return this builder
        public Builder callDataFileId(final @Nullable Address callDataFileId) {
            this.callDataFileId = callDataFileId;
            return this;
        }

        /// Sets the `maxGasAllowance`.
        ///
        /// The most the payer pays for the gas that the Ethereum account does not pay itself; nothing if unset.
        ///
        /// @param maxGasAllowance the value
        /// @return this builder
        public Builder maxGasAllowance(final @Nullable NativeToken<?, ?> maxGasAllowance) {
            this.maxGasAllowance = maxGasAllowance;
            return this;
        }

        /// Creates the `EthereumTransaction` of the values set.
        ///
        /// @return the new `EthereumTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public EthereumTransaction build() {
            return new EthereumTransaction(
                    Objects.requireNonNull(ethereumData, "ethereumData must not be null"),
                    callDataFileId,
                    maxGasAllowance);
        }
    }

    @Override
    public String toString() {
        return "EthereumTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", ethereumData=" + "byte[" + ethereumData().length + "]"
                + ", callDataFileId=" + callDataFileId()
                + ", maxGasAllowance=" + maxGasAllowance()
                + "]";
    }
}
