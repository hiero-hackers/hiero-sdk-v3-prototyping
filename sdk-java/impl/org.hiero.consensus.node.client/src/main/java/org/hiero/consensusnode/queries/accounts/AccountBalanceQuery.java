package org.hiero.consensusnode.queries.accounts;

import java.time.Duration;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.AccountQueries;
import org.hiero.consensusnode.client.internal.Querying;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.FreeQuery;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.jspecify.annotations.Nullable;

/// Free query for the current balance of an account or smart contract.
/// Exactly one of accountId or contractId must be set.
public final class AccountBalanceQuery extends FreeQuery<AccountBalance> {

    private final @Nullable AccountId accountId;
    private final @Nullable ContractId contractId;

    /// Creates a new `AccountBalanceQuery`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public AccountBalanceQuery(final @Nullable AccountId accountId, final @Nullable ContractId contractId) {
        super();
        this.accountId = accountId;
        this.contractId = contractId;
        if ((accountId != null ? 1 : 0) + (contractId != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of accountId, contractId must be set");
        }
    }

    /// Returns the `accountId`.
    public @Nullable AccountId accountId() {
        return accountId;
    }

    /// Returns the `contractId`.
    public @Nullable ContractId contractId() {
        return contractId;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public AccountBalanceQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public AccountBalanceQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    @Override
    public CompletionStage<QueryResponse<AccountBalance>> submit(final HieroClient<?> client) {
        return Querying.free(this, this, AccountQueries.ACCOUNT_BALANCE, client);
    }

    /// Returns a builder for a new `AccountBalanceQuery`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AccountBalanceQuery`.
    ///
    /// `build()` creates the `AccountBalanceQuery` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId accountId;
        private @Nullable ContractId contractId;

        private Builder() {
            // created by AccountBalanceQuery.builder()
        }

        /// Sets the `accountId`.
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final @Nullable AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `contractId`.
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final @Nullable ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Creates the `AccountBalanceQuery` of the values set.
        ///
        /// @return the new `AccountBalanceQuery`
        /// @throws IllegalArgumentException if a value violates its constraints
        public AccountBalanceQuery build() {
            return new AccountBalanceQuery(accountId, contractId);
        }
    }

    @Override
    public String toString() {
        return "AccountBalanceQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", accountId=" + accountId()
                + ", contractId=" + contractId()
                + "]";
    }
}
