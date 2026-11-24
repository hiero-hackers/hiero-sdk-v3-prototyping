package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseHeader;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.hiero.nativeToken.NativeTokenUnit;

/// What the implementation knows about one query type: the gRPC method that answers it, how the request is built and
/// how the answer is read.
///
/// @param service the full gRPC service name, e.g. `proto.CryptoService`
/// @param method  the gRPC method name, e.g. `getAccountInfo`
/// @param request builds the request from the query and the header (response type and payment)
/// @param header  reads the header of the answer (precheck status and cost)
/// @param answer  reads the value of the answer
/// @param <Q> the query type
/// @param <T> the type of the value
public record QueryKind<Q, T>(String service, String method, BiFunction<Q, QueryHeader, Query> request,
                              Function<Response, ResponseHeader> header, Answer<Q, T> answer) {

    /// Reads the value of an answer.
    ///
    /// @param <Q> the query type
    /// @param <T> the type of the value
    @FunctionalInterface
    public interface Answer<Q, T> {

        /// Reads the value of an answer.
        ///
        /// @param query    the query
        /// @param response the answer of the node
        /// @param unit     the native token unit of the network of the client, the unit of every exchange rate the
        ///                 answer holds (rule exchange-rate-unit)
        /// @return the value
        T apply(Q query, Response response, NativeTokenUnit unit);
    }

    /// A query type whose answer does not depend on the network of the client.
    ///
    /// @param service the full gRPC service name
    /// @param method  the gRPC method name
    /// @param request builds the request from the query and the header
    /// @param header  reads the header of the answer
    /// @param answer  reads the value of the answer
    public QueryKind(final String service, final String method, final BiFunction<Q, QueryHeader, Query> request,
                     final Function<Response, ResponseHeader> header, final BiFunction<Q, Response, T> answer) {
        this(service, method, request, header, (query, response, unit) -> answer.apply(query, response));
    }

    /// The gRPC method that answers a query of this type.
    ///
    /// @return the method descriptor
    io.grpc.MethodDescriptor<Query, Response> grpcMethod() {
        return Grpc.unary(service, method, Query.getDefaultInstance(), Response.getDefaultInstance());
    }

    /// Whether the answer may not be available yet: a query for a receipt or a record, which exist only after
    /// consensus (the outcome `PENDING`).
    ///
    /// @return whether the answer may be pending
    boolean mayBePending() {
        return method.equals("getTxRecordByTxID") || method.equals("getTransactionReceipts");
    }
}
