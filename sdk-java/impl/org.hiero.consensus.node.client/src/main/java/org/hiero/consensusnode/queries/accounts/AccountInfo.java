package org.hiero.consensusnode.queries.accounts;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.EvmAddress;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Full account state snapshot. Returned by `AccountInfoQuery`.
///
/// @param evmAddress 20-byte EVM-address alias if assigned
/// @param stakedAccountId mutually exclusive with stakedNodeId
/// @param stakedNodeId mutually exclusive with stakedAccountId
public record AccountInfo(
        AccountId accountId,
        @Nullable EvmAddress evmAddress,
        NativeToken<?, ?> balance,
        @Nullable Authority authority,
        @Nullable String accountMemo,
        ZonedDateTime expirationTime,
        @Nullable Duration autoRenewPeriod,
        int maxAutomaticTokenAssociations,
        boolean receiverSignatureRequired,
        long ownedNfts,
        boolean deleted,
        @Nullable AccountId stakedAccountId,
        @Nullable Long stakedNodeId,
        boolean declineStakingReward) {

    /// Creates a new `AccountInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AccountInfo {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(balance, "balance must not be null");
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
        if ((stakedAccountId != null ? 1 : 0) + (stakedNodeId != null ? 1 : 0) > 1) {
            throw new IllegalArgumentException("at most one of stakedAccountId, stakedNodeId may be set");
        }
    }

    /// Creates a new `AccountInfo` with the default value of `maxAutomaticTokenAssociations`, `receiverSignatureRequired`, `ownedNfts`, `deleted`, `declineStakingReward`.
    public AccountInfo(
            final AccountId accountId,
            final @Nullable EvmAddress evmAddress,
            final NativeToken<?, ?> balance,
            final @Nullable Authority authority,
            final @Nullable String accountMemo,
            final ZonedDateTime expirationTime,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId stakedAccountId,
            final @Nullable Long stakedNodeId) {
        this(accountId, evmAddress, balance, authority, accountMemo, expirationTime, autoRenewPeriod, 0, false, 0L, false, stakedAccountId, stakedNodeId, false);
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
        private @Nullable NativeToken<?, ?> balance;
        private @Nullable Authority authority;
        private @Nullable String accountMemo;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Duration autoRenewPeriod;
        private int maxAutomaticTokenAssociations = 0;
        private boolean receiverSignatureRequired = false;
        private long ownedNfts = 0L;
        private boolean deleted = false;
        private @Nullable AccountId stakedAccountId;
        private @Nullable Long stakedNodeId;
        private boolean declineStakingReward = false;

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
        /// 20-byte EVM-address alias if assigned
        ///
        /// @param evmAddress the value
        /// @return this builder
        public Builder evmAddress(final @Nullable EvmAddress evmAddress) {
            this.evmAddress = evmAddress;
            return this;
        }

        /// Sets the `balance`.
        ///
        /// @param balance the value
        /// @return this builder
        public Builder balance(final NativeToken<?, ?> balance) {
            this.balance = balance;
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

        /// Sets the `maxAutomaticTokenAssociations`.
        ///
        /// @param maxAutomaticTokenAssociations the value
        /// @return this builder
        public Builder maxAutomaticTokenAssociations(final int maxAutomaticTokenAssociations) {
            this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
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

        /// Sets the `ownedNfts`.
        ///
        /// @param ownedNfts the value
        /// @return this builder
        public Builder ownedNfts(final long ownedNfts) {
            this.ownedNfts = ownedNfts;
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

        /// Sets the `declineStakingReward`.
        ///
        /// @param declineStakingReward the value
        /// @return this builder
        public Builder declineStakingReward(final boolean declineStakingReward) {
            this.declineStakingReward = declineStakingReward;
            return this;
        }

        /// Creates the `AccountInfo` of the values set.
        ///
        /// @return the new `AccountInfo`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public AccountInfo build() {
            return new AccountInfo(
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    evmAddress,
                    Objects.requireNonNull(balance, "balance must not be null"),
                    authority,
                    accountMemo,
                    Objects.requireNonNull(expirationTime, "expirationTime must not be null"),
                    autoRenewPeriod,
                    maxAutomaticTokenAssociations,
                    receiverSignatureRequired,
                    ownedNfts,
                    deleted,
                    stakedAccountId,
                    stakedNodeId,
                    declineStakingReward);
        }
    }
}
