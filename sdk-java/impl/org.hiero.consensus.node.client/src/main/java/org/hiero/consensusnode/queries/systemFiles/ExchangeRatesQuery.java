package org.hiero.consensusnode.queries.systemFiles;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.Querying;
import org.hiero.consensusnode.client.internal.SystemFileQueries;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query for the exchange rates of the network. Reads the exchange rate file of the network the query is sent
/// to and returns its content decoded; there is no file id to set.
public final class ExchangeRatesQuery extends PaidQuery<ExchangeRates> {

    /// Creates a new `ExchangeRatesQuery`.
    public ExchangeRatesQuery() {
        super();
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public ExchangeRatesQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public ExchangeRatesQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public ExchangeRatesQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
        super.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up, and with
    /// `unknown-status-error` when a node answers with a status code that the SDK cannot name.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` or `UnknownStatusException` if the operation fails.
    @Override
    public CompletionStage<PaidQueryResponse<ExchangeRates>> submit(final HieroClient<?> client) {
        return Querying.paid(this, SystemFileQueries.exchangeRatesFile(client), maxQueryPayment(),
                SystemFileQueries.EXCHANGE_RATES, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        return Querying.cost(this, SystemFileQueries.exchangeRatesFile(client), SystemFileQueries.EXCHANGE_RATES,
                client);
    }

    @Override
    public String toString() {
        return "ExchangeRatesQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + "]";
    }
}
