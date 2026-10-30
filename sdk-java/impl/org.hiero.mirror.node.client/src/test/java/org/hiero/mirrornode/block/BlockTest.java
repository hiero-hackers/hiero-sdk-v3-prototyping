package org.hiero.mirrornode.block;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Block`, generated from the specs.
@DisplayName("Block")
class BlockTest {

    @Test
    @DisplayName("creates a Block and returns the values")
    @Tag("testId:mirrornode.block.Block/create")
    void shouldCreateAndReturnTheValues() {
        final long count = countValue();
        final @Nullable Long gasUsed = gasUsedValue();
        final @Nullable String hapiVersion = hapiVersionValue();
        final byte[] hash = hashValue();
        final byte @Nullable [] logsBloom = logsBloomValue();
        final String name = nameValue();
        final long number = numberValue();
        final byte[] previousHash = previousHashValue();
        final @Nullable Long size = sizeValue();
        final TimestampRange timestamp = timestampValue();
        final Block subject = new Block(count, gasUsed, hapiVersion, hash, logsBloom, name, number, previousHash, size, timestamp);
        assertValue(count, subject.count());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(hapiVersion, subject.hapiVersion());
        assertValue(hash, subject.hash());
        assertValue(logsBloom, subject.logsBloom());
        assertValue(name, subject.name());
        assertValue(number, subject.number());
        assertValue(previousHash, subject.previousHash());
        assertValue(size, subject.size());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.block.Block/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long count = countValue();
        final @Nullable Long gasUsed = gasUsedValue();
        final @Nullable String hapiVersion = hapiVersionValue();
        final byte[] hash = hashValue();
        final byte @Nullable [] logsBloom = logsBloomValue();
        final String name = nameValue();
        final long number = numberValue();
        final byte[] previousHash = previousHashValue();
        final @Nullable Long size = sizeValue();
        final TimestampRange timestamp = timestampValue();
        final Block subject = Block.builder()
                .count(count)
                .gasUsed(gasUsed)
                .hapiVersion(hapiVersion)
                .hash(hash)
                .logsBloom(logsBloom)
                .name(name)
                .number(number)
                .previousHash(previousHash)
                .size(size)
                .timestamp(timestamp)
                .build();
        assertValue(count, subject.count());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(hapiVersion, subject.hapiVersion());
        assertValue(hash, subject.hash());
        assertValue(logsBloom, subject.logsBloom());
        assertValue(name, subject.name());
        assertValue(number, subject.number());
        assertValue(previousHash, subject.previousHash());
        assertValue(size, subject.size());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.block.Block/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Block subject = Block.builder()
                .count(countValue())
                .hash(hashValue())
                .name(nameValue())
                .number(numberValue())
                .previousHash(previousHashValue())
                .timestamp(timestampValue())
                .build();
        assertNull(subject.gasUsed());
        assertNull(subject.hapiVersion());
        assertNull(subject.logsBloom());
        assertNull(subject.size());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.block.Block/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Block.builder().build());
    }

    @Test
    @DisplayName("count: accepts the minimum (0)")
    @Tag("testId:mirrornode.block.Block/count/accepts-at-minimum")
    void shouldAcceptCountAtMinimum() {
        final long value = 0L;
        final Block subject = new Block(value, gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue());
        assertValue(value, subject.count());
    }

    @Test
    @DisplayName("count: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.block.Block/count/accepts-at-maximum")
    void shouldAcceptCountAtMaximum() {
        final long value = 9223372036854775807L;
        final Block subject = new Block(value, gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue());
        assertValue(value, subject.count());
    }

    @Test
    @DisplayName("count: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.block.Block/count/rejects-below-minimum")
    void shouldRejectCountBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new Block(-1L, gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue()));
    }

    @Test
    @DisplayName("gasUsed: accepts the minimum (0)")
    @Tag("testId:mirrornode.block.Block/gasUsed/accepts-at-minimum")
    void shouldAcceptGasUsedAtMinimum() {
        final @Nullable Long value = 0L;
        final Block subject = new Block(countValue(), value, hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("gasUsed: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.block.Block/gasUsed/accepts-at-maximum")
    void shouldAcceptGasUsedAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final Block subject = new Block(countValue(), value, hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("gasUsed: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.block.Block/gasUsed/rejects-below-minimum")
    void shouldRejectGasUsedBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new Block(countValue(), -1L, hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for hash")
    @Tag("testId:mirrornode.block.Block/hash/rejects-null")
    void shouldRejectNullHash() {
        assertThrows(NullPointerException.class, () -> new Block(countValue(), gasUsedValue(), hapiVersionValue(), (byte[]) null, logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies hash; the accessor returns a copy")
    @Tag("testId:mirrornode.block.Block/hash/copies-in")
    void shouldCopyHashIn() {
        final byte[] value = hashValue();
        final byte[] expected = value.clone();
        final Block subject = new Block(countValue(), gasUsedValue(), hapiVersionValue(), value, logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.hash());
        final byte[] returned = subject.hash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.hash());
    }

    @Test
    @DisplayName("the constructor copies logsBloom; the accessor returns a copy")
    @Tag("testId:mirrornode.block.Block/logsBloom/copies-in")
    void shouldCopyLogsBloomIn() {
        final byte[] value = logsBloomValue();
        final byte[] expected = value.clone();
        final Block subject = new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), value, nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.logsBloom());
        final byte[] returned = subject.logsBloom();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.logsBloom());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:mirrornode.block.Block/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), (String) null, numberValue(), previousHashValue(), sizeValue(), timestampValue()));
    }

    @Test
    @DisplayName("number: accepts the minimum (0)")
    @Tag("testId:mirrornode.block.Block/number/accepts-at-minimum")
    void shouldAcceptNumberAtMinimum() {
        final long value = 0L;
        final Block subject = new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), value, previousHashValue(), sizeValue(), timestampValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("number: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.block.Block/number/accepts-at-maximum")
    void shouldAcceptNumberAtMaximum() {
        final long value = 9223372036854775807L;
        final Block subject = new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), value, previousHashValue(), sizeValue(), timestampValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("number: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.block.Block/number/rejects-below-minimum")
    void shouldRejectNumberBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), -1L, previousHashValue(), sizeValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for previousHash")
    @Tag("testId:mirrornode.block.Block/previousHash/rejects-null")
    void shouldRejectNullPreviousHash() {
        assertThrows(NullPointerException.class, () -> new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), (byte[]) null, sizeValue(), timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies previousHash; the accessor returns a copy")
    @Tag("testId:mirrornode.block.Block/previousHash/copies-in")
    void shouldCopyPreviousHashIn() {
        final byte[] value = previousHashValue();
        final byte[] expected = value.clone();
        final Block subject = new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), value, sizeValue(), timestampValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.previousHash());
        final byte[] returned = subject.previousHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.previousHash());
    }

    @Test
    @DisplayName("size: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.block.Block/size/accepts-at-minimum")
    void shouldAcceptSizeAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final Block subject = new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), value, timestampValue());
        assertValue(value, subject.size());
    }

    @Test
    @DisplayName("size: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.block.Block/size/accepts-at-maximum")
    void shouldAcceptSizeAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final Block subject = new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), value, timestampValue());
        assertValue(value, subject.size());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.block.Block/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), (TimestampRange) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.block.Block/value-semantics")
    void shouldCompareByValue() {
        final Block subject = create();
        final Block other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long countValue() {
        return 1L;
    }

    private static long gasUsedValue() {
        return 1L;
    }

    private static String hapiVersionValue() {
        return "value";
    }

    private static byte[] hashValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] logsBloomValue() {
        return new byte[] {1, 2, 3};
    }

    private static String nameValue() {
        return "value";
    }

    private static long numberValue() {
        return 1L;
    }

    private static byte[] previousHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static long sizeValue() {
        return 1L;
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static Block create() {
        return new Block(countValue(), gasUsedValue(), hapiVersionValue(), hashValue(), logsBloomValue(), nameValue(), numberValue(), previousHashValue(), sizeValue(), timestampValue());
    }
}
