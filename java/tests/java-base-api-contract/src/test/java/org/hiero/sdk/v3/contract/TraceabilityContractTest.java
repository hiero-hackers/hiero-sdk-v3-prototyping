package org.hiero.sdk.v3.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TraceabilityContractTest {
    private static final Map<String, String> SPEC_TO_NAMESPACE = Map.ofEntries(
            Map.entry("authority.md", "authority"),
            Map.entry("common.md", "common"),
            Map.entry("grpc.md", "grpc"),
            Map.entry("hedera.md", "hedera"),
            Map.entry("keys.md", "keys"),
            Map.entry("ledger-config.md", "ledger.config"),
            Map.entry("ledger.md", "ledger"),
            Map.entry("native-token.md", "nativeToken"),
            Map.entry("proto.md", "proto"),
            Map.entry("solo.md", "solo"),
            Map.entry("token.md", "token"));

    private static final List<String> REQUIREMENTS = List.of(
            "Complete base specification coverage",
            "Limited structural value implementation",
            "Operational implementation deferred",
            "Idiomatic Java contract mapping",
            "Immutable and null-safe public values",
            "Stable API and implementation boundaries",
            "Security-sensitive contracts declarative",
            "Async contracts preserve completion/error semantics",
            "Reviewed and traceable direct maintenance",
            "Source questions remain unresolved",
            "Conformance independently verifiable");

    @Test
    void shouldAccountForEveryBaseSpecification() throws IOException {
        final String mapping = Files.readString(ContractTestSupport.DOCS.resolve("mapping-matrix.md"));
        final List<String> actualFiles;
        try (java.util.stream.Stream<Path> paths = Files.list(ContractTestSupport.BASE_SPEC)) {
            actualFiles = paths.filter(path -> path.toString().endsWith(".md"))
                    .map(path -> path.getFileName().toString()).sorted().toList();
        }
        assertThat(actualFiles).containsExactlyElementsOf(SPEC_TO_NAMESPACE.keySet().stream().sorted().toList());
        SPEC_TO_NAMESPACE.forEach((file, namespace) -> assertThat(mapping).contains(file, namespace));
    }

    @Test
    void shouldRetainSourceQuestions() throws IOException {
        final String mapping = Files.readString(ContractTestSupport.DOCS.resolve("mapping-matrix.md"));
        assertThat(mapping).contains(
                "forthcoming `@@sealed`",
                "`Endorsement`",
                "whether `Ledger` should be named `Network`",
                "network identifier bytes",
                "decimal rather than `double`",
                "`TokenId`, `NftId`, and `PendingAirdropId`");
    }

    @Test
    void shouldMapEveryNormativeRequirementToVerification() throws IOException {
        final String verification = Files.readString(ContractTestSupport.DOCS.resolve("verification-matrix.md"));
        assertThat(REQUIREMENTS).allMatch(verification::contains);
    }

    @Test
    void shouldDocumentDirectMaintenanceAndFutureImplementationBoundary() throws IOException {
        final String readme = Files.readString(ContractTestSupport.DOCS.resolve("README.md"));
        assertThat(readme).contains(
                "does not parse the Markdown schema",
                "Future implementation modules",
                "depend on `hiero-sdk-base-api`",
                "must not alter or leak through this public API");
    }
}
