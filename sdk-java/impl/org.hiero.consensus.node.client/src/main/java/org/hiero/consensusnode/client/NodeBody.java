package org.hiero.consensusnode.client;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// A serialized TransactionBody for one target consensus node: the exact bytes a signer must sign. The bytes differ
/// per node because each body carries the account of its target node.
public record NodeBody(AccountId node, byte[] bytes) {

    /// Creates a new `NodeBody`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NodeBody {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(bytes, "bytes must not be null");
        bytes = bytes.clone();
    }

    /// Returns a copy of the `bytes` array.
    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    /// Compares `bytes` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof NodeBody other
            && Objects.equals(node, other.node)
            && Arrays.equals(bytes, other.bytes);
    }

    /// Hashes `bytes` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(node, Arrays.hashCode(bytes));
    }

    /// Shows only the length of `bytes` instead of the identity of the array object.
    @Override
    public String toString() {
        return "NodeBody[node=" + node + ", bytes=" + "byte[" + bytes.length + "]" + "]";
    }
}
