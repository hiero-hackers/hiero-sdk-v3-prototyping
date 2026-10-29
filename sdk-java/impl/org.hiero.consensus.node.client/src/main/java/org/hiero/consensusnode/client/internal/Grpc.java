package org.hiero.consensusnode.client.internal;

import com.google.protobuf.MessageLite;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.protobuf.lite.ProtoLiteUtils;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.hiero.consensusnode.client.TransportFailureKind;

/// Unary gRPC calls without generated stubs: the method descriptor is built from the service and method name.
final class Grpc {

    private static final Map<String, MethodDescriptor<?, ?>> DESCRIPTORS = new ConcurrentHashMap<>();

    private Grpc() {
    }

    /// Returns the descriptor of a unary method.
    ///
    /// @param service  the full service name, e.g. `proto.CryptoService`
    /// @param method   the method name, e.g. `createAccount`
    /// @param request  the default instance of the request message
    /// @param response the default instance of the response message
    /// @param <RequestT>  the request type
    /// @param <ResponseT> the response type
    /// @return the descriptor
    @SuppressWarnings("unchecked")
    static <RequestT extends MessageLite, ResponseT extends MessageLite> MethodDescriptor<RequestT, ResponseT> unary(
            final String service, final String method, final RequestT request, final ResponseT response) {
        return (MethodDescriptor<RequestT, ResponseT>) DESCRIPTORS.computeIfAbsent(service + "/" + method,
                name -> MethodDescriptor.<RequestT, ResponseT>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(name)
                        .setRequestMarshaller(ProtoLiteUtils.marshaller(request))
                        .setResponseMarshaller(ProtoLiteUtils.marshaller(response))
                        .build());
    }

    /// Calls a unary method. A call that does not complete fails with a [TransportException].
    ///
    /// @param channel the channel to the node
    /// @param method  the method
    /// @param request the request
    /// @param timeout the time the call may take, including establishing the connection
    /// @param <RequestT>  the request type
    /// @param <ResponseT> the response type
    /// @return the response
    static <RequestT, ResponseT> CompletableFuture<ResponseT> call(final Channel channel,
                                                                   final MethodDescriptor<RequestT, ResponseT> method,
                                                                   final RequestT request, final Duration timeout) {
        final CompletableFuture<ResponseT> answer = new CompletableFuture<>();
        final ClientCall<RequestT, ResponseT> call = channel.newCall(method, CallOptions.DEFAULT
                .withDeadlineAfter(timeout.toNanos(), TimeUnit.NANOSECONDS));
        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(final ResponseT message) {
                answer.complete(message);
            }

            @Override
            public void onClose(final Status status, final Metadata trailers) {
                if (!answer.isDone()) {
                    answer.completeExceptionally(new TransportException(kind(status), method.getFullMethodName()
                            + " failed: " + status));
                }
            }
        }, new Metadata());
        call.sendMessage(request);
        call.halfClose();
        call.request(1);
        return answer;
    }

    /// The kind of a failed call, as `client.md` maps the gRPC status codes.
    ///
    /// @param status the status the call ended with
    /// @return the kind
    static TransportFailureKind kind(final Status status) {
        return switch (status.getCode()) {
            case UNAVAILABLE -> TransportFailureKind.CONNECTION;
            case DEADLINE_EXCEEDED -> TransportFailureKind.TIMEOUT;
            case RESOURCE_EXHAUSTED -> TransportFailureKind.OVERLOADED;
            case CANCELLED -> TransportFailureKind.INTERRUPTED;
            case INTERNAL -> status.getDescription() != null && status.getDescription().contains("RST_STREAM")
                    ? TransportFailureKind.INTERRUPTED : TransportFailureKind.PROTOCOL;
            default -> TransportFailureKind.PROTOCOL;
        };
    }
}
