package org.hiero.consensusnode.transactions.tokens;

import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

/// The receipt of an NFT mint: it also carries the serial numbers the network assigned.
public final class TokenMintNftReceipt extends TokenMintReceipt {

    private final List<Long> serials;

    /// Creates a new `TokenMintNftReceipt`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenMintNftReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final long totalSupply,
            final List<Long> serials) {
        super(transactionId, status, exchangeRate, nextExchangeRate, totalSupply);
        Objects.requireNonNull(serials, "serials must not be null");
        this.serials = List.copyOf(serials);
    }

    /// Creates a new `TokenMintNftReceipt` with the default value of `serials`.
    public TokenMintNftReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final long totalSupply) {
        this(transactionId, status, exchangeRate, nextExchangeRate, totalSupply, List.of());
    }

    /// the serial numbers of the newly minted NFTs
    public List<Long> serials() {
        return serials;
    }

    /// Returns a builder for a new `TokenMintNftReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenMintNftReceipt`.
    ///
    /// `build()` creates the `TokenMintNftReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable Long totalSupply;
        private List<Long> serials = List.of();

        private Builder() {
            // created by TokenMintNftReceipt.builder()
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
        /// the token's new totalSupply after this mint
        ///
        /// @param totalSupply the value
        /// @return this builder
        public Builder totalSupply(final long totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        /// Sets the `serials`.
        ///
        /// the serial numbers of the newly minted NFTs
        ///
        /// @param serials the value
        /// @return this builder
        public Builder serials(final List<Long> serials) {
            this.serials = serials;
            return this;
        }

        /// Creates the `TokenMintNftReceipt` of the values set.
        ///
        /// @return the new `TokenMintNftReceipt`
        /// @throws NullPointerException if a required value is not set
        public TokenMintNftReceipt build() {
            return new TokenMintNftReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    Objects.requireNonNull(totalSupply, "totalSupply must not be null"),
                    serials);
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof TokenMintNftReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && totalSupply() == other.totalSupply()
            && Objects.equals(serials(), other.serials());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), totalSupply(), serials());
    }

    @Override
    public String toString() {
        return "TokenMintNftReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", totalSupply=" + totalSupply()
                + ", serials=" + serials()
                + "]";
    }
}
