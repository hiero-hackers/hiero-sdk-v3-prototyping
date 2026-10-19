import org.jspecify.annotations.NullMarked;

/// Module `org.hiero.consensus.node.client` of the Hiero SDK.
///
/// Packages:
/// - `org.hiero.consensusnode.client`
/// - `org.hiero.consensusnode.proto.account`
/// - `org.hiero.consensusnode.queries`
/// - `org.hiero.consensusnode.queries.accounts`
/// - `org.hiero.consensusnode.queries.files`
/// - `org.hiero.consensusnode.queries.schedule`
/// - `org.hiero.consensusnode.queries.tokens`
/// - `org.hiero.consensusnode.queries.topics`
/// - `org.hiero.consensusnode.transactions`
/// - `org.hiero.consensusnode.transactions.accounts`
/// - `org.hiero.consensusnode.transactions.batch`
/// - `org.hiero.consensusnode.transactions.files`
/// - `org.hiero.consensusnode.transactions.schedule`
/// - `org.hiero.consensusnode.transactions.spi`
/// - `org.hiero.consensusnode.transactions.tokens`
/// - `org.hiero.consensusnode.transactions.topics`
/// - `org.hiero.consensusnode.transactions.utility`
@NullMarked
// gRPC ships no module descriptor; io.grpc and io.grpc.protobuf.lite are automatic modules
@SuppressWarnings({"requires-automatic", "module"})
module org.hiero.consensus.node.client {
    requires transitive org.hiero.base;
    requires transitive org.hiero.sdk.protobuf;
    requires static transitive org.jspecify;
    requires io.grpc;
    requires io.grpc.protobuf.lite;

    exports org.hiero.consensusnode.client;
    // exports org.hiero.consensusnode.proto.account; (enabled as soon as the package contains generated types)
    exports org.hiero.consensusnode.queries;
    exports org.hiero.consensusnode.queries.accounts;
    exports org.hiero.consensusnode.queries.files;
    exports org.hiero.consensusnode.queries.receipts;
    exports org.hiero.consensusnode.queries.schedule;
    exports org.hiero.consensusnode.queries.tokens;
    exports org.hiero.consensusnode.queries.topics;
    exports org.hiero.consensusnode.transactions;
    exports org.hiero.consensusnode.transactions.accounts;
    exports org.hiero.consensusnode.transactions.batch;
    exports org.hiero.consensusnode.transactions.files;
    exports org.hiero.consensusnode.transactions.schedule;
    exports org.hiero.consensusnode.transactions.spi;
    exports org.hiero.consensusnode.transactions.tokens;
    exports org.hiero.consensusnode.transactions.topics;
    exports org.hiero.consensusnode.transactions.utility;
    // the implementation of the transaction types of the admin client (Packing, TransactionKind, Protobuf)
    exports org.hiero.consensusnode.client.internal to org.hiero.consensus.node.admin.client;

    // transaction types of other modules (the administration client, a custom service)
    uses org.hiero.consensusnode.transactions.spi.TransactionSupport;
}
