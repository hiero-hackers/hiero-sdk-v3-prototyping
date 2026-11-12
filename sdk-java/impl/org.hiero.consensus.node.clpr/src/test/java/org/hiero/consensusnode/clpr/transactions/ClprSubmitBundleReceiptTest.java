package org.hiero.consensusnode.clpr.transactions;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.clpr.ClprTransactionStatus;
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

/// Tests of `ClprSubmitBundleReceipt`, generated from the specs.
@DisplayName("ClprSubmitBundleReceipt")
class ClprSubmitBundleReceiptTest {

    @Test
    @DisplayName("creates a ClprSubmitBundleReceipt and returns the values")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleReceipt/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final ClprSubmitBundleReceipt subject = new ClprSubmitBundleReceipt(transactionId, status, exchangeRate, nextExchangeRate);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleReceipt/transactionId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new ClprSubmitBundleReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleReceipt/status/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new ClprSubmitBundleReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleReceipt/exchangeRate/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ClprSubmitBundleReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleReceipt/nextExchangeRate/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ClprSubmitBundleReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null));
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static TransactionStatus statusValue() {
        return ClprTransactionStatus.CLPR_NOT_ENABLED;
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

    private static ClprSubmitBundleReceipt create() {
        return new ClprSubmitBundleReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue());
    }
}
