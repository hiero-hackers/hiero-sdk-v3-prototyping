package org.hiero.consensusnode.queries.receipts;

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
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `UntypedReceipt`, generated from the specs.
///
/// Not generated:
/// - no valid arguments for `typed(type<consensusnode.transactions.Transaction<$$R, ANY>>)`
@DisplayName("UntypedReceipt")
class UntypedReceiptTest {

    @Test
    @DisplayName("creates an UntypedReceipt and returns the values")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final @Nullable AccountId accountId = accountIdValue();
        final @Nullable Address fileId = fileIdValue();
        final @Nullable ContractId contractId = contractIdValue();
        final @Nullable Address topicId = topicIdValue();
        final @Nullable Long topicSequenceNumber = topicSequenceNumberValue();
        final byte @Nullable [] topicRunningHash = topicRunningHashValue();
        final @Nullable Long topicRunningHashVersion = topicRunningHashVersionValue();
        final @Nullable Address tokenId = tokenIdValue();
        final @Nullable Long totalSupply = totalSupplyValue();
        final @Nullable Address scheduleId = scheduleIdValue();
        final @Nullable TransactionId scheduledTransactionId = scheduledTransactionIdValue();
        final List<Long> serials = serialsValue();
        final @Nullable Long nodeId = nodeIdValue();
        final @Nullable Long registeredNodeId = registeredNodeIdValue();
        final @Nullable Long blockNumber = blockNumberValue();
        final UntypedReceipt subject = new UntypedReceipt(transactionId, status, exchangeRate, nextExchangeRate, accountId, fileId, contractId, topicId, topicSequenceNumber, topicRunningHash, topicRunningHashVersion, tokenId, totalSupply, scheduleId, scheduledTransactionId, serials, nodeId, registeredNodeId, blockNumber);
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(accountId, subject.accountId());
        assertValue(fileId, subject.fileId());
        assertValue(contractId, subject.contractId());
        assertValue(topicId, subject.topicId());
        assertValue(topicSequenceNumber, subject.topicSequenceNumber());
        assertValue(topicRunningHash, subject.topicRunningHash());
        assertValue(topicRunningHashVersion, subject.topicRunningHashVersion());
        assertValue(tokenId, subject.tokenId());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(scheduleId, subject.scheduleId());
        assertValue(scheduledTransactionId, subject.scheduledTransactionId());
        assertValue(serials, subject.serials());
        assertValue(nodeId, subject.nodeId());
        assertValue(registeredNodeId, subject.registeredNodeId());
        assertValue(blockNumber, subject.blockNumber());
    }

    @Test
    @DisplayName("creates an UntypedReceipt with the default values")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        assertValue(List.of(), subject.serials());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final TransactionStatus status = statusValue();
        final ExchangeRate exchangeRate = exchangeRateValue();
        final ExchangeRate nextExchangeRate = nextExchangeRateValue();
        final @Nullable AccountId accountId = accountIdValue();
        final @Nullable Address fileId = fileIdValue();
        final @Nullable ContractId contractId = contractIdValue();
        final @Nullable Address topicId = topicIdValue();
        final @Nullable Long topicSequenceNumber = topicSequenceNumberValue();
        final byte @Nullable [] topicRunningHash = topicRunningHashValue();
        final @Nullable Long topicRunningHashVersion = topicRunningHashVersionValue();
        final @Nullable Address tokenId = tokenIdValue();
        final @Nullable Long totalSupply = totalSupplyValue();
        final @Nullable Address scheduleId = scheduleIdValue();
        final @Nullable TransactionId scheduledTransactionId = scheduledTransactionIdValue();
        final List<Long> serials = serialsValue();
        final @Nullable Long nodeId = nodeIdValue();
        final @Nullable Long registeredNodeId = registeredNodeIdValue();
        final @Nullable Long blockNumber = blockNumberValue();
        final UntypedReceipt subject = UntypedReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .exchangeRate(exchangeRate)
                .nextExchangeRate(nextExchangeRate)
                .accountId(accountId)
                .fileId(fileId)
                .contractId(contractId)
                .topicId(topicId)
                .topicSequenceNumber(topicSequenceNumber)
                .topicRunningHash(topicRunningHash)
                .topicRunningHashVersion(topicRunningHashVersion)
                .tokenId(tokenId)
                .totalSupply(totalSupply)
                .scheduleId(scheduleId)
                .scheduledTransactionId(scheduledTransactionId)
                .serials(serials)
                .nodeId(nodeId)
                .registeredNodeId(registeredNodeId)
                .blockNumber(blockNumber)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(status, subject.status());
        assertValue(exchangeRate, subject.exchangeRate());
        assertValue(nextExchangeRate, subject.nextExchangeRate());
        assertValue(accountId, subject.accountId());
        assertValue(fileId, subject.fileId());
        assertValue(contractId, subject.contractId());
        assertValue(topicId, subject.topicId());
        assertValue(topicSequenceNumber, subject.topicSequenceNumber());
        assertValue(topicRunningHash, subject.topicRunningHash());
        assertValue(topicRunningHashVersion, subject.topicRunningHashVersion());
        assertValue(tokenId, subject.tokenId());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(scheduleId, subject.scheduleId());
        assertValue(scheduledTransactionId, subject.scheduledTransactionId());
        assertValue(serials, subject.serials());
        assertValue(nodeId, subject.nodeId());
        assertValue(registeredNodeId, subject.registeredNodeId());
        assertValue(blockNumber, subject.blockNumber());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final UntypedReceipt subject = UntypedReceipt.builder()
                .transactionId(transactionIdValue())
                .status(statusValue())
                .exchangeRate(exchangeRateValue())
                .nextExchangeRate(nextExchangeRateValue())
                .build();
        assertNull(subject.accountId());
        assertNull(subject.fileId());
        assertNull(subject.contractId());
        assertNull(subject.topicId());
        assertNull(subject.topicSequenceNumber());
        assertNull(subject.topicRunningHash());
        assertNull(subject.topicRunningHashVersion());
        assertNull(subject.tokenId());
        assertNull(subject.totalSupply());
        assertNull(subject.scheduleId());
        assertNull(subject.scheduledTransactionId());
        assertValue(List.of(), subject.serials());
        assertNull(subject.nodeId());
        assertNull(subject.registeredNodeId());
        assertNull(subject.blockNumber());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> UntypedReceipt.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new UntypedReceipt((TransactionId) null, statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new UntypedReceipt(transactionIdValue(), (TransactionStatus) null, exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue()));
    }

    @Test
    @DisplayName("rejects null for exchangeRate")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/exchangeRate/rejects-null")
    void shouldRejectNullExchangeRate() {
        assertThrows(NullPointerException.class, () -> new UntypedReceipt(transactionIdValue(), statusValue(), (ExchangeRate) null, nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue()));
    }

    @Test
    @DisplayName("rejects null for nextExchangeRate")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/nextExchangeRate/rejects-null")
    void shouldRejectNullNextExchangeRate() {
        assertThrows(NullPointerException.class, () -> new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), (ExchangeRate) null, accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue()));
    }

    @Test
    @DisplayName("topicSequenceNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/topicSequenceNumber/accepts-at-minimum")
    void shouldAcceptTopicSequenceNumberAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), value, topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.topicSequenceNumber());
    }

    @Test
    @DisplayName("topicSequenceNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/topicSequenceNumber/accepts-at-maximum")
    void shouldAcceptTopicSequenceNumberAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), value, topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.topicSequenceNumber());
    }

    @Test
    @DisplayName("the constructor copies topicRunningHash; the accessor returns a copy")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/topicRunningHash/copies-in")
    void shouldCopyTopicRunningHashIn() {
        final byte[] value = topicRunningHashValue();
        final byte[] expected = value.clone();
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), value, topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.topicRunningHash());
        final byte[] returned = subject.topicRunningHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.topicRunningHash());
    }

    @Test
    @DisplayName("topicRunningHashVersion: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/topicRunningHashVersion/accepts-at-minimum")
    void shouldAcceptTopicRunningHashVersionAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), value, tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.topicRunningHashVersion());
    }

    @Test
    @DisplayName("topicRunningHashVersion: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/topicRunningHashVersion/accepts-at-maximum")
    void shouldAcceptTopicRunningHashVersionAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), value, tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.topicRunningHashVersion());
    }

    @Test
    @DisplayName("totalSupply: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/totalSupply/accepts-at-minimum")
    void shouldAcceptTotalSupplyAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), value, scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/totalSupply/accepts-at-maximum")
    void shouldAcceptTotalSupplyAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), value, scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("rejects null for serials")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/serials/rejects-null")
    void shouldRejectNullSerials() {
        assertThrows(NullPointerException.class, () -> new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), (List<Long>) null, nodeIdValue(), registeredNodeIdValue(), blockNumberValue()));
    }

    @Test
    @DisplayName("the constructor copies serials; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/serials/copies-in")
    void shouldCopySerialsIn() {
        final List<Long> value = new ArrayList<>(serialsValue());
        final List<Long> expected = List.copyOf(value);
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), value, nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
        value.clear();
        assertValue(expected, subject.serials());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serials()).clear());
    }

    @Test
    @DisplayName("nodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/nodeId/accepts-at-minimum")
    void shouldAcceptNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), value, registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("nodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/nodeId/accepts-at-maximum")
    void shouldAcceptNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), value, registeredNodeIdValue(), blockNumberValue());
        assertValue(value, subject.nodeId());
    }

    @Test
    @DisplayName("registeredNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/registeredNodeId/accepts-at-minimum")
    void shouldAcceptRegisteredNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), value, blockNumberValue());
        assertValue(value, subject.registeredNodeId());
    }

    @Test
    @DisplayName("registeredNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/registeredNodeId/accepts-at-maximum")
    void shouldAcceptRegisteredNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), value, blockNumberValue());
        assertValue(value, subject.registeredNodeId());
    }

    @Test
    @DisplayName("blockNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/blockNumber/accepts-at-minimum")
    void shouldAcceptBlockNumberAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), value);
        assertValue(value, subject.blockNumber());
    }

    @Test
    @DisplayName("blockNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.UntypedReceipt/blockNumber/accepts-at-maximum")
    void shouldAcceptBlockNumberAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final UntypedReceipt subject = new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), value);
        assertValue(value, subject.blockNumber());
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

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static Address topicIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long topicSequenceNumberValue() {
        return 1L;
    }

    private static byte[] topicRunningHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static long topicRunningHashVersionValue() {
        return 1L;
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long totalSupplyValue() {
        return 1L;
    }

    private static Address scheduleIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TransactionId scheduledTransactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static List<Long> serialsValue() {
        return List.of(1L);
    }

    private static long nodeIdValue() {
        return 1L;
    }

    private static long registeredNodeIdValue() {
        return 1L;
    }

    private static long blockNumberValue() {
        return 1L;
    }

    private static UntypedReceipt create() {
        return new UntypedReceipt(transactionIdValue(), statusValue(), exchangeRateValue(), nextExchangeRateValue(), accountIdValue(), fileIdValue(), contractIdValue(), topicIdValue(), topicSequenceNumberValue(), topicRunningHashValue(), topicRunningHashVersionValue(), tokenIdValue(), totalSupplyValue(), scheduleIdValue(), scheduledTransactionIdValue(), serialsValue(), nodeIdValue(), registeredNodeIdValue(), blockNumberValue());
    }
}
