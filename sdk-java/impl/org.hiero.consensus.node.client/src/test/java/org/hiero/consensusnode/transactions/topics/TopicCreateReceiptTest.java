package org.hiero.consensusnode.transactions.topics;

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
import org.junit.jupiter.api.Test;

/// Tests of `TopicCreateReceipt`, generated from the specs.
@DisplayName("TopicCreateReceipt")
class TopicCreateReceiptTest {

    @Test
    @DisplayName("creates a TopicCreateReceipt and returns the values")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final Address topicId = topicIdValue();
        final TopicCreateReceipt subject = new TopicCreateReceipt(transactionId, status, exchangeRate, nextExchangeRate, topicId);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(topicId, subject.topicId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final Address topicId = topicIdValue();
        final TopicCreateReceipt subject = TopicCreateReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .topicId(topicId)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(topicId, subject.topicId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final TopicCreateReceipt subject = TopicCreateReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .topicId(topicIdValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TopicCreateReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new TopicCreateReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), topicIdValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new TopicCreateReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), topicIdValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new TopicCreateReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), topicIdValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new TopicCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, topicIdValue()));
    }

    @Test
    @DisplayName("rejects null for topicId")
    void shouldRejectNullTopicId() {
        assertThrows(NullPointerException.class, () -> new TopicCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), (Address) null));
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

    private static Address topicIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TopicCreateReceipt create() {
        return new TopicCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), topicIdValue());
    }
}
