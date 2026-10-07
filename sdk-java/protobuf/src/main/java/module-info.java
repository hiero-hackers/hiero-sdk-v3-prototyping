/**
 * The Hiero API (HAPI) protobuf messages. The Java classes in this module are generated from the {@code .proto}
 * sources by the build; only the {@code .proto} files are under version control.
 */
module org.hiero.sdk.protobuf {

    requires transitive com.google.protobuf;

    exports org.hiero.hapi.proto;
    exports org.hiero.hapi.proto.mirror;
    exports com.hedera.hapi.node.state.tss.legacy;
    exports com.hedera.hapi.platform.event.legacy;
    exports com.hedera.hapi.services.auxiliary.hints.legacy;
    exports com.hedera.hapi.services.auxiliary.history.legacy;
    exports com.hedera.hapi.services.auxiliary.tss.legacy;
}
