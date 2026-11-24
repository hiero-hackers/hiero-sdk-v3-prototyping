package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.spi.ReceiptHeader;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.NativeTokenUnit;

/// The part of receipts and records that is the same for every transaction type, read by the SDK: a
/// `TransactionSupport` only adds the fields of its type (see the Design Notes of `transactions-spi.md`).
public final class Records {

    private Records() {
    }

    /// The values every receipt holds. A receipt message carries no transaction id, so it is passed in.
    ///
    /// @param transactionId the id of the transaction the receipt belongs to
    /// @param receipt       the receipt message
    /// @param unit          the native token unit of the network, the unit of the exchange rates
    /// @return the header
    public static ReceiptHeader header(final TransactionId transactionId, final TransactionReceipt receipt,
                                       final NativeTokenUnit unit) {
        return new ReceiptHeader(transactionId, Statuses.ofCode(receipt.getStatusValue()),
                Protobuf.currentRate(receipt.getExchangeRate(), unit),
                Protobuf.nextRate(receipt.getExchangeRate(), unit));
    }

    /// Builds the record of a transaction: the transaction id, the timestamps and the result of a contract execution
    /// from the record message, the typed receipt from the support of the transaction type.
    ///
    /// @param support the support of the transaction type
    /// @param record  the record message
    /// @param unit    the native token unit of the network, the unit of the exchange rates
    /// @param <R> the receipt type
    /// @return the record
    public static <R extends Receipt> Record<R> record(final TransactionSupport<R, ?> support,
                                                       final TransactionRecord record, final NativeTokenUnit unit) {
        final TransactionId id = Protobuf.fromProto(record.getTransactionID());
        final R receipt = support.createReceipt(header(id, record.getReceipt(), unit), record);
        return new Record<>(id, Protobuf.fromProto(record.getConsensusTimestamp()), receipt,
                record.hasParentConsensusTimestamp() ? Protobuf.fromProto(record.getParentConsensusTimestamp())
                        : null, ContractKinds.contractResult(record));
    }
}
