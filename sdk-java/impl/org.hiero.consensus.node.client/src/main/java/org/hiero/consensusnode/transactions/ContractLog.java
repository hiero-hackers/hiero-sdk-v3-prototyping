package org.hiero.consensusnode.transactions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.jspecify.annotations.Nullable;

/// A log emitted by a contract during an execution (an event in Solidity). Decode it with `AbiEvent.decodeLog(topics,
/// data)`.
///
/// @param contractId the contract that emitted the log
/// @param bloom the 256-byte bloom filter of the log
/// @param topics the topics, 32 bytes each; the first is the event's topic unless it is anonymous
/// @param data the non-indexed parameters, ABI-encoded
public record ContractLog(ContractId contractId, byte[] bloom, List<byte[]> topics, byte[] data) {

    /// Creates a new `ContractLog`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractLog {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(bloom, "bloom must not be null");
        bloom = bloom.clone();
        Objects.requireNonNull(topics, "topics must not be null");
        topics = topics.stream().map(byte[]::clone).toList();
        Objects.requireNonNull(data, "data must not be null");
        data = data.clone();
    }

    /// Creates a new `ContractLog` with the default value of `topics`.
    public ContractLog(final ContractId contractId, final byte[] bloom, final byte[] data) {
        this(contractId, bloom, List.of(), data);
    }

    /// the 256-byte bloom filter of the log
    ///
    /// Returns a copy of the `bloom` array.
    @Override
    public byte[] bloom() {
        return bloom.clone();
    }

    /// the topics, 32 bytes each; the first is the event's topic unless it is anonymous
    ///
    /// Returns a copy of the `topics` arrays.
    @Override
    public List<byte[]> topics() {
        return topics.stream().map(byte[]::clone).toList();
    }

    /// the non-indexed parameters, ABI-encoded
    ///
    /// Returns a copy of the `data` array.
    @Override
    public byte[] data() {
        return data.clone();
    }

    /// Compares `bloom`, `topics` and `data` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ContractLog other
            && Objects.equals(contractId, other.contractId)
            && Arrays.equals(bloom, other.bloom)
            && Arrays.deepEquals(topics.toArray(), other.topics.toArray())
            && Arrays.equals(data, other.data);
    }

    /// Hashes `bloom`, `topics` and `data` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(contractId, Arrays.hashCode(bloom), Arrays.deepHashCode(topics.toArray()), Arrays.hashCode(data));
    }

    /// Shows only the length of `bloom`, `topics` and `data` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ContractLog[contractId=" + contractId + ", bloom=" + "byte[" + bloom.length + "]" + ", topics=" + topics.stream().map(b -> "byte[" + b.length + "]").toList() + ", data=" + "byte[" + data.length + "]" + "]";
    }
}
