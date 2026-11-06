package org.hiero.consensusnode.transactions;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractLog`, generated from the specs.
@DisplayName("ContractLog")
class ContractLogTest {

    @Test
    @DisplayName("creates a ContractLog and returns the values")
    @Tag("testId:consensusnode.transactions.ContractLog/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final byte[] bloom = bloomValue();
        final List<byte[]> topics = topicsValue();
        final byte[] data = dataValue();
        final ContractLog subject = new ContractLog(contractId, bloom, topics, data);
        assertValue(contractId, subject.contractId());
        assertValue(bloom, subject.bloom());
        assertValue(topics, subject.topics());
        assertValue(data, subject.data());
    }

    @Test
    @DisplayName("creates a ContractLog with the default values")
    @Tag("testId:consensusnode.transactions.ContractLog/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractLog subject = new ContractLog(contractIdValue(), bloomValue(), dataValue());
        assertValue(List.of(), subject.topics());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.transactions.ContractLog/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractLog((ContractId) null, bloomValue(), topicsValue(), dataValue()));
    }

    @Test
    @DisplayName("rejects null for bloom")
    @Tag("testId:consensusnode.transactions.ContractLog/bloom/rejects-null")
    void shouldRejectNullBloom() {
        assertThrows(NullPointerException.class, () -> new ContractLog(contractIdValue(), (byte[]) null, topicsValue(), dataValue()));
    }

    @Test
    @DisplayName("the constructor copies bloom; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.ContractLog/bloom/copies-in")
    void shouldCopyBloomIn() {
        final byte[] value = bloomValue();
        final byte[] expected = value.clone();
        final ContractLog subject = new ContractLog(contractIdValue(), value, topicsValue(), dataValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bloom());
        final byte[] returned = subject.bloom();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bloom());
    }

    @Test
    @DisplayName("rejects null for topics")
    @Tag("testId:consensusnode.transactions.ContractLog/topics/rejects-null")
    void shouldRejectNullTopics() {
        assertThrows(NullPointerException.class, () -> new ContractLog(contractIdValue(), bloomValue(), (List<byte[]>) null, dataValue()));
    }

    @Test
    @DisplayName("the constructor copies topics; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.transactions.ContractLog/topics/copies-in")
    void shouldCopyTopicsIn() {
        final List<byte[]> value = new ArrayList<>(topicsValue());
        final List<byte[]> expected = List.copyOf(value);
        final ContractLog subject = new ContractLog(contractIdValue(), bloomValue(), value, dataValue());
        value.clear();
        assertValue(expected, subject.topics());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.topics()).clear());
    }

    @Test
    @DisplayName("rejects null for data")
    @Tag("testId:consensusnode.transactions.ContractLog/data/rejects-null")
    void shouldRejectNullData() {
        assertThrows(NullPointerException.class, () -> new ContractLog(contractIdValue(), bloomValue(), topicsValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies data; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.ContractLog/data/copies-in")
    void shouldCopyDataIn() {
        final byte[] value = dataValue();
        final byte[] expected = value.clone();
        final ContractLog subject = new ContractLog(contractIdValue(), bloomValue(), topicsValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.data());
        final byte[] returned = subject.data();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.data());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.transactions.ContractLog/value-semantics")
    void shouldCompareByValue() {
        final ContractLog subject = create();
        final ContractLog other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static byte[] bloomValue() {
        return new byte[] {1, 2, 3};
    }

    private static List<byte[]> topicsValue() {
        return List.of(new byte[] {1, 2, 3});
    }

    private static byte[] dataValue() {
        return new byte[] {1, 2, 3};
    }

    private static ContractLog create() {
        return new ContractLog(contractIdValue(), bloomValue(), topicsValue(), dataValue());
    }
}
