package org.hiero.sdk.v3.codegen.manifest;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.codegen.model.MappingDecision;

public record GenerationManifest(
        String sourceRevision,
        GeneratorIdentity generator,
        String configurationSha256,
        int javaRelease,
        String packageRoot,
        List<FileDigest> inputs,
        List<MappingDecision> mappings,
        List<RetainedQuestion> retainedQuestions,
        List<DeferredEnforcement> deferredEnforcement,
        List<FileDigest> outputs) {

    public GenerationManifest {
        requireText(sourceRevision, "sourceRevision");
        Objects.requireNonNull(generator, "generator");
        if (!Objects.requireNonNull(configurationSha256, "configurationSha256").matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("configurationSha256 must be a lowercase SHA-256 digest");
        }
        if (javaRelease < 1) {
            throw new IllegalArgumentException("javaRelease must be positive");
        }
        requireText(packageRoot, "packageRoot");
        inputs = List.copyOf(inputs);
        mappings = List.copyOf(mappings);
        retainedQuestions = List.copyOf(retainedQuestions);
        deferredEnforcement = List.copyOf(deferredEnforcement);
        outputs = List.copyOf(outputs);
    }

    public GenerationManifest(
            String sourceRevision,
            GeneratorIdentity generator,
            String configurationSha256,
            int javaRelease,
            String packageRoot,
            List<FileDigest> inputs,
            List<MappingDecision> mappings,
            List<DeferredEnforcement> deferredEnforcement,
            List<FileDigest> outputs) {
        this(sourceRevision, generator, configurationSha256, javaRelease, packageRoot, inputs, mappings,
                List.of(), deferredEnforcement, outputs);
    }

    private static void requireText(String value, String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
