package org.hiero.mirrornode.account;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// @param balance hbar balance in tinybars
/// @param balanceTimestamp timestamp of the reported balance
/// @param tokenBalances the token balances at balanceTimestamp
/// @param pendingReward tinybars receivable in the next staking payout
/// @param alias HIP-32 public-key alias (serialised protobuf Key bytes)
/// @param stakedAccountId mutually exclusive with stakedNodeId
/// @param stakedNodeId mutually exclusive with stakedAccountId
/// @param delegationAddress the EVM address the account delegates its code to
public record AccountInfo(
        AccountId accountId,
        @Nullable EvmAddress evmAddress,
        long balance,
        @Nullable ZonedDateTime balanceTimestamp,
        List<TokenBalance> tokenBalances,
        long ethereumNonce,
        long pendingReward,
        byte @Nullable [] alias,
        @Nullable Authority authority,
        @Nullable String accountMemo,
        @Nullable ZonedDateTime createdTimestamp,
        @Nullable ZonedDateTime expiryTimestamp,
        @Nullable Duration autoRenewPeriod,
        @Nullable Integer maxAutomaticTokenAssociations,
        @Nullable Boolean receiverSignatureRequired,
        boolean declineReward,
        boolean deleted,
        @Nullable AccountId stakedAccountId,
        @Nullable Long stakedNodeId,
        @Nullable ZonedDateTime stakePeriodStart,
        @Nullable EvmAddress delegationAddress) {

    /// Creates a new `AccountInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AccountInfo {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(tokenBalances, "tokenBalances must not be null");
        tokenBalances = List.copyOf(tokenBalances);
        alias = alias == null ? null : alias.clone();
    }

    /// Creates a new `AccountInfo` with the default value of `tokenBalances`, `declineReward`, `deleted`.
    public AccountInfo(
            final AccountId accountId,
            final @Nullable EvmAddress evmAddress,
            final long balance,
            final @Nullable ZonedDateTime balanceTimestamp,
            final long ethereumNonce,
            final long pendingReward,
            final byte @Nullable [] alias,
            final @Nullable Authority authority,
            final @Nullable String accountMemo,
            final @Nullable ZonedDateTime createdTimestamp,
            final @Nullable ZonedDateTime expiryTimestamp,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable Boolean receiverSignatureRequired,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId,
            final @Nullable ZonedDateTime stakePeriodStart,
            final @Nullable EvmAddress delegationAddress) {
        this(accountId, evmAddress, balance, balanceTimestamp, List.of(), ethereumNonce, pendingReward, alias, authority, accountMemo, createdTimestamp, expiryTimestamp, autoRenewPeriod, maxAutomaticTokenAssociations, receiverSignatureRequired, false, false, stakedAccountId, stakedNodeId, stakePeriodStart, delegationAddress);
    }

    /// HIP-32 public-key alias (serialised protobuf Key bytes)
    ///
    /// Returns a copy of the `alias` array.
    @Override
    public byte @Nullable [] alias() {
        return alias == null ? null : alias.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof AccountInfo other
            && Objects.equals(accountId, other.accountId)
            && Objects.equals(evmAddress, other.evmAddress)
            && balance == other.balance
            && Objects.equals(balanceTimestamp, other.balanceTimestamp)
            && Objects.equals(tokenBalances, other.tokenBalances)
            && ethereumNonce == other.ethereumNonce
            && pendingReward == other.pendingReward
            && Arrays.equals(alias, other.alias)
            && Objects.equals(authority, other.authority)
            && Objects.equals(accountMemo, other.accountMemo)
            && Objects.equals(createdTimestamp, other.createdTimestamp)
            && Objects.equals(expiryTimestamp, other.expiryTimestamp)
            && Objects.equals(autoRenewPeriod, other.autoRenewPeriod)
            && Objects.equals(maxAutomaticTokenAssociations, other.maxAutomaticTokenAssociations)
            && Objects.equals(receiverSignatureRequired, other.receiverSignatureRequired)
            && declineReward == other.declineReward
            && deleted == other.deleted
            && Objects.equals(stakedAccountId, other.stakedAccountId)
            && Objects.equals(stakedNodeId, other.stakedNodeId)
            && Objects.equals(stakePeriodStart, other.stakePeriodStart)
            && Objects.equals(delegationAddress, other.delegationAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, evmAddress, balance, balanceTimestamp, tokenBalances, ethereumNonce, pendingReward, Arrays.hashCode(alias), authority, accountMemo, createdTimestamp, expiryTimestamp, autoRenewPeriod, maxAutomaticTokenAssociations, receiverSignatureRequired, declineReward, deleted, stakedAccountId, stakedNodeId, stakePeriodStart, delegationAddress);
    }

    @Override
    public String toString() {
        return "AccountInfo[accountId=" + accountId + ", evmAddress=" + evmAddress + ", balance=" + balance + ", balanceTimestamp=" + balanceTimestamp + ", tokenBalances=" + tokenBalances + ", ethereumNonce=" + ethereumNonce + ", pendingReward=" + pendingReward + ", alias=" + (alias == null ? "null" : "byte[" + alias.length + "]") + ", authority=" + authority + ", accountMemo=" + accountMemo + ", createdTimestamp=" + createdTimestamp + ", expiryTimestamp=" + expiryTimestamp + ", autoRenewPeriod=" + autoRenewPeriod + ", maxAutomaticTokenAssociations=" + maxAutomaticTokenAssociations + ", receiverSignatureRequired=" + receiverSignatureRequired + ", declineReward=" + declineReward + ", deleted=" + deleted + ", stakedAccountId=" + stakedAccountId + ", stakedNodeId=" + stakedNodeId + ", stakePeriodStart=" + stakePeriodStart + ", delegationAddress=" + delegationAddress + "]";
    }

    /// Returns a builder for a new `AccountInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AccountInfo`.
    ///
    /// `build()` creates the `AccountInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId accountId;
        private @Nullable EvmAddress evmAddress;
        private @Nullable Long balance;
        private @Nullable ZonedDateTime balanceTimestamp;
        private List<TokenBalance> tokenBalances = List.of();
        private @Nullable Long ethereumNonce;
        private @Nullable Long pendingReward;
        private byte @Nullable [] alias;
        private @Nullable Authority authority;
        private @Nullable String accountMemo;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable ZonedDateTime expiryTimestamp;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable Integer maxAutomaticTokenAssociations;
        private @Nullable Boolean receiverSignatureRequired;
        private boolean declineReward = false;
        private boolean deleted = false;
        private @Nullable AccountId stakedAccountId;
        private @Nullable Long stakedNodeId;
        private @Nullable ZonedDateTime stakePeriodStart;
        private @Nullable EvmAddress delegationAddress;

        private Builder() {
            // created by AccountInfo.builder()
        }

        /// Sets the `accountId`.
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `evmAddress`.
        ///
        /// @param evmAddress the value
        /// @return this builder
        public Builder evmAddress(final @Nullable EvmAddress evmAddress) {
            this.evmAddress = evmAddress;
            return this;
        }

        /// Sets the `balance`.
        ///
        /// hbar balance in tinybars
        ///
        /// @param balance the value
        /// @return this builder
        public Builder balance(final long balance) {
            this.balance = balance;
            return this;
        }

        /// Sets the `balanceTimestamp`.
        ///
        /// timestamp of the reported balance
        ///
        /// @param balanceTimestamp the value
        /// @return this builder
        public Builder balanceTimestamp(final @Nullable ZonedDateTime balanceTimestamp) {
            this.balanceTimestamp = balanceTimestamp;
            return this;
        }

        /// Sets the `tokenBalances`.
        ///
        /// the token balances at balanceTimestamp
        ///
        /// @param tokenBalances the value
        /// @return this builder
        public Builder tokenBalances(final List<TokenBalance> tokenBalances) {
            this.tokenBalances = tokenBalances;
            return this;
        }

        /// Sets the `ethereumNonce`.
        ///
        /// @param ethereumNonce the value
        /// @return this builder
        public Builder ethereumNonce(final long ethereumNonce) {
            this.ethereumNonce = ethereumNonce;
            return this;
        }

        /// Sets the `pendingReward`.
        ///
        /// tinybars receivable in the next staking payout
        ///
        /// @param pendingReward the value
        /// @return this builder
        public Builder pendingReward(final long pendingReward) {
            this.pendingReward = pendingReward;
            return this;
        }

        /// Sets the `alias`.
        ///
        /// HIP-32 public-key alias (serialised protobuf Key bytes)
        ///
        /// @param alias the value
        /// @return this builder
        public Builder alias(final byte @Nullable [] alias) {
            this.alias = alias;
            return this;
        }

        /// Sets the `authority`.
        ///
        /// @param authority the value
        /// @return this builder
        public Builder authority(final @Nullable Authority authority) {
            this.authority = authority;
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

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `expiryTimestamp`.
        ///
        /// @param expiryTimestamp the value
        /// @return this builder
        public Builder expiryTimestamp(final @Nullable ZonedDateTime expiryTimestamp) {
            this.expiryTimestamp = expiryTimestamp;
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

        /// Sets the `maxAutomaticTokenAssociations`.
        ///
        /// @param maxAutomaticTokenAssociations the value
        /// @return this builder
        public Builder maxAutomaticTokenAssociations(final @Nullable Integer maxAutomaticTokenAssociations) {
            this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
            return this;
        }

        /// Sets the `receiverSignatureRequired`.
        ///
        /// @param receiverSignatureRequired the value
        /// @return this builder
        public Builder receiverSignatureRequired(final @Nullable Boolean receiverSignatureRequired) {
            this.receiverSignatureRequired = receiverSignatureRequired;
            return this;
        }

        /// Sets the `declineReward`.
        ///
        /// @param declineReward the value
        /// @return this builder
        public Builder declineReward(final boolean declineReward) {
            this.declineReward = declineReward;
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

        /// Sets the `stakePeriodStart`.
        ///
        /// @param stakePeriodStart the value
        /// @return this builder
        public Builder stakePeriodStart(final @Nullable ZonedDateTime stakePeriodStart) {
            this.stakePeriodStart = stakePeriodStart;
            return this;
        }

        /// Sets the `delegationAddress`.
        ///
        /// the EVM address the account delegates its code to
        ///
        /// @param delegationAddress the value
        /// @return this builder
        public Builder delegationAddress(final @Nullable EvmAddress delegationAddress) {
            this.delegationAddress = delegationAddress;
            return this;
        }

        /// Creates the `AccountInfo` of the values set.
        ///
        /// @return the new `AccountInfo`
        /// @throws NullPointerException if a required value is not set
        public AccountInfo build() {
            return new AccountInfo(
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    evmAddress,
                    Objects.requireNonNull(balance, "balance must not be null"),
                    balanceTimestamp,
                    tokenBalances,
                    Objects.requireNonNull(ethereumNonce, "ethereumNonce must not be null"),
                    Objects.requireNonNull(pendingReward, "pendingReward must not be null"),
                    alias,
                    authority,
                    accountMemo,
                    createdTimestamp,
                    expiryTimestamp,
                    autoRenewPeriod,
                    maxAutomaticTokenAssociations,
                    receiverSignatureRequired,
                    declineReward,
                    deleted,
                    stakedAccountId,
                    stakedNodeId,
                    stakePeriodStart,
                    delegationAddress);
        }
    }
}
