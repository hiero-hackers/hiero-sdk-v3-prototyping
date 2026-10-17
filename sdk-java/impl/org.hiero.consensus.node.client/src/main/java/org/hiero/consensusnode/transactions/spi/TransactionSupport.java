package org.hiero.consensusnode.transactions.spi;

import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.grpc.MethodDescriptor;

/// Adds support for one concrete transaction type: converts between the transaction and its result
/// types and their protobuf representations.
public interface TransactionSupport<ReceiptT extends Receipt, TransactionT extends Transaction<ReceiptT, TransactionT>> {

    /// the concrete transaction type this support handles
    Class<?> getTransactionType();

    /// the gRPC method used to submit the transaction
    MethodDescriptor getMethodDescriptor();

    /// updates the proto TransactionBody with the fields from the Transaction
    TransactionBody updateBody(final TransactionT transaction, final TransactionBody protoBody);

    /// creates a Transaction from a proto TransactionBody
    TransactionT create(final TransactionBody protoBody);

    /// Creates the typed receipt of a receipt query: the values of the header plus the fields of this transaction
    /// type in the receipt message.
    ReceiptT createReceipt(final ReceiptHeader header, final TransactionReceipt protoReceipt);

    /// Creates the typed receipt of a record: the values of the header plus the fields of this transaction type in
    /// the record, which can hold more than its embedded receipt.
    ReceiptT createReceipt(final ReceiptHeader header, final TransactionRecord protoRecord);
}
