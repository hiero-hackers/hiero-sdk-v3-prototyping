package org.hiero.evm;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;
import org.jspecify.annotations.Nullable;

/// A `bytes`.
public record AbiBytes(byte[] value) implements AbiValue {

    /// Creates a new `AbiBytes`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AbiBytes {
        Objects.requireNonNull(value, "value must not be null");
        value = value.clone();
    }

    /// Returns a copy of the `value` array.
    @Override
    public byte[] value() {
        return value.clone();
    }

    /// Compares `value` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof AbiBytes other
            && Arrays.equals(value, other.value);
    }

    /// Hashes `value` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(value));
    }

    /// Shows only the length of `value` instead of the identity of the array object.
    @Override
    public String toString() {
        return "AbiBytes[value=" + "byte[" + value.length + "]" + "]";
    }

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }
}
