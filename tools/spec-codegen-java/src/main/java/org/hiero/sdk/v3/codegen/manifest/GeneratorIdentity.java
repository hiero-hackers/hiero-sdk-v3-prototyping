package org.hiero.sdk.v3.codegen.manifest;

import java.util.Objects;

public record GeneratorIdentity(String groupId, String artifactId, String version) {
    public GeneratorIdentity {
        requireText(groupId, "groupId");
        requireText(artifactId, "artifactId");
        requireText(version, "version");
    }

    private static void requireText(String value, String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
