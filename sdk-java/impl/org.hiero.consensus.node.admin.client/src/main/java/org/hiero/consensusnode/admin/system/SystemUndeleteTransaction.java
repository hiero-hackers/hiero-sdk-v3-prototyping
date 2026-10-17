package org.hiero.consensusnode.admin.system;

import org.hiero.consensusnode.admin.internal.AdminKinds;
import org.hiero.consensusnode.client.internal.Packing;
import java.time.Duration;
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

/// Reverses a prior `SystemDeleteTransaction`. Exactly one of `fileId` and `contractId` must be set, matching the
/// target of the prior system delete. Must be submitted before the file's expirationTime; afterwards
/// the un-delete fails with INVALID_FILE_ID.
public final class SystemUndeleteTransaction extends Transaction<SystemUndeleteReceipt, SystemUndeleteTransaction> {

    private final @Nullable Address fileId;
    private final @Nullable ContractId contractId;

    /// Creates a new `SystemUndeleteTransaction`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public SystemUndeleteTransaction(final @Nullable Address fileId, final @Nullable ContractId contractId) {
        super();
        this.fileId = fileId;
        this.contractId = contractId;
        if ((fileId != null ? 1 : 0) + (contractId != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of fileId, contractId must be set");
        }
    }

    /// the file to restore; absent when contractId is set
    public @Nullable Address fileId() {
        return fileId;
    }

    /// the contract to restore; absent when fileId is set
    public @Nullable ContractId contractId() {
        return contractId;
    }

    @Override
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<SystemUndeleteReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<SystemUndeleteReceipt, SystemUndeleteTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(AdminKinds.SYSTEM_UNDELETE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `SystemUndeleteTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `SystemUndeleteTransaction`.
    ///
    /// `build()` creates the `SystemUndeleteTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address fileId;
        private @Nullable ContractId contractId;

        private Builder() {
            // created by SystemUndeleteTransaction.builder()
        }

        /// Sets the `fileId`.
        ///
        /// the file to restore; absent when contractId is set
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final @Nullable Address fileId) {
            this.fileId = fileId;
            return this;
        }

        /// Sets the `contractId`.
        ///
        /// the contract to restore; absent when fileId is set
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final @Nullable ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Creates the `SystemUndeleteTransaction` of the values set.
        ///
        /// @return the new `SystemUndeleteTransaction`
        /// @throws IllegalArgumentException if a value violates its constraints
        public SystemUndeleteTransaction build() {
            return new SystemUndeleteTransaction(fileId, contractId);
        }
    }

    @Override
    public String toString() {
        return "SystemUndeleteTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", fileId=" + fileId()
                + ", contractId=" + contractId()
                + "]";
    }
}
