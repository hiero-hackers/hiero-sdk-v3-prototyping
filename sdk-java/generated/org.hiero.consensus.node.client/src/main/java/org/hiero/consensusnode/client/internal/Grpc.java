package org.hiero.consensusnode.client.internal;

import com.google.protobuf.MessageLite;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.protobuf.lite.ProtoLiteUtils;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/// The gRPC calls to the consensus node. The service contract is built from the service and method
/// names instead of generated stubs, so the protobuf module needs no gRPC code generation.
public final class Grpc {

    private static final Map<String, MethodDescriptor<?, ?>> DESCRIPTORS = new ConcurrentHashMap<>();

    private Grpc() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /// Returns the descriptor of a unary method, creating it on first use.
    ///
    /// @param <RequestT>  the request message
    /// @param <ResponseT> the response message
    /// @param service     the qualified service name, e.g. `proto.CryptoService`
    /// @param method      the method name, e.g. `createAccount`
    /// @param request     the default instance of the request
    /// @param response    the default instance of the response
    /// @return the descriptor
    public static <RequestT extends MessageLite, ResponseT extends MessageLite>
            MethodDescriptor<RequestT, ResponseT> unary(
                    final String service, final String method,
                    final Supplier<RequestT> request, final Supplier<ResponseT> response) {
        @SuppressWarnings("unchecked")
        final MethodDescriptor<RequestT, ResponseT> descriptor =
                (MethodDescriptor<RequestT, ResponseT>) DESCRIPTORS.computeIfAbsent(
                        service + "/" + method,
                        name -> MethodDescriptor.<RequestT, ResponseT>newBuilder()
                                .setType(MethodDescriptor.MethodType.UNARY)
                                .setFullMethodName(name)
                                .setRequestMarshaller(ProtoLiteUtils.marshaller(request.get()))
                                .setResponseMarshaller(ProtoLiteUtils.marshaller(response.get()))
                                .build());
        return descriptor;
    }

    /// Calls a unary method.
    ///
    /// @param <RequestT>  the request message
    /// @param <ResponseT> the response message
    /// @param channel     the channel to the node
    /// @param method      the descriptor
    /// @param request     the request
    /// @return the response
    public static <RequestT, ResponseT> CompletableFuture<ResponseT> call(
            final Channel channel, final MethodDescriptor<RequestT, ResponseT> method, final RequestT request) {
        final CompletableFuture<ResponseT> answer = new CompletableFuture<>();
        final ClientCall<RequestT, ResponseT> call = channel.newCall(method, CallOptions.DEFAULT);
        call.start(new ClientCall.Listener<ResponseT>() {
            @Override
            public void onMessage(final ResponseT message) {
                answer.complete(message);
            }

            @Override
            public void onClose(final Status status, final Metadata trailers) {
                if (!answer.isDone()) {
                    answer.completeExceptionally(new IllegalStateException(
                            method.getFullMethodName() + " failed: " + status, status.asException()));
                }
            }
        }, new Metadata());
        call.sendMessage(request);
        call.halfClose();
        call.request(1);
        return answer;
    }
}
