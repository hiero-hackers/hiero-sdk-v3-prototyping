package org.hiero.consensusnode.admin.nodes;

import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

/// Receipt of a `NodeCreateTransaction`; contains the id assigned to the new node.
public final class NodeCreateReceipt extends Receipt {

    private final long nodeId;

    /// Creates a new `NodeCreateReceipt`.
    public NodeCreateReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final long nodeId) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        this.nodeId = nodeId;
    }

    /// network-assigned numeric id of the new node
    public long nodeId() {
        return nodeId;
    }

    /// Returns a builder for a new `NodeCreateReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NodeCreateReceipt`.
    ///
    /// `build()` creates the `NodeCreateReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable Long nodeId;

        private Builder() {
            // created by NodeCreateReceipt.builder()
        }

        /// Sets the `transactionId`.
        ///
        /// the id of the transaction
        ///
        /// @param transactionId the value
        /// @return this builder
        public Builder transactionId(final TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /// Sets the `status`.
        ///
        /// the status of the transaction
        ///
        /// @param status the value
        /// @return this builder
        public Builder status(final TransactionStatus status) {
            this.status = status;
            return this;
        }

        /// Sets the `exchangeRate`.
        ///
        /// the exchange rate at the time of the transaction
        ///
        /// @param exchangeRate the value
        /// @return this builder
        public Builder exchangeRate(final ExchangeRate exchangeRate) {
            this.exchangeRate = exchangeRate;
            return this;
        }

        /// Sets the `nextExchangeRate`.
        ///
        /// the next exchange rate
        ///
        /// @param nextExchangeRate the value
        /// @return this builder
        public Builder nextExchangeRate(final ExchangeRate nextExchangeRate) {
            this.nextExchangeRate = nextExchangeRate;
            return this;
        }

        /// Sets the `nodeId`.
        ///
        /// network-assigned numeric id of the new node
        ///
        /// @param nodeId the value
        /// @return this builder
        public Builder nodeId(final long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /// Creates the `NodeCreateReceipt` of the values set.
        ///
        /// @return the new `NodeCreateReceipt`
        /// @throws NullPointerException if a required value is not set
        public NodeCreateReceipt build() {
            return new NodeCreateReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    Objects.requireNonNull(nodeId, "nodeId must not be null"));
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof NodeCreateReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && nodeId() == other.nodeId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), nodeId());
    }

    @Override
    public String toString() {
        return "NodeCreateReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", nodeId=" + nodeId()
                + "]";
    }
}
