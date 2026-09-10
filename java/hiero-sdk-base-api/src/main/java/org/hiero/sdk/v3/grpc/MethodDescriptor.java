// Source: spec/base/grpc.md (grpc#MethodDescriptor).

package org.hiero.sdk.v3.grpc;

import java.util.Objects;

/** Identifies a transport-independent service method. */
public record MethodDescriptor(String serviceName, String methodName) {
    /** Validates descriptor names. */
    public MethodDescriptor {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(methodName, "methodName");
    }
}
