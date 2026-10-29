package org.hiero.consensusnode.queries.receipts;

import java.time.Duration;
import java.util.Objects;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.ClientRuntime;
import org.hiero.consensusnode.client.internal.Receipts;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.FreeQuery;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// Free query for the receipt of a transaction by its transaction id, optionally with the receipts of its duplicates
/// and children. The receipt is available for about three minutes after consensus; the query waits until it is.
public final class TransactionReceiptQuery extends FreeQuery<TransactionReceipts> {

    private final TransactionId transactionId;
    private final boolean includeDuplicates;
    private final boolean includeChildren;
    private final @Nullable AccountId nodeAccountId;

    /// Creates a new `TransactionReceiptQuery`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TransactionReceiptQuery(
            final TransactionId transactionId,
            final boolean includeDuplicates,
            final boolean includeChildren,
            final @Nullable AccountId nodeAccountId) {
        super();
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        this.transactionId = transactionId;
        this.includeDuplicates = includeDuplicates;
        this.includeChildren = includeChildren;
        this.nodeAccountId = nodeAccountId;
    }

    /// Creates a new `TransactionReceiptQuery` with the default value of `includeDuplicates`, `includeChildren`.
    public TransactionReceiptQuery(final TransactionId transactionId, final @Nullable AccountId nodeAccountId) {
        this(transactionId, false, false, nodeAccountId);
    }

    /// the id of the transaction
    public TransactionId transactionId() {
        return transactionId;
    }

    /// also return the receipts of duplicate transactions
    public boolean includeDuplicates() {
        return includeDuplicates;
    }

    /// also return the receipts of child transactions
    public boolean includeChildren() {
        return includeChildren;
    }

    /// the node to ask; null: any node of the client
    public @Nullable AccountId nodeAccountId() {
        return nodeAccountId;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public TransactionReceiptQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public TransactionReceiptQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    @Override
    public CompletionStage<QueryResponse<TransactionReceipts>> submit(final HieroClient<?> client) {
        Objects.requireNonNull(client, "client must not be null");
        return ClientRuntime.of(client).queryReceipt(this, nodeAccountId, transactionId, includeDuplicates,
                        includeChildren)
                .thenApply(answer -> new QueryResponse<>(Receipts.receipts(transactionId, answer.value()),
                        answer.answeredBy()));
    }

    /// Returns a builder for a new `TransactionReceiptQuery`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TransactionReceiptQuery`.
    ///
    /// `build()` creates the `TransactionReceiptQuery` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private boolean includeDuplicates = false;
        private boolean includeChildren = false;
        private @Nullable AccountId nodeAccountId;

        private Builder() {
            // created by TransactionReceiptQuery.builder()
        }

        /// Sets the `transactionId`.
        ///
        /// the id of the transaction
        ///
        /// @param transactionId the value
        /// @return this builder
        public Builder transactionId(final TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /// Sets the `includeDuplicates`.
        ///
        /// also return the receipts of duplicate transactions
        ///
        /// @param includeDuplicates the value
        /// @return this builder
        public Builder includeDuplicates(final boolean includeDuplicates) {
            this.includeDuplicates = includeDuplicates;
            return this;
        }

        /// Sets the `includeChildren`.
        ///
        /// also return the receipts of child transactions
        ///
        /// @param includeChildren the value
        /// @return this builder
        public Builder includeChildren(final boolean includeChildren) {
            this.includeChildren = includeChildren;
            return this;
        }

        /// Sets the `nodeAccountId`.
        ///
        /// the node to ask; null: any node of the client
        ///
        /// @param nodeAccountId the value
        /// @return this builder
        public Builder nodeAccountId(final @Nullable AccountId nodeAccountId) {
            this.nodeAccountId = nodeAccountId;
            return this;
        }

        /// Creates the `TransactionReceiptQuery` of the values set.
        ///
        /// @return the new `TransactionReceiptQuery`
        /// @throws NullPointerException if a required value is not set
        public TransactionReceiptQuery build() {
            return new TransactionReceiptQuery(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    includeDuplicates,
                    includeChildren,
                    nodeAccountId);
        }
    }

    @Override
    public String toString() {
        return "TransactionReceiptQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", transactionId=" + transactionId()
                + ", includeDuplicates=" + includeDuplicates()
                + ", includeChildren=" + includeChildren()
                + ", nodeAccountId=" + nodeAccountId()
                + "]";
    }
}
