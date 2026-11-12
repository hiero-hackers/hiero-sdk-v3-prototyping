package org.hiero.blocknode;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.blocknode.status.BlockNodeStatus;
import org.hiero.blocknode.status.BlockNodeStatusDetail;
import org.hiero.ledger.BlockNode;
import org.hiero.sdk.common.HieroStream;
import org.jspecify.annotations.Nullable;

/// Reads the status and the blocks of one block node.
public abstract class BlockNodeClient {

    private final BlockNode blockNode;

    /// Creates a new `BlockNodeClient`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected BlockNodeClient(final BlockNode blockNode) {
        Objects.requireNonNull(blockNode, "blockNode must not be null");
        this.blockNode = blockNode;
    }

    /// the block node this client talks to
    public BlockNode blockNode() {
        return blockNode;
    }

    /// The status of the block node: the range of blocks it serves and the next block it expects.
    ///
    /// The returned stage completes exceptionally with `BlockNodeException` if the operation fails.
    public abstract CompletionStage<BlockNodeStatus> status();

    /// The detailed status of the block node: its exact block ranges, its versions and the data needed to verify its
    /// blocks.
    ///
    /// The returned stage completes exceptionally with `BlockNodeException` if the operation fails.
    public abstract CompletionStage<BlockNodeStatusDetail> statusDetail();

    /// The block with the given number, or null if the block node does not have it.
    ///
    /// The returned stage completes exceptionally with `BlockNodeException` if the operation fails.
    public abstract CompletionStage<@Nullable Block> findByNumber(final long number);

    /// The latest block of the block node, or null if it has none.
    ///
    /// The returned stage completes exceptionally with `BlockNodeException` if the operation fails.
    public abstract CompletionStage<@Nullable Block> findLatest();

    /// The blocks from startBlockNumber to endBlockNumber (both inclusive), in ascending order. Without an end block
    /// the stream does not end: it continues with every new block as soon as the block node has it. The stream fails
    /// with a block-node-error if the block node does not serve the range.
    ///
    /// The stream ends with `BlockNodeException` if it fails.
    public abstract HieroStream<Block> subscribe(final long startBlockNumber, final @Nullable Long endBlockNumber);
}
