import org.jspecify.annotations.NullMarked;

/// Module `org.hiero.block.node.client` of the Hiero SDK.
///
/// Packages:
/// - `org.hiero.blocknode`
/// - `org.hiero.blocknode.status`
@NullMarked
// gRPC ships no module descriptor; io.grpc and io.grpc.protobuf.lite are automatic modules
@SuppressWarnings("requires-automatic")
module org.hiero.block.node.client {
    requires transitive org.hiero.base;
    requires org.hiero.sdk.protobuf;
    requires io.grpc;
    requires io.grpc.protobuf.lite;
    requires static transitive org.jspecify;
    requires static transitive java.compiler;

    exports org.hiero.blocknode;
    exports org.hiero.blocknode.status;
}
