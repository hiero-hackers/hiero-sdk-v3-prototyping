package org.hiero.enterprise.service.contract;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The parameters of a new smart contract: everything that can be set when it is created. The bytecode comes either
/// from a file or directly; a contract is staked to an account or to a node, not to both.
///
/// @param fileId The file that holds the bytecode.
/// @param bytecode The bytecode itself.
/// @param constructorParams The parameters of the constructor, in order.
/// @param gas The gas for running the constructor; a default of the service if absent.
/// @param initialBalance Amount transferred from the payer to the new contract.
/// @param adminAuthority Allows to update and delete the contract; without it the contract cannot be changed.
/// @param autoRenewPeriod The renewal period applied when the expiration approaches; the network default if absent.
/// @param autoRenewAccount The account that pays for the renewal.
public record ContractCreateParams(
        @Nullable Address fileId,
        byte @Nullable [] bytecode,
        List<Param<?, ?>> constructorParams,
        @Nullable Long gas,
        @Nullable NativeToken<?, ?> initialBalance,
        @Nullable Authority adminAuthority,
        @Nullable String contractMemo,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccount,
        @Nullable Integer maxAutomaticTokenAssociations,
        @Nullable AccountId stakedAccountId,
        @Nullable Long stakedNodeId,
        boolean declineStakingReward) {

    /// Creates a new `ContractCreateParams`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractCreateParams {
        bytecode = bytecode == null ? null : bytecode.clone();
        Objects.requireNonNull(constructorParams, "constructorParams must not be null");
        constructorParams = List.copyOf(constructorParams);
        if (gas != null && gas < 1L) {
            throw new IllegalArgumentException("gas must be at least 1");
        }
        if (contractMemo != null && contractMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("contractMemo must be at most 100 bytes long in UTF-8");
        }
        if ((fileId != null ? 1 : 0) + (bytecode != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of fileId, bytecode must be set");
        }
        if ((stakedAccountId != null ? 1 : 0) + (stakedNodeId != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of stakedAccountId, stakedNodeId may be set");
        }
    }

    /// Creates a new `ContractCreateParams` with the default value of `constructorParams`, `declineStakingReward`.
    public ContractCreateParams(
            final @Nullable Address fileId,
            final byte @Nullable [] bytecode,
            final @Nullable Long gas,
            final @Nullable NativeToken<?, ?> initialBalance,
            final @Nullable Authority adminAuthority,
            final @Nullable String contractMemo,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId) {
        this(fileId, bytecode, List.of(), gas, initialBalance, adminAuthority, contractMemo, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations, stakedAccountId, stakedNodeId, false);
    }

    /// The bytecode itself.
    ///
    /// Returns a copy of the `bytecode` array.
    @Override
    public byte @Nullable [] bytecode() {
        return bytecode == null ? null : bytecode.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ContractCreateParams other
            && Objects.equals(fileId, other.fileId)
            && Arrays.equals(bytecode, other.bytecode)
            && Objects.equals(constructorParams, other.constructorParams)
            && Objects.equals(gas, other.gas)
            && Objects.equals(initialBalance, other.initialBalance)
            && Objects.equals(adminAuthority, other.adminAuthority)
            && Objects.equals(contractMemo, other.contractMemo)
            && Objects.equals(autoRenewPeriod, other.autoRenewPeriod)
            && Objects.equals(autoRenewAccount, other.autoRenewAccount)
            && Objects.equals(maxAutomaticTokenAssociations, other.maxAutomaticTokenAssociations)
            && Objects.equals(stakedAccountId, other.stakedAccountId)
            && Objects.equals(stakedNodeId, other.stakedNodeId)
            && declineStakingReward == other.declineStakingReward;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, Arrays.hashCode(bytecode), constructorParams, gas, initialBalance, adminAuthority, contractMemo, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations, stakedAccountId, stakedNodeId, declineStakingReward);
    }

    @Override
    public String toString() {
        return "ContractCreateParams[fileId=" + fileId + ", bytecode=" + (bytecode == null ? "null" : "byte[" + bytecode.length + "]") + ", constructorParams=" + constructorParams + ", gas=" + gas + ", initialBalance=" + initialBalance + ", adminAuthority=" + adminAuthority + ", contractMemo=" + contractMemo + ", autoRenewPeriod=" + autoRenewPeriod + ", autoRenewAccount=" + autoRenewAccount + ", maxAutomaticTokenAssociations=" + maxAutomaticTokenAssociations + ", stakedAccountId=" + stakedAccountId + ", stakedNodeId=" + stakedNodeId + ", declineStakingReward=" + declineStakingReward + "]";
    }

    /// Returns a builder for a new `ContractCreateParams`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractCreateParams`.
    ///
    /// `build()` creates the `ContractCreateParams` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address fileId;
        private byte @Nullable [] bytecode;
        private List<Param<?, ?>> constructorParams = List.of();
        private @Nullable Long gas;
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
            // created by ContractCreateParams.builder()
        }

        /// Sets the `fileId`.
        ///
        /// The file that holds the bytecode.
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final @Nullable Address fileId) {
            this.fileId = fileId;
            return this;
        }

        /// Sets the `bytecode`.
        ///
        /// The bytecode itself.
        ///
        /// @param bytecode the value
        /// @return this builder
        public Builder bytecode(final byte @Nullable [] bytecode) {
            this.bytecode = bytecode;
            return this;
        }

        /// Sets the `constructorParams`.
        ///
        /// The parameters of the constructor, in order.
        ///
        /// @param constructorParams the value
        /// @return this builder
        public Builder constructorParams(final List<Param<?, ?>> constructorParams) {
            this.constructorParams = constructorParams;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// The gas for running the constructor; a default of the service if absent.
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final @Nullable Long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `initialBalance`.
        ///
        /// Amount transferred from the payer to the new contract.
        ///
        /// @param initialBalance the value
        /// @return this builder
        public Builder initialBalance(final @Nullable NativeToken<?, ?> initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// Allows to update and delete the contract; without it the contract cannot be changed.
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
        /// The renewal period applied when the expiration approaches; the network default if absent.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// The account that pays for the renewal.
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
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

        /// Creates the `ContractCreateParams` of the values set.
        ///
        /// @return the new `ContractCreateParams`
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractCreateParams build() {
            return new ContractCreateParams(
                    fileId,
                    bytecode,
                    constructorParams,
                    gas,
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
}
