package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.UInt64Value;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptResponse;
import com.hederahashgraph.api.proto.java.TransactionGetRecordQuery;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.hiero.consensusnode.queries.receipts.TransactionReceipts;
import org.hiero.consensusnode.queries.receipts.UntypedReceipt;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.spi.ReceiptHeader;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// Receipts read by their transaction id: the untyped receipts of the protocol message, and the typed receipt of a
/// transaction type through its `TransactionSupport`.
///
/// The protocol's `TransactionReceipt` has no presence for its numbers (proto3 scalars): a number that is `0` is
/// read as absent. Entity ids are messages and keep their presence.
public final class Receipts {

    private Receipts() {
    }

    /// The answer of a receipt query as untyped receipts. The protocol message names no transaction id; every
    /// receipt gets the id that was queried, as the current SDKs do.
    ///
    /// @param transactionId the queried id
    /// @param answer        the answer of the node
    /// @return the receipts
    public static TransactionReceipts receipts(final TransactionId transactionId,
                                               final TransactionGetReceiptResponse answer) {
        return new TransactionReceipts(untyped(transactionId, answer.getReceipt()),
                answer.getDuplicateTransactionReceiptsList().stream().map(r -> untyped(transactionId, r)).toList(),
                answer.getChildTransactionReceiptsList().stream().map(r -> untyped(transactionId, r)).toList());
    }

    /// The untyped receipt of a protocol message.
    ///
    /// @param transactionId the id of the transaction
    /// @param receipt       the receipt message
    /// @return the untyped receipt
    public static UntypedReceipt untyped(final TransactionId transactionId, final TransactionReceipt receipt) {
        final ReceiptHeader header = Records.header(transactionId, receipt);
        return new UntypedReceipt(transactionId, header.status(), header.exchangeRate(), header.nextExchangeRate(),
                receipt.hasAccountID() ? Protobuf.fromProto(receipt.getAccountID()) : null,
                receipt.hasFileID() ? Protobuf.fromProto(receipt.getFileID()) : null,
                receipt.hasContractID() ? Protobuf.fromProto(receipt.getContractID()) : null,
                receipt.hasTopicID() ? Protobuf.fromProto(receipt.getTopicID()) : null,
                number(receipt.getTopicSequenceNumber()),
                receipt.getTopicRunningHash().isEmpty() ? null : receipt.getTopicRunningHash().toByteArray(),
                number(receipt.getTopicRunningHashVersion()),
                receipt.hasTokenID() ? Protobuf.fromProto(receipt.getTokenID()) : null,
                number(receipt.getNewTotalSupply()),
                receipt.hasScheduleID() ? Protobuf.fromProto(receipt.getScheduleID()) : null,
                receipt.hasScheduledTransactionID() ? Protobuf.fromProto(receipt.getScheduledTransactionID()) : null,
                List.copyOf(receipt.getSerialNumbersList()),
                number(receipt.getNodeId()),
                number(receipt.getRegisteredNodeId()),
                receipt.hasBlockNumber() ? receipt.getBlockNumber().getValue() : null);
    }

    /// The protocol message of an untyped receipt, the inverse of [#untyped]. The exchange rates are not written:
    /// a `TransactionSupport` takes them from the `ReceiptHeader`.
    ///
    /// @param receipt the untyped receipt
    /// @return the receipt message
    public static TransactionReceipt toProto(final UntypedReceipt receipt) {
        final TransactionReceipt.Builder proto = TransactionReceipt.newBuilder()
                .setStatusValue(receipt.status().code());
        if (receipt.accountId() != null) {
            proto.setAccountID(Protobuf.toProto(receipt.accountId()));
        }
        if (receipt.fileId() != null) {
            proto.setFileID(Protobuf.toFileId(receipt.fileId()));
        }
        if (receipt.contractId() != null) {
            proto.setContractID(Protobuf.toProto(receipt.contractId()));
        }
        if (receipt.topicId() != null) {
            proto.setTopicID(Protobuf.toTopicId(receipt.topicId()));
        }
        if (receipt.topicSequenceNumber() != null) {
            proto.setTopicSequenceNumber(receipt.topicSequenceNumber());
        }
        if (receipt.topicRunningHash() != null) {
            proto.setTopicRunningHash(ByteString.copyFrom(receipt.topicRunningHash()));
        }
        if (receipt.topicRunningHashVersion() != null) {
            proto.setTopicRunningHashVersion(receipt.topicRunningHashVersion());
        }
        if (receipt.tokenId() != null) {
            proto.setTokenID(Protobuf.toTokenId(receipt.tokenId()));
        }
        if (receipt.totalSupply() != null) {
            proto.setNewTotalSupply(receipt.totalSupply());
        }
        if (receipt.scheduleId() != null) {
            proto.setScheduleID(Protobuf.toScheduleId(receipt.scheduleId()));
        }
        if (receipt.scheduledTransactionId() != null) {
            proto.setScheduledTransactionID(Protobuf.toProto(receipt.scheduledTransactionId()));
        }
        proto.addAllSerialNumbers(receipt.serials());
        if (receipt.nodeId() != null) {
            proto.setNodeId(receipt.nodeId());
        }
        if (receipt.registeredNodeId() != null) {
            proto.setRegisteredNodeId(receipt.registeredNodeId());
        }
        if (receipt.blockNumber() != null) {
            proto.setBlockNumber(UInt64Value.of(receipt.blockNumber()));
        }
        return proto.build();
    }

