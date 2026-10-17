package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TransactionList;
import com.hederahashgraph.api.proto.java.AtomicBatchTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import com.hederahashgraph.api.proto.java.UtilPrngTransactionBody;
import java.util.List;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.batch.BatchReceipt;
import org.hiero.consensusnode.transactions.batch.BatchTransaction;
import org.hiero.consensusnode.transactions.utility.PrngReceipt;
import org.hiero.consensusnode.transactions.utility.PrngTransaction;

/// The transaction types of the utility service (PRNG) and atomic batches.
public final class UtilityKinds {

    /// `PrngTransaction`: `UtilService.prng`. The random number or bytes are in the record, not the receipt.
    public static final TransactionKind<PrngReceipt, PrngTransaction> PRNG = new TransactionKind<>(
            PrngTransaction.class, "proto.UtilService", "prng", TransactionBody.DataCase.UTIL_PRNG,
            (transaction, body) -> {
                final UtilPrngTransactionBody.Builder prng = UtilPrngTransactionBody.newBuilder();
                final Integer range = transaction.range();
                if (range != null) {
                    prng.setRange(range);
                }
                body.setUtilPrng(prng);
            },
            // the network returns the random value only in the record (HIP-351), not in the receipt
            (id, status, rate, nextRate, receipt) -> new PrngReceipt(id, status, rate, nextRate, null, null),
            any -> true,
            (header, record) -> new PrngReceipt(header.transactionId(), header.status(), header.exchangeRate(),
                    header.nextExchangeRate(),
                    record.getEntropyCase() == TransactionRecord.EntropyCase.PRNG_NUMBER ? record.getPrngNumber() : null,
                    record.getEntropyCase() == TransactionRecord.EntropyCase.PRNG_BYTES
                            ? record.getPrngBytes().toByteArray() : null));

    /// `BatchTransaction`: `UtilService.atomicBatch`.
    public static final TransactionKind<BatchReceipt, BatchTransaction> BATCH = new TransactionKind<>(
            BatchTransaction.class, "proto.UtilService", "atomicBatch", TransactionBody.DataCase.ATOMIC_BATCH,
            UtilityKinds::batch,
            (id, status, rate, nextRate, receipt) -> new BatchReceipt(id, status, rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(PRNG, BATCH);

    private UtilityKinds() {
    }

    /// The body of a batch: the signed bytes (`SignedTransaction`) of every inner transaction, in their order.
    private static void batch(final BatchTransaction transaction, final TransactionBody.Builder body) {
        final AtomicBatchTransactionBody.Builder batch = AtomicBatchTransactionBody.newBuilder();
        for (final PackedTransaction<?, ?> inner : transaction.innerTransactions()) {
            batch.addTransactions(signedTransactionBytes(inner));
        }
        body.setAtomicBatch(batch);
    }

    /// The signed bytes of an inner transaction: its only `SignedTransaction`, read from its byte form.
    ///
    /// @param inner the inner transaction
    /// @return the bytes of the `SignedTransaction`
    /// @throws IllegalArgumentException if the transaction has more than one body, i.e. was not packed for a batch
    private static ByteString signedTransactionBytes(final PackedTransaction<?, ?> inner) {
        final TransactionList list;
        try {
            list = TransactionList.parseFrom(inner.toBytes());
        } catch (final InvalidProtocolBufferException invalid) {
            throw new IllegalArgumentException("The inner transaction " + inner.transactionId()
                    + " has no valid byte form", invalid);
        }
        if (list.getTransactionListCount() != 1) {
            throw new IllegalArgumentException("The inner transaction " + inner.transactionId() + " has "
                    + list.getTransactionListCount() + " bodies; an inner transaction of a batch has exactly one"
                    + " (pack it with packForBatch or signForBatch)");
        }
        return list.getTransactionList(0).getSignedTransactionBytes();
    }
}
