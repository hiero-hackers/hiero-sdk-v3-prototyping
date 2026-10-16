package org.hiero.consensusnode.queries.accounts;

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

    /// Sets the `maxAttempts`.
    ///
    /// @param maxAttempts the new value
    /// @return this object
    @Override
    public AccountBalanceQuery setMaxAttempts(final @Nullable Integer maxAttempts) {
        super.setMaxAttempts(maxAttempts);
        return this;
    }

    /// Sets the `maxBackoff`.
    ///
    /// @param maxBackoff the new value
    /// @return this object
    @Override
    public AccountBalanceQuery setMaxBackoff(final @Nullable Long maxBackoff) {
        super.setMaxBackoff(maxBackoff);
        return this;
    }

    /// Sets the `minBackoff`.
    ///
    /// @param minBackoff the new value
    /// @return this object
    @Override
    public AccountBalanceQuery setMinBackoff(final @Nullable Long minBackoff) {
        super.setMinBackoff(minBackoff);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public AccountBalanceQuery setAttemptTimeout(final @Nullable Long attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently.
    @Override
    public CompletionStage<QueryResponse<AccountBalance>> submit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: AccountBalanceQuery.submit");
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
        return "AccountBalanceQuery[maxAttempts=" + maxAttempts()
                + ", maxBackoff=" + maxBackoff()
                + ", minBackoff=" + minBackoff()
                + ", attemptTimeout=" + attemptTimeout()
                + ", accountId=" + accountId()
                + ", contractId=" + contractId()
                + "]";
    }
}
