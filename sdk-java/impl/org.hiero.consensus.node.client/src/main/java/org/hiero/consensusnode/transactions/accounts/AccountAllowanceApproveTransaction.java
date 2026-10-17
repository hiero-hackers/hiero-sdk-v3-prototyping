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

/// Grants delegated-spend rights from one or more owners. Per-leg semantics:
///
/// - Every HBAR / fungible-token / per-serial NFT entry is independent — granting an NFT
/// allowance does not affect the owner's HBAR allowance to the same spender, etc.
/// - The owner of each leg (`ownerAccountId`, or `delegatingSpender` for HIP-336 NFT
/// sub-approvals) must sign the transaction.
public final class AccountAllowanceApproveTransaction extends Transaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> {

    private final List<HbarAllowance> hbarAllowances;
    private final List<TokenAllowance> tokenAllowances;
    private final List<NftAllowance> nftAllowances;

    /// Creates a new `AccountAllowanceApproveTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AccountAllowanceApproveTransaction(
            final List<HbarAllowance> hbarAllowances,
            final List<TokenAllowance> tokenAllowances,
            final List<NftAllowance> nftAllowances) {
        super();
        Objects.requireNonNull(hbarAllowances, "hbarAllowances must not be null");
        this.hbarAllowances = List.copyOf(hbarAllowances);
        Objects.requireNonNull(tokenAllowances, "tokenAllowances must not be null");
        this.tokenAllowances = List.copyOf(tokenAllowances);
        Objects.requireNonNull(nftAllowances, "nftAllowances must not be null");
        this.nftAllowances = List.copyOf(nftAllowances);
    }

    /// Creates a new `AccountAllowanceApproveTransaction` with the default value of `hbarAllowances`, `tokenAllowances`, `nftAllowances`.
    public AccountAllowanceApproveTransaction() {
        this(List.of(), List.of(), List.of());
    }

    /// Returns the `hbarAllowances`.
    public List<HbarAllowance> hbarAllowances() {
        return hbarAllowances;
    }

    /// Returns the `tokenAllowances`.
    public List<TokenAllowance> tokenAllowances() {
        return tokenAllowances;
    }

    /// Returns the `nftAllowances`.
    public List<NftAllowance> nftAllowances() {
        return nftAllowances;
    }

    @Override
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<AccountAllowanceApproveReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(AccountKinds.ACCOUNT_ALLOWANCE_APPROVE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `AccountAllowanceApproveTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AccountAllowanceApproveTransaction`.
    ///
    /// `build()` creates the `AccountAllowanceApproveTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private List<HbarAllowance> hbarAllowances = List.of();
        private List<TokenAllowance> tokenAllowances = List.of();
        private List<NftAllowance> nftAllowances = List.of();

        private Builder() {
            // created by AccountAllowanceApproveTransaction.builder()
        }

        /// Sets the `hbarAllowances`.
        ///
        /// @param hbarAllowances the value
        /// @return this builder
        public Builder hbarAllowances(final List<HbarAllowance> hbarAllowances) {
            this.hbarAllowances = hbarAllowances;
            return this;
        }

        /// Sets the `tokenAllowances`.
        ///
        /// @param tokenAllowances the value
        /// @return this builder
        public Builder tokenAllowances(final List<TokenAllowance> tokenAllowances) {
            this.tokenAllowances = tokenAllowances;
            return this;
        }

        /// Sets the `nftAllowances`.
        ///
        /// @param nftAllowances the value
        /// @return this builder
        public Builder nftAllowances(final List<NftAllowance> nftAllowances) {
            this.nftAllowances = nftAllowances;
            return this;
        }

        /// Creates the `AccountAllowanceApproveTransaction` of the values set.
        ///
        /// @return the new `AccountAllowanceApproveTransaction`
        public AccountAllowanceApproveTransaction build() {
            return new AccountAllowanceApproveTransaction(hbarAllowances, tokenAllowances, nftAllowances);
        }
    }

    @Override
    public String toString() {
        return "AccountAllowanceApproveTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", hbarAllowances=" + hbarAllowances()
                + ", tokenAllowances=" + tokenAllowances()
                + ", nftAllowances=" + nftAllowances()
                + "]";
    }
}
