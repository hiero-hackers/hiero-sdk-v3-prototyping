package org.hiero.mirrornode.contract;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.Address;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// A call within the execution of a transaction, e.g. a call of one contract by another.
///
/// @param callDepth the nesting depth of the call
/// @param caller the calling account or contract
/// @param from the EVM address of the caller
/// @param gas the gas provided
/// @param index the position of the action in the actions of the transaction
/// @param input the input data in hex
/// @param recipient the receiving account or contract
/// @param resultData the result data in hex
/// @param timestamp the consensus time of the transaction
/// @param to the EVM address of the recipient
/// @param value the value transferred in tinybars
public record ContractAction(
        int callDepth,
        ContractCallOperationType callOperationType,
        ContractActionType callType,
        @Nullable Address caller,
        ContractEntityType callerType,
        EvmAddress from,
        long gas,
        long gasUsed,
        int index,
        @Nullable String input,
        @Nullable Address recipient,
        @Nullable ContractEntityType recipientType,
        @Nullable String resultData,
        ContractResultDataType resultDataType,
        ZonedDateTime timestamp,
        @Nullable EvmAddress to,
        long value) {

    /// Creates a new `ContractAction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractAction {
        Objects.requireNonNull(callOperationType, "callOperationType must not be null");
        Objects.requireNonNull(callType, "callType must not be null");
        Objects.requireNonNull(callerType, "callerType must not be null");
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(resultDataType, "resultDataType must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Returns a builder for a new `ContractAction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractAction`.
    ///
    /// `build()` creates the `ContractAction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Integer callDepth;
        private @Nullable ContractCallOperationType callOperationType;
        private @Nullable ContractActionType callType;
        private @Nullable Address caller;
        private @Nullable ContractEntityType callerType;
        private @Nullable EvmAddress from;
        private @Nullable Long gas;
        private @Nullable Long gasUsed;
        private @Nullable Integer index;
        private @Nullable String input;
        private @Nullable Address recipient;
        private @Nullable ContractEntityType recipientType;
        private @Nullable String resultData;
        private @Nullable ContractResultDataType resultDataType;
        private @Nullable ZonedDateTime timestamp;
        private @Nullable EvmAddress to;
        private @Nullable Long value;

        private Builder() {
            // created by ContractAction.builder()
        }

        /// Sets the `callDepth`.
        ///
        /// the nesting depth of the call
        ///
        /// @param callDepth the value
        /// @return this builder
        public Builder callDepth(final int callDepth) {
            this.callDepth = callDepth;
            return this;
        }

        /// Sets the `callOperationType`.
        ///
        /// @param callOperationType the value
        /// @return this builder
        public Builder callOperationType(final ContractCallOperationType callOperationType) {
            this.callOperationType = callOperationType;
            return this;
        }

        /// Sets the `callType`.
        ///
        /// @param callType the value
        /// @return this builder
        public Builder callType(final ContractActionType callType) {
            this.callType = callType;
            return this;
        }

        /// Sets the `caller`.
        ///
        /// the calling account or contract
        ///
        /// @param caller the value
        /// @return this builder
        public Builder caller(final @Nullable Address caller) {
            this.caller = caller;
            return this;
        }

        /// Sets the `callerType`.
        ///
        /// @param callerType the value
        /// @return this builder
        public Builder callerType(final ContractEntityType callerType) {
            this.callerType = callerType;
            return this;
        }

        /// Sets the `from`.
        ///
        /// the EVM address of the caller
        ///
        /// @param from the value
        /// @return this builder
        public Builder from(final EvmAddress from) {
            this.from = from;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// the gas provided
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `gasUsed`.
        ///
        /// @param gasUsed the value
        /// @return this builder
        public Builder gasUsed(final long gasUsed) {
            this.gasUsed = gasUsed;
            return this;
        }

        /// Sets the `index`.
        ///
        /// the position of the action in the actions of the transaction
        ///
        /// @param index the value
        /// @return this builder
        public Builder index(final int index) {
            this.index = index;
            return this;
        }

        /// Sets the `input`.
        ///
        /// the input data in hex
        ///
        /// @param input the value
        /// @return this builder
        public Builder input(final @Nullable String input) {
            this.input = input;
            return this;
        }

        /// Sets the `recipient`.
        ///
        /// the receiving account or contract
        ///
        /// @param recipient the value
        /// @return this builder
        public Builder recipient(final @Nullable Address recipient) {
            this.recipient = recipient;
            return this;
        }

        /// Sets the `recipientType`.
        ///
        /// @param recipientType the value
        /// @return this builder
        public Builder recipientType(final @Nullable ContractEntityType recipientType) {
            this.recipientType = recipientType;
            return this;
        }

        /// Sets the `resultData`.
        ///
        /// the result data in hex
        ///
        /// @param resultData the value
        /// @return this builder
        public Builder resultData(final @Nullable String resultData) {
            this.resultData = resultData;
            return this;
        }

        /// Sets the `resultDataType`.
        ///
        /// @param resultDataType the value
        /// @return this builder
        public Builder resultDataType(final ContractResultDataType resultDataType) {
            this.resultDataType = resultDataType;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the consensus time of the transaction
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Sets the `to`.
        ///
        /// the EVM address of the recipient
        ///
        /// @param to the value
        /// @return this builder
        public Builder to(final @Nullable EvmAddress to) {
            this.to = to;
            return this;
        }

        /// Sets the `value`.
        ///
        /// the value transferred in tinybars
        ///
        /// @param value the value
        /// @return this builder
        public Builder value(final long value) {
            this.value = value;
            return this;
        }

        /// Creates the `ContractAction` of the values set.
        ///
        /// @return the new `ContractAction`
        /// @throws NullPointerException if a required value is not set
        public ContractAction build() {
            return new ContractAction(
                    Objects.requireNonNull(callDepth, "callDepth must not be null"),
                    Objects.requireNonNull(callOperationType, "callOperationType must not be null"),
                    Objects.requireNonNull(callType, "callType must not be null"),
                    caller,
                    Objects.requireNonNull(callerType, "callerType must not be null"),
                    Objects.requireNonNull(from, "from must not be null"),
                    Objects.requireNonNull(gas, "gas must not be null"),
                    Objects.requireNonNull(gasUsed, "gasUsed must not be null"),
                    Objects.requireNonNull(index, "index must not be null"),
                    input,
                    recipient,
                    recipientType,
                    resultData,
                    Objects.requireNonNull(resultDataType, "resultDataType must not be null"),
                    Objects.requireNonNull(timestamp, "timestamp must not be null"),
                    to,
                    Objects.requireNonNull(value, "value must not be null"));
        }
    }
}
