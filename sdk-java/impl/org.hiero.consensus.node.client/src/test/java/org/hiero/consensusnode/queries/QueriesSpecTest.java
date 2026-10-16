package org.hiero.consensusnode.queries;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.consensusnode.queries.accounts.AccountBalanceQuery;
import org.hiero.consensusnode.queries.accounts.AccountInfoQuery;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries")
class QueriesSpecTest {

    /// `consensusnode.queries/a-new-query-has-no-payment-limit-and-no-retry-settings`
    ///
    /// Source: the Python SDK (query_test.py, test_set_max_query_payment_valid_param: no limit before one is set) and
    /// this spec (if a retry setting is absent, the SDK default is used).
    @Test
    @DisplayName("a new query has no payment limit and no retry settings")
    void aNewQueryHasNoPaymentLimitAndNoRetrySettings() {
        // when
        final AccountInfoQuery paid = new AccountInfoQuery(defaultAccountId());
        final AccountBalanceQuery free = new AccountBalanceQuery(defaultAccountId(), null);

        // then
        assertNull(paid.maxQueryPayment());
        assertNull(paid.maxAttempts());
        assertNull(paid.minBackoff());
        assertNull(paid.maxBackoff());
        assertNull(paid.attemptTimeout());
        assertNull(free.maxAttempts());
        assertNull(free.minBackoff());
        assertNull(free.maxBackoff());
        assertNull(free.attemptTimeout());
    }

    /// `consensusnode.queries/a-paid-query-keeps-its-payment-limit`
    ///
    /// Source: the Python SDK (query_test.py, test_set_max_query_payment_valid_param) and the Java SDK (Query
    /// setMaxQueryPayment): a payment limit set on a paid query is kept.
    @Test
    @DisplayName("a paid query keeps its payment limit")
    void aPaidQueryKeepsItsPaymentLimit() {
        // given
        final NativeToken<?, ?> limit = defaultHbar();

        // when
        final AccountInfoQuery query = new AccountInfoQuery(defaultAccountId()).setMaxQueryPayment(limit);

        // then
        assertEquals(limit, query.maxQueryPayment());
    }

    /// `consensusnode.queries/a-query-keeps-its-retry-settings`
    ///
    /// Source: the Python SDK (client_test.py, test_set_max_attempts_with_valid_param, test_set_min_backoff_with_valid_param,
    /// test_set_max_backoff_with_valid_param) and the Java SDK (ClientTest.setMaxAttempts, setMinBackoffValid,
    /// setMaxBackoffValid): retry settings that are set are kept.
    @Test
    @DisplayName("a query keeps its retry settings")
    void aQueryKeepsItsRetrySettings() {
        // when
        final AccountInfoQuery query = new AccountInfoQuery(defaultAccountId()).setMaxAttempts(5).setMaxBackoff(8000L).setMinBackoff(250L).setAttemptTimeout(10000L);

        // then
        assertEquals(5, query.maxAttempts());
        assertEquals(250L, query.minBackoff());
        assertEquals(8000L, query.maxBackoff());
        assertEquals(10000L, query.attemptTimeout());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Hbar defaultHbar() {
        return new Hbar(1L, HbarUnit.HBAR);
    }
}
