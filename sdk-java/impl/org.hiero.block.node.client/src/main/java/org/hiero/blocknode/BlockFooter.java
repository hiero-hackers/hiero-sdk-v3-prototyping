package org.hiero.blocknode;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The footer of a block: the hashes that chain a block to its predecessor and to the network state.
///
/// @param previousBlockRootHash the root hash of the previous block
/// @param rootHashOfAllBlockHashesTree the root hash of the tree of the hashes of all previous blocks
/// @param startOfBlockStateRootHash the root hash of the network state at the start of the block
public record BlockFooter(
        byte[] previousBlockRootHash,
        byte[] rootHashOfAllBlockHashesTree,
        byte[] startOfBlockStateRootHash) {

    /// Creates a new `BlockFooter`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public BlockFooter {
        Objects.requireNonNull(previousBlockRootHash, "previousBlockRootHash must not be null");
        previousBlockRootHash = previousBlockRootHash.clone();
        Objects.requireNonNull(rootHashOfAllBlockHashesTree, "rootHashOfAllBlockHashesTree must not be null");
        rootHashOfAllBlockHashesTree = rootHashOfAllBlockHashesTree.clone();
        Objects.requireNonNull(startOfBlockStateRootHash, "startOfBlockStateRootHash must not be null");
        startOfBlockStateRootHash = startOfBlockStateRootHash.clone();
    }

    /// the root hash of the previous block
    ///
    /// Returns a copy of the `previousBlockRootHash` array.
    @Override
    public byte[] previousBlockRootHash() {
        return previousBlockRootHash.clone();
    }

    /// the root hash of the tree of the hashes of all previous blocks
    ///
    /// Returns a copy of the `rootHashOfAllBlockHashesTree` array.
    @Override
    public byte[] rootHashOfAllBlockHashesTree() {
        return rootHashOfAllBlockHashesTree.clone();
    }

    /// the root hash of the network state at the start of the block
    ///
    /// Returns a copy of the `startOfBlockStateRootHash` array.
    @Override
    public byte[] startOfBlockStateRootHash() {
        return startOfBlockStateRootHash.clone();
    }

    /// Compares `previousBlockRootHash`, `rootHashOfAllBlockHashesTree` and `startOfBlockStateRootHash` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof BlockFooter other
            && Arrays.equals(previousBlockRootHash, other.previousBlockRootHash)
            && Arrays.equals(rootHashOfAllBlockHashesTree, other.rootHashOfAllBlockHashesTree)
            && Arrays.equals(startOfBlockStateRootHash, other.startOfBlockStateRootHash);
    }

    /// Hashes `previousBlockRootHash`, `rootHashOfAllBlockHashesTree` and `startOfBlockStateRootHash` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(previousBlockRootHash), Arrays.hashCode(rootHashOfAllBlockHashesTree), Arrays.hashCode(startOfBlockStateRootHash));
    }

    /// Shows only the length of `previousBlockRootHash`, `rootHashOfAllBlockHashesTree` and `startOfBlockStateRootHash` instead of the identity of the array object.
    @Override
    public String toString() {
        return "BlockFooter[previousBlockRootHash=" + "byte[" + previousBlockRootHash.length + "]" + ", rootHashOfAllBlockHashesTree=" + "byte[" + rootHashOfAllBlockHashesTree.length + "]" + ", startOfBlockStateRootHash=" + "byte[" + startOfBlockStateRootHash.length + "]" + "]";
    }
}
