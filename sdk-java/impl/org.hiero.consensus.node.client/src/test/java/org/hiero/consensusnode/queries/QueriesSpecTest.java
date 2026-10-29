package org.hiero.consensusnode.queries;

import static org.hiero.sdk.testing.Testing.*;

import java.time.Duration;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.queries.accounts.AccountBalanceQuery;
import org.hiero.consensusnode.queries.accounts.AccountInfoQuery;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries")
class QueriesSpecTest {

    /// `consensusnode.queries/a-new-query-has-no-payment-limit-and-no-retry-settings`
    ///
    /// Source: the Python SDK (query_test.py, test_set_max_query_payment_valid_param: no limit before one is set) and
    /// this spec (if a setting of a request is absent, the one of the client's `ExecutionSettings` is used).
    @Test
    @DisplayName("a new query has no payment limit and no retry settings")
    @Tag("testId:consensusnode.queries/a-new-query-has-no-payment-limit-and-no-retry-settings")
    void aNewQueryHasNoPaymentLimitAndNoRetrySettings() {
        // when
        final AccountInfoQuery paid = new AccountInfoQuery(defaultAccountId());
        final AccountBalanceQuery free = new AccountBalanceQuery(defaultAccountId(), null);

        // then
        assertNull(paid.maxQueryPayment());
        assertNull(paid.retryPolicy());
        assertNull(paid.attemptTimeout());
        assertNull(free.retryPolicy());
        assertNull(free.attemptTimeout());
    }

    /// `consensusnode.queries/a-paid-query-keeps-its-payment-limit`
    ///
    /// Source: the Python SDK (query_test.py, test_set_max_query_payment_valid_param) and the Java SDK (Query
    /// setMaxQueryPayment): a payment limit set on a paid query is kept.
    @Test
    @DisplayName("a paid query keeps its payment limit")
    @Tag("testId:consensusnode.queries/a-paid-query-keeps-its-payment-limit")
    void aPaidQueryKeepsItsPaymentLimit() {
        // given
        final NativeToken<?, ?> limit = defaultHbar();

        // when
        final AccountInfoQuery query = new AccountInfoQuery(defaultAccountId()).setMaxQueryPayment(limit);

        // then
        assertEquals(limit, query.maxQueryPayment());
    }

    /// `consensusnode.queries/a-query-keeps-its-execution-settings`
    ///
    /// Source: this spec (Submittable: a request keeps the execution settings set on it).
    @Test
    @DisplayName("a query keeps its execution settings")
    @Tag("testId:consensusnode.queries/a-query-keeps-its-execution-settings")
    void aQueryKeepsItsExecutionSettings() {
        // given
        final RetryPolicy policy = defaultRetryPolicy();
        final Duration timeout = toDuration("PT5S");

        // when
        final AccountInfoQuery query = new AccountInfoQuery(defaultAccountId()).setRetryPolicy(policy).setAttemptTimeout(timeout);

        // then
        assertEquals(policy, query.retryPolicy());
        assertEquals(timeout, query.attemptTimeout());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Hbar defaultHbar() {
        return new Hbar(1L, HbarUnit.HBAR);
    }

    private static RetryPolicy defaultRetryPolicy() {
        return defaultExecutionSettings().retryPolicy();
    }

    private static ExecutionSettings defaultExecutionSettings() {
        return ExecutionSettings.createDefault();
    }
}
