package org.hiero.consensusnode.transactions;

import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.internal.ClientRuntime;
import org.hiero.consensusnode.client.internal.TransactionKind;
import org.hiero.consensusnode.client.internal.TransactionKinds;
import org.hiero.ledger.TransactionId;

/// Factory methods of the package `org.hiero.consensusnode.transactions`.
public final class TransactionsFactory {

    /// Reconstructs a client-bound Response for a transaction that was submitted elsewhere — e.g. the
    /// inner transaction of a schedule (identified by ScheduleCreateReceipt.scheduledTransactionId),
    /// which executes on the network without the caller ever holding a Response for it.
    ///
    /// `transactionType` is the concrete transaction type (e.g. TransferTransaction); it determines the
    /// receipt type, so the returned Response is typed. This call makes no network request — querying
    /// happens lazily through the returned Response's queryReceipt() / queryRecord(), exactly as for a
    /// Response from submit().
    public static <ReceiptT extends Receipt> Response<ReceiptT> getResponse(final TransactionId transactionId, final Class<? extends Transaction<ReceiptT, ?>> transactionType, final HieroClient<?> client) {
        final TransactionKind<?, ?> kind = TransactionKinds.of(transactionType);
        if (kind == null) {
            throw new IllegalArgumentException("No transaction support is registered for "
                    + transactionType.getName());
        }
        ClientRuntime.of(client).track(transactionId, kind);
        return new Response<>(transactionId);
    }

    private TransactionsFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
