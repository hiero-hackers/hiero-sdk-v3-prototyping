package org.hiero.sdk.v3.contract;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class ContractTestSupport {

    private ContractTestSupport() {}

    static Path repositoryRoot() {
        var current = Path.of("").toAbsolutePath().normalize();
        while (current != null && !Files.isRegularFile(current.resolve("codegen/java-base.yml"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("repository root not found");
        }
        return current;
    }

    static String manifest() throws IOException {
        return Files.readString(repositoryRoot().resolve("java/hiero-sdk-base-api/generated-api-manifest.json"));
    }
}
