package org.hiero.sdk.v3.codegen.manifest;

import java.util.Objects;

public record DeferredEnforcement(String sourceElement, String decisionId, String reason, String enforcement) {
    public DeferredEnforcement {
        requireText(sourceElement, "sourceElement");
        requireText(decisionId, "decisionId");
        requireText(reason, "reason");
        requireText(enforcement, "enforcement");
    }

    private static void requireText(String value, String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
