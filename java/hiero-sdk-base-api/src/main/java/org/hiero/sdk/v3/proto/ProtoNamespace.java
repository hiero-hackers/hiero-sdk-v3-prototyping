// Source: spec/base/proto.md (proto namespace; Java JPMS export marker).

package org.hiero.sdk.v3.proto;

/**
 * Marks the currently declaration-free {@code proto} namespace so it can be exported by JPMS.
 * No protocol implementation is present in the API artifact.
 */
public final class ProtoNamespace {
    private ProtoNamespace() {}
}
