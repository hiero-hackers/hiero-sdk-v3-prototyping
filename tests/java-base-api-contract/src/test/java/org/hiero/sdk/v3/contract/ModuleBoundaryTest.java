package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModuleBoundaryTest {

    @Test
    void moduleHasReviewedExportsAndNoRuntimeLibraryDependency() {
        var classes = ContractTestSupport.repositoryRoot().resolve("java/hiero-sdk-base-api/target/classes");
        var descriptor = ModuleFinder.of(classes).find("org.hiero.sdk.v3.base").orElseThrow().descriptor();
        assertEquals(11, descriptor.exports().size());
        assertEquals(Set.of("java.base", "org.jspecify"), descriptor.requires().stream()
                .map(requirement -> requirement.name()).collect(java.util.stream.Collectors.toSet()));
        var jspecify = descriptor.requires().stream().filter(requirement -> requirement.name().equals("org.jspecify")).findFirst().orElseThrow();
        assertTrue(jspecify.modifiers().contains(java.lang.module.ModuleDescriptor.Requires.Modifier.STATIC));
    }

    @Test
    void generatedArtifactContainsNoGeneratorTestOrImplementationLeakage() throws Exception {
        var generated = ContractTestSupport.repositoryRoot().resolve("java/hiero-sdk-base-api/src/main/java");
        try (var paths = Files.walk(generated)) {
            for (var path : paths.filter(Files::isRegularFile).toList()) {
                var source = Files.readString(path);
                assertFalse(source.contains("org.hiero.sdk.v3.codegen"), path.toString());
                assertFalse(source.contains("org.junit"), path.toString());
                assertFalse(source.matches("(?s).*package .*\\.impl(?:\\.|;).*"), path.toString());
            }
        }
    }
}
