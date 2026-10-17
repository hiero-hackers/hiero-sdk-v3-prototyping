package org.hiero.consensusnode.client.internal;

import com.google.protobuf.Descriptors;
import com.hederahashgraph.api.proto.java.SchedulableTransactionBody;
import com.hederahashgraph.api.proto.java.ScheduleCreateTransactionBody;
import com.hederahashgraph.api.proto.java.ScheduleDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.ScheduleSignTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionID;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.schedule.ScheduleCreateReceipt;
import org.hiero.consensusnode.transactions.schedule.ScheduleCreateTransaction;
import org.hiero.consensusnode.transactions.schedule.ScheduleDeleteReceipt;
import org.hiero.consensusnode.transactions.schedule.ScheduleDeleteTransaction;
import org.hiero.consensusnode.transactions.schedule.ScheduleSignReceipt;
import org.hiero.consensusnode.transactions.schedule.ScheduleSignTransaction;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.NativeToken;

/// The transaction types of schedules.
public final class ScheduleKinds {

    /// The maximum fee of an inner transaction that sets none - the default fee of a packed transaction.
    private static final Hbar DEFAULT_SCHEDULED_TRANSACTION_FEE = new Hbar(2, HbarUnit.HBAR);

    /// The address of a schedule the receipt names none of (a failed create).
    private static final Address NO_SCHEDULE = new Address(0L, 0L, "", 0L);

    /// The transaction id a receipt names none of (a failed create or sign).
    private static final TransactionId NO_TRANSACTION = TransactionId.fromString("0.0.0@0.0");

    /// `ScheduleCreateTransaction`: `ScheduleService.createSchedule`.
    public static final TransactionKind<ScheduleCreateReceipt, ScheduleCreateTransaction> SCHEDULE_CREATE = new TransactionKind<>(
            ScheduleCreateTransaction.class, "proto.ScheduleService", "createSchedule", TransactionBody.DataCase.SCHEDULECREATE,
            ScheduleKinds::scheduleCreate,
            (id, status, rate, nextRate, receipt) -> new ScheduleCreateReceipt(id, status, rate, nextRate,
                    receipt.hasScheduleID() ? Protobuf.fromProto(receipt.getScheduleID()) : NO_SCHEDULE,
                    receipt.hasScheduledTransactionID() ? Protobuf.fromProto(receipt.getScheduledTransactionID())
                            : NO_TRANSACTION));

    /// `ScheduleSignTransaction`: `ScheduleService.signSchedule`.
    public static final TransactionKind<ScheduleSignReceipt, ScheduleSignTransaction> SCHEDULE_SIGN = new TransactionKind<>(
            ScheduleSignTransaction.class, "proto.ScheduleService", "signSchedule", TransactionBody.DataCase.SCHEDULESIGN,
            (transaction, body) -> body.setScheduleSign(ScheduleSignTransactionBody.newBuilder()
                    .setScheduleID(Protobuf.toScheduleId(transaction.scheduleId()))),
            (id, status, rate, nextRate, receipt) -> new ScheduleSignReceipt(id, status, rate, nextRate,
                    receipt.hasScheduledTransactionID() ? Protobuf.fromProto(receipt.getScheduledTransactionID())
                            : NO_TRANSACTION));

    /// `ScheduleDeleteTransaction`: `ScheduleService.deleteSchedule`.
    public static final TransactionKind<ScheduleDeleteReceipt, ScheduleDeleteTransaction> SCHEDULE_DELETE = new TransactionKind<>(
            ScheduleDeleteTransaction.class, "proto.ScheduleService", "deleteSchedule", TransactionBody.DataCase.SCHEDULEDELETE,
            (transaction, body) -> body.setScheduleDelete(ScheduleDeleteTransactionBody.newBuilder()
                    .setScheduleID(Protobuf.toScheduleId(transaction.scheduleId()))),
            (id, status, rate, nextRate, receipt) -> new ScheduleDeleteReceipt(id, status, rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(SCHEDULE_CREATE, SCHEDULE_SIGN, SCHEDULE_DELETE);

    private ScheduleKinds() {
    }

    private static void scheduleCreate(final ScheduleCreateTransaction transaction,
                                       final TransactionBody.Builder body) {
        final ScheduleCreateTransactionBody.Builder create = ScheduleCreateTransactionBody.newBuilder()
                .setScheduledTransactionBody(schedulable(transaction.scheduledTransaction(), body.getTransactionID()))
                .setWaitForExpiry(transaction.waitForExpiry());
        final Authority adminKey = transaction.adminKey();
        if (adminKey != null) {
            create.setAdminKey(Protobuf.toProto(adminKey));
        }
        final AccountId payerAccountId = transaction.payerAccountId();
        if (payerAccountId != null) {
            create.setPayerAccountID(Protobuf.toProto(payerAccountId));
        }
        final String memo = transaction.scheduleMemo();
        if (memo != null) {
            create.setMemo(memo);
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            create.setExpirationTime(Protobuf.toProto(expirationTime));
        }
        body.setScheduleCreate(create);
    }

    /// The body of an inner transaction as `SchedulableTransactionBody`: its maximum fee, its memo and the field of
    /// its type. The fields of `SchedulableTransactionBody` carry the names of those of `TransactionBody`, so the field
    /// is copied by name.
    ///
    /// @param inner         the inner transaction
    /// @param transactionId the id of the transaction that schedules it; defaults that depend on the valid start (the
    ///                      expiration time of a new file) are computed from it
    /// @return the schedulable body
    /// @throws IllegalArgumentException if the type of the inner transaction is unknown or cannot be scheduled
    static SchedulableTransactionBody schedulable(final Transaction<?, ?> inner, final TransactionID transactionId) {
        if (inner instanceof ScheduledTransaction captured) {
            return captured.body();
        }
        final TransactionKind<?, ?> kind = TransactionKinds.of(inner.getClass());
        if (kind == null) {
            throw new IllegalArgumentException("A " + inner.getClass().getSimpleName() + " cannot be scheduled: the"
                    + " type is unknown");
        }
        final TransactionBody.Builder full = TransactionBody.newBuilder().setTransactionID(transactionId);
        fill(kind, inner, full);
        final Descriptors.FieldDescriptor source = full.getOneofFieldDescriptor(
                TransactionBody.getDescriptor().findFieldByNumber(kind.dataCase().getNumber()).getContainingOneof());
        final Descriptors.FieldDescriptor target = source == null ? null
                : SchedulableTransactionBody.getDescriptor().findFieldByName(source.getName());
        if (source == null || target == null) {
            throw new IllegalArgumentException("A " + inner.getClass().getSimpleName() + " cannot be scheduled");
        }
        final NativeToken<?, ?> fee = inner.maxTransactionFee();
        final SchedulableTransactionBody.Builder schedulable = SchedulableTransactionBody.newBuilder()
                .setTransactionFee((fee == null ? DEFAULT_SCHEDULED_TRANSACTION_FEE : fee).toBaseUnits())
                .setField(target, full.getField(source));
        final String memo = inner.memo();
        if (memo != null) {
            schedulable.setMemo(memo);
        }
        return schedulable.build();
    }

    private static <R extends Receipt, T extends Transaction<R, T>> void fill(final TransactionKind<R, T> kind,
                                                                              final Transaction<?, ?> inner,
                                                                              final TransactionBody.Builder body) {
        kind.body().accept(kind.type().cast(inner), body);
    }
}
