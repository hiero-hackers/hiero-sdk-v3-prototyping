package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record MappingDecision(
        String sourceElement,
        String javaElement,
        MappingKind mappingKind,
        String rationale,
        List<String> requirementIds) {

    public MappingDecision {
        requireText(sourceElement, "sourceElement");
        requireText(javaElement, "javaElement");
        Objects.requireNonNull(mappingKind, "mappingKind");
        requireText(rationale, "rationale");
        requirementIds = List.copyOf(requirementIds);
        if (requirementIds.isEmpty()) {
            throw new IllegalArgumentException("requirementIds must not be empty");
        }
    }

    public static MappingDecision direct(
            String sourceElement, String javaElement, String rationale, String... requirementIds) {
        return new MappingDecision(
                sourceElement, javaElement, MappingKind.DIRECT, rationale, List.of(requirementIds));
    }

    private static void requireText(String value, String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
