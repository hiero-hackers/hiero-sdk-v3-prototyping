package org.hiero.sdk.v3.codegen.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.sdk.v3.codegen.mapping.JavaTypeMapper;
import org.hiero.sdk.v3.codegen.model.PrimitiveKind;
import org.hiero.sdk.v3.codegen.model.PrimitiveTypeReference;
import org.hiero.sdk.v3.codegen.model.SourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenConfigurationTest {

    @Test
    void loadsReviewedConfigurationAndCanonicalMappings() throws Exception {
        var root = repositoryRoot();
        var configuration = CodegenConfiguration.load(root, Path.of("codegen/java-base.yml"));
        var mapper = new JavaTypeMapper(configuration);
        var location = new SourceLocation("spec/base/test.md", 1, 1);

        assertEquals(11, configuration.specifications().size());
        assertEquals("org.hiero.sdk.v3.nativetoken", mapper.packageName("nativeToken"));
        assertEquals("int", mapper.map(new PrimitiveTypeReference(PrimitiveKind.UINT16, location), false));
        assertEquals("Long", mapper.map(new PrimitiveTypeReference(PrimitiveKind.UINT64, location), true));
    }

    @Test
    void rejectsUnknownConfigurationKeys(@TempDir Path root) throws Exception {
        var source = repositoryRoot().resolve("codegen/java-base.yml");
        var configuration = Files.readString(source) + "unknown: value\n";
        Files.createDirectories(root.resolve("codegen"));
        Files.writeString(root.resolve("codegen/java-base.yml"), configuration);

        assertThrows(
                IllegalArgumentException.class,
                () -> CodegenConfiguration.load(root, Path.of("codegen/java-base.yml")));
    }

    private static Path repositoryRoot() {
        var current = Path.of("").toAbsolutePath().normalize();
        while (current != null && !Files.isRegularFile(current.resolve("codegen/java-base.yml"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("repository root not found");
        }
        return current;
    }
}
