package org.hiero.enterprise.service.contract;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.evm.AbiValue;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The options of a new smart contract. Where its bytecode comes from is chosen by the method that creates it.
///
/// @param constructorArguments The arguments of the constructor, in order.
/// @param gas The gas for running the constructor; a default of the service if absent.
/// @param initialBalance Amount transferred from the payer to the new contract.
/// @param adminAuthority Allows to update and delete the contract; without it the contract cannot be changed.
/// @param autoRenewPeriod The renewal period applied when the expiration approaches; the network default if absent.
/// @param autoRenewAccount The account that pays for the renewal.
public record ContractCreateParams(
        List<AbiValue> constructorArguments,
        @Nullable Long gas,
        @Nullable NativeToken<?, ?> initialBalance,
        @Nullable Authority adminAuthority,
        @Nullable String contractMemo,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccount,
        @Nullable Integer maxAutomaticTokenAssociations) {

    /// Creates a new `ContractCreateParams`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractCreateParams {
        Objects.requireNonNull(constructorArguments, "constructorArguments must not be null");
        constructorArguments = List.copyOf(constructorArguments);
        if (gas != null && gas < 1L) {
            throw new IllegalArgumentException("gas must be at least 1");
        }
        if (contractMemo != null && contractMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("contractMemo must be at most 100 bytes long in UTF-8");
        }
    }

    /// Creates a new `ContractCreateParams` with the default value of `constructorArguments`.
    public ContractCreateParams(
            final @Nullable Long gas,
            final @Nullable NativeToken<?, ?> initialBalance,
            final @Nullable Authority adminAuthority,
            final @Nullable String contractMemo,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Integer maxAutomaticTokenAssociations) {
        this(List.of(), gas, initialBalance, adminAuthority, contractMemo, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations);
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

        private List<AbiValue> constructorArguments = List.of();
        private @Nullable Long gas;
        private @Nullable NativeToken<?, ?> initialBalance;
        private @Nullable Authority adminAuthority;
        private @Nullable String contractMemo;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Integer maxAutomaticTokenAssociations;

        private Builder() {
            // created by ContractCreateParams.builder()
        }

        /// Sets the `constructorArguments`.
        ///
        /// The arguments of the constructor, in order.
        ///
        /// @param constructorArguments the value
        /// @return this builder
        public Builder constructorArguments(final List<AbiValue> constructorArguments) {
            this.constructorArguments = constructorArguments;
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

        /// Creates the `ContractCreateParams` of the values set.
        ///
        /// @return the new `ContractCreateParams`
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractCreateParams build() {
            return new ContractCreateParams(
                    constructorArguments,
                    gas,
                    initialBalance,
                    adminAuthority,
                    contractMemo,
                    autoRenewPeriod,
                    autoRenewAccount,
                    maxAutomaticTokenAssociations);
        }
    }
}
