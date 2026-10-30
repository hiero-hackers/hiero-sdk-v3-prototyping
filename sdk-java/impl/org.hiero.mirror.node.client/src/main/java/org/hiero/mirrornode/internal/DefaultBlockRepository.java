package org.hiero.mirrornode.internal;

import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.mirrornode.block.Block;
import org.hiero.mirrornode.block.BlockRepository;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.jspecify.annotations.Nullable;

/// The `BlockRepository` of the SDK, on the endpoints `/blocks` and `/blocks/{hashOrNumber}` of the Mirror Node REST
/// API.
///
/// The hashes and the logs bloom are hex texts with a `0x` prefix in the REST API; a `Block` holds the decoded bytes.
public final class DefaultBlockRepository implements BlockRepository {

    private static final HexFormat HEX = HexFormat.of();

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultBlockRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<@Nullable Block> findByNumber(final long number) {
        if (number < 0L) {
            throw new IllegalArgumentException("number must be at least 0");
        }
        return rest.getAndMap("/blocks/" + number, DefaultBlockRepository::block);
    }

    @Override
    public CompletionStage<@Nullable Block> findByHash(final byte[] hash) {
        Objects.requireNonNull(hash, "hash must not be null");
        return rest.getAndMap("/blocks/0x" + HEX.formatHex(hash), DefaultBlockRepository::block);
    }

    @Override
    public CompletionStage<Page<Block>> findAll() {
        return findAll(List.of(), null, null, List.of());
    }

    @Override
    public CompletionStage<Page<Block>> findAll(final List<String> blockNumber, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> timestamp) {
        final String path = Query.of("/blocks").addAll("block.number", blockNumber).add("limit", limit)
                .add("order", order).addAll("timestamp", timestamp).build();
        return rest.page(path, "blocks", DefaultBlockRepository::block);
    }

    private static Block block(final JsonObject json) {
        return new Block(
                json.longValue("count"),
                json.optLong("gas_used"),
                json.optString("hapi_version"),
                hex(json.string("hash")),
                optHex(json.optString("logs_bloom")),
                json.string("name"),
                json.longValue("number"),
                hex(json.string("previous_hash")),
                json.optLong("size"),
                Values.range(json, "timestamp"));
    }

    private static byte[] hex(final String text) {
        return HEX.parseHex(text.startsWith("0x") ? text.substring(2) : text);
    }

    private static byte @Nullable [] optHex(final @Nullable String text) {
        return text == null || text.isEmpty() ? null : hex(text);
    }
}
