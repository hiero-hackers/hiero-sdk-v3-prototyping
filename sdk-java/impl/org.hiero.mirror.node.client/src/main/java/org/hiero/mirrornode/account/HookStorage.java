package org.hiero.mirrornode.account;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A storage slot of a hook. GET /accounts/{id}/hooks/{hookId}/storage.
///
/// @param key the 32-byte storage key
/// @param value the stored value; null if the slot was cleared
/// @param timestamp consensus time of the last change
public record HookStorage(byte[] key, byte @Nullable [] value, ZonedDateTime timestamp) {

    /// Creates a new `HookStorage`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public HookStorage {
        Objects.requireNonNull(key, "key must not be null");
        key = key.clone();
        value = value == null ? null : value.clone();
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// the 32-byte storage key
    ///
    /// Returns a copy of the `key` array.
    @Override
    public byte[] key() {
        return key.clone();
    }

    /// the stored value; null if the slot was cleared
    ///
    /// Returns a copy of the `value` array.
    @Override
    public byte @Nullable [] value() {
        return value == null ? null : value.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof HookStorage other
            && Arrays.equals(key, other.key)
            && Arrays.equals(value, other.value)
            && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(key), Arrays.hashCode(value), timestamp);
    }

    @Override
    public String toString() {
        return "HookStorage[key=" + "byte[" + key.length + "]" + ", value=" + (value == null ? "null" : "byte[" + value.length + "]") + ", timestamp=" + timestamp + "]";
    }
}
