package org.hiero.mirrornode.contract;

import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// A storage slot of a contract that an execution read or changed.
///
/// @param address the EVM address of the contract
/// @param slot the storage slot in hex
/// @param valueRead the value read from the slot in hex
/// @param valueWritten the value written to the slot in hex; null if nothing was written
public record ContractStateChange(
        EvmAddress address,
        ContractId contractId,
        String slot,
        String valueRead,
        @Nullable String valueWritten) {

    /// Creates a new `ContractStateChange`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractStateChange {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(slot, "slot must not be null");
        Objects.requireNonNull(valueRead, "valueRead must not be null");
    }

    /// Returns a builder for a new `ContractStateChange`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractStateChange`.
    ///
    /// `build()` creates the `ContractStateChange` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable EvmAddress address;
        private @Nullable ContractId contractId;
        private @Nullable String slot;
        private @Nullable String valueRead;
        private @Nullable String valueWritten;

        private Builder() {
            // created by ContractStateChange.builder()
        }

        /// Sets the `address`.
        ///
        /// the EVM address of the contract
        ///
        /// @param address the value
        /// @return this builder
        public Builder address(final EvmAddress address) {
            this.address = address;
            return this;
        }

        /// Sets the `contractId`.
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `slot`.
        ///
        /// the storage slot in hex
        ///
        /// @param slot the value
        /// @return this builder
        public Builder slot(final String slot) {
            this.slot = slot;
            return this;
        }

        /// Sets the `valueRead`.
        ///
        /// the value read from the slot in hex
        ///
        /// @param valueRead the value
        /// @return this builder
        public Builder valueRead(final String valueRead) {
            this.valueRead = valueRead;
            return this;
        }

        /// Sets the `valueWritten`.
        ///
        /// the value written to the slot in hex; null if nothing was written
        ///
        /// @param valueWritten the value
        /// @return this builder
        public Builder valueWritten(final @Nullable String valueWritten) {
            this.valueWritten = valueWritten;
            return this;
        }

        /// Creates the `ContractStateChange` of the values set.
        ///
        /// @return the new `ContractStateChange`
        /// @throws NullPointerException if a required value is not set
        public ContractStateChange build() {
            return new ContractStateChange(
                    Objects.requireNonNull(address, "address must not be null"),
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    Objects.requireNonNull(slot, "slot must not be null"),
                    Objects.requireNonNull(valueRead, "valueRead must not be null"),
                    valueWritten);
        }
    }
}
