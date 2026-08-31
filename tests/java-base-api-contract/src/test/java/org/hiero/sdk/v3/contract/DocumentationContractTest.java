package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class DocumentationContractTest {

    @Test
    void everyGeneratedCompilationUnitHasOwnershipHeaderAndDocumentation() throws Exception {
        var generated = ContractTestSupport.repositoryRoot().resolve("java/hiero-sdk-base-api/src/main/java");
        try (var paths = Files.walk(generated)) {
            for (var path : paths.filter(Files::isRegularFile).toList()) {
                var source = Files.readString(path);
                assertTrue(source.startsWith("// Generated from "), path.toString());
                assertTrue(source.contains("DO NOT EDIT."), path.toString());
                assertTrue(source.contains("/**"), path.toString());
            }
        }
    }

    @Test
    void provenanceRetainsSourceQuestions() throws Exception {
        var manifest = ContractTestSupport.manifest();
        assertTrue(manifest.contains("\"retainedQuestions\""));
        assertTrue(manifest.contains("Should we rename `Ledger` to `Network`?"));
        assertTrue(manifest.contains("spec/base/ledger.md"));
    }
}
