package org.hiero.mirrornode.contract;

import java.util.List;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// The opcode trace of the execution of a transaction.
///
/// @param address the recipient of the transaction; the zero address for a contract creation
/// @param failed whether the execution failed
/// @param gas the gas used
/// @param returnValue the data returned by the transaction in hex
public record OpcodeTrace(
        EvmAddress address,
        @Nullable ContractId contractId,
        boolean failed,
        long gas,
        List<Opcode> opcodes,
        String returnValue) {

    /// Creates a new `OpcodeTrace`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public OpcodeTrace {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(opcodes, "opcodes must not be null");
        opcodes = List.copyOf(opcodes);
        Objects.requireNonNull(returnValue, "returnValue must not be null");
    }

    /// Creates a new `OpcodeTrace` with the default value of `opcodes`.
    public OpcodeTrace(
            final EvmAddress address,
            final @Nullable ContractId contractId,
            final boolean failed,
            final long gas,
            final String returnValue) {
        this(address, contractId, failed, gas, List.of(), returnValue);
    }

    /// Returns a builder for a new `OpcodeTrace`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `OpcodeTrace`.
    ///
    /// `build()` creates the `OpcodeTrace` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable EvmAddress address;
        private @Nullable ContractId contractId;
        private @Nullable Boolean failed;
        private @Nullable Long gas;
        private List<Opcode> opcodes = List.of();
        private @Nullable String returnValue;

        private Builder() {
            // created by OpcodeTrace.builder()
        }

        /// Sets the `address`.
        ///
        /// the recipient of the transaction; the zero address for a contract creation
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
        public Builder contractId(final @Nullable ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `failed`.
        ///
        /// whether the execution failed
        ///
        /// @param failed the value
        /// @return this builder
        public Builder failed(final boolean failed) {
            this.failed = failed;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// the gas used
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `opcodes`.
        ///
        /// @param opcodes the value
        /// @return this builder
        public Builder opcodes(final List<Opcode> opcodes) {
            this.opcodes = opcodes;
            return this;
        }

        /// Sets the `returnValue`.
        ///
        /// the data returned by the transaction in hex
        ///
        /// @param returnValue the value
        /// @return this builder
        public Builder returnValue(final String returnValue) {
            this.returnValue = returnValue;
            return this;
        }

        /// Creates the `OpcodeTrace` of the values set.
        ///
        /// @return the new `OpcodeTrace`
        /// @throws NullPointerException if a required value is not set
        public OpcodeTrace build() {
            return new OpcodeTrace(
                    Objects.requireNonNull(address, "address must not be null"),
                    contractId,
                    Objects.requireNonNull(failed, "failed must not be null"),
                    Objects.requireNonNull(gas, "gas must not be null"),
                    opcodes,
                    Objects.requireNonNull(returnValue, "returnValue must not be null"));
        }
    }
}
