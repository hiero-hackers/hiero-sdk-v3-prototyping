package org.hiero.consensusnode.transactions.contracts;

import org.hiero.consensusnode.client.internal.ContractKinds;
import org.hiero.consensusnode.client.internal.Packing;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
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

/// Updates one or more properties of a contract. Every nullable field is "leave unchanged" when null.
/// A contract is staked to another account or to a node, not to both.
public final class ContractUpdateTransaction extends Transaction<ContractUpdateReceipt, ContractUpdateTransaction> {

    private final ContractId contractId;
    private final @Nullable Authority adminAuthority;
    private final @Nullable String contractMemo;
    private final @Nullable ZonedDateTime expirationTime;
    private final @Nullable Duration autoRenewPeriod;
    private final @Nullable AccountId autoRenewAccount;
    private final @Nullable Integer maxAutomaticTokenAssociations;
    private final @Nullable AccountId stakedAccountId;
    private final @Nullable Long stakedNodeId;
    private final @Nullable Boolean declineStakingReward;

    /// Creates a new `ContractUpdateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractUpdateTransaction(
            final ContractId contractId,
            final @Nullable Authority adminAuthority,
            final @Nullable String contractMemo,
            final @Nullable ZonedDateTime expirationTime,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId,
            final @Nullable Boolean declineStakingReward) {
        super();
        Objects.requireNonNull(contractId, "contractId must not be null");
        this.contractId = contractId;
        this.adminAuthority = adminAuthority;
        if (contractMemo != null && contractMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("contractMemo must be at most 100 bytes long in UTF-8");
        }
        this.contractMemo = contractMemo;
        this.expirationTime = expirationTime;
        this.autoRenewPeriod = autoRenewPeriod;
        this.autoRenewAccount = autoRenewAccount;
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        this.stakedAccountId = stakedAccountId;
        this.stakedNodeId = stakedNodeId;
        this.declineStakingReward = declineStakingReward;
        if ((stakedAccountId != null ? 1 : 0) + (stakedNodeId != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of stakedAccountId, stakedNodeId may be set");
        }
    }

    /// the contract that is updated
    public ContractId contractId() {
        return contractId;
    }

    /// the new admin authority; the new one MUST sign as well
    public @Nullable Authority adminAuthority() {
        return adminAuthority;
    }

    /// Returns the `contractMemo`.
    public @Nullable String contractMemo() {
        return contractMemo;
    }

    /// a later expiration time; cannot be earlier than the current one
    public @Nullable ZonedDateTime expirationTime() {
        return expirationTime;
    }

    /// Returns the `autoRenewPeriod`.
    public @Nullable Duration autoRenewPeriod() {
        return autoRenewPeriod;
    }

    /// the new account MUST sign; ZERO_ACCOUNT_ID removes it
    public @Nullable AccountId autoRenewAccount() {
        return autoRenewAccount;
    }

    /// -1: unlimited
    public @Nullable Integer maxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
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
    public @Nullable Boolean declineStakingReward() {
        return declineStakingReward;
    }

    @Override
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ContractUpdateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_UPDATE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `ContractUpdateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractUpdateTransaction`.
    ///
    /// `build()` creates the `ContractUpdateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ContractId contractId;
        private @Nullable Authority adminAuthority;
        private @Nullable String contractMemo;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Integer maxAutomaticTokenAssociations;
        private @Nullable AccountId stakedAccountId;
        private @Nullable Long stakedNodeId;
        private @Nullable Boolean declineStakingReward;

        private Builder() {
            // created by ContractUpdateTransaction.builder()
        }

        /// Sets the `contractId`.
        ///
        /// the contract that is updated
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// the new admin authority; the new one MUST sign as well
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `contractMemo`.
        ///
        /// @param contractMemo the value
        /// @return this builder
        public Builder contractMemo(final @Nullable String contractMemo) {
            this.contractMemo = contractMemo;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// a later expiration time; cannot be earlier than the current one
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
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

        /// Sets the `autoRenewAccount`.
        ///
        /// the new account MUST sign; ZERO_ACCOUNT_ID removes it
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Sets the `maxAutomaticTokenAssociations`.
        ///
        /// -1: unlimited
        ///
        /// @param maxAutomaticTokenAssociations the value
        /// @return this builder
        public Builder maxAutomaticTokenAssociations(final @Nullable Integer maxAutomaticTokenAssociations) {
            this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
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
        public Builder declineStakingReward(final @Nullable Boolean declineStakingReward) {
            this.declineStakingReward = declineStakingReward;
            return this;
        }

        /// Creates the `ContractUpdateTransaction` of the values set.
        ///
        /// @return the new `ContractUpdateTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractUpdateTransaction build() {
            return new ContractUpdateTransaction(
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    adminAuthority,
                    contractMemo,
                    expirationTime,
                    autoRenewPeriod,
                    autoRenewAccount,
                    maxAutomaticTokenAssociations,
                    stakedAccountId,
                    stakedNodeId,
                    declineStakingReward);
        }
    }

    @Override
    public String toString() {
        return "ContractUpdateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", contractId=" + contractId()
                + ", adminAuthority=" + adminAuthority()
                + ", contractMemo=" + contractMemo()
                + ", expirationTime=" + expirationTime()
                + ", autoRenewPeriod=" + autoRenewPeriod()
                + ", autoRenewAccount=" + autoRenewAccount()
                + ", maxAutomaticTokenAssociations=" + maxAutomaticTokenAssociations()
                + ", stakedAccountId=" + stakedAccountId()
                + ", stakedNodeId=" + stakedNodeId()
                + ", declineStakingReward=" + declineStakingReward()
                + "]";
    }
}
