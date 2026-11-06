package org.hiero.consensusnode.transactions.contracts;

import org.hiero.consensusnode.client.internal.ContractKinds;
import org.hiero.consensusnode.client.internal.Packing;
import java.nio.charset.StandardCharsets;
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

/// Deploys a contract. The initcode is either given as `bytecode` or read from the file `bytecodeFileId`.
/// A contract is staked to another account or to a node, not to both.
public final class ContractCreateTransaction extends Transaction<ContractCreateReceipt, ContractCreateTransaction> {

    private final byte @Nullable [] bytecode;
    private final @Nullable Address bytecodeFileId;
    private final long gas;
    private final byte[] constructorParameters;
    private final @Nullable NativeToken<?, ?> initialBalance;
    private final @Nullable Authority adminAuthority;
    private final @Nullable String contractMemo;
    private final @Nullable Duration autoRenewPeriod;
    private final @Nullable AccountId autoRenewAccount;
    private final @Nullable Integer maxAutomaticTokenAssociations;
    private final @Nullable AccountId stakedAccountId;
    private final @Nullable Long stakedNodeId;
    private final boolean declineStakingReward;

    /// Creates a new `ContractCreateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractCreateTransaction(
            final byte @Nullable [] bytecode,
            final @Nullable Address bytecodeFileId,
            final long gas,
            final byte[] constructorParameters,
            final @Nullable NativeToken<?, ?> initialBalance,
            final @Nullable Authority adminAuthority,
            final @Nullable String contractMemo,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId,
            final boolean declineStakingReward) {
        super();
        this.bytecode = bytecode == null ? null : bytecode.clone();
        this.bytecodeFileId = bytecodeFileId;
        if (gas < 0L) {
            throw new IllegalArgumentException("gas must be at least 0");
        }
        this.gas = gas;
        Objects.requireNonNull(constructorParameters, "constructorParameters must not be null");
        this.constructorParameters = constructorParameters.clone();
        this.initialBalance = initialBalance;
        this.adminAuthority = adminAuthority;
        if (contractMemo != null && contractMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("contractMemo must be at most 100 bytes long in UTF-8");
        }
        this.contractMemo = contractMemo;
        this.autoRenewPeriod = autoRenewPeriod;
        this.autoRenewAccount = autoRenewAccount;
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        this.stakedAccountId = stakedAccountId;
        this.stakedNodeId = stakedNodeId;
        this.declineStakingReward = declineStakingReward;
        if ((bytecode != null ? 1 : 0) + (bytecodeFileId != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of bytecode, bytecodeFileId must be set");
        }
        if ((stakedAccountId != null ? 1 : 0) + (stakedNodeId != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of stakedAccountId, stakedNodeId may be set");
        }
    }

    /// Creates a new `ContractCreateTransaction` with the default value of `constructorParameters`, `declineStakingReward`.
    public ContractCreateTransaction(
            final byte @Nullable [] bytecode,
            final @Nullable Address bytecodeFileId,
            final long gas,
            final @Nullable NativeToken<?, ?> initialBalance,
            final @Nullable Authority adminAuthority,
            final @Nullable String contractMemo,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId) {
        this(bytecode, bytecodeFileId, gas, new byte[0], initialBalance, adminAuthority, contractMemo, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations, stakedAccountId, stakedNodeId, false);
    }

    /// the initcode of the contract
    public byte @Nullable [] bytecode() {
        return bytecode == null ? null : bytecode.clone();
    }

    /// the file that holds the initcode, as hex text
    public @Nullable Address bytecodeFileId() {
        return bytecodeFileId;
    }

    /// the maximum gas the constructor may use
    public long gas() {
        return gas;
    }

    /// The ABI-encoded arguments of the constructor, without a selector (see `AbiTuple.encode()`).
    public byte[] constructorParameters() {
        return constructorParameters.clone();
    }

    /// Amount transferred from the payer to the new contract. The constructor must be payable if it is set.
    public @Nullable NativeToken<?, ?> initialBalance() {
        return initialBalance;
    }

    /// Allows to update and delete the contract; without it the contract cannot be changed. MUST sign if set.
    public @Nullable Authority adminAuthority() {
        return adminAuthority;
    }

    /// Returns the `contractMemo`.
    public @Nullable String contractMemo() {
        return contractMemo;
    }

    /// the renewal period; the network default if unset
    public @Nullable Duration autoRenewPeriod() {
        return autoRenewPeriod;
    }

    /// the account that pays for the renewal; MUST sign if set
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
    public boolean declineStakingReward() {
        return declineStakingReward;
    }

    @Override
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ContractCreateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ContractKinds.CONTRACT_CREATE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `ContractCreateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractCreateTransaction`.
    ///
    /// `build()` creates the `ContractCreateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private byte @Nullable [] bytecode;
        private @Nullable Address bytecodeFileId;
        private @Nullable Long gas;
        private byte[] constructorParameters = new byte[0];
        private @Nullable NativeToken<?, ?> initialBalance;
        private @Nullable Authority adminAuthority;
        private @Nullable String contractMemo;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Integer maxAutomaticTokenAssociations;
        private @Nullable AccountId stakedAccountId;
        private @Nullable Long stakedNodeId;
        private boolean declineStakingReward = false;

        private Builder() {
            // created by ContractCreateTransaction.builder()
        }

        /// Sets the `bytecode`.
        ///
        /// the initcode of the contract
        ///
        /// @param bytecode the value
        /// @return this builder
        public Builder bytecode(final byte @Nullable [] bytecode) {
            this.bytecode = bytecode;
            return this;
        }

        /// Sets the `bytecodeFileId`.
        ///
        /// the file that holds the initcode, as hex text
        ///
        /// @param bytecodeFileId the value
        /// @return this builder
        public Builder bytecodeFileId(final @Nullable Address bytecodeFileId) {
            this.bytecodeFileId = bytecodeFileId;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// the maximum gas the constructor may use
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `constructorParameters`.
        ///
        /// The ABI-encoded arguments of the constructor, without a selector (see `AbiTuple.encode()`).
        ///
        /// @param constructorParameters the value
        /// @return this builder
        public Builder constructorParameters(final byte[] constructorParameters) {
            this.constructorParameters = constructorParameters;
            return this;
        }

        /// Sets the `initialBalance`.
        ///
        /// Amount transferred from the payer to the new contract. The constructor must be payable if it is set.
        ///
        /// @param initialBalance the value
        /// @return this builder
        public Builder initialBalance(final @Nullable NativeToken<?, ?> initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// Allows to update and delete the contract; without it the contract cannot be changed. MUST sign if set.
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

        /// Sets the `autoRenewPeriod`.
        ///
        /// the renewal period; the network default if unset
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// the account that pays for the renewal; MUST sign if set
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
        public Builder declineStakingReward(final boolean declineStakingReward) {
            this.declineStakingReward = declineStakingReward;
            return this;
        }

        /// Creates the `ContractCreateTransaction` of the values set.
        ///
        /// @return the new `ContractCreateTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractCreateTransaction build() {
            return new ContractCreateTransaction(
                    bytecode,
                    bytecodeFileId,
                    Objects.requireNonNull(gas, "gas must not be null"),
                    constructorParameters,
                    initialBalance,
                    adminAuthority,
                    contractMemo,
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
        return "ContractCreateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", bytecode=" + (bytecode() == null ? "null" : "byte[" + bytecode().length + "]")
                + ", bytecodeFileId=" + bytecodeFileId()
                + ", gas=" + gas()
                + ", constructorParameters=" + "byte[" + constructorParameters().length + "]"
                + ", initialBalance=" + initialBalance()
                + ", adminAuthority=" + adminAuthority()
                + ", contractMemo=" + contractMemo()
                + ", autoRenewPeriod=" + autoRenewPeriod()
                + ", autoRenewAccount=" + autoRenewAccount()
                + ", maxAutomaticTokenAssociations=" + maxAutomaticTokenAssociations()
                + ", stakedAccountId=" + stakedAccountId()
                + ", stakedNodeId=" + stakedNodeId()
                + ", declineStakingReward=" + declineStakingReward()
                + "]";
    }
}
