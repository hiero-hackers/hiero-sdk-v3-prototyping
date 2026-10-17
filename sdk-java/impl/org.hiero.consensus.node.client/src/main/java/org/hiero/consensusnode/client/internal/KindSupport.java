package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.spi.ReceiptHeader;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;
import org.hiero.grpc.MethodDescriptor;

/// The `TransactionSupport` of a transaction type the implementation knows. The administration client registers its
/// types with subclasses of it (a provider of the `ServiceLoader` needs a public constructor without parameters).
///
/// The SDK reads the values every receipt holds (`ReceiptHeader`) and builds `Record` and `Response` itself; a support
/// only adds the fields of its type. A transaction cannot be read back from its body yet (see the
/// `## Questions & Comments` of `transactions-spi.md`).
///
/// @param <R> the receipt type
/// @param <T> the transaction type
public class KindSupport<R extends Receipt, T extends Transaction<R, T>> implements TransactionSupport<R, T> {

    private final TransactionKind<R, T> kind;

    /// Creates the support of a transaction type.
    ///
    /// @param kind the transaction type
    public KindSupport(final TransactionKind<R, T> kind) {
        this.kind = kind;
    }

    /// The transaction type.
    ///
    /// @return the kind
    public TransactionKind<R, T> kind() {
        return kind;
    }

    @Override
    public Class<?> getTransactionType() {
        return kind.type();
    }

    @Override
    public MethodDescriptor getMethodDescriptor() {
        return new Method(kind.service(), kind.method());
    }

    @Override
    public TransactionBody updateBody(final T transaction, final TransactionBody protoBody) {
        final TransactionBody.Builder body = protoBody.toBuilder();
        kind.body().accept(transaction, body);
        return body.build();
    }

    @Override
    public T create(final TransactionBody protoBody) {
        throw new UnsupportedOperationException("A " + kind.type().getSimpleName()
                + " cannot be read back from its body yet");
    }

    @Override
    public R createReceipt(final ReceiptHeader header, final TransactionReceipt protoReceipt) {
        return kind.receipts().create(header.transactionId(), header.status(), header.exchangeRate(),
                header.nextExchangeRate(), protoReceipt);
    }

    @Override
    public R createReceipt(final ReceiptHeader header, final TransactionRecord protoRecord) {
        final TransactionKind.RecordReceiptFactory<R> recordReceipts = kind.recordReceipts();
        return recordReceipts != null ? recordReceipts.create(header, protoRecord)
                : createReceipt(header, protoRecord.getReceipt());
    }

    /// The gRPC method of a transaction type.
    private static final class Method extends MethodDescriptor {

        Method(final String serviceName, final String methodName) {
            super(serviceName, methodName);
        }
    }
}
