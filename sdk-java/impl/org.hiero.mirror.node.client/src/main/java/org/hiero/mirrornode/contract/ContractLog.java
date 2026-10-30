package org.hiero.mirrornode.contract;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// An event that a contract emitted during an execution.
///
/// @param address the EVM address of the contract
/// @param bloom the bloom filter of the log in hex
/// @param data the data of the log in hex
/// @param index the position of the log in the logs of the execution
/// @param topics the topics of the event in hex
/// @param blockHash the hash of the block in hex; null in the logs of a ContractResult
/// @param blockNumber the number of the block; null in the logs of a ContractResult
/// @param rootContractId the executed contract that created the log
/// @param timestamp consensus time of the execution; null in the logs of a ContractResult
/// @param transactionHash the hash of the transaction in hex; null in the logs of a ContractResult
/// @param transactionIndex the position of the transaction in the block
public record ContractLog(
        EvmAddress address,
        @Nullable String bloom,
        @Nullable ContractId contractId,
        @Nullable String data,
        int index,
        List<String> topics,
        @Nullable String blockHash,
        @Nullable Long blockNumber,
        @Nullable ContractId rootContractId,
        @Nullable ZonedDateTime timestamp,
        @Nullable String transactionHash,
        @Nullable Integer transactionIndex) {

    /// Creates a new `ContractLog`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractLog {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(topics, "topics must not be null");
        topics = List.copyOf(topics);
    }

    /// Creates a new `ContractLog` with the default value of `topics`.
    public ContractLog(
            final EvmAddress address,
            final @Nullable String bloom,
            final @Nullable ContractId contractId,
            final @Nullable String data,
            final int index,
            final @Nullable String blockHash,
            final @Nullable Long blockNumber,
            final @Nullable ContractId rootContractId,
            final @Nullable ZonedDateTime timestamp,
            final @Nullable String transactionHash,
            final @Nullable Integer transactionIndex) {
        this(address, bloom, contractId, data, index, List.of(), blockHash, blockNumber, rootContractId, timestamp, transactionHash, transactionIndex);
    }

    /// Returns a builder for a new `ContractLog`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractLog`.
    ///
    /// `build()` creates the `ContractLog` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable EvmAddress address;
        private @Nullable String bloom;
        private @Nullable ContractId contractId;
        private @Nullable String data;
        private @Nullable Integer index;
        private List<String> topics = List.of();
        private @Nullable String blockHash;
        private @Nullable Long blockNumber;
        private @Nullable ContractId rootContractId;
        private @Nullable ZonedDateTime timestamp;
        private @Nullable String transactionHash;
        private @Nullable Integer transactionIndex;

        private Builder() {
            // created by ContractLog.builder()
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

        /// Sets the `bloom`.
        ///
        /// the bloom filter of the log in hex
        ///
        /// @param bloom the value
        /// @return this builder
        public Builder bloom(final @Nullable String bloom) {
            this.bloom = bloom;
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

        /// Sets the `data`.
        ///
        /// the data of the log in hex
        ///
        /// @param data the value
        /// @return this builder
        public Builder data(final @Nullable String data) {
            this.data = data;
            return this;
        }

        /// Sets the `index`.
        ///
        /// the position of the log in the logs of the execution
        ///
        /// @param index the value
        /// @return this builder
        public Builder index(final int index) {
            this.index = index;
            return this;
        }

        /// Sets the `topics`.
        ///
        /// the topics of the event in hex
        ///
        /// @param topics the value
        /// @return this builder
        public Builder topics(final List<String> topics) {
            this.topics = topics;
            return this;
        }

        /// Sets the `blockHash`.
        ///
        /// the hash of the block in hex; null in the logs of a ContractResult
        ///
        /// @param blockHash the value
        /// @return this builder
        public Builder blockHash(final @Nullable String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        /// Sets the `blockNumber`.
        ///
        /// the number of the block; null in the logs of a ContractResult
        ///
        /// @param blockNumber the value
        /// @return this builder
        public Builder blockNumber(final @Nullable Long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        /// Sets the `rootContractId`.
        ///
        /// the executed contract that created the log
        ///
        /// @param rootContractId the value
        /// @return this builder
        public Builder rootContractId(final @Nullable ContractId rootContractId) {
            this.rootContractId = rootContractId;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// consensus time of the execution; null in the logs of a ContractResult
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final @Nullable ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Sets the `transactionHash`.
        ///
        /// the hash of the transaction in hex; null in the logs of a ContractResult
        ///
        /// @param transactionHash the value
        /// @return this builder
        public Builder transactionHash(final @Nullable String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        /// Sets the `transactionIndex`.
        ///
        /// the position of the transaction in the block
        ///
        /// @param transactionIndex the value
        /// @return this builder
        public Builder transactionIndex(final @Nullable Integer transactionIndex) {
            this.transactionIndex = transactionIndex;
            return this;
        }

        /// Creates the `ContractLog` of the values set.
        ///
        /// @return the new `ContractLog`
        /// @throws NullPointerException if a required value is not set
        public ContractLog build() {
            return new ContractLog(
                    Objects.requireNonNull(address, "address must not be null"),
                    bloom,
                    contractId,
                    data,
                    Objects.requireNonNull(index, "index must not be null"),
                    topics,
                    blockHash,
                    blockNumber,
                    rootContractId,
                    timestamp,
                    transactionHash,
                    transactionIndex);
        }
    }
}
