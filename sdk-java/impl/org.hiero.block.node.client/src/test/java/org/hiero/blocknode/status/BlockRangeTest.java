package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockRange`, generated from the specs.
@DisplayName("BlockRange")
class BlockRangeTest {

    @Test
    @DisplayName("creates a BlockRange and returns the values")
    @Tag("testId:blocknode.status.BlockRange/create")
    void shouldCreateAndReturnTheValues() {
        final long start = startValue();
        final long end = endValue();
        final BlockRange subject = new BlockRange(start, end);
        assertValue(start, subject.start());
        assertValue(end, subject.end());
    }

    @Test
    @DisplayName("start: accepts the minimum (0)")
    @Tag("testId:blocknode.status.BlockRange/start/accepts-at-minimum")
    void shouldAcceptStartAtMinimum() {
        final long value = 0L;
        final BlockRange subject = new BlockRange(value, endValue());
        assertValue(value, subject.start());
    }

    @Test
    @DisplayName("start: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.BlockRange/start/accepts-at-maximum")
    void shouldAcceptStartAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final BlockRange subject = new BlockRange(value, endValue());
        assertValue(value, subject.start());
    }

    @Test
    @DisplayName("end: accepts the minimum (0)")
    @Tag("testId:blocknode.status.BlockRange/end/accepts-at-minimum")
    void shouldAcceptEndAtMinimum() {
        final long value = 0L;
        final BlockRange subject = new BlockRange(startValue(), value);
        assertValue(value, subject.end());
    }

    @Test
    @DisplayName("end: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.BlockRange/end/accepts-at-maximum")
    void shouldAcceptEndAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final BlockRange subject = new BlockRange(startValue(), value);
        assertValue(value, subject.end());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.BlockRange/value-semantics")
    void shouldCompareByValue() {
        final BlockRange subject = create();
        final BlockRange other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long startValue() {
        return 1L;
    }

    private static long endValue() {
        return 1L;
    }

    private static BlockRange create() {
        return new BlockRange(startValue(), endValue());
    }

    private static BlockRange defaultSubject() {
        return new BlockRange(0L, 100L);
    }
}
