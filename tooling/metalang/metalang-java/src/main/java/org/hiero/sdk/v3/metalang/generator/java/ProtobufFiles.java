package org.hiero.sdk.v3.metalang.generator.java;

/**
 * The protobuf messages of the Hiero node types: the wire format the SDK talks to a node with. They are compiled
 * from the vendored definitions in {@code /protobuf} by the hand-written Maven module {@code sdk-java/protobuf}
 * (artifact {@value #ARTIFACT}, JPMS module {@value #MODULE}); a generated module that needs them depends on it.
 *
 * <p>Unlike the support types, the protobuf messages are <strong>not</strong> part of the API: the wire format
 * changes with every network release, so exposing it would make every protocol change a breaking change of the SDK.
 * The generated {@code module-info.java} therefore uses a plain {@code requires} and not
 * {@code requires transitive} — a consumer of the SDK does not read the protobuf module — and the protobuf module
 * itself exports its packages only to the modules listed here ({@code exports ... to ...}).
 *
 * <p>Which modules get the dependency is a project decision the specs cannot express; it is configured with
 * {@link JavaGeneratorConfig#PROTOBUF}.
 */
final class ProtobufFiles {

    /** The JPMS module of the protobuf messages. */
    static final String MODULE = "org.hiero.sdk.protobuf";

    /** The Maven artifactId of the protobuf messages. */
    static final String ARTIFACT = "hiero-sdk-protobuf";

    private ProtobufFiles() {
    }
}
