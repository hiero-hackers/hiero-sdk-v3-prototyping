package org.hiero.mirrornode.block;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;

public interface BlockRepository {

    /// The block with the given number, or null if there is none. Maps to GET /api/v1/blocks/{hashOrNumber}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable Block> findByNumber(final long number);

    /// The block with the given hash (32 bytes in the Ethereum format or 48 bytes in the Hedera format), or null if
    /// there is none. Maps to GET /api/v1/blocks/{hashOrNumber}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable Block> findByHash(final byte[] hash);

    /// The blocks of the network. Maps to GET /api/v1/blocks.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Block>> findAll();

    /// The blocks of the network, filtered by block number and consensus time. Maps to GET /api/v1/blocks.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Block>> findAll(final List<String> blockNumber, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp);
}
