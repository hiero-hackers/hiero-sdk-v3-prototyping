package org.hiero.sdk.v3.metalang.generator;

import java.util.Objects;

/**
 * A file produced by a generator.
 *
 * @param path    the path relative to the output directory, with {@code /} as separator
 * @param content the file content (LF line endings)
 */
public record GeneratedFile(String path, String content) implements Comparable<GeneratedFile> {

    /**
     * Creates a generated file.
     *
     * @param path    the relative path
     * @param content the content
     */
    public GeneratedFile {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (path.startsWith("/") || path.contains("..") || path.contains("\\")) {
            throw new IllegalArgumentException("path must be relative and use '/': " + path);
        }
    }

    @Override
    public int compareTo(final GeneratedFile other) {
        return path.compareTo(other.path);
    }
}
