package org.hiero.blocknode;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// One item of a block.
///
/// @param kind the kind of the item
/// @param content The content of the item in the protobuf encoding of the block stream: the encoded message of its kind, or for a
///   SIGNED_TRANSACTION the bytes of the signed transaction.
public record BlockItem(BlockItemKind kind, byte[] content) {

    /// Creates a new `BlockItem`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public BlockItem {
        Objects.requireNonNull(kind, "kind must not be null");
        Objects.requireNonNull(content, "content must not be null");
        content = content.clone();
    }

    /// The content of the item in the protobuf encoding of the block stream: the encoded message of its kind, or for a
    /// SIGNED_TRANSACTION the bytes of the signed transaction.
    ///
    /// Returns a copy of the `content` array.
    @Override
    public byte[] content() {
        return content.clone();
    }

    /// Compares `content` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof BlockItem other
            && Objects.equals(kind, other.kind)
            && Arrays.equals(content, other.content);
    }

    /// Hashes `content` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(kind, Arrays.hashCode(content));
    }

    /// Shows only the length of `content` instead of the identity of the array object.
    @Override
    public String toString() {
        return "BlockItem[kind=" + kind + ", content=" + "byte[" + content.length + "]" + "]";
    }
}
