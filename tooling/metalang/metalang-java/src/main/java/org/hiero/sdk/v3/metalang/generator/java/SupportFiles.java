package org.hiero.sdk.v3.metalang.generator.java;

/**
 * The Java support types of the SDK: types that the generated API uses but that the specs do not declare
 * ({@code @ThreadSafe}, {@code HieroStream}, {@code StreamItem}, ...). They are hand-written in the Maven module
 * {@code sdk-java/support} (artifact {@value #ARTIFACT}, JPMS module {@value #MODULE}); the generated modules that use
 * them depend on it.
 */
final class SupportFiles {

    /** The JPMS module of the support types. */
    static final String MODULE = "org.hiero.sdk.support";

    /** The Maven artifact of the support types (same group ID and version as the generated API). */
    static final String ARTIFACT = "hiero-sdk-support";

    /** The package of the streaming support ({@code @@streaming}, {@code streamResult<T>}). */
    static final String STREAMING_PACKAGE = "org.hiero.sdk.common";

    private SupportFiles() {
    }
}
