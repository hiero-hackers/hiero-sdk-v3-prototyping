/**
 * The protobuf messages of the Hiero node types, compiled from the vendored definitions in
 * {@code tooling/protobuf}.
 *
 * <p><strong>Almost nothing here is public API of the SDK.</strong> Protobuf is how the SDK talks to
 * a node, not something an SDK user should see or depend on: the wire format changes with every
 * network release, and exposing it would make every protocol change a breaking change of the SDK.
 * Every package is therefore exported with a qualified {@code exports ... to ...} that names exactly
 * the SDK modules which need it, so the generated classes are invisible to anything else on the
 * module path.
 *
 * <p>The one exception is {@code com.hederahashgraph.api.proto.java}. The SPI for custom services and
 * transaction types ({@code consensusnode.transactions.spi}) hands the raw transaction body,
 * response, receipt and record to its implementor, so these four messages are part of the API an SDK
 * user implements against. The specs declare them in the namespace {@code consensusnode.proto} and
 * the Java generator maps that namespace to this package instead of generating placeholders beside
 * it ({@code java.externalNamespace.consensusnode.proto}); see ADR-0009. The package is therefore
 * exported unqualified - the rest of the module is not.
 *
 * <p>Java is the only one of the target languages that needs a separate module for this: JPMS
 * forbids a package from being split across modules, so the protobuf packages must have exactly one
 * owner. TypeScript and Rust keep the same code inside the consuming unit instead (a package subpath
 * that is absent from {@code exports}, a non-{@code pub} module).
 */
module org.hiero.sdk.protobuf {

    requires transitive com.google.protobuf;

    // The Hiero API (HAPI) messages: transaction bodies, queries, receipts, basic types. Unqualified
    // because the SPI exposes four of them; see the module documentation and ADR-0009.
    exports com.hederahashgraph.api.proto.java;

    // The HAPI service definitions (descriptors only; the gRPC stubs are not generated).
    exports com.hederahashgraph.service.proto.java to
            org.hiero.consensus.node.client,
            org.hiero.consensus.node.admin.client;

    // The SDK-defined messages: TransactionList is the byte form of a packed transaction with one signed
    // transaction per node, as all Hiero SDKs write it.
    exports com.hedera.hashgraph.sdk.proto to
            org.hiero.consensus.node.client;

    // The mirror node's gRPC API.
    exports com.hedera.mirror.api.proto to
            org.hiero.mirror.node.client;
}
