package org.hiero.mirrornode.contract;

import java.util.Objects;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// A call of a contract that the Mirror Node simulates without submitting a transaction.
///
/// @param block a block number in hex, or "earliest", "pending", "latest" (the default)
/// @param data the function selector and the encoded parameters in hex, at most 131072 bytes
/// @param estimate whether the gas is estimated instead of the call executed; default false
/// @param from the sender
/// @param gas the gas provided; default 15000000
/// @param gasPrice the price of a unit of gas
/// @param to the contract that is called
/// @param value the value sent with the call; default 0
public record ContractCallRequest(
        @Nullable String block,
        @Nullable String data,
        @Nullable Boolean estimate,
        @Nullable EvmAddress from,
        @Nullable Long gas,
        @Nullable Long gasPrice,
        EvmAddress to,
        @Nullable Long value) {

    /// Creates a new `ContractCallRequest`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractCallRequest {
        if (gas != null && gas < 0L) {
            throw new IllegalArgumentException("gas must be at least 0");
        }
        if (gasPrice != null && gasPrice < 0L) {
            throw new IllegalArgumentException("gasPrice must be at least 0");
        }
        Objects.requireNonNull(to, "to must not be null");
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("value must be at least 0");
        }
    }

    /// Returns a builder for a new `ContractCallRequest`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractCallRequest`.
    ///
    /// `build()` creates the `ContractCallRequest` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable String block;
        private @Nullable String data;
        private @Nullable Boolean estimate;
        private @Nullable EvmAddress from;
        private @Nullable Long gas;
        private @Nullable Long gasPrice;
        private @Nullable EvmAddress to;
        private @Nullable Long value;

        private Builder() {
            // created by ContractCallRequest.builder()
        }

        /// Sets the `block`.
        ///
        /// a block number in hex, or "earliest", "pending", "latest" (the default)
        ///
        /// @param block the value
        /// @return this builder
        public Builder block(final @Nullable String block) {
            this.block = block;
            return this;
        }

        /// Sets the `data`.
        ///
        /// the function selector and the encoded parameters in hex, at most 131072 bytes
        ///
        /// @param data the value
        /// @return this builder
        public Builder data(final @Nullable String data) {
            this.data = data;
            return this;
        }

        /// Sets the `estimate`.
        ///
        /// whether the gas is estimated instead of the call executed; default false
        ///
        /// @param estimate the value
        /// @return this builder
        public Builder estimate(final @Nullable Boolean estimate) {
            this.estimate = estimate;
            return this;
        }

        /// Sets the `from`.
        ///
        /// the sender
        ///
        /// @param from the value
        /// @return this builder
        public Builder from(final @Nullable EvmAddress from) {
            this.from = from;
            return this;
        }

        /// Sets the `gas`.
        ///
        /// the gas provided; default 15000000
        ///
        /// @param gas the value
        /// @return this builder
        public Builder gas(final @Nullable Long gas) {
            this.gas = gas;
            return this;
        }

        /// Sets the `gasPrice`.
        ///
        /// the price of a unit of gas
        ///
        /// @param gasPrice the value
        /// @return this builder
        public Builder gasPrice(final @Nullable Long gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        /// Sets the `to`.
        ///
        /// the contract that is called
        ///
        /// @param to the value
        /// @return this builder
        public Builder to(final EvmAddress to) {
            this.to = to;
            return this;
        }

        /// Sets the `value`.
        ///
        /// the value sent with the call; default 0
        ///
        /// @param value the value
        /// @return this builder
        public Builder value(final @Nullable Long value) {
            this.value = value;
            return this;
        }

        /// Creates the `ContractCallRequest` of the values set.
        ///
        /// @return the new `ContractCallRequest`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractCallRequest build() {
            return new ContractCallRequest(
                    block,
                    data,
                    estimate,
                    from,
                    gas,
                    gasPrice,
                    Objects.requireNonNull(to, "to must not be null"),
                    value);
        }
    }
}
