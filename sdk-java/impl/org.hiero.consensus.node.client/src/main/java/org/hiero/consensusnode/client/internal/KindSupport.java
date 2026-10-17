package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;
import org.hiero.grpc.MethodDescriptor;
import org.hiero.ledger.TransactionId;

/// The `TransactionSupport` of a transaction type the implementation knows. The administration client registers its
/// types with subclasses of it (a provider of the `ServiceLoader` needs a public constructor without parameters).
///
/// The SPI cannot be implemented completely: a `TransactionResponse` and a `TransactionReceipt` carry no transaction
/// id, but `Response` and every receipt need one (see the `## Questions & Comments` of `transactions-spi.md`), and a
/// transaction cannot be read back from its body yet.
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
    public Response<R> create(final TransactionResponse protoResponse) {
        throw new UnsupportedOperationException("A TransactionResponse carries no transaction id, which a Response"
                + " needs");
    }

    @Override
    public R create(final TransactionReceipt protoReceipt) {
        throw new UnsupportedOperationException("A TransactionReceipt carries no transaction id, which a "
                + kind.type().getSimpleName() + " receipt needs; use create(TransactionRecord)");
    }

    @Override
    public Record<R> create(final TransactionRecord protoRecord) {
        final TransactionReceipt receipt = protoRecord.getReceipt();
        final TransactionId id = Protobuf.fromProto(protoRecord.getTransactionID());
        final R typed = kind.receipts().create(id, HapiTransactionStatus.ofCode(receipt.getStatusValue()),
                Protobuf.currentRate(receipt.getExchangeRate()), Protobuf.nextRate(receipt.getExchangeRate()),
                receipt);
        return new Record<>(id, Protobuf.fromProto(protoRecord.getConsensusTimestamp()), typed,
                protoRecord.hasParentConsensusTimestamp()
                        ? Protobuf.fromProto(protoRecord.getParentConsensusTimestamp()) : null);
    }

    /// The gRPC method of a transaction type.
    private static final class Method extends MethodDescriptor {

        Method(final String serviceName, final String methodName) {
            super(serviceName, methodName);
        }
    }
}
