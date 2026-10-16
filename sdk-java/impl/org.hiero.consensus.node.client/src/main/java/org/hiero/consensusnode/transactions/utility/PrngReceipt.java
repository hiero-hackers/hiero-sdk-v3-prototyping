package org.hiero.consensusnode.transactions.utility;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

/// Carries the generated pseudorandom value. Exactly one field is set, matching the input mode of
/// the PrngTransaction that produced it.
public final class PrngReceipt extends Receipt {

    private final @Nullable Integer prngNumber;
    private final byte @Nullable [] prngBytes;

    /// Creates a new `PrngReceipt`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public PrngReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final @Nullable Integer prngNumber,
            final byte @Nullable [] prngBytes) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        this.prngNumber = prngNumber;
        this.prngBytes = prngBytes == null ? null : prngBytes.clone();
        if ((prngNumber != null ? 1 : 0) + (prngBytes != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of prngNumber, prngBytes must be set");
        }
    }

    /// set when range was provided; uniform in [0, range)
    public @Nullable Integer prngNumber() {
        return prngNumber;
    }

    /// set when range was unset; 384 bits (48 bytes) of randomness
    public byte @Nullable [] prngBytes() {
        return prngBytes == null ? null : prngBytes.clone();
    }

    /// Returns a builder for a new `PrngReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `PrngReceipt`.
    ///
    /// `build()` creates the `PrngReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable Integer prngNumber;
        private byte @Nullable [] prngBytes;

        private Builder() {
            // created by PrngReceipt.builder()
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

        /// Sets the `prngNumber`.
        ///
        /// set when range was provided; uniform in [0, range)
        ///
        /// @param prngNumber the value
        /// @return this builder
        public Builder prngNumber(final @Nullable Integer prngNumber) {
            this.prngNumber = prngNumber;
            return this;
        }

        /// Sets the `prngBytes`.
        ///
        /// set when range was unset; 384 bits (48 bytes) of randomness
        ///
        /// @param prngBytes the value
        /// @return this builder
        public Builder prngBytes(final byte @Nullable [] prngBytes) {
            this.prngBytes = prngBytes;
            return this;
        }

        /// Creates the `PrngReceipt` of the values set.
        ///
        /// @return the new `PrngReceipt`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public PrngReceipt build() {
            return new PrngReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    prngNumber,
                    prngBytes);
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof PrngReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && Objects.equals(prngNumber(), other.prngNumber())
            && Arrays.equals(prngBytes(), other.prngBytes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), prngNumber(), Arrays.hashCode(prngBytes()));
    }

    @Override
    public String toString() {
        return "PrngReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", prngNumber=" + prngNumber()
                + ", prngBytes=" + (prngBytes() == null ? "null" : "byte[" + prngBytes().length + "]")
                + "]";
    }
}
