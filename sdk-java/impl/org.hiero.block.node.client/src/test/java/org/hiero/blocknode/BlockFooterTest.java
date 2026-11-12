package org.hiero.blocknode;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockFooter`, generated from the specs.
@DisplayName("BlockFooter")
class BlockFooterTest {

    @Test
    @DisplayName("creates a BlockFooter and returns the values")
    @Tag("testId:blocknode.BlockFooter/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] previousBlockRootHash = previousBlockRootHashValue();
        final byte[] rootHashOfAllBlockHashesTree = rootHashOfAllBlockHashesTreeValue();
        final byte[] startOfBlockStateRootHash = startOfBlockStateRootHashValue();
        final BlockFooter subject = new BlockFooter(previousBlockRootHash, rootHashOfAllBlockHashesTree, startOfBlockStateRootHash);
        assertValue(previousBlockRootHash, subject.previousBlockRootHash());
        assertValue(rootHashOfAllBlockHashesTree, subject.rootHashOfAllBlockHashesTree());
        assertValue(startOfBlockStateRootHash, subject.startOfBlockStateRootHash());
    }

    @Test
    @DisplayName("rejects null for previousBlockRootHash")
    @Tag("testId:blocknode.BlockFooter/previousBlockRootHash/rejects-null")
    void shouldRejectNullPreviousBlockRootHash() {
        assertThrows(NullPointerException.class, () -> new BlockFooter((byte[]) null, rootHashOfAllBlockHashesTreeValue(), startOfBlockStateRootHashValue()));
    }

    @Test
    @DisplayName("the constructor copies previousBlockRootHash; the accessor returns a copy")
    @Tag("testId:blocknode.BlockFooter/previousBlockRootHash/copies-in")
    void shouldCopyPreviousBlockRootHashIn() {
        final byte[] value = previousBlockRootHashValue();
        final byte[] expected = value.clone();
        final BlockFooter subject = new BlockFooter(value, rootHashOfAllBlockHashesTreeValue(), startOfBlockStateRootHashValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.previousBlockRootHash());
        final byte[] returned = subject.previousBlockRootHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.previousBlockRootHash());
    }

    @Test
    @DisplayName("rejects null for rootHashOfAllBlockHashesTree")
    @Tag("testId:blocknode.BlockFooter/rootHashOfAllBlockHashesTree/rejects-null")
    void shouldRejectNullRootHashOfAllBlockHashesTree() {
        assertThrows(NullPointerException.class, () -> new BlockFooter(previousBlockRootHashValue(), (byte[]) null, startOfBlockStateRootHashValue()));
    }

    @Test
    @DisplayName("the constructor copies rootHashOfAllBlockHashesTree; the accessor returns a copy")
    @Tag("testId:blocknode.BlockFooter/rootHashOfAllBlockHashesTree/copies-in")
    void shouldCopyRootHashOfAllBlockHashesTreeIn() {
        final byte[] value = rootHashOfAllBlockHashesTreeValue();
        final byte[] expected = value.clone();
        final BlockFooter subject = new BlockFooter(previousBlockRootHashValue(), value, startOfBlockStateRootHashValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.rootHashOfAllBlockHashesTree());
        final byte[] returned = subject.rootHashOfAllBlockHashesTree();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.rootHashOfAllBlockHashesTree());
    }

    @Test
    @DisplayName("rejects null for startOfBlockStateRootHash")
    @Tag("testId:blocknode.BlockFooter/startOfBlockStateRootHash/rejects-null")
    void shouldRejectNullStartOfBlockStateRootHash() {
        assertThrows(NullPointerException.class, () -> new BlockFooter(previousBlockRootHashValue(), rootHashOfAllBlockHashesTreeValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies startOfBlockStateRootHash; the accessor returns a copy")
    @Tag("testId:blocknode.BlockFooter/startOfBlockStateRootHash/copies-in")
    void shouldCopyStartOfBlockStateRootHashIn() {
        final byte[] value = startOfBlockStateRootHashValue();
        final byte[] expected = value.clone();
        final BlockFooter subject = new BlockFooter(previousBlockRootHashValue(), rootHashOfAllBlockHashesTreeValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.startOfBlockStateRootHash());
        final byte[] returned = subject.startOfBlockStateRootHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.startOfBlockStateRootHash());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.BlockFooter/value-semantics")
    void shouldCompareByValue() {
        final BlockFooter subject = create();
        final BlockFooter other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static byte[] previousBlockRootHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] rootHashOfAllBlockHashesTreeValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] startOfBlockStateRootHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static BlockFooter create() {
        return new BlockFooter(previousBlockRootHashValue(), rootHashOfAllBlockHashesTreeValue(), startOfBlockStateRootHashValue());
    }

    private static BlockFooter defaultSubject() {
        return new BlockFooter(new byte[] {(byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1}, new byte[] {(byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2}, new byte[] {(byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3});
    }
}
