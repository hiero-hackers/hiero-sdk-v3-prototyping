package org.hiero.consensusnode.admin.network;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.admin.internal.AdminQueries;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.Querying;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query that returns the current address book of the network. The network charges for reading the
/// address-book file (0.0.102 on default Hedera shards/realms), and the operator pays that fee. Has no input
/// fields — the answer is per network.
public final class NodeAddressBookQuery extends PaidQuery<NodeAddressBook> {

    /// Creates a new `NodeAddressBookQuery`.
    public NodeAddressBookQuery() {
        super();
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public NodeAddressBookQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public NodeAddressBookQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public NodeAddressBookQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
        super.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    @Override
    public CompletionStage<PaidQueryResponse<NodeAddressBook>> submit(final HieroClient<?> client) {
        return Querying.paid(this, AdminQueries.addressBookFile(client.operatorAccount().accountId()), maxQueryPayment(),
                AdminQueries.NODE_ADDRESS_BOOK, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        final NativeToken<?, ?> maxQueryPayment = maxQueryPayment();
        return Querying.cost(this, AdminQueries.addressBookFile(client.operatorAccount().accountId()),
                AdminQueries.NODE_ADDRESS_BOOK, client).thenApply(cost -> {
                    if (maxQueryPayment != null && cost.toBaseUnits() > maxQueryPayment.toBaseUnits()) {
                        throw new IllegalStateException("The query costs " + cost + ", more than its maxQueryPayment "
                                + maxQueryPayment);
                    }
                    return cost;
                });
    }

    @Override
    public String toString() {
        return "NodeAddressBookQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + "]";
    }
}
