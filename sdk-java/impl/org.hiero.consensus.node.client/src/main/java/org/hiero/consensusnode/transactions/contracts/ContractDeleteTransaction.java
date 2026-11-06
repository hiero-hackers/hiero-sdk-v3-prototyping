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

/// Deletes a contract. Its remaining balance is transferred to an account or to another contract.
public final class ContractDeleteTransaction extends Transaction<ContractDeleteReceipt, ContractDeleteTransaction> {

    private final ContractId contractId;
    private final @Nullable AccountId transferAccountId;
    private final @Nullable ContractId transferContractId;

    /// Creates a new `ContractDeleteTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractDeleteTransaction(
            final ContractId contractId,
            final @Nullable AccountId transferAccountId,
            final @Nullable ContractId transferContractId) {
        super();
        Objects.requireNonNull(contractId, "contractId must not be null");
        this.contractId = contractId;
        this.transferAccountId = transferAccountId;
        this.transferContractId = transferContractId;
        if ((transferAccountId != null ? 1 : 0) + (transferContractId != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of transferAccountId, transferContractId must be set");
        }
    }

    /// the contract that is deleted
    public ContractId contractId() {
        return contractId;
    }

    /// the account that receives the remaining balance
    public @Nullable AccountId transferAccountId() {
        return transferAccountId;
    }

    /// the contract that receives the remaining balance
    public @Nullable ContractId transferContractId() {
        return transferContractId;
    }

    @Override
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ContractDeleteReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_DELETE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `ContractDeleteTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractDeleteTransaction`.
    ///
    /// `build()` creates the `ContractDeleteTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ContractId contractId;
        private @Nullable AccountId transferAccountId;
        private @Nullable ContractId transferContractId;

        private Builder() {
            // created by ContractDeleteTransaction.builder()
        }

        /// Sets the `contractId`.
        ///
        /// the contract that is deleted
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `transferAccountId`.
        ///
        /// the account that receives the remaining balance
        ///
        /// @param transferAccountId the value
        /// @return this builder
        public Builder transferAccountId(final @Nullable AccountId transferAccountId) {
            this.transferAccountId = transferAccountId;
            return this;
        }

        /// Sets the `transferContractId`.
        ///
        /// the contract that receives the remaining balance
        ///
        /// @param transferContractId the value
        /// @return this builder
        public Builder transferContractId(final @Nullable ContractId transferContractId) {
            this.transferContractId = transferContractId;
            return this;
        }

        /// Creates the `ContractDeleteTransaction` of the values set.
        ///
        /// @return the new `ContractDeleteTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractDeleteTransaction build() {
            return new ContractDeleteTransaction(
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    transferAccountId,
                    transferContractId);
        }
    }

    @Override
    public String toString() {
        return "ContractDeleteTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", contractId=" + contractId()
                + ", transferAccountId=" + transferAccountId()
                + ", transferContractId=" + transferContractId()
                + "]";
    }
}
