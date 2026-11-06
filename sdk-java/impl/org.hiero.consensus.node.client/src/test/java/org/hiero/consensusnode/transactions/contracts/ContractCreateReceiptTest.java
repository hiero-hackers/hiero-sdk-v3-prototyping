package org.hiero.consensusnode.transactions.contracts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractCreateReceipt`, generated from the specs.
@DisplayName("ContractCreateReceipt")
class ContractCreateReceiptTest {

    @Test
    @DisplayName("creates a ContractCreateReceipt and returns the values")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final ContractId contractId = contractIdValue();
        final ContractCreateReceipt subject = new ContractCreateReceipt(transactionId, status, exchangeRate, nextExchangeRate, contractId);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(contractId, subject.contractId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final ContractId contractId = contractIdValue();
        final ContractCreateReceipt subject = ContractCreateReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .contractId(contractId)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(contractId, subject.contractId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractCreateReceipt subject = ContractCreateReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .contractId(contractIdValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractCreateReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new ContractCreateReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), contractIdValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new ContractCreateReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), contractIdValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ContractCreateReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), contractIdValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new ContractCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, contractIdValue()));
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateReceipt/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), (ContractId) null));
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

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static ContractCreateReceipt create() {
        return new ContractCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), contractIdValue());
    }
}
