package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
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
/// @param selects  whether a body of the field is one of this type - needed where two types share a field (the
///                 fungible and the NFT form of mint, burn and wipe)
/// @param <R> the receipt type
/// @param <T> the transaction type
public record TransactionKind<R extends Receipt, T extends Transaction<R, T>>(
        Class<T> type, String service, String method, TransactionBody.DataCase dataCase,
        BiConsumer<T, TransactionBody.Builder> body, ReceiptFactory<R> receipts, Predicate<TransactionBody> selects) {

    /// Creates the kind of a type that is the only one to fill its field of the transaction body.
    ///
    /// @param type     the transaction type
    /// @param service  the full gRPC service name
    /// @param method   the gRPC method name
    /// @param dataCase the field of the transaction body
    /// @param body     fills the field of the transaction body from a transaction
    /// @param receipts builds the receipt
    public TransactionKind(final Class<T> type, final String service, final String method,
                           final TransactionBody.DataCase dataCase, final BiConsumer<T, TransactionBody.Builder> body,
                           final ReceiptFactory<R> receipts) {
        this(type, service, method, dataCase, body, receipts, any -> true);
    }

    /// The gRPC method that submits a transaction of this type.
    ///
    /// @return the method descriptor
    public io.grpc.MethodDescriptor<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse> grpcMethod() {
        return Grpc.unary(service, method, com.hederahashgraph.api.proto.java.Transaction.getDefaultInstance(),
                TransactionResponse.getDefaultInstance());
    }

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
