package org.hiero.consensusnode.transactions.spi;

import java.util.Objects;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;

/// The values every receipt holds, read by the SDK: the receipt message itself carries no transaction id.
///
/// @param transactionId the id of the transaction the receipt belongs to
/// @param status the status of the transaction
/// @param exchangeRate the exchange rate at the time of the transaction
/// @param nextExchangeRate the next exchange rate
public record ReceiptHeader(
        TransactionId transactionId,
        TransactionStatus status,
        ExchangeRate exchangeRate,
        ExchangeRate nextExchangeRate) {

    /// Creates a new `ReceiptHeader`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ReceiptHeader {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(exchangeRate, "exchangeRate must not be null");
        Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null");
    }
}
