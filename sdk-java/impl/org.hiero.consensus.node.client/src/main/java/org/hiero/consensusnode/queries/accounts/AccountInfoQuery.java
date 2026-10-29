package org.hiero.consensusnode.queries.accounts;

import java.time.Duration;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.AccountQueries;
import org.hiero.consensusnode.client.internal.Querying;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query for the full account state.
public final class AccountInfoQuery extends PaidQuery<AccountInfo> {

    private final AccountId accountId;

    /// Creates a new `AccountInfoQuery`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AccountInfoQuery(final AccountId accountId) {
        super();
        Objects.requireNonNull(accountId, "accountId must not be null");
        this.accountId = accountId;
    }

    /// Returns the `accountId`.
    public AccountId accountId() {
        return accountId;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public AccountInfoQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public AccountInfoQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public AccountInfoQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
        super.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    @Override
    public CompletionStage<PaidQueryResponse<AccountInfo>> submit(final HieroClient<?> client) {
        return Querying.paid(this, this, maxQueryPayment(), AccountQueries.ACCOUNT_INFO, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        return Querying.cost(this, this, AccountQueries.ACCOUNT_INFO, client);
    }

    @Override
    public String toString() {
        return "AccountInfoQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + ", accountId=" + accountId()
                + "]";
    }
}
