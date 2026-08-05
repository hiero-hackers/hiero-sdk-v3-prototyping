package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.lang.module.ModuleFinder;
import java.util.Set;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

class PublicApiBoundaryTest {

    @Test
    void artifactHasReviewedModuleBoundaryAndNoGeneratorDependency() throws Exception {
        var apiClasses = ContractTestSupport.repositoryRoot().resolve("java/hiero-sdk-base-api/target/classes");
        var module = ModuleFinder.of(apiClasses).find("org.hiero.sdk.v3.base").orElseThrow().descriptor();
        assertEquals("org.hiero.sdk.v3.base", module.name());
        var exports = module.exports().stream().map(export -> export.source()).collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of(
                "org.hiero.sdk.v3.authority", "org.hiero.sdk.v3.common", "org.hiero.sdk.v3.grpc",
                "org.hiero.sdk.v3.hedera", "org.hiero.sdk.v3.keys", "org.hiero.sdk.v3.ledger",
                "org.hiero.sdk.v3.ledger.config", "org.hiero.sdk.v3.nativetoken", "org.hiero.sdk.v3.proto",
                "org.hiero.sdk.v3.solo", "org.hiero.sdk.v3.token"), exports);

        var apiPom = Files.readString(ContractTestSupport.repositoryRoot().resolve("java/hiero-sdk-base-api/pom.xml"));
        assertFalse(apiPom.contains("spec-codegen-java"));
    }

    @Test
    void representativeConsumerCompilesAgainstApiArtifactOnly() throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fixture = ContractTestSupport.repositoryRoot()
                .resolve("tests/java-base-api-contract/src/test/resources/fixtures/positive/AllNamespacesConsumer.java");
        var output = ContractTestSupport.repositoryRoot().resolve("tests/java-base-api-contract/target/consumer-fixture");
        Files.createDirectories(output);
        var result = compiler.run(null, null, null, "--release", "21", "-proc:none", "-d", output.toString(), fixture.toString());
        assertEquals(0, result);
        assertTrue(Files.isRegularFile(output.resolve("org/hiero/sdk/v3/fixture/AllNamespacesConsumer.class")));
    }
}
