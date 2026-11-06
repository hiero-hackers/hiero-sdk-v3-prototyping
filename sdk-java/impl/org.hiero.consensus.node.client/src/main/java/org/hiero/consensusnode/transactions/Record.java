package org.hiero.consensusnode.transactions;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// @param transactionId the id of the transaction
/// @param consensusTimestamp the consensus time of the transaction
/// @param receipt the typed receipt of the transaction
/// @param parentConsensusTimestamp For a child/triggered transaction — a batch inner transaction, a scheduled transaction, or a
///   contract-spawned child — the consensusTimestamp of the parent that spawned it; absent for an
///   ordinary top-level transaction. This is the link from a child record back to its parent.
/// @param contractResult The result of the contract execution of the transaction: of a contract creation, a contract call or an Ethereum
///   transaction, and of the child transactions of a contract call. Absent for every other transaction.
public record Record<ReceiptT extends Receipt>(
        TransactionId transactionId,
        ZonedDateTime consensusTimestamp,
        ReceiptT receipt,
        @Nullable ZonedDateTime parentConsensusTimestamp,
        @Nullable ContractFunctionResult contractResult) {

    /// Creates a new `Record`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Record {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        Objects.requireNonNull(receipt, "receipt must not be null");
    }

    /// Returns a builder for a new `Record`.
    ///
    /// @return the builder
    public static <ReceiptT extends Receipt> Builder<ReceiptT> builder() {
        return new Builder<>();
    }

    /// The builder of `Record`.
    ///
    /// `build()` creates the `Record` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder<ReceiptT extends Receipt> {

        private @Nullable TransactionId transactionId;
        private @Nullable ZonedDateTime consensusTimestamp;
        private @Nullable ReceiptT receipt;
        private @Nullable ZonedDateTime parentConsensusTimestamp;
        private @Nullable ContractFunctionResult contractResult;

        private Builder() {
            // created by Record.builder()
        }

        /// Sets the `transactionId`.
        ///
        /// the id of the transaction
        ///
        /// @param transactionId the value
        /// @return this builder
        public Builder<ReceiptT> transactionId(final TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /// Sets the `consensusTimestamp`.
        ///
        /// the consensus time of the transaction
        ///
        /// @param consensusTimestamp the value
        /// @return this builder
        public Builder<ReceiptT> consensusTimestamp(final ZonedDateTime consensusTimestamp) {
            this.consensusTimestamp = consensusTimestamp;
            return this;
        }

        /// Sets the `receipt`.
        ///
        /// the typed receipt of the transaction
        ///
        /// @param receipt the value
        /// @return this builder
        public Builder<ReceiptT> receipt(final ReceiptT receipt) {
            this.receipt = receipt;
            return this;
        }

        /// Sets the `parentConsensusTimestamp`.
        ///
        /// For a child/triggered transaction — a batch inner transaction, a scheduled transaction, or a
        /// contract-spawned child — the consensusTimestamp of the parent that spawned it; absent for an
        /// ordinary top-level transaction. This is the link from a child record back to its parent.
        ///
        /// @param parentConsensusTimestamp the value
        /// @return this builder
        public Builder<ReceiptT> parentConsensusTimestamp(final @Nullable ZonedDateTime parentConsensusTimestamp) {
            this.parentConsensusTimestamp = parentConsensusTimestamp;
            return this;
        }

        /// Sets the `contractResult`.
        ///
        /// The result of the contract execution of the transaction: of a contract creation, a contract call or an Ethereum
        /// transaction, and of the child transactions of a contract call. Absent for every other transaction.
        ///
        /// @param contractResult the value
        /// @return this builder
        public Builder<ReceiptT> contractResult(final @Nullable ContractFunctionResult contractResult) {
            this.contractResult = contractResult;
            return this;
        }

        /// Creates the `Record` of the values set.
        ///
        /// @return the new `Record`
        /// @throws NullPointerException if a required value is not set
        public Record<ReceiptT> build() {
            return new Record<>(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null"),
                    Objects.requireNonNull(receipt, "receipt must not be null"),
                    parentConsensusTimestamp,
                    contractResult);
        }
    }
}
