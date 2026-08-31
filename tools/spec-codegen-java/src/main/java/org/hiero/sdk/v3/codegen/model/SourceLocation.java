package org.hiero.sdk.v3.codegen.model;

import java.nio.file.Path;
import java.util.Objects;

public record SourceLocation(String path, int line, int column) implements Comparable<SourceLocation> {

    public SourceLocation {
        path = requireRepositoryRelative(path);
        if (line < 1 || column < 1) {
            throw new IllegalArgumentException("line and column must be positive");
        }
    }

    public String display() {
        return path + ":" + line + ":" + column;
    }

    @Override
    public int compareTo(SourceLocation other) {
        var pathComparison = path.compareTo(other.path);
        if (pathComparison != 0) {
            return pathComparison;
        }
        var lineComparison = Integer.compare(line, other.line);
        return lineComparison != 0 ? lineComparison : Integer.compare(column, other.column);
    }

    public static String requireRepositoryRelative(String value) {
        Objects.requireNonNull(value, "path");
        var normalized = value.replace('\\', '/');
        if (normalized.isBlank()
                || normalized.startsWith("/")
                || normalized.matches("^[A-Za-z]:/.*")
                || Path.of(normalized).isAbsolute()
                || normalized.equals("..")
                || normalized.startsWith("../")
                || normalized.contains("/../")) {
            throw new IllegalArgumentException("path must be repository-relative: " + value);
        }
        return normalized;
    }
}
