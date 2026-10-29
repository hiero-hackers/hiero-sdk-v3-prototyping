package org.hiero.consensusnode.queries.tokens;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.Querying;
import org.hiero.consensusnode.client.internal.TokenQueries;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query for the metadata snapshot of a token (fungible or NFT collection). Does not return
/// per-serial NFT data — use `TokenNftInfoQuery` for a single serial.
public final class TokenInfoQuery extends PaidQuery<TokenInfo> {

    private final Address tokenId;

    /// Creates a new `TokenInfoQuery`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenInfoQuery(final Address tokenId) {
        super();
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        this.tokenId = tokenId;
    }

    /// Returns the `tokenId`.
    public Address tokenId() {
        return tokenId;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public TokenInfoQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public TokenInfoQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public TokenInfoQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
        super.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    @Override
    public CompletionStage<PaidQueryResponse<TokenInfo>> submit(final HieroClient<?> client) {
        return Querying.paid(this, this, maxQueryPayment(), TokenQueries.TOKEN_INFO, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        return TokenQueries.cost(this, TokenQueries.TOKEN_INFO, client);
    }

    @Override
    public String toString() {
        return "TokenInfoQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + ", tokenId=" + tokenId()
                + "]";
    }
}
