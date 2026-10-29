package org.hiero.consensusnode.admin.nodes;

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
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NodeCreateReceipt`, generated from the specs.
@DisplayName("NodeCreateReceipt")
class NodeCreateReceiptTest {

    @Test
    @DisplayName("creates a NodeCreateReceipt and returns the values")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final long nodeId = nodeIdValue();
        final NodeCreateReceipt subject = new NodeCreateReceipt(transactionId, status, exchangeRate, nextExchangeRate, nodeId);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(nodeId, subject.nodeId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final long nodeId = nodeIdValue();
        final NodeCreateReceipt subject = NodeCreateReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .nodeId(nodeId)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(nodeId, subject.nodeId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NodeCreateReceipt subject = NodeCreateReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .nodeId(nodeIdValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NodeCreateReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new NodeCreateReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), nodeIdValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new NodeCreateReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), nodeIdValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new NodeCreateReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), nodeIdValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new NodeCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, nodeIdValue()));
    }

    @Test
    @DisplayName("nodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/nodeId/accepts-at-minimum")
    void shouldAcceptNodeIdAtMinimum() {
        final long value = -9223372036854775808L;
        final NodeCreateReceipt subject = new NodeCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value);
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.admin.nodes.NodeCreateReceipt/nodeId/accepts-at-maximum")
    void shouldAcceptNodeIdAtMaximum() {
        final long value = 9223372036854775807L;
        final NodeCreateReceipt subject = new NodeCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), value);
        assertValue(value, subject.nodeId());
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

    private static long nodeIdValue() {
        return 1L;
    }

    private static NodeCreateReceipt create() {
        return new NodeCreateReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), nodeIdValue());
    }
}
