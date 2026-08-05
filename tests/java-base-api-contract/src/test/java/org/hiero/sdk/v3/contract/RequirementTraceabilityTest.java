package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class RequirementTraceabilityTest {

    @Test
    void everyFunctionalRequirementHasSpecificationTaskAndVerificationEvidence() throws Exception {
        var root = ContractTestSupport.repositoryRoot();
        var specification = Files.readString(root.resolve("specs/001-java-base-api/spec.md"));
        var tasks = Files.readString(root.resolve("specs/001-java-base-api/tasks.md"));
        var matrix = Files.readString(root.resolve("specs/001-java-base-api/contracts/verification-matrix.md"));
        for (var number = 1; number <= 17; number++) {
            var requirement = "FR-%03d".formatted(number);
            assertTrue(specification.contains(requirement), requirement + " missing from spec");
            assertTrue(tasks.contains(requirement), requirement + " missing from tasks");
            assertTrue(matrix.contains(requirement), requirement + " missing from verification matrix");
        }
    }
}