    /// The receipt of a transaction type, created by its `TransactionSupport` as the receipt of a submitted
    /// transaction is: a failed transaction gets the receipt the support creates for its status.
    ///
    /// @param transactionType the transaction type
    /// @param transactionId   the id of the transaction
    /// @param receipt         the receipt message
    /// @param <R> the receipt type
    /// @return the typed receipt
    public static <R extends Receipt> R create(final Class<? extends Transaction<R, ?>> transactionType,
                                               final TransactionId transactionId, final TransactionReceipt receipt) {
        return support(transactionType).createReceipt(Records.header(transactionId, receipt), receipt);
    }

    /// `UntypedReceipt.typed`: the receipt of a transaction type. Fails when the untyped receipt lacks an attribute
    /// the typed receipt requires - an attribute of the typed receipt that is not nullable, has an object type and is
    /// absent in the untyped receipt. Numbers are not checked: the protocol cannot tell an absent number from `0`.
    ///
    /// @param receipt         the untyped receipt
    /// @param transactionType the transaction type
    /// @param <R> the receipt type
    /// @return the typed receipt
    /// @throws IllegalArgumentException if the receipt lacks an attribute the typed receipt requires
    public static <R extends Receipt> R typed(final UntypedReceipt receipt,
                                              final Class<? extends Transaction<R, ?>> transactionType) {
        final ReceiptHeader header = new ReceiptHeader(receipt.transactionId(), receipt.status(),
                receipt.exchangeRate(), receipt.nextExchangeRate());
        final R typed = support(transactionType).createReceipt(header, toProto(receipt));
        for (final Method attribute : typed.getClass().getMethods()) {
            if (attribute.getDeclaringClass() == Receipt.class || attribute.getDeclaringClass() == Object.class
                    || attribute.getParameterCount() != 0 || Modifier.isStatic(attribute.getModifiers())
                    || attribute.getReturnType().isPrimitive()
                    || attribute.getAnnotatedReturnType().isAnnotationPresent(Nullable.class)) {
                continue;
            }
            if (absent(receipt, attribute.getName())) {
                throw new IllegalArgumentException("The receipt has no " + attribute.getName() + ", which a "
                        + typed.getClass().getSimpleName() + " requires: it is not the receipt of a successful "
                        + transactionType.getSimpleName());
            }
        }
        return typed;
    }

    /// The query of the record of a transaction (`CryptoService.getTxRecordByTxID`, paid), read with the support of
    /// its type.
    ///
    /// @param transactionType the transaction type
    /// @param <R> the receipt type
    /// @return the query type
    public static <R extends Receipt> QueryKind<TransactionId, Record<R>> recordQuery(
            final Class<? extends Transaction<R, ?>> transactionType) {
        final TransactionSupport<R, ?> support = support(transactionType);
        return new QueryKind<>("proto.CryptoService", "getTxRecordByTxID",
                (transactionId, header) -> Query.newBuilder().setTransactionGetRecord(TransactionGetRecordQuery
                        .newBuilder().setHeader(header).setTransactionID(Protobuf.toProto(transactionId))).build(),
                response -> response.getTransactionGetRecord().getHeader(),
                (transactionId, response) -> Records.record(support,
                        response.getTransactionGetRecord().getTransactionRecord()));
    }

    @SuppressWarnings("unchecked")
    private static <R extends Receipt> TransactionSupport<R, ?> support(
            final Class<? extends Transaction<R, ?>> transactionType) {
        return (TransactionSupport<R, ?>) SpiFactory.getTransactionSupport(transactionType);
    }

    /// Whether the untyped receipt has no value for an attribute of the same name; an attribute it does not have
    /// counts as present (the typed receipt computes it).
    private static boolean absent(final UntypedReceipt receipt, final String name) {
        try {
            final Method attribute = UntypedReceipt.class.getMethod(name);
            return attribute.getDeclaringClass() == UntypedReceipt.class && attribute.invoke(receipt) == null;
        } catch (final NoSuchMethodException unknown) {
            return false;
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read " + name + " of an untyped receipt", e);
        }
    }

    private static @Nullable Long number(final long value) {
        return value == 0L ? null : value;
    }
}
