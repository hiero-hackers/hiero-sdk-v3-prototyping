package org.hiero.blocknode.internal;

import com.google.protobuf.MessageLite;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.protobuf.lite.ProtoLiteUtils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.hiero.block.api.protoc.BlockRequest;
import org.hiero.block.api.protoc.BlockResponse;
import org.hiero.block.api.protoc.ServerStatusDetailResponse;
import org.hiero.block.api.protoc.ServerStatusRequest;
import org.hiero.block.api.protoc.ServerStatusResponse;
import org.hiero.block.api.protoc.SubscribeStreamRequest;
import org.hiero.block.internal.protoc.BlockResponseUnparsed;
import org.hiero.block.internal.protoc.SubscribeStreamResponseUnparsed;
import org.hiero.blocknode.Block;
import org.hiero.blocknode.BlockNodeClient;
import org.hiero.blocknode.BlockNodeException;
import org.hiero.blocknode.status.AddressBookRange;
import org.hiero.blocknode.status.BlockNodeStatus;
import org.hiero.blocknode.status.BlockNodeStatusDetail;
import org.hiero.blocknode.status.BlockNodeVersions;
import org.hiero.blocknode.status.BlockRange;
import org.hiero.blocknode.status.PluginVersion;
import org.hiero.blocknode.status.TssData;
import org.hiero.blocknode.status.TssRosterEntry;
import org.hiero.ledger.BlockNode;
import org.hiero.sdk.common.HieroStream;
import org.jspecify.annotations.Nullable;

/// The `BlockNodeClient` of a block node, over gRPC without generated stubs. The channel is opened with the first
/// request.
public final class DefaultBlockNodeClient extends BlockNodeClient {

    /// The value of the protocol for "no end" and "the latest block".
    static final long UINT64_MAX = -1L;

    /// The time a request other than a subscription may take (rule block-node-request-timeout).
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /// The largest message the client accepts: a block can be many megabytes in size (rule block-node-message-size).
    private static final int MAX_MESSAGE_SIZE = 256 * 1024 * 1024;

    private static final MethodDescriptor<ServerStatusRequest, ServerStatusResponse> SERVER_STATUS = method(
            "org.hiero.block.api.BlockNodeService/serverStatus", MethodDescriptor.MethodType.UNARY,
            ServerStatusRequest.getDefaultInstance(), ServerStatusResponse.getDefaultInstance());

    private static final MethodDescriptor<ServerStatusRequest, ServerStatusDetailResponse> SERVER_STATUS_DETAIL =
            method("org.hiero.block.api.BlockNodeService/serverStatusDetail", MethodDescriptor.MethodType.UNARY,
                    ServerStatusRequest.getDefaultInstance(), ServerStatusDetailResponse.getDefaultInstance());

    private static final MethodDescriptor<BlockRequest, BlockResponseUnparsed> GET_BLOCK = method(
            "org.hiero.block.api.BlockAccessService/getBlock", MethodDescriptor.MethodType.UNARY,
            BlockRequest.getDefaultInstance(), BlockResponseUnparsed.getDefaultInstance());

    /// `subscribeBlockStream`, with the items left as bytes (`BlockItemUnparsed`), as the block node itself and the
    /// importer of the mirror node read them.
    static final MethodDescriptor<SubscribeStreamRequest, SubscribeStreamResponseUnparsed> SUBSCRIBE = method(
            "org.hiero.block.api.BlockStreamSubscribeService/subscribeBlockStream",
            MethodDescriptor.MethodType.SERVER_STREAMING, SubscribeStreamRequest.getDefaultInstance(),
            SubscribeStreamResponseUnparsed.getDefaultInstance());

    private final Object lock = new Object();

    private @Nullable ManagedChannel channel;

    /// Creates the client.
    ///
    /// @param blockNode the block node
    public DefaultBlockNodeClient(final BlockNode blockNode) {
        super(blockNode);
    }

    @Override
    public CompletionStage<BlockNodeStatus> status() {
        return unary(SERVER_STATUS, ServerStatusRequest.getDefaultInstance())
                .thenApply(response -> new BlockNodeStatus(response.getFirstAvailableBlock(),
                        orNull(response.getLastAvailableBlock()), response.getOnlyLatestState(),
                        orNull(response.getNextExpectedBlock())));
    }

    @Override
    public CompletionStage<BlockNodeStatusDetail> statusDetail() {
        return unary(SERVER_STATUS_DETAIL, ServerStatusRequest.getDefaultInstance())
                .thenApply(DefaultBlockNodeClient::detail);
    }

    @Override
    public CompletionStage<@Nullable Block> findByNumber(final long number) {
        return block(BlockRequest.newBuilder().setBlockNumber(number).build());
    }

    @Override
    public CompletionStage<@Nullable Block> findLatest() {
        return block(BlockRequest.newBuilder().setRetrieveLatest(true).build());
    }

