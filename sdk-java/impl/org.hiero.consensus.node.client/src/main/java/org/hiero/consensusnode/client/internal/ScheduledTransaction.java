package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.SchedulableTransactionBody;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// The inner transaction of a schedule as `ScheduleInfoQuery` reads it: the captured `SchedulableTransactionBody`.
///
/// A transaction cannot be read back from its body yet (see `KindSupport.create(TransactionBody)`), so the captured
/// body is kept as it is. Its memo and maximum fee are visible through the `Transaction` accessors. It can be
/// scheduled again (a `ScheduleCreateTransaction` copies the body unchanged), but never packed or signed - a scheduled
/// transaction is executed by the network, not submitted.
public final class ScheduledTransaction extends Transaction<Receipt, ScheduledTransaction> {

    private final SchedulableTransactionBody body;

    /// Creates the inner transaction of a schedule.
    ///
    /// @param body the captured body
    public ScheduledTransaction(final SchedulableTransactionBody body) {
        this.body = Objects.requireNonNull(body, "body must not be null");
        if (!body.getMemo().isEmpty()) {
            setMemo(body.getMemo());
        }
        if (body.getTransactionFee() != 0) {
            setMaxTransactionFee(Protobuf.hbar(body.getTransactionFee()));
        }
    }

    /// The captured body.
    ///
    /// @return the body
    public SchedulableTransactionBody body() {
        return body;
    }

    /// The field of the captured body, e.g. `cryptoTransfer`.
    ///
    /// @return the name of the field, `DATA_NOT_SET` for an empty body
    public String type() {
        return body.getDataCase().name();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw notPackable();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> signWithOperator(final HieroClient<?> client) {
        throw notPackable();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw notPackable();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> sign(final AccountId payerId,
                                                                final TransactionSigner signer,
                                                                final List<AccountId> nodes) {
        throw notPackable();
    }

    @Override
    public CompletionStage<Response<Receipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw notPackable();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> packForBatch(final Account payer,
                                                                        final Authority batchKey) {
        throw notPackable();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> signForBatchWithOperator(final HieroClient<?> client,
                                                                                    final Authority batchKey) {
        throw notPackable();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> signForBatch(final Account payer,
                                                                        final Authority batchKey) {
        throw notPackable();
    }

    @Override
    public PackedTransaction<Receipt, ScheduledTransaction> signForBatch(final AccountId payerId,
                                                                        final TransactionSigner signer,
                                                                        final Authority batchKey) {
        throw notPackable();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ScheduledTransaction other && body.equals(other.body);
    }

    @Override
    public int hashCode() {
        return body.hashCode();
    }

    @Override
    public String toString() {
        return "ScheduledTransaction[type=" + type() + ", memo=" + memo() + ", maxTransactionFee="
                + maxTransactionFee() + "]";
    }

    private static UnsupportedOperationException notPackable() {
        return new UnsupportedOperationException("The inner transaction of a schedule is executed by the network; it"
                + " cannot be packed, signed or submitted");
    }
}
