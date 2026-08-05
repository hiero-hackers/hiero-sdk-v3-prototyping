package org.hiero.sdk.v3.codegen.render;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public final class JavaRenderer {

    private final String headerTemplate;

    public JavaRenderer(String headerTemplate) {
        this.headerTemplate = Objects.requireNonNull(headerTemplate, "headerTemplate");
    }

    public List<RenderedFile> render(List<JavaSourceFile> sourceFiles) {
        var sorted = sourceFiles.stream()
                .sorted(Comparator.comparing(file -> normalized(file.relativePath())))
                .toList();
        var paths = new HashSet<String>();
        var rendered = new ArrayList<RenderedFile>();
        for (var sourceFile : sorted) {
            var path = validateRelativePath(sourceFile.relativePath());
            if (!paths.add(path)) {
                throw new SchemaException("DUPLICATE_OUTPUT", new SourceLocation(sourceFile.sourcePath(), 1, 1),
                        "multiple declarations render to '" + path + "'");
            }
            rendered.add(new RenderedFile(Path.of(path), renderSource(sourceFile).getBytes(StandardCharsets.UTF_8)));
        }
        return List.copyOf(rendered);
    }

    private String renderSource(JavaSourceFile file) {
        var result = new StringBuilder();
        result.append(headerTemplate
                        .replace("{sourcePath}", file.sourcePath())
                        .replace("{declaration}", file.declarationIdentity()))
                .append("\n\n");
        if (!file.packageName().isBlank()) {
            result.append("package ").append(file.packageName()).append(";\n\n");
        }
        var imports = new TreeSet<>(file.imports());
        imports.forEach(imported -> result.append("import ").append(imported).append(";\n"));
        if (!imports.isEmpty()) {
            result.append('\n');
        }
        result.append(normalizeBody(file.body()));
        return result.toString();
    }

    private static String normalizeBody(String body) {
        var normalized = body.replace("\r\n", "\n").replace('\r', '\n');
        return normalized.stripTrailing() + "\n";
    }

    private static String validateRelativePath(Path path) {
        var value = normalized(path);
        if (path.isAbsolute() || value.isBlank() || value.equals("..") || value.startsWith("../")
                || value.contains("/../")) {
            throw new IllegalArgumentException("output path must be relative and contained: " + path);
        }
        return value;
    }

    private static String normalized(Path path) {
        return path.toString().replace('\\', '/');
    }
}
