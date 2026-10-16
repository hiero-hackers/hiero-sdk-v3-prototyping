package org.hiero.consensusnode.transactions.utility;

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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `PrngReceipt`, generated from the specs.
@DisplayName("PrngReceipt")
class PrngReceiptTest {

    @Test
    @DisplayName("creates a PrngReceipt and returns the values")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final @Nullable Integer prngNumber = prngNumberValue();
        final byte @Nullable [] prngBytes = (byte[]) null;
        final PrngReceipt subject = new PrngReceipt(transactionId, status, exchangeRate, nextExchangeRate, prngNumber, prngBytes);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(prngNumber, subject.prngNumber());
        assertValue(prngBytes, subject.prngBytes());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final @Nullable Integer prngNumber = prngNumberValue();
        final byte @Nullable [] prngBytes = (byte[]) null;
        final PrngReceipt subject = PrngReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .prngNumber(prngNumber)
                .prngBytes(prngBytes)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(prngNumber, subject.prngNumber());
        assertValue(prngBytes, subject.prngBytes());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final PrngReceipt subject = PrngReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .prngNumber(prngNumberValue())
                .build();
        assertValue(prngNumberValue(), subject.prngNumber());
        assertNull(subject.prngBytes());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> PrngReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new PrngReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), prngNumberValue(), (byte[]) null));
    }

    @Test
    @DisplayName("rejects null for status")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new PrngReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), prngNumberValue(), (byte[]) null));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new PrngReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), prngNumberValue(), (byte[]) null));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new PrngReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, prngNumberValue(), (byte[]) null));
    }

    @Test
    @DisplayName("prngNumber: accepts the minimum (-2147483648)")
    void shouldAcceptPrngNumberAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final PrngReceipt subject = new PrngReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value, (byte[]) null);
        assertValue(value, subject.prngNumber());
    }

    @Test
    @DisplayName("prngNumber: accepts the maximum (2147483647)")
    void shouldAcceptPrngNumberAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final PrngReceipt subject = new PrngReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value, (byte[]) null);
        assertValue(value, subject.prngNumber());
    }

    @Test
    @DisplayName("the constructor copies prngBytes; the accessor returns a copy")
    void shouldCopyPrngBytesIn() {
        final byte[] value = prngBytesValue();
        final byte[] expected = value.clone();
        final PrngReceipt subject = new PrngReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), (Integer) null, value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.prngBytes());
        final byte[] returned = subject.prngBytes();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.prngBytes());
    }

    @Test
    @DisplayName("rejects prngNumber and prngBytes together (@@oneOf)")
    void shouldRejectTwoOfPrngNumberOrPrngBytes() {
        assertThrows(IllegalArgumentException.class, () -> new PrngReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), prngNumberValue(), prngBytesValue()));
    }

    @Test
    @DisplayName("rejects none of prngNumber, prngBytes (@@oneOf)")
    void shouldRejectNoneOfPrngNumberOrPrngBytes() {
        assertThrows(IllegalArgumentException.class, () -> new PrngReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), (Integer) null, (byte[]) null));
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

    private static int prngNumberValue() {
        return 1;
    }

    private static byte[] prngBytesValue() {
        return new byte[] {1, 2, 3};
    }

    private static PrngReceipt create() {
        return new PrngReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), prngNumberValue(), (byte[]) null);
    }
}
