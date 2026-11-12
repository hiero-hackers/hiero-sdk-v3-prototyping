package org.hiero.blocknode;

import java.util.List;
import java.util.Objects;

/// A block of the block stream as stored by a block node. The SDK does not verify a block.
///
/// @param header decoded from the first BLOCK_HEADER item
/// @param footer decoded from the BLOCK_FOOTER item
/// @param items Every item of the block in order, byte for byte as received: from the BLOCK_HEADER to the BLOCK_PROOF items,
///   header and footer included.
public record Block(BlockHeader header, BlockFooter footer, List<BlockItem> items) {

    /// Creates a new `Block`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public Block {
        Objects.requireNonNull(header, "header must not be null");
        Objects.requireNonNull(footer, "footer must not be null");
        Objects.requireNonNull(items, "items must not be null");
        items = List.copyOf(items);
        if (items.size() < 1) {
            throw new IllegalArgumentException("items must contain at least 1 element(s)");
        }
    }
}
