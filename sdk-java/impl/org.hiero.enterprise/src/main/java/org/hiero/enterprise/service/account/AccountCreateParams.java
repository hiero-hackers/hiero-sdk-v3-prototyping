package org.hiero.enterprise.service.account;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The parameters of a new account: everything that can be set when an account is created. Only the authority is
/// required.
///
/// @param initialBalance Amount transferred from the payer to the new account. If absent, the account is created with a balance
///   of 0 in the native token of the network.
public record AccountCreateParams(
        Authority authority,
        @Nullable NativeToken<?, ?> initialBalance,
        @Nullable String accountMemo,
        boolean receiverSignatureRequired,
        @Nullable Integer maxAutomaticTokenAssociations,
        @Nullable Duration autoRenewPeriod,
        byte @Nullable [] alias) {

    /// Creates a new `AccountCreateParams`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AccountCreateParams {
        Objects.requireNonNull(authority, "authority must not be null");
        if (accountMemo != null && accountMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("accountMemo must be at most 100 bytes long in UTF-8");
        }
        alias = alias == null ? null : alias.clone();
    }

    /// Creates a new `AccountCreateParams` with the default value of `receiverSignatureRequired`.
    public AccountCreateParams(
            final Authority authority,
            final @Nullable NativeToken<?, ?> initialBalance,
            final @Nullable String accountMemo,
            final @Nullable Integer maxAutomaticTokenAssociations,
            final @Nullable Duration autoRenewPeriod,
            final byte @Nullable [] alias) {
        this(authority, initialBalance, accountMemo, false, maxAutomaticTokenAssociations, autoRenewPeriod, alias);
    }

    /// Returns a copy of the `alias` array.
    @Override
    public byte @Nullable [] alias() {
        return alias == null ? null : alias.clone();
    }

    /// Compares `alias` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof AccountCreateParams other
            && Objects.equals(authority, other.authority)
            && Objects.equals(initialBalance, other.initialBalance)
            && Objects.equals(accountMemo, other.accountMemo)
            && receiverSignatureRequired == other.receiverSignatureRequired
            && Objects.equals(maxAutomaticTokenAssociations, other.maxAutomaticTokenAssociations)
            && Objects.equals(autoRenewPeriod, other.autoRenewPeriod)
            && Arrays.equals(alias, other.alias);
    }

    /// Hashes `alias` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(authority, initialBalance, accountMemo, receiverSignatureRequired, maxAutomaticTokenAssociations, autoRenewPeriod, Arrays.hashCode(alias));
    }

    /// Shows only the length of `alias` instead of the identity of the array object.
    @Override
    public String toString() {
        return "AccountCreateParams[authority=" + authority + ", initialBalance=" + initialBalance + ", accountMemo=" + accountMemo + ", receiverSignatureRequired=" + receiverSignatureRequired + ", maxAutomaticTokenAssociations=" + maxAutomaticTokenAssociations + ", autoRenewPeriod=" + autoRenewPeriod + ", alias=" + (alias == null ? "null" : "byte[" + alias.length + "]") + "]";
    }

    /// Returns a builder for a new `AccountCreateParams`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AccountCreateParams`.
    ///
    /// `build()` creates the `AccountCreateParams` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Authority authority;
        private @Nullable NativeToken<?, ?> initialBalance;
        private @Nullable String accountMemo;
        private boolean receiverSignatureRequired = false;
        private @Nullable Integer maxAutomaticTokenAssociations;
        private @Nullable Duration autoRenewPeriod;
        private byte @Nullable [] alias;

        private Builder() {
            // created by AccountCreateParams.builder()
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

        /// Sets the `alias`.
        ///
        /// @param alias the value
        /// @return this builder
        public Builder alias(final byte @Nullable [] alias) {
            this.alias = alias;
            return this;
        }

        /// Creates the `AccountCreateParams` of the values set.
        ///
        /// @return the new `AccountCreateParams`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public AccountCreateParams build() {
            return new AccountCreateParams(
                    Objects.requireNonNull(authority, "authority must not be null"),
                    initialBalance,
                    accountMemo,
                    receiverSignatureRequired,
                    maxAutomaticTokenAssociations,
                    autoRenewPeriod,
                    alias);
        }
    }
}
