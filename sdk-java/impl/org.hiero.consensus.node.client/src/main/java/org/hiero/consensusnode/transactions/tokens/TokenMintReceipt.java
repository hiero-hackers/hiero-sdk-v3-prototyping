package org.hiero.consensusnode.transactions.tokens;

import java.util.Objects;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

public class TokenMintReceipt extends Receipt {

    private final long totalSupply;

    /// Creates a new `TokenMintReceipt`.
    public TokenMintReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final long totalSupply) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        this.totalSupply = totalSupply;
    }

    /// the token's new totalSupply after this mint
    public long totalSupply() {
        return totalSupply;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof TokenMintReceipt other
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
        return "TokenMintReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", totalSupply=" + totalSupply()
                + "]";
    }
}
