package org.hiero.consensusnode.transactions.contracts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractUpdateReceipt`, generated from the specs.
@DisplayName("ContractUpdateReceipt")
class ContractUpdateReceiptTest {

    @Test
    @DisplayName("creates a ContractUpdateReceipt and returns the values")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final ContractUpdateReceipt subject = new ContractUpdateReceipt(transactionId, status, exchangeRate, nextExchangeRate);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new ContractUpdateReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new ContractUpdateReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ContractUpdateReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ContractUpdateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null));
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static TransactionStatus statusValue() {
        return BasicTransactionStatus.OK;
    }

    private static ExchangeRate exchangeRateValue() {
        return new ExchangeRate(30000, HbarUnit.HBAR, 580150, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static ExchangeRate nextExchangeRateValue() {
        return new ExchangeRate(30000, HbarUnit.HBAR, 580150, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static ContractUpdateReceipt create() {
        return new ContractUpdateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue());
    }
}
