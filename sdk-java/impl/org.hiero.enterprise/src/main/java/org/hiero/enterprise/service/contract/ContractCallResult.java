package org.hiero.enterprise.service.contract;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.evm.AbiValue;
import org.jspecify.annotations.Nullable;

/// The result of a call of a function without a transaction.
///
/// @param values the returned values, decoded with the result types of the function
/// @param result the returned data, ABI-encoded
public record ContractCallResult(List<AbiValue> values, byte[] result) {

    /// Creates a new `ContractCallResult`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractCallResult {
        Objects.requireNonNull(values, "values must not be null");
        values = List.copyOf(values);
        Objects.requireNonNull(result, "result must not be null");
        result = result.clone();
    }

    /// Creates a new `ContractCallResult` with the default value of `values`.
    public ContractCallResult(final byte[] result) {
        this(List.of(), result);
    }

    /// the returned data, ABI-encoded
    ///
    /// Returns a copy of the `result` array.
    @Override
    public byte[] result() {
        return result.clone();
    }

    /// Compares `result` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ContractCallResult other
            && Objects.equals(values, other.values)
            && Arrays.equals(result, other.result);
    }

    /// Hashes `result` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(values, Arrays.hashCode(result));
    }

    /// Shows only the length of `result` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ContractCallResult[values=" + values + ", result=" + "byte[" + result.length + "]" + "]";
    }
}
