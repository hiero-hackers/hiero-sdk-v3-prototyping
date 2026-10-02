package org.hiero.sdk.v3.metalang.diagnostic;

import java.util.Objects;

/**
 * A position in a source file. Lines and columns are 1-based and refer to the original Markdown file,
 * not to the extracted schema block.
 *
 * @param file   the source file name as given to the tool (relative path where possible)
 * @param line   1-based line number
 * @param column 1-based column number
 */
public record SourceLocation(String file, int line, int column) implements Comparable<SourceLocation> {

    /**
     * Creates a new location.
     *
     * @param file   the source file name
     * @param line   1-based line number
     * @param column 1-based column number
     */
    public SourceLocation {
        Objects.requireNonNull(file, "file must not be null");
    }

    @Override
    public int compareTo(final SourceLocation other) {
        final int byFile = file.compareTo(other.file);
        if (byFile != 0) {
            return byFile;
        }
        final int byLine = Integer.compare(line, other.line);
        return byLine != 0 ? byLine : Integer.compare(column, other.column);
    }

    @Override
    public String toString() {
        return file + ":" + line + ":" + column;
    }
}
