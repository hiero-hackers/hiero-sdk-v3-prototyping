package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `HookStorage`, generated from the specs.
@DisplayName("HookStorage")
class HookStorageTest {

    @Test
    @DisplayName("creates a HookStorage and returns the values")
    @Tag("testId:mirrornode.account.HookStorage/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] key = keyValue();
        final byte @Nullable [] valueArgument = valueValue();
        final ZonedDateTime timestamp = timestampValue();
        final HookStorage subject = new HookStorage(key, valueArgument, timestamp);
        assertValue(key, subject.key());
        assertValue(valueArgument, subject.value());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("rejects null for key")
    @Tag("testId:mirrornode.account.HookStorage/key/rejects-null")
    void shouldRejectNullKey() {
        assertThrows(NullPointerException.class, () -> new HookStorage((byte[]) null, valueValue(), timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies key; the accessor returns a copy")
    @Tag("testId:mirrornode.account.HookStorage/key/copies-in")
    void shouldCopyKeyIn() {
        final byte[] value = keyValue();
        final byte[] expected = value.clone();
        final HookStorage subject = new HookStorage(value, valueValue(), timestampValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.key());
        final byte[] returned = subject.key();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.key());
    }

    @Test
    @DisplayName("the constructor copies value; the accessor returns a copy")
    @Tag("testId:mirrornode.account.HookStorage/value/copies-in")
    void shouldCopyValueIn() {
        final byte[] value = valueValue();
        final byte[] expected = value.clone();
        final HookStorage subject = new HookStorage(keyValue(), value, timestampValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.value());
        final byte[] returned = subject.value();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.value());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.account.HookStorage/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new HookStorage(keyValue(), valueValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.account.HookStorage/value-semantics")
    void shouldCompareByValue() {
        final HookStorage subject = create();
        final HookStorage other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static byte[] keyValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] valueValue() {
        return new byte[] {1, 2, 3};
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static HookStorage create() {
        return new HookStorage(keyValue(), valueValue(), timestampValue());
    }
}
