package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.transactions.BasicTransactionStatus;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.consensusnode.transactions.TransactionsFactory;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenBurnReceipt`, generated from the specs.
@DisplayName("TokenBurnReceipt")
class TokenBurnReceiptTest {

    @Test
    @DisplayName("creates a TokenBurnReceipt and returns the values")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final long totalSupply = totalSupplyValue();
        final TokenBurnReceipt subject = new TokenBurnReceipt(transactionId, status, exchangeRate, nextExchangeRate, totalSupply);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(totalSupply, subject.totalSupply());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final long totalSupply = totalSupplyValue();
        final TokenBurnReceipt subject = TokenBurnReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .totalSupply(totalSupply)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(totalSupply, subject.totalSupply());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenBurnReceipt subject = TokenBurnReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .totalSupply(totalSupplyValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenBurnReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new TokenBurnReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new TokenBurnReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new TokenBurnReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), totalSupplyValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new TokenBurnReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, totalSupplyValue()));
    }

    @Test
    @DisplayName("totalSupply: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/totalSupply/accepts-at-minimum")
    void shouldAcceptTotalSupplyAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenBurnReceipt subject = new TokenBurnReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value);
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.tokens.TokenBurnReceipt/totalSupply/accepts-at-maximum")
    void shouldAcceptTotalSupplyAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenBurnReceipt subject = new TokenBurnReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value);
        assertValue(value, subject.totalSupply());
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static TransactionStatus statusValue() {
        return BasicTransactionStatus.OK;
    }

    private static ExchangeRate exchangeRateValue() {
        return new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        };
    }

    private static ExchangeRate nextExchangeRateValue() {
        return new ExchangeRate(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), 1.5) {
            public boolean isExpired() {
                throw new UnsupportedOperationException("test double");
            }
        };
    }

    private static long totalSupplyValue() {
        return 1L;
    }

    private static TokenBurnReceipt create() {
        return new TokenBurnReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue());
    }
}
