package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class CleanRegenerationTest {

    @Test
    void twoFreshGenerationsMatchEachOtherAndCheckedInOutput() throws Exception {
        var root = ContractTestSupport.repositoryRoot();
        var base = root.resolve("tests/java-base-api-contract/target/clean-regeneration");
        var first = base.resolve("first");
        var second = base.resolve("second");
        generate(root, first, base.resolve("first.json"), base.resolve("stage-first"));
        generate(root, second, base.resolve("second.json"), base.resolve("stage-second"));

        var checked = root.resolve("java/hiero-sdk-base-api/src/main/java");
        assertEquals(snapshot(first), snapshot(second));
        assertEquals(snapshot(checked), snapshot(first));
        assertEquals(Files.readString(base.resolve("first.json")), Files.readString(base.resolve("second.json")));
        assertEquals(ContractTestSupport.manifest(), Files.readString(base.resolve("first.json")));
    }

    private static void generate(Path root, Path output, Path manifest, Path staging) throws Exception {
        var javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var generatorClasses = root.resolve("tools/spec-codegen-java/target/classes").toString();
        var process = new ProcessBuilder(
                        javaExecutable, "-cp", generatorClasses, "org.hiero.sdk.v3.codegen.cli.Main", "generate",
                        "--repository-root", root.toString(),
                        "--physical-source-root", output.toString(),
                        "--physical-manifest", manifest.toString(),
                        "--physical-staging-root", staging.toString())
                .redirectErrorStream(true)
                .start();
        var diagnostics = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), diagnostics);
        assertTrue(diagnostics.contains("Generated 62 Java source files."));
    }

    private static TreeMap<String, String> snapshot(Path root) throws Exception {
        var result = new TreeMap<String, String>();
        try (var paths = Files.walk(root)) {
            for (var path : paths.filter(Files::isRegularFile).toList()) {
                result.put(root.relativize(path).toString().replace('\\', '/'), sha256(Files.readAllBytes(path)));
            }
        }
        return result;
    }

    private static String sha256(byte[] bytes) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
