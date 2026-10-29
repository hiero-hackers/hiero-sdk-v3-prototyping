package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
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

/// Tests of `TokenMintNftReceipt`, generated from the specs.
@DisplayName("TokenMintNftReceipt")
class TokenMintNftReceiptTest {

    @Test
    @DisplayName("creates a TokenMintNftReceipt and returns the values")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final long totalSupply = totalSupplyValue();
        final List<Long> serials = serialsValue();
        final TokenMintNftReceipt subject = new TokenMintNftReceipt(transactionId, status, exchangeRate, nextExchangeRate, totalSupply, serials);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(serials, subject.serials());
    }

    @Test
    @DisplayName("creates a TokenMintNftReceipt with the default values")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TokenMintNftReceipt subject = new TokenMintNftReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue());
        assertValue(List.of(), subject.serials());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final long totalSupply = totalSupplyValue();
        final List<Long> serials = serialsValue();
        final TokenMintNftReceipt subject = TokenMintNftReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .totalSupply(totalSupply)
                .serials(serials)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(serials, subject.serials());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenMintNftReceipt subject = TokenMintNftReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .totalSupply(totalSupplyValue())
                .build();
        assertValue(List.of(), subject.serials());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenMintNftReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new TokenMintNftReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue(), serialsValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new TokenMintNftReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue(), serialsValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new TokenMintNftReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), totalSupplyValue(), serialsValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new TokenMintNftReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, totalSupplyValue(), serialsValue()));
    }

    @Test
    @DisplayName("totalSupply: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/totalSupply/accepts-at-minimum")
    void shouldAcceptTotalSupplyAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenMintNftReceipt subject = new TokenMintNftReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value, serialsValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/totalSupply/accepts-at-maximum")
    void shouldAcceptTotalSupplyAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenMintNftReceipt subject = new TokenMintNftReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value, serialsValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("rejects null for serials")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/serials/rejects-null")
    void shouldRejectNullSerials() {
        assertThrows(NullPointerException.class, () -> new TokenMintNftReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue(), (List<Long>) null));
    }

    @Test
    @DisplayName("the constructor copies serials; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftReceipt/serials/copies-in")
    void shouldCopySerialsIn() {
        final List<Long> value = new ArrayList<>(serialsValue());
        final List<Long> expected = List.copyOf(value);
        final TokenMintNftReceipt subject = new TokenMintNftReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue(), value);
        value.clear();
        assertValue(expected, subject.serials());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serials()).clear());
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

    private static List<Long> serialsValue() {
        return List.of(1L);
    }

    private static TokenMintNftReceipt create() {
        return new TokenMintNftReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), totalSupplyValue(), serialsValue());
    }
}
