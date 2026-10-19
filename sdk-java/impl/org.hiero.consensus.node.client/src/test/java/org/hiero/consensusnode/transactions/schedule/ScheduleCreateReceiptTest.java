package org.hiero.consensusnode.transactions.schedule;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.transactions.BasicTransactionStatus;
import org.hiero.consensusnode.transactions.TransactionStatus;
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

/// Tests of `ScheduleCreateReceipt`, generated from the specs.
@DisplayName("ScheduleCreateReceipt")
class ScheduleCreateReceiptTest {

    @Test
    @DisplayName("creates a ScheduleCreateReceipt and returns the values")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final Address scheduleId = scheduleIdValue();
        final TransactionId scheduledTransactionId = scheduledTransactionIdValue();
        final ScheduleCreateReceipt subject = new ScheduleCreateReceipt(transactionId, status, exchangeRate, nextExchangeRate, scheduleId, scheduledTransactionId);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(scheduleId, subject.scheduleId());
        assertValue(scheduledTransactionId, subject.scheduledTransactionId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final Address scheduleId = scheduleIdValue();
        final TransactionId scheduledTransactionId = scheduledTransactionIdValue();
        final ScheduleCreateReceipt subject = ScheduleCreateReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .scheduleId(scheduleId)
                .scheduledTransactionId(scheduledTransactionId)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(scheduleId, subject.scheduleId());
        assertValue(scheduledTransactionId, subject.scheduledTransactionId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ScheduleCreateReceipt subject = ScheduleCreateReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .scheduleId(scheduleIdValue())
                .scheduledTransactionId(scheduledTransactionIdValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ScheduleCreateReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new ScheduleCreateReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), scheduleIdValue(), scheduledTransactionIdValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new ScheduleCreateReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), scheduleIdValue(), scheduledTransactionIdValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ScheduleCreateReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), scheduleIdValue(), scheduledTransactionIdValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ScheduleCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, scheduleIdValue(), scheduledTransactionIdValue()));
    }

    @Test
    @DisplayName("rejects null for scheduleId")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/scheduleId/rejects-null")
    void shouldRejectNullScheduleId() {
        assertThrows(NullPointerException.class, () -> new ScheduleCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), (Address) null, scheduledTransactionIdValue()));
    }

    @Test
    @DisplayName("rejects null for scheduledTransactionId")
    @Tag("testId:consensusnode.transactions.schedule.ScheduleCreateReceipt/scheduledTransactionId/rejects-null")
    void shouldRejectNullScheduledTransactionId() {
        assertThrows(NullPointerException.class, () -> new ScheduleCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), scheduleIdValue(), (TransactionId) null));
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

    private static Address scheduleIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TransactionId scheduledTransactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static ScheduleCreateReceipt create() {
        return new ScheduleCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), scheduleIdValue(), scheduledTransactionIdValue());
    }
}
