package org.hiero.consensusnode.queries.contracts;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.evm.EvmAddress;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The properties of a contract. Returned by `ContractInfoQuery`.
///
/// @param accountId the account of the contract, which holds its balance
/// @param evmAddress the 20-byte EVM address of the contract
/// @param adminAuthority Allows to update and delete the contract; absent if the contract is immutable.
/// @param autoRenewAccount pays the renewal; if absent, the contract pays
/// @param storage the bytes of storage the contract uses
/// @param contractMemo empty if the contract has no memo
/// @param balance the native-token balance of the contract
/// @param maxAutomaticTokenAssociations -1: unlimited
/// @param stakedAccountId mutually exclusive with stakedNodeId
/// @param stakedNodeId mutually exclusive with stakedAccountId
public record ContractInfo(
        ContractId contractId,
        AccountId accountId,
        EvmAddress evmAddress,
        @Nullable Authority adminAuthority,
        ZonedDateTime expirationTime,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccount,
        long storage,
        String contractMemo,
        NativeToken<?, ?> balance,
        boolean deleted,
        int maxAutomaticTokenAssociations,
        @Nullable AccountId stakedAccountId,
        @Nullable Long stakedNodeId,
        boolean declineStakingReward) {

    /// Creates a new `ContractInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractInfo {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(evmAddress, "evmAddress must not be null");
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
        Objects.requireNonNull(contractMemo, "contractMemo must not be null");
        Objects.requireNonNull(balance, "balance must not be null");
        if ((stakedAccountId != null ? 1 : 0) + (stakedNodeId != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of stakedAccountId, stakedNodeId may be set");
        }
    }

    /// Creates a new `ContractInfo` with the default value of `contractMemo`, `deleted`, `maxAutomaticTokenAssociations`, `declineStakingReward`.
    public ContractInfo(
            final ContractId contractId,
            final AccountId accountId,
            final EvmAddress evmAddress,
            final @Nullable Authority adminAuthority,
            final ZonedDateTime expirationTime,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final long storage,
            final NativeToken<?, ?> balance,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId) {
        this(contractId, accountId, evmAddress, adminAuthority, expirationTime, autoRenewPeriod, autoRenewAccount, storage, "", balance, false, 0, stakedAccountId, stakedNodeId, false);
    }

    /// Returns a builder for a new `ContractInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractInfo`.
    ///
    /// `build()` creates the `ContractInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ContractId contractId;
        private @Nullable AccountId accountId;
        private @Nullable EvmAddress evmAddress;
        private @Nullable Authority adminAuthority;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Long storage;
        private String contractMemo = "";
        private @Nullable NativeToken<?, ?> balance;
        private boolean deleted = false;
        private int maxAutomaticTokenAssociations = 0;
        private @Nullable AccountId stakedAccountId;
        private @Nullable Long stakedNodeId;
        private boolean declineStakingReward = false;

        private Builder() {
            // created by ContractInfo.builder()
        }

        /// Sets the `contractId`.
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// the account of the contract, which holds its balance
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `evmAddress`.
        ///
        /// the 20-byte EVM address of the contract
        ///
        /// @param evmAddress the value
        /// @return this builder
        public Builder evmAddress(final EvmAddress evmAddress) {
            this.evmAddress = evmAddress;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// Allows to update and delete the contract; absent if the contract is immutable.
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final ZonedDateTime expirationTime) {
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
        /// pays the renewal; if absent, the contract pays
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Sets the `storage`.
        ///
        /// the bytes of storage the contract uses
        ///
        /// @param storage the value
        /// @return this builder
        public Builder storage(final long storage) {
            this.storage = storage;
            return this;
        }

        /// Sets the `contractMemo`.
        ///
        /// empty if the contract has no memo
        ///
        /// @param contractMemo the value
        /// @return this builder
        public Builder contractMemo(final String contractMemo) {
            this.contractMemo = contractMemo;
            return this;
        }

        /// Sets the `balance`.
        ///
        /// the native-token balance of the contract
        ///
        /// @param balance the value
        /// @return this builder
        public Builder balance(final NativeToken<?, ?> balance) {
            this.balance = balance;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /// Sets the `maxAutomaticTokenAssociations`.
        ///
        /// -1: unlimited
        ///
        /// @param maxAutomaticTokenAssociations the value
        /// @return this builder
        public Builder maxAutomaticTokenAssociations(final int maxAutomaticTokenAssociations) {
            this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
            return this;
        }

        /// Sets the `stakedAccountId`.
        ///
        /// mutually exclusive with stakedNodeId
        ///
        /// @param stakedAccountId the value
        /// @return this builder
        public Builder stakedAccountId(final @Nullable AccountId stakedAccountId) {
            this.stakedAccountId = stakedAccountId;
            return this;
        }

        /// Sets the `stakedNodeId`.
        ///
        /// mutually exclusive with stakedAccountId
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

        /// Creates the `ContractInfo` of the values set.
        ///
        /// @return the new `ContractInfo`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractInfo build() {
            return new ContractInfo(
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    Objects.requireNonNull(evmAddress, "evmAddress must not be null"),
                    adminAuthority,
                    Objects.requireNonNull(expirationTime, "expirationTime must not be null"),
                    autoRenewPeriod,
                    autoRenewAccount,
                    Objects.requireNonNull(storage, "storage must not be null"),
                    contractMemo,
                    Objects.requireNonNull(balance, "balance must not be null"),
                    deleted,
                    maxAutomaticTokenAssociations,
                    stakedAccountId,
                    stakedNodeId,
                    declineStakingReward);
        }
    }
}
