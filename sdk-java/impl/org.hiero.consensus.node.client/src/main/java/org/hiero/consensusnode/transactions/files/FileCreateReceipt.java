package org.hiero.consensusnode.transactions.files;

import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

public final class FileCreateReceipt extends Receipt {

    private final Address fileId;

    /// Creates a new `FileCreateReceipt`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FileCreateReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final Address fileId) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        Objects.requireNonNull(fileId, "fileId must not be null");
        this.fileId = fileId;
    }

    /// id of the newly created file
    public Address fileId() {
        return fileId;
    }

    /// Returns a builder for a new `FileCreateReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FileCreateReceipt`.
    ///
    /// `build()` creates the `FileCreateReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable Address fileId;

        private Builder() {
            // created by FileCreateReceipt.builder()
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

        /// Sets the `fileId`.
        ///
        /// id of the newly created file
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final Address fileId) {
            this.fileId = fileId;
            return this;
        }

        /// Creates the `FileCreateReceipt` of the values set.
        ///
        /// @return the new `FileCreateReceipt`
        /// @throws NullPointerException if a required value is not set
        public FileCreateReceipt build() {
            return new FileCreateReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    Objects.requireNonNull(fileId, "fileId must not be null"));
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof FileCreateReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && Objects.equals(fileId(), other.fileId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), fileId());
    }

    @Override
    public String toString() {
        return "FileCreateReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", fileId=" + fileId()
                + "]";
    }
}
