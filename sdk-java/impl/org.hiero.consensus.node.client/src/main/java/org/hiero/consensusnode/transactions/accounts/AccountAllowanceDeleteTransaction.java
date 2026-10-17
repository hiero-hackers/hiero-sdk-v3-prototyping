package org.hiero.consensusnode.transactions.accounts;

import org.hiero.consensusnode.client.internal.AccountKinds;
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
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Revokes previously granted NFT allowances. NFT-only — HBAR and fungible-token allowances are
/// revoked by approving them with amount = 0, and collection-wide grants are revoked by approving
/// with an `NftCollectionGrant(false)`.
public final class AccountAllowanceDeleteTransaction extends Transaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> {

    private final List<NftAllowanceDeletion> nftAllowanceDeletions;

    /// Creates a new `AccountAllowanceDeleteTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AccountAllowanceDeleteTransaction(final List<NftAllowanceDeletion> nftAllowanceDeletions) {
        super();
        Objects.requireNonNull(nftAllowanceDeletions, "nftAllowanceDeletions must not be null");
        this.nftAllowanceDeletions = List.copyOf(nftAllowanceDeletions);
    }

    /// Returns the `nftAllowanceDeletions`.
    public List<NftAllowanceDeletion> nftAllowanceDeletions() {
        return nftAllowanceDeletions;
    }

    @Override
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<AccountAllowanceDeleteReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_DELETE).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "AccountAllowanceDeleteTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", nftAllowanceDeletions=" + nftAllowanceDeletions()
                + "]";
    }
}
