package org.hiero.blocknode.internal;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.hiero.block.api.protoc.SubscribeStreamRequest;
import org.hiero.block.api.protoc.SubscribeStreamResponse;
import org.hiero.block.internal.protoc.BlockItemUnparsed;
import org.hiero.block.internal.protoc.SubscribeStreamResponseUnparsed;
import org.hiero.blocknode.Block;
import org.hiero.blocknode.BlockNodeException;
import org.hiero.sdk.common.HieroStream;
import org.jspecify.annotations.Nullable;

/// A subscription to the block stream: joins the items of each block and yields the block once its `BlockEnd` has
/// arrived. The next message is requested only when the consumer has taken the one before (flow control), and
/// closing the stream cancels the call (rules block-stream-assembly, block-stream-flow-control and block-stream-close of
/// `block-node.md`).
final class BlockSubscription implements HieroStream<Block> {

    /// A message of the call, or its end.
    private sealed interface Event {
    }

    private record Message(SubscribeStreamResponseUnparsed response) implements Event {
    }

    private record Closed(Status status) implements Event {
    }

    private final ClientCall<SubscribeStreamRequest, SubscribeStreamResponseUnparsed> call;

    private final BlockingQueue<Event> events = new LinkedBlockingQueue<>();

    private final List<BlockItemUnparsed> items = new ArrayList<>();

    private @Nullable Block next;

    private boolean done;

    private boolean iterated;

    /// Starts the call.
    ///
    /// @param call    the call
    /// @param request the request
    BlockSubscription(final ClientCall<SubscribeStreamRequest, SubscribeStreamResponseUnparsed> call,
                      final SubscribeStreamRequest request) {
        this.call = call;
        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(final SubscribeStreamResponseUnparsed message) {
                events.add(new Message(message));
            }

            @Override
            public void onClose(final Status status, final Metadata trailers) {
                events.add(new Closed(status));
            }
        }, new Metadata());
        call.sendMessage(request);
        call.halfClose();
        call.request(1);
    }

    @Override
    public Iterator<Block> iterator() {
        if (iterated) {
            throw new IllegalStateException("A block stream can be iterated only once");
        }
        iterated = true;
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return advance();
            }

            @Override
            public Block next() {
                if (!advance()) {
                    throw new NoSuchElementException();
                }
                final Block block = next;
                next = null;
                return block;
            }
        };
    }

    @Override
    public void close() {
        if (!done) {
            done = true;
            call.cancel("closed by the consumer", null);
        }
    }

    /// Reads messages until a block is complete or the stream has ended.
    private boolean advance() {
        while (next == null && !done) {
            final Event event;
            try {
                event = events.take();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                close();
                throw new BlockNodeException("Interrupted while waiting for the block stream", e);
            }
            switch (event) {
                case Message(SubscribeStreamResponseUnparsed response) -> {
                    accept(response);
                    if (!done) {
                        call.request(1);
                    }
                }
                case Closed(Status status) -> {
                    done = true;
                    if (!status.isOk()) {
                        throw DefaultBlockNodeClient.failure(DefaultBlockNodeClient.SUBSCRIBE, status);
                    }
                }
            }
        }
        return next != null;
    }

    private void accept(final SubscribeStreamResponseUnparsed response) {
        switch (response.getResponseCase()) {
            case BLOCK_ITEMS -> items.addAll(response.getBlockItems().getBlockItemsList());
            case END_OF_BLOCK -> {
                next = Blocks.block(List.copyOf(items));
                items.clear();
            }
            case STATUS -> {
                done = true;
                if (response.getStatus() != SubscribeStreamResponse.Code.SUCCESS) {
                    call.cancel("the block node ended the stream", null);
                    throw new BlockNodeException("The block node ended the stream with " + response.getStatus(),
                            null);
                }
            }
            case RESPONSE_NOT_SET -> {
                // nothing to do
            }
        }
    }
}
