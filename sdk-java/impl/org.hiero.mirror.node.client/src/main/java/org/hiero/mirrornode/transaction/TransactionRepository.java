package org.hiero.mirrornode.transaction;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;

public interface TransactionRepository {

    /// All transactions. Maps to GET /api/v1/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TransactionInfo>> findAll();

    /// The transactions, filtered by account, consensus time, type, result and balance modification.
    /// Maps to GET /api/v1/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TransactionInfo>> findAll(final List<String> accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp, final @Nullable TransactionType transactionType, final @Nullable TransactionResult result, final @Nullable BalanceModification type);

    /// The transactions of an account. Maps to GET /api/v1/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TransactionInfo>> findByAccount(final AccountId accountId);

    /// The transactions of an account of the given type. Maps to GET /api/v1/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TransactionInfo>> findByAccountAndType(final AccountId accountId, final TransactionType type);

    /// The transactions of an account with the given result. Maps to GET /api/v1/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TransactionInfo>> findByAccountAndResult(final AccountId accountId, final TransactionResult result);

    /// The transactions that credited or debited the balance of an account. Maps to GET /api/v1/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TransactionInfo>> findByAccountAndModification(final AccountId accountId, final BalanceModification modification);

    /// The transaction with the given id and its nonce that is not scheduled.
    /// Maps to GET /api/v1/transactions/{transactionId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable TransactionInfo> findById(final TransactionId transactionId);

    /// All transactions with the payer and the valid start of the given id: the transaction, its child transactions
    /// and the scheduled transaction. Empty if there is none. Maps to GET /api/v1/transactions/{transactionId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<List<TransactionInfo>> findAllById(final TransactionId transactionId);

    /// The transactions with the payer and the valid start of the given id, filtered by nonce and by whether they are
    /// scheduled. Empty if there is none. Maps to GET /api/v1/transactions/{transactionId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<List<TransactionInfo>> findAllById(final TransactionId transactionId, final @Nullable Integer nonce, final @Nullable Boolean scheduled);
}
