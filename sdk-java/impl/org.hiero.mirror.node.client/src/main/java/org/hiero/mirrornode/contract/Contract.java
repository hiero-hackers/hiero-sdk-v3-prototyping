package org.hiero.mirrornode.contract;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// @param fileId the file that holds the bytecode of the contract, if any
/// @param obtainerId account or contract that received the remaining balance when the contract was deleted
/// @param proxyAccountId proxy account of the contract, if any
/// @param timestamp the period this state of the contract is valid for
/// @param bytecode the bytecode in hex during deployment; only set by findById
/// @param runtimeBytecode the bytecode in hex after deployment; only set by findById
public record Contract(
        ContractId contractId,
        @Nullable Authority adminAuthority,
        @Nullable AccountId autoRenewAccount,
        @Nullable Duration autoRenewPeriod,
        @Nullable ZonedDateTime createdTimestamp,
        boolean deleted,
        @Nullable ZonedDateTime expirationTimestamp,
        @Nullable Address fileId,
        @Nullable EvmAddress evmAddress,
        @Nullable String memo,
        @Nullable Integer maxAutomaticTokenAssociations,
        @Nullable Long nonce,
        @Nullable Address obtainerId,
        @Nullable Boolean permanentRemoval,
        @Nullable AccountId proxyAccountId,
        TimestampRange timestamp,
        @Nullable String bytecode,
        @Nullable String runtimeBytecode) {

    /// Creates a new `Contract`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Contract {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Returns a builder for a new `Contract`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Contract`.
    ///
    /// `build()` creates the `Contract` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable ContractId contractId;
        private @Nullable Authority adminAuthority;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable Boolean deleted;
        private @Nullable ZonedDateTime expirationTimestamp;
        private @Nullable Address fileId;
        private @Nullable EvmAddress evmAddress;
        private @Nullable String memo;
        private @Nullable Integer maxAutomaticTokenAssociations;
        private @Nullable Long nonce;
        private @Nullable Address obtainerId;
        private @Nullable Boolean permanentRemoval;
        private @Nullable AccountId proxyAccountId;
        private @Nullable TimestampRange timestamp;
        private @Nullable String bytecode;
        private @Nullable String runtimeBytecode;

        private Builder() {
            // created by Contract.builder()
        }

        /// Sets the `contractId`.
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
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

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
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

        /// Sets the `expirationTimestamp`.
        ///
        /// @param expirationTimestamp the value
        /// @return this builder
        public Builder expirationTimestamp(final @Nullable ZonedDateTime expirationTimestamp) {
            this.expirationTimestamp = expirationTimestamp;
            return this;
        }

        /// Sets the `fileId`.
        ///
        /// the file that holds the bytecode of the contract, if any
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final @Nullable Address fileId) {
            this.fileId = fileId;
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

        /// Sets the `memo`.
        ///
        /// @param memo the value
        /// @return this builder
        public Builder memo(final @Nullable String memo) {
            this.memo = memo;
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

        /// Sets the `nonce`.
        ///
        /// @param nonce the value
        /// @return this builder
        public Builder nonce(final @Nullable Long nonce) {
            this.nonce = nonce;
            return this;
        }

        /// Sets the `obtainerId`.
        ///
        /// account or contract that received the remaining balance when the contract was deleted
        ///
        /// @param obtainerId the value
        /// @return this builder
        public Builder obtainerId(final @Nullable Address obtainerId) {
            this.obtainerId = obtainerId;
            return this;
        }

        /// Sets the `permanentRemoval`.
        ///
        /// @param permanentRemoval the value
        /// @return this builder
        public Builder permanentRemoval(final @Nullable Boolean permanentRemoval) {
            this.permanentRemoval = permanentRemoval;
            return this;
        }

        /// Sets the `proxyAccountId`.
        ///
        /// proxy account of the contract, if any
        ///
        /// @param proxyAccountId the value
        /// @return this builder
        public Builder proxyAccountId(final @Nullable AccountId proxyAccountId) {
            this.proxyAccountId = proxyAccountId;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the period this state of the contract is valid for
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final TimestampRange timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Sets the `bytecode`.
        ///
        /// the bytecode in hex during deployment; only set by findById
        ///
        /// @param bytecode the value
        /// @return this builder
        public Builder bytecode(final @Nullable String bytecode) {
            this.bytecode = bytecode;
            return this;
        }

        /// Sets the `runtimeBytecode`.
        ///
        /// the bytecode in hex after deployment; only set by findById
        ///
        /// @param runtimeBytecode the value
        /// @return this builder
        public Builder runtimeBytecode(final @Nullable String runtimeBytecode) {
            this.runtimeBytecode = runtimeBytecode;
            return this;
        }

        /// Creates the `Contract` of the values set.
        ///
        /// @return the new `Contract`
        /// @throws NullPointerException if a required value is not set
        public Contract build() {
            return new Contract(
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    adminAuthority,
                    autoRenewAccount,
                    autoRenewPeriod,
                    createdTimestamp,
                    Objects.requireNonNull(deleted, "deleted must not be null"),
                    expirationTimestamp,
                    fileId,
                    evmAddress,
                    memo,
                    maxAutomaticTokenAssociations,
                    nonce,
                    obtainerId,
                    permanentRemoval,
                    proxyAccountId,
                    Objects.requireNonNull(timestamp, "timestamp must not be null"),
                    bytecode,
                    runtimeBytecode);
        }
    }
}
