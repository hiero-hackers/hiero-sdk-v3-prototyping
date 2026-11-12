package org.hiero.blocknode;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.blocknode.status.SemanticVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Block`, generated from the specs.
@DisplayName("Block")
class BlockTest {

    @Test
    @DisplayName("creates a Block and returns the values")
    @Tag("testId:blocknode.Block/create")
    void shouldCreateAndReturnTheValues() {
        final BlockHeader header = headerValue();
        final BlockFooter footer = footerValue();
        final List<BlockItem> items = itemsValue();
        final Block subject = new Block(header, footer, items);
        assertValue(header, subject.header());
        assertValue(footer, subject.footer());
        assertValue(items, subject.items());
    }

    @Test
    @DisplayName("rejects null for header")
    @Tag("testId:blocknode.Block/header/rejects-null")
    void shouldRejectNullHeader() {
        assertThrows(NullPointerException.class, () -> new Block((BlockHeader) null, footerValue(), itemsValue()));
    }

    @Test
    @DisplayName("rejects null for footer")
    @Tag("testId:blocknode.Block/footer/rejects-null")
    void shouldRejectNullFooter() {
        assertThrows(NullPointerException.class, () -> new Block(headerValue(), (BlockFooter) null, itemsValue()));
    }

    @Test
    @DisplayName("rejects null for items")
    @Tag("testId:blocknode.Block/items/rejects-null")
    void shouldRejectNullItems() {
        assertThrows(NullPointerException.class, () -> new Block(headerValue(), footerValue(), (List<BlockItem>) null));
    }

    @Test
    @DisplayName("items: accepts the minimum number of elements")
    @Tag("testId:blocknode.Block/items/accepts-at-minimum-size")
    void shouldAcceptItemsAtMinimumSize() {
        final List<BlockItem> value = List.of(new BlockItem(BasicBlockItemKind.BLOCK_HEADER, new byte[] {(byte) 1, (byte) 2, (byte) 3}));
        final Block subject = new Block(headerValue(), footerValue(), value);
        assertValue(value, subject.items());
    }

    @Test
    @DisplayName("items: rejects fewer elements than the minimum")
    @Tag("testId:blocknode.Block/items/rejects-below-minimum-size")
    void shouldRejectItemsBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new Block(headerValue(), footerValue(), List.of()));
    }

    @Test
    @DisplayName("the constructor copies items; the accessor returns an unmodifiable collection")
    @Tag("testId:blocknode.Block/items/copies-in")
    void shouldCopyItemsIn() {
        final List<BlockItem> value = new ArrayList<>(itemsValue());
        final List<BlockItem> expected = List.copyOf(value);
        final Block subject = new Block(headerValue(), footerValue(), value);
        value.clear();
        assertValue(expected, subject.items());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.items()).clear());
    }

    private static BlockHeader headerValue() {
        return new BlockHeader(1L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new SemanticVersion(0, 70, 0, "", ""), new SemanticVersion(0, 70, 0, "", ""), BlockHashAlgorithm.SHA2_384);
    }

    private static BlockFooter footerValue() {
        return new BlockFooter(new byte[] {(byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1}, new byte[] {(byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2}, new byte[] {(byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3});
    }

    private static List<BlockItem> itemsValue() {
        return List.of(new BlockItem(BasicBlockItemKind.BLOCK_HEADER, new byte[] {(byte) 1, (byte) 2, (byte) 3}));
    }

    private static Block create() {
        return new Block(headerValue(), footerValue(), itemsValue());
    }

    private static Block defaultSubject() {
        return new Block(new BlockHeader(1L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new SemanticVersion(0, 70, 0, "", ""), new SemanticVersion(0, 70, 0, "", ""), BlockHashAlgorithm.SHA2_384), new BlockFooter(new byte[] {(byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1}, new byte[] {(byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2}, new byte[] {(byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3}), List.of(new BlockItem(BasicBlockItemKind.BLOCK_HEADER, new byte[] {(byte) 1, (byte) 2, (byte) 3})));
    }
}
