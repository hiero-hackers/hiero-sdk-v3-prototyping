package org.hiero.consensusnode.transactions.files;

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

/// Tests of `FileCreateReceipt`, generated from the specs.
@DisplayName("FileCreateReceipt")
class FileCreateReceiptTest {

    @Test
    @DisplayName("creates a FileCreateReceipt and returns the values")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final Address fileId = fileIdValue();
        final FileCreateReceipt subject = new FileCreateReceipt(transactionId, status, exchangeRate, nextExchangeRate, fileId);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(fileId, subject.fileId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final Address fileId = fileIdValue();
        final FileCreateReceipt subject = FileCreateReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .fileId(fileId)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(fileId, subject.fileId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FileCreateReceipt subject = FileCreateReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .fileId(fileIdValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FileCreateReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new FileCreateReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), fileIdValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new FileCreateReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), fileIdValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new FileCreateReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), fileIdValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new FileCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, fileIdValue()));
    }

    @Test
    @DisplayName("rejects null for fileId")
    @Tag("testId:consensusnode.transactions.files.FileCreateReceipt/fileId/rejects-null")
    void shouldRejectNullFileId() {
        assertThrows(NullPointerException.class, () -> new FileCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), (Address) null));
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

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static FileCreateReceipt create() {
        return new FileCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), fileIdValue());
    }
}
