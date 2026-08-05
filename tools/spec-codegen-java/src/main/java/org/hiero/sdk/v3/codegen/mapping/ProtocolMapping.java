package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class ProtocolMapping {
    public List<JavaSourceFile> sourceFiles() {
        return List.of(
                MappingSources.type("grpc", "MethodDescriptor", """
                        /** Identifies a transport-independent service method. */
                        public record MethodDescriptor(String serviceName, String methodName) {
                            /** Validates descriptor names. */
                            public MethodDescriptor {
                                Objects.requireNonNull(serviceName, "serviceName");
                                Objects.requireNonNull(methodName, "methodName");
                            }
                        }
                        """, "java.util.Objects"),
                MappingSources.type("proto", "ProtoPackageAnchor", """
                        /** Package-private anchor retaining the reserved JPMS package without adding a public type. */
                        final class ProtoPackageAnchor { private ProtoPackageAnchor() {} }
                        """));
    }
}
