package org.hiero.consensusnode.transactions;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.internal.ClientRuntime;
import org.hiero.consensusnode.client.internal.Querying;
import org.hiero.consensusnode.client.internal.Receipts;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.consensusnode.queries.receipts.TransactionReceiptQuery;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// The acknowledgment of a submitted transaction: what is needed to read its outcome. A plain value that can be stored
/// and used in another process. Create one yourself from a transaction id and the transaction's type to read the
/// outcome of a transaction you did not submit, e.g. the execution of a schedule.
///
/// @param transactionId the id of the transaction
/// @param transactionType the type of the transaction; types the receipt
/// @param nodeAccountId the node the transaction was sent to; null: any node
public record Response<ReceiptT extends Receipt>(
        TransactionId transactionId,
        Class<? extends Transaction<ReceiptT, ?>> transactionType,
        @Nullable AccountId nodeAccountId) {

    /// Creates a new `Response`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Response {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(transactionType, "transactionType must not be null");
    }

    /// Creates a new `Response` with the default value of `nodeAccountId`.
    public Response(
            final TransactionId transactionId,
            final Class<? extends Transaction<ReceiptT, ?>> transactionType) {
        this(transactionId, transactionType, null);
    }

    /// Waits for the receipt of the transaction and returns it. The receipt is read from `nodeAccountId`, or from any
    /// node of the client when it is null; it is available for about three minutes after consensus.
    public CompletionStage<ReceiptT> queryReceipt(final HieroClient<?> client) {
        Objects.requireNonNull(client, "client must not be null");
        return ClientRuntime.of(client).queryReceipt(new TransactionReceiptQuery(transactionId, nodeAccountId),
                        nodeAccountId, transactionId, false, false)
                .thenApply(answer -> Receipts.create(transactionType, transactionId, answer.value().getReceipt()));
    }

    /// Reads the record of the transaction, once it has reached consensus. The client's operator pays for the query.
    public CompletionStage<Record<ReceiptT>> queryRecord(final HieroClient<?> client) {
        Objects.requireNonNull(client, "client must not be null");
        // the record exists once the receipt is final
        return queryReceipt(client).thenCompose(receipt -> Querying.paid(Receipts.recordRequest(transactionId),
                transactionId, null,
                Receipts.recordQuery(transactionType), client)).thenApply(QueryResponse::value);
    }
}
