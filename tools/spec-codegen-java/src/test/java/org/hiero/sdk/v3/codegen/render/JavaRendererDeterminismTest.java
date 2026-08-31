package org.hiero.sdk.v3.codegen.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.junit.jupiter.api.Test;

class JavaRendererDeterminismTest {

    private final JavaRenderer renderer =
            new JavaRenderer("// Generated from {sourcePath} ({declaration}); DO NOT EDIT.");

    @Test
    void rendersCanonicalOrderImportsUtf8LfAndFixedSpacing() {
        var alpha = new JavaSourceFile(
                Path.of("org/example/Alpha.java"),
                "spec/base/alpha.md",
                "alpha#Alpha",
                "org.example",
                List.of("java.util.Set", "java.util.List", "java.util.List"),
                "/** ℏ value. */\r\npublic interface Alpha {\r\n}\r\n");
        var beta = new JavaSourceFile(
                Path.of("org/example/Beta.java"),
                "spec/base/beta.md",
                "beta#Beta",
                "org.example",
                List.of(),
                "public record Beta(int value) {}\n");

        var rendered = renderer.render(List.of(beta, alpha));
        var repeated = renderer.render(List.of(alpha, beta));

        assertEquals(List.of(Path.of("org/example/Alpha.java"), Path.of("org/example/Beta.java")),
                rendered.stream().map(RenderedFile::relativePath).toList());
        assertArrayEquals(rendered.getFirst().content(), repeated.getFirst().content());
        var text = new String(rendered.getFirst().content(), StandardCharsets.UTF_8);
        assertTrue(text.contains("ℏ"));
        assertTrue(text.contains("import java.util.List;\nimport java.util.Set;"));
        assertFalse(text.contains("\r"));
        assertTrue(text.endsWith("}\n"));
    }

    @Test
    void outputContainsNoHostClockOrEnvironmentMetadata() {
        var source = new JavaSourceFile(
                Path.of("org/example/Safe.java"),
                "spec/base/safe.md",
                "safe#Safe",
                "org.example",
                List.of(),
                "public interface Safe {}\n");

        var text = new String(renderer.render(List.of(source)).getFirst().content(), StandardCharsets.UTF_8);

        assertFalse(text.contains(System.getProperty("user.home")));
        assertFalse(text.contains(System.getProperty("user.name")));
        assertFalse(text.matches("(?s).*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*"));
    }

    @Test
    void rejectsDuplicateOrEscapingOutputPaths() {
        var source = new JavaSourceFile(
                Path.of("Same.java"), "spec/base/a.md", "a#Same", "org.example", List.of(), "class Same {}\n");
        assertEquals(
                "DUPLICATE_OUTPUT",
                assertThrows(SchemaException.class, () -> renderer.render(List.of(source, source))).code());

        var escaping = new JavaSourceFile(
                Path.of("../Escape.java"), "spec/base/a.md", "a#Escape", "org.example", List.of(), "class Escape {}\n");
        assertThrows(IllegalArgumentException.class, () -> renderer.render(List.of(escaping)));
    }
}
