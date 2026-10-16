package org.hiero.consensusnode.admin.system;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
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
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Marks a file or contract as deleted by privileged council action. Exactly one of `fileId` and
/// `contractId` must be set. For files, expirationTime is required and marks when the deletion
/// becomes permanent (a SystemUndelete before that time reverses it). For contracts,
/// expirationTime is ignored.
public final class SystemDeleteTransaction extends Transaction<SystemDeleteReceipt, SystemDeleteTransaction> {

    private final @Nullable Address fileId;
    private final @Nullable ContractId contractId;
    private final @Nullable ZonedDateTime expirationTime;

    /// Creates a new `SystemDeleteTransaction`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public SystemDeleteTransaction(
            final @Nullable Address fileId,
            final @Nullable ContractId contractId,
            final @Nullable ZonedDateTime expirationTime) {
        super();
        this.fileId = fileId;
        this.contractId = contractId;
        this.expirationTime = expirationTime;
        if ((fileId != null ? 1 : 0) + (contractId != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of fileId, contractId must be set");
        }
    }

    /// the file to delete; absent when contractId is set
    public @Nullable Address fileId() {
        return fileId;
    }

    /// the contract to delete; absent when fileId is set
    public @Nullable ContractId contractId() {
        return contractId;
    }

    /// when a system-deleted file becomes permanently unrecoverable; required when fileId is set, ignored when contractId is set
    public @Nullable ZonedDateTime expirationTime() {
        return expirationTime;
    }

    @Override
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.pack");
    }

    @Override
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.sign");
    }

    @Override
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.sign");
    }

    @Override
    public CompletionStage<Response<SystemDeleteReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.signWithOperatorAndSubmit");
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
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<SystemDeleteReceipt, SystemDeleteTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: SystemDeleteTransaction.signForBatch");
    }

    /// Returns a builder for a new `SystemDeleteTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `SystemDeleteTransaction`.
    ///
    /// `build()` creates the `SystemDeleteTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address fileId;
        private @Nullable ContractId contractId;
        private @Nullable ZonedDateTime expirationTime;

        private Builder() {
            // created by SystemDeleteTransaction.builder()
        }

        /// Sets the `fileId`.
        ///
        /// the file to delete; absent when contractId is set
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final @Nullable Address fileId) {
            this.fileId = fileId;
            return this;
        }

        /// Sets the `contractId`.
        ///
        /// the contract to delete; absent when fileId is set
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final @Nullable ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// when a system-deleted file becomes permanently unrecoverable; required when fileId is set, ignored when contractId is set
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Creates the `SystemDeleteTransaction` of the values set.
        ///
        /// @return the new `SystemDeleteTransaction`
        /// @throws IllegalArgumentException if a value violates its constraints
        public SystemDeleteTransaction build() {
            return new SystemDeleteTransaction(fileId, contractId, expirationTime);
        }
    }

    @Override
    public String toString() {
        return "SystemDeleteTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", fileId=" + fileId()
                + ", contractId=" + contractId()
                + ", expirationTime=" + expirationTime()
                + "]";
    }
}
