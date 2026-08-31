package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.manifest.DeferredEnforcement;
import org.hiero.sdk.v3.codegen.model.MappingDecision;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public record ApiGeneration(
        List<JavaSourceFile> sourceFiles,
        List<MappingDecision> mappings,
        List<DeferredEnforcement> deferredEnforcement) {
    public ApiGeneration {
        sourceFiles = List.copyOf(sourceFiles);
        mappings = List.copyOf(mappings);
        deferredEnforcement = List.copyOf(deferredEnforcement);
    }
}
