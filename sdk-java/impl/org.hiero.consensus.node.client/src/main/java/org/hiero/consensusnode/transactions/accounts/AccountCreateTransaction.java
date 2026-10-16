package org.hiero.consensusnode.transactions.accounts;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.client.internal.Packing;
import org.hiero.consensusnode.client.internal.TransactionKinds;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// An account is staked to another account or to a node, not to both.
public final class AccountCreateTransaction extends Transaction<AccountCreateReceipt, AccountCreateTransaction> {

    private final Authority authority;
    private final @Nullable NativeToken<?, ?> initialBalance;
    private final @Nullable String accountMemo;
    private final boolean receiverSignatureRequired;
    private final @Nullable Integer maxAutomaticTokenAssociations;
    private final @Nullable Duration autoRenewPeriod;
    private final @Nullable AccountId stakedAccountId;
    private final @Nullable Long stakedNodeId;
    private final boolean declineStakingReward;
    private final byte @Nullable [] alias;

    /// Creates a new `AccountCreateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AccountCreateTransaction(
            final Authority authority,
            final @Nullable NativeToken<?, ?> initialBalance,
            final @Nullable String accountMemo,
            final boolean receiverSignatureRequired,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId,
            final boolean declineStakingReward,
            final byte @Nullable [] alias) {
        super();
        Objects.requireNonNull(authority, "authority must not be null");
        this.authority = authority;
        this.initialBalance = initialBalance;
        this.accountMemo = accountMemo;
        this.receiverSignatureRequired = receiverSignatureRequired;
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        this.autoRenewPeriod = autoRenewPeriod;
        this.stakedAccountId = stakedAccountId;
        this.stakedNodeId = stakedNodeId;
        this.declineStakingReward = declineStakingReward;
        this.alias = alias == null ? null : alias.clone();
        if ((stakedAccountId != null ? 1 : 0) + (stakedNodeId != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of stakedAccountId, stakedNodeId may be set");
        }
    }

    /// Creates a new `AccountCreateTransaction` with the default value of `receiverSignatureRequired`, `declineStakingReward`.
    public AccountCreateTransaction(
            final Authority authority,
            final @Nullable NativeToken<?, ?> initialBalance,
            final @Nullable String accountMemo,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId,
            final byte @Nullable [] alias) {
        this(authority, initialBalance, accountMemo, false, maxAutomaticTokenAssociations, autoRenewPeriod, stakedAccountId, stakedNodeId, false, alias);
    }

    /// Returns the `authority`.
    public Authority authority() {
        return authority;
    }

    /// Amount transferred from the payer to the new account. If absent, the account is created with a balance
    /// of 0 in the native token of the network.
    public @Nullable NativeToken<?, ?> initialBalance() {
        return initialBalance;
    }

    /// Returns the `accountMemo`.
    public @Nullable String accountMemo() {
        return accountMemo;
    }

    /// Returns the `receiverSignatureRequired`.
    public boolean receiverSignatureRequired() {
        return receiverSignatureRequired;
    }

    /// Returns the `maxAutomaticTokenAssociations`.
    public @Nullable Integer maxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
    }

    /// Returns the `autoRenewPeriod`.
    public @Nullable Duration autoRenewPeriod() {
        return autoRenewPeriod;
    }

    /// Returns the `stakedAccountId`.
    public @Nullable AccountId stakedAccountId() {
        return stakedAccountId;
    }

    /// Returns the `stakedNodeId`.
    public @Nullable Long stakedNodeId() {
        return stakedNodeId;
    }

    /// Returns the `declineStakingReward`.
    public boolean declineStakingReward() {
        return declineStakingReward;
    }

    /// Returns the `alias`.
    public byte @Nullable [] alias() {
        return alias == null ? null : alias.clone();
    }

    @Override
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<AccountCreateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(TransactionKinds.ACCOUNT_CREATE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `AccountCreateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AccountCreateTransaction`.
    ///
    /// `build()` creates the `AccountCreateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Authority authority;
        private @Nullable NativeToken<?, ?> initialBalance;
        private @Nullable String accountMemo;
        private boolean receiverSignatureRequired = false;
        private @Nullable Integer maxAutomaticTokenAssociations;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId stakedAccountId;
        private @Nullable Long stakedNodeId;
        private boolean declineStakingReward = false;
        private byte @Nullable [] alias;

        private Builder() {
            // created by AccountCreateTransaction.builder()
        }

        /// Sets the `authority`.
        ///
        /// @param authority the value
        /// @return this builder
        public Builder authority(final Authority authority) {
            this.authority = authority;
            return this;
        }

        /// Sets the `initialBalance`.
        ///
        /// Amount transferred from the payer to the new account. If absent, the account is created with a balance
        /// of 0 in the native token of the network.
        ///
        /// @param initialBalance the value
        /// @return this builder
        public Builder initialBalance(final @Nullable NativeToken<?, ?> initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        /// Sets the `accountMemo`.
        ///
        /// @param accountMemo the value
        /// @return this builder
        public Builder accountMemo(final @Nullable String accountMemo) {
            this.accountMemo = accountMemo;
            return this;
        }

        /// Sets the `receiverSignatureRequired`.
        ///
        /// @param receiverSignatureRequired the value
        /// @return this builder
        public Builder receiverSignatureRequired(final boolean receiverSignatureRequired) {
            this.receiverSignatureRequired = receiverSignatureRequired;
            return this;
        }

        /// Sets the `maxAutomaticTokenAssociations`.
        ///
        /// @param maxAutomaticTokenAssociations the value
        /// @return this builder
        public Builder maxAutomaticTokenAssociations(final @Nullable Integer maxAutomaticTokenAssociations) {
            this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `stakedAccountId`.
        ///
        /// @param stakedAccountId the value
        /// @return this builder
        public Builder stakedAccountId(final @Nullable AccountId stakedAccountId) {
            this.stakedAccountId = stakedAccountId;
            return this;
        }

        /// Sets the `stakedNodeId`.
        ///
        /// @param stakedNodeId the value
        /// @return this builder
        public Builder stakedNodeId(final @Nullable Long stakedNodeId) {
            this.stakedNodeId = stakedNodeId;
            return this;
        }

        /// Sets the `declineStakingReward`.
        ///
        /// @param declineStakingReward the value
        /// @return this builder
        public Builder declineStakingReward(final boolean declineStakingReward) {
            this.declineStakingReward = declineStakingReward;
            return this;
        }

        /// Sets the `alias`.
        ///
        /// @param alias the value
        /// @return this builder
        public Builder alias(final byte @Nullable [] alias) {
            this.alias = alias;
            return this;
        }

        /// Creates the `AccountCreateTransaction` of the values set.
        ///
        /// @return the new `AccountCreateTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public AccountCreateTransaction build() {
            return new AccountCreateTransaction(
                    Objects.requireNonNull(authority, "authority must not be null"),
                    initialBalance,
                    accountMemo,
                    receiverSignatureRequired,
                    maxAutomaticTokenAssociations,
                    autoRenewPeriod,
                    stakedAccountId,
                    stakedNodeId,
                    declineStakingReward,
                    alias);
        }
    }

    @Override
    public String toString() {
        return "AccountCreateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", authority=" + authority()
                + ", initialBalance=" + initialBalance()
                + ", accountMemo=" + accountMemo()
                + ", receiverSignatureRequired=" + receiverSignatureRequired()
                + ", maxAutomaticTokenAssociations=" + maxAutomaticTokenAssociations()
                + ", autoRenewPeriod=" + autoRenewPeriod()
                + ", stakedAccountId=" + stakedAccountId()
                + ", stakedNodeId=" + stakedNodeId()
                + ", declineStakingReward=" + declineStakingReward()
                + ", alias=" + (alias() == null ? "null" : "byte[" + alias().length + "]")
                + "]";
    }
}
