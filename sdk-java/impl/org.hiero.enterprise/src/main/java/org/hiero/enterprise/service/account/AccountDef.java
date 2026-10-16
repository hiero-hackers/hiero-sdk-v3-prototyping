package org.hiero.enterprise.service.account;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The definition of a new account: everything that can be set when an account is created. Only the authority is
/// required. An account is staked to another account or to a node, not to both.
///
/// @param initialBalance Amount transferred from the payer to the new account. If absent, the account is created with a balance
///   of 0 in the native token of the network.
public record AccountDef(
        Authority authority,
        @Nullable NativeToken<?, ?> initialBalance,
        @Nullable String accountMemo,
        boolean receiverSignatureRequired,
        @Nullable Integer maxAutomaticTokenAssociations,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId stakedAccountId,
        @Nullable Long stakedNodeId,
        boolean declineStakingReward,
        byte @Nullable [] alias) {

    /// Creates a new `AccountDef`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AccountDef {
        Objects.requireNonNull(authority, "authority must not be null");
        alias = alias == null ? null : alias.clone();
        if ((stakedAccountId != null ? 1 : 0) + (stakedNodeId != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of stakedAccountId, stakedNodeId may be set");
        }
    }

    /// Creates a new `AccountDef` with the default value of `receiverSignatureRequired`, `declineStakingReward`.
    public AccountDef(
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

    /// Returns a copy of the `alias` array.
    @Override
    public byte @Nullable [] alias() {
        return alias == null ? null : alias.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof AccountDef other
            && Objects.equals(authority, other.authority)
            && Objects.equals(initialBalance, other.initialBalance)
            && Objects.equals(accountMemo, other.accountMemo)
            && receiverSignatureRequired == other.receiverSignatureRequired
            && Objects.equals(maxAutomaticTokenAssociations, other.maxAutomaticTokenAssociations)
            && Objects.equals(autoRenewPeriod, other.autoRenewPeriod)
            && Objects.equals(stakedAccountId, other.stakedAccountId)
            && Objects.equals(stakedNodeId, other.stakedNodeId)
            && declineStakingReward == other.declineStakingReward
            && Arrays.equals(alias, other.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority, initialBalance, accountMemo, receiverSignatureRequired, maxAutomaticTokenAssociations, autoRenewPeriod, stakedAccountId, stakedNodeId, declineStakingReward, Arrays.hashCode(alias));
    }

    @Override
    public String toString() {
        return "AccountDef[authority=" + authority + ", initialBalance=" + initialBalance + ", accountMemo=" + accountMemo + ", receiverSignatureRequired=" + receiverSignatureRequired + ", maxAutomaticTokenAssociations=" + maxAutomaticTokenAssociations + ", autoRenewPeriod=" + autoRenewPeriod + ", stakedAccountId=" + stakedAccountId + ", stakedNodeId=" + stakedNodeId + ", declineStakingReward=" + declineStakingReward + ", alias=" + (alias == null ? "null" : "byte[" + alias.length + "]") + "]";
    }

    /// Returns a builder for a new `AccountDef`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AccountDef`.
    ///
    /// `build()` creates the `AccountDef` of the values set one by one.
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
            // created by AccountDef.builder()
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

        /// Creates the `AccountDef` of the values set.
        ///
        /// @return the new `AccountDef`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public AccountDef build() {
            return new AccountDef(
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
}
