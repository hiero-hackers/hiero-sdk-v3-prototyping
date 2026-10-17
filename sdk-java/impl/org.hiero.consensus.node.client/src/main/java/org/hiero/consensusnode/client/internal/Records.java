package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.spi.ReceiptHeader;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;
import org.hiero.ledger.TransactionId;

/// The part of receipts and records that is the same for every transaction type, read by the SDK: a
/// `TransactionSupport` only adds the fields of its type (see the Design Notes of `transactions-spi.md`).
public final class Records {

    private Records() {
    }

    /// The values every receipt holds. A receipt message carries no transaction id, so it is passed in.
    ///
    /// @param transactionId the id of the transaction the receipt belongs to
    /// @param receipt       the receipt message
    /// @return the header
    public static ReceiptHeader header(final TransactionId transactionId, final TransactionReceipt receipt) {
        return new ReceiptHeader(transactionId, HapiTransactionStatus.ofCode(receipt.getStatusValue()),
                Protobuf.currentRate(receipt.getExchangeRate()), Protobuf.nextRate(receipt.getExchangeRate()));
    }

    /// Builds the record of a transaction: the transaction id and the timestamps from the record message, the typed
    /// receipt from the support of the transaction type.
    ///
    /// @param support the support of the transaction type
    /// @param record  the record message
    /// @param <R> the receipt type
    /// @return the record
    public static <R extends Receipt> Record<R> record(final TransactionSupport<R, ?> support,
                                                       final TransactionRecord record) {
        final TransactionId id = Protobuf.fromProto(record.getTransactionID());
        final R receipt = support.createReceipt(header(id, record.getReceipt()), record);
        return new Record<>(id, Protobuf.fromProto(record.getConsensusTimestamp()), receipt,
                record.hasParentConsensusTimestamp() ? Protobuf.fromProto(record.getParentConsensusTimestamp())
                        : null);
    }
}
