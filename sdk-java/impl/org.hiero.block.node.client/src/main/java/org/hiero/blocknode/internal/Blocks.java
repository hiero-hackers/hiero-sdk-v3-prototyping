package org.hiero.blocknode.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;
import com.hedera.hapi.block.stream.output.protoc.BlockFooter;
import com.hedera.hapi.block.stream.output.protoc.BlockHeader;
import com.hederahashgraph.api.proto.java.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hiero.block.internal.protoc.BlockItemUnparsed;
import org.hiero.blocknode.BasicBlockItemKind;
import org.hiero.blocknode.Block;
import org.hiero.blocknode.BlockHashAlgorithm;
import org.hiero.blocknode.BlockItem;
import org.hiero.blocknode.BlockItemKind;
import org.hiero.blocknode.BlockNodeException;
import org.hiero.blocknode.status.SemanticVersion;

/// Builds a `Block` from the items of the block stream: every item as its kind and its bytes, byte for byte as
/// received; only the header and the footer are decoded.
public final class Blocks {

    private static final Map<Integer, BasicBlockItemKind> KINDS = Arrays.stream(BasicBlockItemKind.values())
            .collect(Collectors.toUnmodifiableMap(BasicBlockItemKind::code, Function.identity()));

    private Blocks() {
    }

    /// Builds a block from its items.
    ///
    /// @param items the items of the block, in their order
    /// @return the block
    /// @throws BlockNodeException if the block has no header or no footer, or one of them cannot be decoded
    public static Block block(final List<BlockItemUnparsed> items) {
        final List<BlockItem> blockItems = new ArrayList<>(items.size());
        for (final BlockItemUnparsed item : items) {
            blockItems.addAll(items(item));
        }
        BlockItem header = null;
        BlockItem footer = null;
        for (final BlockItem item : blockItems) {
            if (item.kind() == BasicBlockItemKind.BLOCK_HEADER && header == null) {
                header = item;
            } else if (item.kind() == BasicBlockItemKind.BLOCK_FOOTER) {
                footer = item;
            }
        }
        if (header == null) {
            throw new BlockNodeException("The block has no block header", null);
        }
        if (footer == null) {
            throw new BlockNodeException("The block has no block footer", null);
        }
        return new Block(header(header.content()), footer(footer.content()), blockItems);
    }

    /// The items of one unparsed item: the item itself, or - for an item of a kind this SDK does not know - the
    /// fields the protobuf runtime kept as unknown fields.
    private static List<BlockItem> items(final BlockItemUnparsed item) {
        if (item.getItemCase() != BlockItemUnparsed.ItemCase.ITEM_NOT_SET) {
            final ByteString content = (ByteString) item.getField(
                    BlockItemUnparsed.getDescriptor().findFieldByNumber(item.getItemCase().getNumber()));
            return List.of(new BlockItem(kind(item.getItemCase().getNumber()), content.toByteArray()));
        }
        final List<BlockItem> unknown = new ArrayList<>();
        for (final Map.Entry<Integer, UnknownFieldSet.Field> field : item.getUnknownFields().asMap().entrySet()) {
            for (final ByteString content : field.getValue().getLengthDelimitedList()) {
                unknown.add(new BlockItem(kind(field.getKey()), content.toByteArray()));
            }
        }
        return unknown;
    }

    /// The kind of a field number of the `item` oneof of `BlockItem`.
    private static BlockItemKind kind(final int code) {
        final BasicBlockItemKind basic = KINDS.get(code);
        return basic != null ? basic : new UnknownBlockItemKind(code);
    }

    private static org.hiero.blocknode.BlockHeader header(final byte[] content) {
        final BlockHeader header;
        try {
            header = BlockHeader.parseFrom(content);
        } catch (final InvalidProtocolBufferException e) {
            throw new BlockNodeException("The block header cannot be decoded", e);
        }
        return new org.hiero.blocknode.BlockHeader(header.getNumber(), time(header.getBlockTimestamp()),
                version(header.getHapiProtoVersion()), version(header.getSoftwareVersion()),
                switch (header.getHashAlgorithm()) {
                    case SHA2_384 -> BlockHashAlgorithm.SHA2_384;
                    case UNRECOGNIZED -> throw new BlockNodeException("The block uses an unknown hash algorithm",
                            null);
                });
    }

    private static org.hiero.blocknode.BlockFooter footer(final byte[] content) {
        final BlockFooter footer;
        try {
            footer = BlockFooter.parseFrom(content);
        } catch (final InvalidProtocolBufferException e) {
            throw new BlockNodeException("The block footer cannot be decoded", e);
        }
        return new org.hiero.blocknode.BlockFooter(footer.getPreviousBlockRootHash().toByteArray(),
                footer.getRootHashOfAllBlockHashesTree().toByteArray(),
                footer.getStartOfBlockStateRootHash().toByteArray());
    }

    /// Maps a semantic version message.
    ///
    /// @param version the message
    /// @return the version
    public static SemanticVersion version(final com.hederahashgraph.api.proto.java.SemanticVersion version) {
        return new SemanticVersion(version.getMajor(), version.getMinor(), version.getPatch(), version.getPre(),
                version.getBuild());
    }

    private static ZonedDateTime time(final Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()).atZone(ZoneOffset.UTC);
    }

    /// A kind of block item that this SDK does not name.
    ///
    /// @param code the field number of the item in `BlockItem`
    private record UnknownBlockItemKind(int code) implements BlockItemKind {
    }
}
