package org.hiero.mirrornode.contract;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// A storage slot of a contract and its value.
///
/// @param address the EVM address of the contract
/// @param timestamp the consensus time at which the value was written
/// @param slot the storage slot in hex
/// @param value the value of the slot in hex; "0x" if no value was written
public record ContractState(
        EvmAddress address,
        ContractId contractId,
        ZonedDateTime timestamp,
        String slot,
        String value) {

    /// Creates a new `ContractState`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractState {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(slot, "slot must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }

    /// Returns a builder for a new `ContractState`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractState`.
    ///
    /// `build()` creates the `ContractState` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable EvmAddress address;
        private @Nullable ContractId contractId;
        private @Nullable ZonedDateTime timestamp;
        private @Nullable String slot;
        private @Nullable String value;

        private Builder() {
            // created by ContractState.builder()
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

        /// Sets the `timestamp`.
        ///
        /// the consensus time at which the value was written
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
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

        /// Sets the `value`.
        ///
        /// the value of the slot in hex; "0x" if no value was written
        ///
        /// @param value the value
        /// @return this builder
        public Builder value(final String value) {
            this.value = value;
            return this;
        }

        /// Creates the `ContractState` of the values set.
        ///
        /// @return the new `ContractState`
        /// @throws NullPointerException if a required value is not set
        public ContractState build() {
            return new ContractState(
                    Objects.requireNonNull(address, "address must not be null"),
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"),
                    Objects.requireNonNull(slot, "slot must not be null"),
                    Objects.requireNonNull(value, "value must not be null"));
        }
    }
}
