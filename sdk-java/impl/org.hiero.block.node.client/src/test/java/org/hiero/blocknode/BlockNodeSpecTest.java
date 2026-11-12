package org.hiero.blocknode;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.blocknode.status.SemanticVersion;
import org.hiero.ledger.BlockNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `block-node`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("block-node")
class BlockNodeSpecTest {

    /// `blocknode/a-block-node-client-talks-to-the-block-node-it-was-created-for`
    ///
    /// Source: this spec (a client talks to the block node it was created for; creating the client sends no request).
    @Test
    @DisplayName("a block node client talks to the block node it was created for")
    @Tag("testId:blocknode/a-block-node-client-talks-to-the-block-node-it-was-created-for")
    void aBlockNodeClientTalksToTheBlockNodeItWasCreatedFor() {
        // given
        final BlockNode blockNode = new BlockNode("block-node.example.com", 40840, true);

        // when
        final BlockNodeClient client = BlocknodeFactory.createBlockNodeClient(blockNode);

        // then
        assertEquals(blockNode, client.blockNode());
    }

    /// `blocknode/a-block-node-without-a-host-is-rejected`
    ///
    /// Source: this spec (the host of a block node must not be empty).
    @Test
    @DisplayName("a block node without a host is rejected")
    @Tag("testId:blocknode/a-block-node-without-a-host-is-rejected")
    void aBlockNodeWithoutAHostIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new BlockNode("", 40840, false));
    }

    /// `blocknode/block-item-kinds-carry-the-field-numbers-of-the-block-stream`
    ///
    /// Source: block_item.proto of the consensus node (the field numbers of the `item` oneof of `BlockItem`).
    @Test
    @DisplayName("block item kinds carry the field numbers of the block stream")
    @Tag("testId:blocknode/block-item-kinds-carry-the-field-numbers-of-the-block-stream")
    void blockItemKindsCarryTheFieldNumbersOfTheBlockStream() {
        // when / then
        assertEquals(1, BasicBlockItemKind.BLOCK_HEADER.code());
        assertEquals(4, BasicBlockItemKind.SIGNED_TRANSACTION.code());
        assertEquals(7, BasicBlockItemKind.STATE_CHANGES.code());
        assertEquals(9, BasicBlockItemKind.BLOCK_PROOF.code());
        assertEquals(12, BasicBlockItemKind.BLOCK_FOOTER.code());
        assertEquals(19, BasicBlockItemKind.REDACTED_ITEM.code());
    }

    /// `blocknode/a-block-item-keeps-its-content-byte-for-byte`
    ///
    /// Source: this spec (the content of an item is kept byte for byte and not decoded).
    @Test
    @DisplayName("a block item keeps its content byte for byte")
    @Tag("testId:blocknode/a-block-item-keeps-its-content-byte-for-byte")
    void aBlockItemKeepsItsContentByteForByte() {
        // given
        final byte[] content = hex("0a00ff10ef");

        // when
        final BlockItem item = new BlockItem(BasicBlockItemKind.SIGNED_TRANSACTION, content);

        // then
        assertEquals(4, item.kind().code());
        assertEquals(content, item.content());
    }

    /// `blocknode/a-block-keeps-its-header-footer-and-items`
    ///
    /// Source: block_header.proto and block_footer.proto of the consensus node (the fields of `BlockHeader` and
    /// `BlockFooter`); this spec (header and footer stay in the items).
    @Test
    @DisplayName("a block keeps its header, footer and items")
    @Tag("testId:blocknode/a-block-keeps-its-header-footer-and-items")
    void aBlockKeepsItsHeaderFooterAndItems() {
        // given
        final SemanticVersion version = new SemanticVersion(0, 70, 0, "", "");
        final BlockHeader header = new BlockHeader(42L, toZonedDateTime("2026-01-02T03:04:05Z"), version, version, BlockHashAlgorithm.SHA2_384);
        final BlockFooter footer = new BlockFooter(repeat(hex("01"), 48L), repeat(hex("02"), 48L), repeat(hex("03"), 48L));
        final List<BlockItem> items = List.of(new BlockItem(BasicBlockItemKind.BLOCK_HEADER, hex("1803")), new BlockItem(BasicBlockItemKind.BLOCK_FOOTER, hex("0a00")));

        // when
        final Block block = new Block(header, footer, items);

        // then
        assertEquals(42L, block.header().number());
        assertEquals(BlockHashAlgorithm.SHA2_384, block.header().hashAlgorithm());
        assertEquals(footer.previousBlockRootHash(), block.footer().previousBlockRootHash());
        assertEquals(items, block.items());
    }

    /// `blocknode/a-block-without-items-is-rejected`
    ///
    /// Source: this spec (a block has at least its header item).
    @Test
    @DisplayName("a block without items is rejected")
    @Tag("testId:blocknode/a-block-without-items-is-rejected")
    void aBlockWithoutItemsIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new Block(defaultBlockHeader(), defaultBlockFooter(), List.of()));
    }

    private static BlockHeader defaultBlockHeader() {
        return new BlockHeader(1L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new SemanticVersion(0, 70, 0, "", ""), new SemanticVersion(0, 70, 0, "", ""), BlockHashAlgorithm.SHA2_384);
    }

    private static BlockFooter defaultBlockFooter() {
        return new BlockFooter(new byte[] {(byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1}, new byte[] {(byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2}, new byte[] {(byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3});
    }
}
