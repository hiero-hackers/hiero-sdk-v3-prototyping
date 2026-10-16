package org.hiero.consensusnode.transactions.schedule;

import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

public final class ScheduleSignReceipt extends Receipt {

    private final TransactionId scheduledTransactionId;

    /// Creates a new `ScheduleSignReceipt`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ScheduleSignReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final TransactionId scheduledTransactionId) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        Objects.requireNonNull(scheduledTransactionId, "scheduledTransactionId must not be null");
        this.scheduledTransactionId = scheduledTransactionId;
    }

    /// the id of the inner transaction (populated whether or not this signature triggered execution)
    public TransactionId scheduledTransactionId() {
        return scheduledTransactionId;
    }

    /// Returns a builder for a new `ScheduleSignReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ScheduleSignReceipt`.
    ///
    /// `build()` creates the `ScheduleSignReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable TransactionId scheduledTransactionId;

        private Builder() {
            // created by ScheduleSignReceipt.builder()
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

        /// Sets the `scheduledTransactionId`.
        ///
        /// the id of the inner transaction (populated whether or not this signature triggered execution)
        ///
        /// @param scheduledTransactionId the value
        /// @return this builder
        public Builder scheduledTransactionId(final TransactionId scheduledTransactionId) {
            this.scheduledTransactionId = scheduledTransactionId;
            return this;
        }

        /// Creates the `ScheduleSignReceipt` of the values set.
        ///
        /// @return the new `ScheduleSignReceipt`
        /// @throws NullPointerException if a required value is not set
        public ScheduleSignReceipt build() {
            return new ScheduleSignReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    Objects.requireNonNull(scheduledTransactionId, "scheduledTransactionId must not be null"));
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof ScheduleSignReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && Objects.equals(scheduledTransactionId(), other.scheduledTransactionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), scheduledTransactionId());
    }

    @Override
    public String toString() {
        return "ScheduleSignReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", scheduledTransactionId=" + scheduledTransactionId()
                + "]";
    }
}
