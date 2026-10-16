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
