package org.hiero.mirrornode.common;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TimestampRange`, generated from the specs.
@DisplayName("TimestampRange")
class TimestampRangeTest {

    @Test
    @DisplayName("creates a TimestampRange and returns the values")
    @Tag("testId:mirrornode.common.TimestampRange/create")
    void shouldCreateAndReturnTheValues() {
        final ZonedDateTime from = fromValue();
        final @Nullable ZonedDateTime to = toValue();
        final TimestampRange subject = new TimestampRange(from, to);
        assertValue(from, subject.from());
        assertValue(to, subject.to());
    }

    @Test
    @DisplayName("rejects null for from")
    @Tag("testId:mirrornode.common.TimestampRange/from/rejects-null")
    void shouldRejectNullFrom() {
        assertThrows(NullPointerException.class, () -> new TimestampRange((ZonedDateTime) null, toValue()));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.common.TimestampRange/value-semantics")
    void shouldCompareByValue() {
        final TimestampRange subject = create();
        final TimestampRange other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ZonedDateTime fromValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime toValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static TimestampRange create() {
        return new TimestampRange(fromValue(), toValue());
    }
}
