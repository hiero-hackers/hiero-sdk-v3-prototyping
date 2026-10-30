package org.hiero.mirrornode.contract;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// One step of an opcode trace: an opcode and the state of the EVM when it is executed.
///
/// @param depth the current call depth
/// @param gas the remaining gas
/// @param gasCost the cost of the opcode
/// @param memory the EVM memory in hex; empty unless requested
/// @param op the opcode
/// @param pc the program counter
/// @param reason the revert reason in hex
/// @param stack the EVM stack in hex; empty unless requested
/// @param storage the storage slots read and written by the contract, in hex; empty unless requested
public record Opcode(
        int depth,
        long gas,
        long gasCost,
        List<String> memory,
        String op,
        int pc,
        @Nullable String reason,
        List<String> stack,
        Map<String, String> storage) {

    /// Creates a new `Opcode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Opcode {
        Objects.requireNonNull(memory, "memory must not be null");
        memory = List.copyOf(memory);
        Objects.requireNonNull(op, "op must not be null");
        Objects.requireNonNull(stack, "stack must not be null");
        stack = List.copyOf(stack);
        Objects.requireNonNull(storage, "storage must not be null");
        storage = Map.copyOf(storage);
    }

    /// Creates a new `Opcode` with the default value of `memory`, `stack`.
    public Opcode(
            final int depth,
            final long gas,
            final long gasCost,
            final String op,
            final int pc,
            final @Nullable String reason,
            final Map<String, String> storage) {
        this(depth, gas, gasCost, List.of(), op, pc, reason, List.of(), storage);
    }

    /// Returns a builder for a new `Opcode`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Opcode`.
    ///
    /// `build()` creates the `Opcode` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Integer depth;
        private @Nullable Long gas;
        private @Nullable Long gasCost;
        private List<String> memory = List.of();
        private @Nullable String op;
        private @Nullable Integer pc;
        private @Nullable String reason;
        private List<String> stack = List.of();
        private @Nullable Map<String, String> storage;

        private Builder() {
            // created by Opcode.builder()
        }

        /// Sets the `depth`.
        ///
        /// the current call depth
        ///
        /// @param depth the value
        /// @return this builder
        public Builder depth(final int depth) {
            this.depth = depth;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// the remaining gas
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `gasCost`.
        ///
        /// the cost of the opcode
        ///
        /// @param gasCost the value
        /// @return this builder
        public Builder gasCost(final long gasCost) {
            this.gasCost = gasCost;
            return this;
        }

        /// Sets the `memory`.
        ///
        /// the EVM memory in hex; empty unless requested
        ///
        /// @param memory the value
        /// @return this builder
        public Builder memory(final List<String> memory) {
            this.memory = memory;
            return this;
        }

        /// Sets the `op`.
        ///
        /// the opcode
        ///
        /// @param op the value
        /// @return this builder
        public Builder op(final String op) {
            this.op = op;
            return this;
        }

        /// Sets the `pc`.
        ///
        /// the program counter
        ///
        /// @param pc the value
        /// @return this builder
        public Builder pc(final int pc) {
            this.pc = pc;
            return this;
        }

        /// Sets the `reason`.
        ///
        /// the revert reason in hex
        ///
        /// @param reason the value
        /// @return this builder
        public Builder reason(final @Nullable String reason) {
            this.reason = reason;
            return this;
        }

        /// Sets the `stack`.
        ///
        /// the EVM stack in hex; empty unless requested
        ///
        /// @param stack the value
        /// @return this builder
        public Builder stack(final List<String> stack) {
            this.stack = stack;
            return this;
        }

        /// Sets the `storage`.
        ///
        /// the storage slots read and written by the contract, in hex; empty unless requested
        ///
        /// @param storage the value
        /// @return this builder
        public Builder storage(final Map<String, String> storage) {
            this.storage = storage;
            return this;
        }

        /// Creates the `Opcode` of the values set.
        ///
        /// @return the new `Opcode`
        /// @throws NullPointerException if a required value is not set
        public Opcode build() {
            return new Opcode(
                    Objects.requireNonNull(depth, "depth must not be null"),
                    Objects.requireNonNull(gas, "gas must not be null"),
                    Objects.requireNonNull(gasCost, "gasCost must not be null"),
                    memory,
                    Objects.requireNonNull(op, "op must not be null"),
                    Objects.requireNonNull(pc, "pc must not be null"),
                    reason,
                    stack,
                    Objects.requireNonNull(storage, "storage must not be null"));
        }
    }
}
