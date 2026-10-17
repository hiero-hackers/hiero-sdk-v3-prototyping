package org.hiero.consensusnode.transactions;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.internal.ClientRuntime;
import org.hiero.ledger.TransactionId;

/// @param transactionId the id of the transaction
public record Response<ReceiptT extends Receipt>(TransactionId transactionId) {

    /// Creates a new `Response`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Response {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
    }

    /// query for the receipt of the transaction
    public CompletionStage<ReceiptT> queryReceipt() {
        @SuppressWarnings("unchecked")
        final CompletionStage<ReceiptT> receipt = ClientRuntime.queryReceipt(transactionId)
                .thenApply(value -> (ReceiptT) value);
        return receipt;
    }

    /// query for the record of the transaction
    public CompletionStage<Record<ReceiptT>> queryRecord() {
        @SuppressWarnings("unchecked")
        final CompletionStage<Record<ReceiptT>> record = ClientRuntime.queryRecord(transactionId)
                .thenApply(value -> (Record<ReceiptT>) (Record<?>) value);
        return record;
    }
}
