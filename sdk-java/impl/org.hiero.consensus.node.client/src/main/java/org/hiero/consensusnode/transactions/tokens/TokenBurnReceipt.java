package org.hiero.consensusnode.transactions.tokens;

import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

public final class TokenBurnReceipt extends Receipt {

    private final long totalSupply;

    /// Creates a new `TokenBurnReceipt`.
    public TokenBurnReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final long totalSupply) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        this.totalSupply = totalSupply;
    }

    /// the token's new totalSupply after this burn
    public long totalSupply() {
        return totalSupply;
    }

    /// Returns a builder for a new `TokenBurnReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenBurnReceipt`.
    ///
    /// `build()` creates the `TokenBurnReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable Long totalSupply;

        private Builder() {
            // created by TokenBurnReceipt.builder()
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

        /// Sets the `totalSupply`.
        ///
        /// the token's new totalSupply after this burn
        ///
        /// @param totalSupply the value
        /// @return this builder
        public Builder totalSupply(final long totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        /// Creates the `TokenBurnReceipt` of the values set.
        ///
        /// @return the new `TokenBurnReceipt`
        /// @throws NullPointerException if a required value is not set
        public TokenBurnReceipt build() {
            return new TokenBurnReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    Objects.requireNonNull(totalSupply, "totalSupply must not be null"));
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof TokenBurnReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && totalSupply() == other.totalSupply();
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), totalSupply());
    }

    @Override
    public String toString() {
        return "TokenBurnReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", totalSupply=" + totalSupply()
                + "]";
    }
}
