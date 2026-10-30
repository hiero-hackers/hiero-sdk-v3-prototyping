package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractLog`, generated from the specs.
@DisplayName("ContractLog")
class ContractLogTest {

    @Test
    @DisplayName("creates a ContractLog and returns the values")
    @Tag("testId:mirrornode.contract.ContractLog/create")
    void shouldCreateAndReturnTheValues() {
        final EvmAddress address = addressValue();
        final @Nullable String bloom = bloomValue();
        final @Nullable ContractId contractId = contractIdValue();
        final @Nullable String data = dataValue();
        final int index = indexValue();
        final List<String> topics = topicsValue();
        final @Nullable String blockHash = blockHashValue();
        final @Nullable Long blockNumber = blockNumberValue();
        final @Nullable ContractId rootContractId = rootContractIdValue();
        final @Nullable ZonedDateTime timestamp = timestampValue();
        final @Nullable String transactionHash = transactionHashValue();
        final @Nullable Integer transactionIndex = transactionIndexValue();
        final ContractLog subject = new ContractLog(address, bloom, contractId, data, index, topics, blockHash, blockNumber, rootContractId, timestamp, transactionHash, transactionIndex);
        assertValue(address, subject.address());
        assertValue(bloom, subject.bloom());
        assertValue(contractId, subject.contractId());
        assertValue(data, subject.data());
        assertValue(index, subject.index());
        assertValue(topics, subject.topics());
        assertValue(blockHash, subject.blockHash());
        assertValue(blockNumber, subject.blockNumber());
        assertValue(rootContractId, subject.rootContractId());
        assertValue(timestamp, subject.timestamp());
        assertValue(transactionHash, subject.transactionHash());
        assertValue(transactionIndex, subject.transactionIndex());
    }

    @Test
    @DisplayName("creates a ContractLog with the default values")
    @Tag("testId:mirrornode.contract.ContractLog/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue());
        assertValue(List.of(), subject.topics());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.ContractLog/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final EvmAddress address = addressValue();
        final @Nullable String bloom = bloomValue();
        final @Nullable ContractId contractId = contractIdValue();
        final @Nullable String data = dataValue();
        final int index = indexValue();
        final List<String> topics = topicsValue();
        final @Nullable String blockHash = blockHashValue();
        final @Nullable Long blockNumber = blockNumberValue();
        final @Nullable ContractId rootContractId = rootContractIdValue();
        final @Nullable ZonedDateTime timestamp = timestampValue();
        final @Nullable String transactionHash = transactionHashValue();
        final @Nullable Integer transactionIndex = transactionIndexValue();
        final ContractLog subject = ContractLog.builder()
                .address(address)
                .bloom(bloom)
                .contractId(contractId)
                .data(data)
                .index(index)
                .topics(topics)
                .blockHash(blockHash)
                .blockNumber(blockNumber)
                .rootContractId(rootContractId)
                .timestamp(timestamp)
                .transactionHash(transactionHash)
                .transactionIndex(transactionIndex)
                .build();
        assertValue(address, subject.address());
        assertValue(bloom, subject.bloom());
        assertValue(contractId, subject.contractId());
        assertValue(data, subject.data());
        assertValue(index, subject.index());
        assertValue(topics, subject.topics());
        assertValue(blockHash, subject.blockHash());
        assertValue(blockNumber, subject.blockNumber());
        assertValue(rootContractId, subject.rootContractId());
        assertValue(timestamp, subject.timestamp());
        assertValue(transactionHash, subject.transactionHash());
        assertValue(transactionIndex, subject.transactionIndex());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.ContractLog/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractLog subject = ContractLog.builder()
                .address(addressValue())
                .index(indexValue())
                .build();
        assertNull(subject.bloom());
        assertNull(subject.contractId());
        assertNull(subject.data());
        assertValue(List.of(), subject.topics());
        assertNull(subject.blockHash());
        assertNull(subject.blockNumber());
        assertNull(subject.rootContractId());
        assertNull(subject.timestamp());
        assertNull(subject.transactionHash());
        assertNull(subject.transactionIndex());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.ContractLog/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractLog.builder().build());
    }

    @Test
    @DisplayName("rejects null for address")
    @Tag("testId:mirrornode.contract.ContractLog/address/rejects-null")
    void shouldRejectNullAddress() {
        assertThrows(NullPointerException.class, () -> new ContractLog((EvmAddress) null, bloomValue(), contractIdValue(), dataValue(), indexValue(), topicsValue(), blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue()));
    }

    @Test
    @DisplayName("index: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.ContractLog/index/accepts-at-minimum")
    void shouldAcceptIndexAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), value, topicsValue(), blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue());
        assertValue(value, subject.index());
    }

    @Test
    @DisplayName("index: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.ContractLog/index/accepts-at-maximum")
    void shouldAcceptIndexAtMaximum() {
        final int value = 2147483647;
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), value, topicsValue(), blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue());
        assertValue(value, subject.index());
    }

    @Test
    @DisplayName("rejects null for topics")
    @Tag("testId:mirrornode.contract.ContractLog/topics/rejects-null")
    void shouldRejectNullTopics() {
        assertThrows(NullPointerException.class, () -> new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), (List<String>) null, blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue()));
    }

    @Test
    @DisplayName("the constructor copies topics; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.ContractLog/topics/copies-in")
    void shouldCopyTopicsIn() {
        final List<String> value = new ArrayList<>(topicsValue());
        final List<String> expected = List.copyOf(value);
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), value, blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue());
        value.clear();
        assertValue(expected, subject.topics());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.topics()).clear());
    }

    @Test
    @DisplayName("blockNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractLog/blockNumber/accepts-at-minimum")
    void shouldAcceptBlockNumberAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), topicsValue(), blockHashValue(), value, rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue());
        assertValue(value, subject.blockNumber());
    }

    @Test
    @DisplayName("blockNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractLog/blockNumber/accepts-at-maximum")
    void shouldAcceptBlockNumberAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), topicsValue(), blockHashValue(), value, rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue());
        assertValue(value, subject.blockNumber());
    }

    @Test
    @DisplayName("transactionIndex: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.ContractLog/transactionIndex/accepts-at-minimum")
    void shouldAcceptTransactionIndexAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), topicsValue(), blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), value);
        assertValue(value, subject.transactionIndex());
    }

    @Test
    @DisplayName("transactionIndex: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.ContractLog/transactionIndex/accepts-at-maximum")
    void shouldAcceptTransactionIndexAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final ContractLog subject = new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), topicsValue(), blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), value);
        assertValue(value, subject.transactionIndex());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.ContractLog/value-semantics")
    void shouldCompareByValue() {
        final ContractLog subject = create();
        final ContractLog other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static String bloomValue() {
        return "value";
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static String dataValue() {
        return "value";
    }

    private static int indexValue() {
        return 1;
    }

    private static List<String> topicsValue() {
        return List.of("value");
    }

    private static String blockHashValue() {
        return "value";
    }

    private static long blockNumberValue() {
        return 1L;
    }

    private static ContractId rootContractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String transactionHashValue() {
        return "value";
    }

    private static int transactionIndexValue() {
        return 1;
    }

    private static ContractLog create() {
        return new ContractLog(addressValue(), bloomValue(), contractIdValue(), dataValue(), indexValue(), topicsValue(), blockHashValue(), blockNumberValue(), rootContractIdValue(), timestampValue(), transactionHashValue(), transactionIndexValue());
    }
}