    @Override
    public HieroStream<Block> subscribe(final long startBlockNumber, final @Nullable Long endBlockNumber) {
        final SubscribeStreamRequest request = SubscribeStreamRequest.newBuilder()
                .setStartBlockNumber(startBlockNumber)
                .setEndBlockNumber(endBlockNumber != null ? endBlockNumber : UINT64_MAX)
                .build();
        return new BlockSubscription(channel().newCall(SUBSCRIBE, CallOptions.DEFAULT), request);
    }

    /// Reads one block: `null` if the block node does not have it.
    private CompletionStage<@Nullable Block> block(final BlockRequest request) {
        return unary(GET_BLOCK, request).thenApply(response -> switch (response.getStatus()) {
            case SUCCESS -> Blocks.block(response.getBlock().getBlockItemsList());
            case NOT_FOUND, NOT_AVAILABLE -> null;
            default -> throw new BlockNodeException("The block node answered " + response.getStatus(), null);
        });
    }

    private static BlockNodeStatusDetail detail(final ServerStatusDetailResponse response) {
        final org.hiero.block.api.protoc.BlockNodeVersions versions = response.getVersionInformation();
        final List<PluginVersion> plugins = versions.getInstalledPluginVersionsList().stream()
                .map(plugin -> new PluginVersion(plugin.getPluginId(),
                        Blocks.version(plugin.getPluginSoftwareVersion()), plugin.getPluginFeatureNamesList()))
                .toList();
        final @Nullable TssData tss = response.hasTssData() ? new TssData(
                response.getTssData().getLedgerId().toByteArray(),
                response.getTssData().getWrapsVerificationKey().toByteArray(),
                response.getTssData().getCurrentRoster().getRosterEntriesList().stream()
                        .map(entry -> new TssRosterEntry(entry.getNodeId(), entry.getWeight(),
                                entry.getSchnorrPublicKey().toByteArray()))
                        .toList(),
                response.getTssData().getValidFromBlock()) : null;
        return new BlockNodeStatusDetail(
                new BlockNodeVersions(Blocks.version(versions.getStreamProtoVersion()),
                        Blocks.version(versions.getBlockNodeVersion()), plugins),
                response.getAvailableRangesList().stream().map(DefaultBlockNodeClient::range).toList(),
                response.getStoredRangesList().stream().map(DefaultBlockNodeClient::range).toList(),
                tss,
                response.hasNodeAddressBook() ? response.getNodeAddressBook().toByteArray() : new byte[0],
                response.getRangedAddressBookHistory().getAddressBooksList().stream()
                        .map(book -> new AddressBookRange(book.getAddressBook().toByteArray(), book.getStartBlock(),
                                orNull(book.getEndBlock())))
                        .toList());
    }

    private static BlockRange range(final org.hiero.block.api.protoc.BlockRange range) {
        return new BlockRange(range.getRangeStart(), range.getRangeEnd());
    }

    /// `null` for the sentinel `uint64_max`.
    private static @Nullable Long orNull(final long value) {
        return value == UINT64_MAX ? null : value;
    }

    /// Calls a unary method. A failed call completes with a [BlockNodeException].
    private <RequestT, ResponseT> CompletableFuture<ResponseT> unary(
            final MethodDescriptor<RequestT, ResponseT> method, final RequestT request) {
        final CompletableFuture<ResponseT> answer = new CompletableFuture<>();
        final ClientCall<RequestT, ResponseT> call = channel().newCall(method, CallOptions.DEFAULT
                .withDeadlineAfter(TIMEOUT.toNanos(), TimeUnit.NANOSECONDS));
        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(final ResponseT message) {
                answer.complete(message);
            }

            @Override
            public void onClose(final Status status, final Metadata trailers) {
                if (!answer.isDone()) {
                    answer.completeExceptionally(failure(method, status));
                }
            }
        }, new Metadata());
        call.sendMessage(request);
        call.halfClose();
        call.request(1);
        return answer;
    }

    /// The error of a call that ended without an answer.
    static BlockNodeException failure(final MethodDescriptor<?, ?> method, final Status status) {
        return new BlockNodeException(method.getFullMethodName() + " failed: " + status, status.getCause());
    }

    /// The channel to the block node, opened with the first request (rule block-node-connection).
    private ManagedChannel channel() {
        synchronized (lock) {
            if (channel == null) {
                final ManagedChannelBuilder<?> builder = ManagedChannelBuilder
                        .forAddress(blockNode().host(), blockNode().port())
                        .maxInboundMessageSize(MAX_MESSAGE_SIZE);
                if (blockNode().requiresTls()) {
                    builder.useTransportSecurity();
                } else {
                    builder.usePlaintext();
                }
                channel = builder.build();
            }
            return channel;
        }
    }

    private static <RequestT extends MessageLite, ResponseT extends MessageLite> MethodDescriptor<RequestT, ResponseT>
            method(final String name, final MethodDescriptor.MethodType type, final RequestT request,
                   final ResponseT response) {
        return MethodDescriptor.<RequestT, ResponseT>newBuilder()
                .setType(type)
                .setFullMethodName(name)
                .setRequestMarshaller(ProtoLiteUtils.marshaller(request))
                .setResponseMarshaller(ProtoLiteUtils.marshaller(response))
                .build();
    }
}
