package org.hiero.blocknode;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockItem`, generated from the specs.
@DisplayName("BlockItem")
class BlockItemTest {

    @Test
    @DisplayName("creates a BlockItem and returns the values")
    @Tag("testId:blocknode.BlockItem/create")
    void shouldCreateAndReturnTheValues() {
        final BlockItemKind kind = kindValue();
        final byte[] content = contentValue();
        final BlockItem subject = new BlockItem(kind, content);
        assertValue(kind, subject.kind());
        assertValue(content, subject.content());
    }

    @Test
    @DisplayName("rejects null for kind")
    @Tag("testId:blocknode.BlockItem/kind/rejects-null")
    void shouldRejectNullKind() {
        assertThrows(NullPointerException.class, () -> new BlockItem((BlockItemKind) null, contentValue()));
    }

    @Test
    @DisplayName("rejects null for content")
    @Tag("testId:blocknode.BlockItem/content/rejects-null")
    void shouldRejectNullContent() {
        assertThrows(NullPointerException.class, () -> new BlockItem(kindValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies content; the accessor returns a copy")
    @Tag("testId:blocknode.BlockItem/content/copies-in")
    void shouldCopyContentIn() {
        final byte[] value = contentValue();
        final byte[] expected = value.clone();
        final BlockItem subject = new BlockItem(kindValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.content());
        final byte[] returned = subject.content();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.content());
    }

    private static BlockItemKind kindValue() {
        return BasicBlockItemKind.BLOCK_HEADER;
    }

    private static byte[] contentValue() {
        return new byte[] {1, 2, 3};
    }

    private static BlockItem create() {
        return new BlockItem(kindValue(), contentValue());
    }

    private static BlockItem defaultSubject() {
        return new BlockItem(BasicBlockItemKind.BLOCK_HEADER, new byte[] {(byte) 1, (byte) 2, (byte) 3});
    }
}
