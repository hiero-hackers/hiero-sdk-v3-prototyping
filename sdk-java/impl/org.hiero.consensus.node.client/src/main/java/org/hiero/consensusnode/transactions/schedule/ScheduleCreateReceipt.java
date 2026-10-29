package org.hiero.consensusnode.transactions.schedule;

import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

public final class ScheduleCreateReceipt extends Receipt {

    private final Address scheduleId;
    private final TransactionId scheduledTransactionId;

    /// Creates a new `ScheduleCreateReceipt`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ScheduleCreateReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final Address scheduleId,
            final TransactionId scheduledTransactionId) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        this.scheduleId = scheduleId;
        Objects.requireNonNull(scheduledTransactionId, "scheduledTransactionId must not be null");
        this.scheduledTransactionId = scheduledTransactionId;
    }

    /// the id of the newly created schedule
    public Address scheduleId() {
        return scheduleId;
    }

    /// the id the inner transaction carries when it executes (the scheduled flag is set on it)
    public TransactionId scheduledTransactionId() {
        return scheduledTransactionId;
    }

    /// Returns a builder for a new `ScheduleCreateReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ScheduleCreateReceipt`.
    ///
    /// `build()` creates the `ScheduleCreateReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable Address scheduleId;
        private @Nullable TransactionId scheduledTransactionId;

        private Builder() {
            // created by ScheduleCreateReceipt.builder()
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

        /// Sets the `scheduleId`.
        ///
        /// the id of the newly created schedule
        ///
        /// @param scheduleId the value
        /// @return this builder
        public Builder scheduleId(final Address scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        /// Sets the `scheduledTransactionId`.
        ///
        /// the id the inner transaction carries when it executes (the scheduled flag is set on it)
        ///
        /// @param scheduledTransactionId the value
        /// @return this builder
        public Builder scheduledTransactionId(final TransactionId scheduledTransactionId) {
            this.scheduledTransactionId = scheduledTransactionId;
            return this;
        }

        /// Creates the `ScheduleCreateReceipt` of the values set.
        ///
        /// @return the new `ScheduleCreateReceipt`
        /// @throws NullPointerException if a required value is not set
        public ScheduleCreateReceipt build() {
            return new ScheduleCreateReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    Objects.requireNonNull(scheduleId, "scheduleId must not be null"),
                    Objects.requireNonNull(scheduledTransactionId, "scheduledTransactionId must not be null"));
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof ScheduleCreateReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && Objects.equals(scheduleId(), other.scheduleId())
            && Objects.equals(scheduledTransactionId(), other.scheduledTransactionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), scheduleId(), scheduledTransactionId());
    }

    @Override
    public String toString() {
        return "ScheduleCreateReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", scheduleId=" + scheduleId()
                + ", scheduledTransactionId=" + scheduledTransactionId()
                + "]";
    }
}
