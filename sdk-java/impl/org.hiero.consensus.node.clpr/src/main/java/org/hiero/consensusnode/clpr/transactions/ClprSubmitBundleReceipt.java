package org.hiero.consensusnode.clpr.transactions;

import java.util.Objects;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

/// Receipt of a `ClprSubmitBundleTransaction`.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public final class ClprSubmitBundleReceipt extends Receipt {

    /// Creates a new `ClprSubmitBundleReceipt`.
    public ClprSubmitBundleReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof ClprSubmitBundleReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate());
    }

    @Override
    public String toString() {
        return "ClprSubmitBundleReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + "]";
    }
}
