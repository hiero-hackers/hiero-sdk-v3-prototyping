package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockNodeStatus`, generated from the specs.
@DisplayName("BlockNodeStatus")
class BlockNodeStatusTest {

    @Test
    @DisplayName("creates a BlockNodeStatus and returns the values")
    @Tag("testId:blocknode.status.BlockNodeStatus/create")
    void shouldCreateAndReturnTheValues() {
        final long firstAvailableBlock = firstAvailableBlockValue();
        final @Nullable Long lastAvailableBlock = lastAvailableBlockValue();
        final boolean onlyLatestState = onlyLatestStateValue();
        final @Nullable Long nextExpectedBlock = nextExpectedBlockValue();
        final BlockNodeStatus subject = new BlockNodeStatus(firstAvailableBlock, lastAvailableBlock, onlyLatestState, nextExpectedBlock);
        assertValue(firstAvailableBlock, subject.firstAvailableBlock());
        assertValue(lastAvailableBlock, subject.lastAvailableBlock());
        assertValue(onlyLatestState, subject.onlyLatestState());
        assertValue(nextExpectedBlock, subject.nextExpectedBlock());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:blocknode.status.BlockNodeStatus/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long firstAvailableBlock = firstAvailableBlockValue();
        final @Nullable Long lastAvailableBlock = lastAvailableBlockValue();
        final boolean onlyLatestState = onlyLatestStateValue();
        final @Nullable Long nextExpectedBlock = nextExpectedBlockValue();
        final BlockNodeStatus subject = BlockNodeStatus.builder()
                .firstAvailableBlock(firstAvailableBlock)
                .lastAvailableBlock(lastAvailableBlock)
                .onlyLatestState(onlyLatestState)
                .nextExpectedBlock(nextExpectedBlock)
                .build();
        assertValue(firstAvailableBlock, subject.firstAvailableBlock());
        assertValue(lastAvailableBlock, subject.lastAvailableBlock());
        assertValue(onlyLatestState, subject.onlyLatestState());
        assertValue(nextExpectedBlock, subject.nextExpectedBlock());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:blocknode.status.BlockNodeStatus/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final BlockNodeStatus subject = BlockNodeStatus.builder()
                .firstAvailableBlock(firstAvailableBlockValue())
                .onlyLatestState(onlyLatestStateValue())
                .build();
        assertNull(subject.lastAvailableBlock());
        assertNull(subject.nextExpectedBlock());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:blocknode.status.BlockNodeStatus/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> BlockNodeStatus.builder().build());
    }

    @Test
    @DisplayName("firstAvailableBlock: accepts the minimum (0)")
    @Tag("testId:blocknode.status.BlockNodeStatus/firstAvailableBlock/accepts-at-minimum")
    void shouldAcceptFirstAvailableBlockAtMinimum() {
        final long value = 0L;
        final BlockNodeStatus subject = new BlockNodeStatus(value, lastAvailableBlockValue(), onlyLatestStateValue(), nextExpectedBlockValue());
        assertValue(value, subject.firstAvailableBlock());
    }

    @Test
    @DisplayName("firstAvailableBlock: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.BlockNodeStatus/firstAvailableBlock/accepts-at-maximum")
    void shouldAcceptFirstAvailableBlockAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final BlockNodeStatus subject = new BlockNodeStatus(value, lastAvailableBlockValue(), onlyLatestStateValue(), nextExpectedBlockValue());
        assertValue(value, subject.firstAvailableBlock());
    }

    @Test
    @DisplayName("lastAvailableBlock: accepts the minimum (0)")
    @Tag("testId:blocknode.status.BlockNodeStatus/lastAvailableBlock/accepts-at-minimum")
    void shouldAcceptLastAvailableBlockAtMinimum() {
        final @Nullable Long value = 0L;
        final BlockNodeStatus subject = new BlockNodeStatus(firstAvailableBlockValue(), value, onlyLatestStateValue(), nextExpectedBlockValue());
        assertValue(value, subject.lastAvailableBlock());
    }

    @Test
    @DisplayName("lastAvailableBlock: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.BlockNodeStatus/lastAvailableBlock/accepts-at-maximum")
    void shouldAcceptLastAvailableBlockAtMaximum() {
        final @Nullable Long value = Long.parseUnsignedLong("18446744073709551615");
        final BlockNodeStatus subject = new BlockNodeStatus(firstAvailableBlockValue(), value, onlyLatestStateValue(), nextExpectedBlockValue());
        assertValue(value, subject.lastAvailableBlock());
    }

    @Test
    @DisplayName("nextExpectedBlock: accepts the minimum (0)")
    @Tag("testId:blocknode.status.BlockNodeStatus/nextExpectedBlock/accepts-at-minimum")
    void shouldAcceptNextExpectedBlockAtMinimum() {
        final @Nullable Long value = 0L;
        final BlockNodeStatus subject = new BlockNodeStatus(firstAvailableBlockValue(), lastAvailableBlockValue(), onlyLatestStateValue(), value);
        assertValue(value, subject.nextExpectedBlock());
    }

    @Test
    @DisplayName("nextExpectedBlock: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.BlockNodeStatus/nextExpectedBlock/accepts-at-maximum")
    void shouldAcceptNextExpectedBlockAtMaximum() {
        final @Nullable Long value = Long.parseUnsignedLong("18446744073709551615");
        final BlockNodeStatus subject = new BlockNodeStatus(firstAvailableBlockValue(), lastAvailableBlockValue(), onlyLatestStateValue(), value);
        assertValue(value, subject.nextExpectedBlock());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.BlockNodeStatus/value-semantics")
    void shouldCompareByValue() {
        final BlockNodeStatus subject = create();
        final BlockNodeStatus other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long firstAvailableBlockValue() {
        return 1L;
    }

    private static long lastAvailableBlockValue() {
        return 1L;
    }

    private static boolean onlyLatestStateValue() {
        return true;
    }

    private static long nextExpectedBlockValue() {
        return 1L;
    }

    private static BlockNodeStatus create() {
        return new BlockNodeStatus(firstAvailableBlockValue(), lastAvailableBlockValue(), onlyLatestStateValue(), nextExpectedBlockValue());
    }
}
