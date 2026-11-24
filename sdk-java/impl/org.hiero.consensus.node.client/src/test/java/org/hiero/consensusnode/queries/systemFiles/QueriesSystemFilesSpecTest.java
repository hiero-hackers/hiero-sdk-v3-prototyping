package org.hiero.consensusnode.queries.systemFiles;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.hedera.HbarUnit;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-system-files`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-system-files")
class QueriesSystemFilesSpecTest {

    /// `consensusnode.queries.systemFiles/exchange-rates-keep-the-current-and-the-next-rate`
    ///
    /// Source: the Java SDK (ExchangeRatesTest) for the two rates: 580150 resp. 587660 cents for 30000 HBAR, expiring at
    /// 1645714800 resp. 1645718400, i.e. 2022-02-24T15:00:00Z resp. 16:00:00Z.
    @Test
    @DisplayName("exchange rates keep the current and the next rate")
    @Tag("testId:consensusnode.queries.systemFiles/exchange-rates-keep-the-current-and-the-next-rate")
    void exchangeRatesKeepTheCurrentAndTheNextRate() {
        // given
        final ExchangeRate current = new ExchangeRate(30000, HbarUnit.HBAR, 580150, toZonedDateTime("2022-02-24T15:00:00Z"));
        final ExchangeRate next = new ExchangeRate(30000, HbarUnit.HBAR, 587660, toZonedDateTime("2022-02-24T16:00:00Z"));

        // when
        final ExchangeRates rates = new ExchangeRates(current, next);

        // then
        assertEquals(current, rates.currentRate());
        assertEquals(next, rates.nextRate());
        assertNotEquals(rates.currentRate(), rates.nextRate());
    }

    /// `consensusnode.queries.systemFiles/a-new-exchange-rates-query-has-no-payment-limit`
    ///
    /// Source: this spec (a system file query has no file id to set; a new query has no payment limit).
    @Test
    @DisplayName("a new exchange rates query has no payment limit")
    @Tag("testId:consensusnode.queries.systemFiles/a-new-exchange-rates-query-has-no-payment-limit")
    void aNewExchangeRatesQueryHasNoPaymentLimit() {
        // when
        final ExchangeRatesQuery query = new ExchangeRatesQuery();

        // then
        assertNull(query.maxQueryPayment());
    }
}
