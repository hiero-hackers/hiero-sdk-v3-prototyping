/**
 * The protobuf messages of the Hiero node types, compiled from the vendored definitions in
 * {@code /protobuf}.
 *
 * <p><strong>Not part of the public API of the SDK.</strong> Protobuf is how the SDK talks to a node,
 * not something an SDK user should see or depend on: the wire format changes with every network
 * release, and exposing it would make every protocol change a breaking change of the SDK. Every
 * package is therefore exported with a qualified {@code exports ... to ...} that names exactly the
 * SDK modules which need it, so the generated classes are invisible to anything else on the module
 * path.
 *
 * <p>Java is the only one of the three target languages that needs a separate module for this: JPMS
 * forbids a package from being split across modules, so the protobuf packages must have exactly one
 * owner. TypeScript and Rust keep the same code inside the consuming unit instead (a package subpath
 * that is absent from {@code exports}, a non-{@code pub} module).
 */
module org.hiero.sdk.protobuf {

    requires transitive com.google.protobuf;

    // The Hiero API (HAPI) messages: transaction bodies, queries, receipts, basic types.
    exports com.hederahashgraph.api.proto.java to
            org.hiero.consensus.node.client,
            org.hiero.consensus.node.admin.client,
            org.hiero.mirror.node.client;

    // The HAPI service definitions (descriptors only; the gRPC stubs are not generated).
    exports com.hederahashgraph.service.proto.java to
            org.hiero.consensus.node.client,
            org.hiero.consensus.node.admin.client;

    // The mirror node's gRPC API.
    exports com.hedera.mirror.api.proto to
            org.hiero.mirror.node.client;
}
