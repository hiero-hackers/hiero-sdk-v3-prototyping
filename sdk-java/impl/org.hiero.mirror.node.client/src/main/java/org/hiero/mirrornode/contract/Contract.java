package org.hiero.mirrornode.contract;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// @param fileId id of the file that holds the contract bytecode, if any
/// @param obtainerId account that received the remaining balance when the contract was deleted
/// @param proxyAccountId proxy account of the contract, if any
public record Contract(
        ContractId contractId,
        @Nullable Authority adminAuthority,
        @Nullable AccountId autoRenewAccount,
        Duration autoRenewPeriod,
        ZonedDateTime createdTimestamp,
        boolean deleted,
        @Nullable ZonedDateTime expirationTimestamp,
        @Nullable String fileId,
        @Nullable EvmAddress evmAddress,
        @Nullable String memo,
        @Nullable Integer maxAutomaticTokenAssociations,
        @Nullable Long nonce,
        @Nullable String obtainerId,
        boolean permanentRemoval,
        @Nullable String proxyAccountId,
        ZonedDateTime fromTimestamp,
        ZonedDateTime toTimestamp,
        @Nullable String bytecode,
        @Nullable String runtimeBytecode) {

    /// Creates a new `Contract`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Contract {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(autoRenewPeriod, "autoRenewPeriod must not be null");
        Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null");
        Objects.requireNonNull(fromTimestamp, "fromTimestamp must not be null");
        Objects.requireNonNull(toTimestamp, "toTimestamp must not be null");
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
        private @Nullable String fileId;
        private @Nullable EvmAddress evmAddress;
        private @Nullable String memo;
        private @Nullable Integer maxAutomaticTokenAssociations;
        private @Nullable Long nonce;
        private @Nullable String obtainerId;
        private @Nullable Boolean permanentRemoval;
        private @Nullable String proxyAccountId;
        private @Nullable ZonedDateTime fromTimestamp;
        private @Nullable ZonedDateTime toTimestamp;
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
        public Builder autoRenewPeriod(final Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final ZonedDateTime createdTimestamp) {
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
        /// id of the file that holds the contract bytecode, if any
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final @Nullable String fileId) {
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
        /// account that received the remaining balance when the contract was deleted
        ///
        /// @param obtainerId the value
        /// @return this builder
        public Builder obtainerId(final @Nullable String obtainerId) {
            this.obtainerId = obtainerId;
            return this;
        }

        /// Sets the `permanentRemoval`.
        ///
        /// @param permanentRemoval the value
        /// @return this builder
        public Builder permanentRemoval(final boolean permanentRemoval) {
            this.permanentRemoval = permanentRemoval;
            return this;
        }

        /// Sets the `proxyAccountId`.
        ///
        /// proxy account of the contract, if any
        ///
        /// @param proxyAccountId the value
        /// @return this builder
        public Builder proxyAccountId(final @Nullable String proxyAccountId) {
            this.proxyAccountId = proxyAccountId;
            return this;
        }

        /// Sets the `fromTimestamp`.
        ///
        /// @param fromTimestamp the value
        /// @return this builder
        public Builder fromTimestamp(final ZonedDateTime fromTimestamp) {
            this.fromTimestamp = fromTimestamp;
            return this;
        }

        /// Sets the `toTimestamp`.
        ///
        /// @param toTimestamp the value
        /// @return this builder
        public Builder toTimestamp(final ZonedDateTime toTimestamp) {
            this.toTimestamp = toTimestamp;
            return this;
        }

        /// Sets the `bytecode`.
        ///
        /// @param bytecode the value
        /// @return this builder
        public Builder bytecode(final @Nullable String bytecode) {
            this.bytecode = bytecode;
            return this;
        }

        /// Sets the `runtimeBytecode`.
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
                    Objects.requireNonNull(autoRenewPeriod, "autoRenewPeriod must not be null"),
                    Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null"),
                    Objects.requireNonNull(deleted, "deleted must not be null"),
                    expirationTimestamp,
                    fileId,
                    evmAddress,
                    memo,
                    maxAutomaticTokenAssociations,
                    nonce,
                    obtainerId,
                    Objects.requireNonNull(permanentRemoval, "permanentRemoval must not be null"),
                    proxyAccountId,
                    Objects.requireNonNull(fromTimestamp, "fromTimestamp must not be null"),
                    Objects.requireNonNull(toTimestamp, "toTimestamp must not be null"),
                    bytecode,
                    runtimeBytecode);
        }
    }
}
