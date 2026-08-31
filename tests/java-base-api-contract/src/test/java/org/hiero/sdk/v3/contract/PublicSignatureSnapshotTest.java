package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class PublicSignatureSnapshotTest {

    @Test
    void compiledPublicTypesMatchReviewedSnapshot() throws Exception {
        var repositoryRoot = ContractTestSupport.repositoryRoot();
        var classesRoot = repositoryRoot.resolve("java/hiero-sdk-base-api/target/classes");
        var snapshot = repositoryRoot.resolve(
                "tests/java-base-api-contract/src/test/resources/api-signatures/base-api.txt");

        var expected = new TreeSet<String>();
        for (var line : Files.readAllLines(snapshot)) {
            var entry = line.strip();
            if (!entry.isEmpty() && !entry.startsWith("#")) {
                expected.add(entry);
            }
        }

        var actual = new TreeSet<String>();
        try (var paths = Files.walk(classesRoot)) {
            for (var path : paths.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".class"))
                    .filter(file -> !file.getFileName().toString().contains("$"))
                    .toList()) {
                var relative = classesRoot.relativize(path).toString();
                var className = relative.substring(0, relative.length() - ".class".length())
                        .replace(java.io.File.separatorChar, '.');
                if (className.equals("module-info") || className.endsWith("package-info")) {
                    continue;
                }
                var type = Class.forName(className);
                if (Modifier.isPublic(type.getModifiers())) {
                    actual.add(className);
                }
            }
        }

        assertEquals(expected, actual, "Update the reviewed API snapshot only for approved mapping changes");
    }
}
