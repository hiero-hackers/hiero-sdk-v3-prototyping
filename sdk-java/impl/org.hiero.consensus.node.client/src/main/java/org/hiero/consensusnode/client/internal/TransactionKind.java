package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import java.util.function.BiConsumer;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;

/// What the implementation knows about one transaction type: the gRPC method that submits it, the field of the
/// `TransactionBody` it fills, and how its receipt is built.
///
/// @param type     the transaction type
/// @param service  the full gRPC service name, e.g. `proto.CryptoService`
/// @param method   the gRPC method name, e.g. `createAccount`
/// @param dataCase the field of the transaction body
/// @param body     fills the field of the transaction body from a transaction
/// @param receipts builds the receipt
/// @param <R> the receipt type
/// @param <T> the transaction type
public record TransactionKind<R extends Receipt, T extends Transaction<R, T>>(
        Class<T> type, String service, String method, TransactionBody.DataCase dataCase,
        BiConsumer<T, TransactionBody.Builder> body, ReceiptFactory<R> receipts) {

    /// Builds the typed receipt of a transaction from the receipt the node returned.
    ///
    /// @param <R> the receipt type
    @FunctionalInterface
    public interface ReceiptFactory<R extends Receipt> {

        /// Builds the receipt.
        ///
        /// @param transactionId    the id of the transaction
        /// @param status           its status
        /// @param exchangeRate     the current exchange rate
        /// @param nextExchangeRate the next exchange rate
        /// @param receipt          the receipt message
        /// @return the receipt
        R create(TransactionId transactionId, TransactionStatus status, ExchangeRate exchangeRate,
                 ExchangeRate nextExchangeRate, TransactionReceipt receipt);
    }
}
