package org.hiero.sdk.v3.metalang.check;

import java.util.Objects;

/**
 * A difference between the expected API (generated from the specs) and the API of a project, or a source of the
 * project that cannot be read.
 *
 * @param file    the source file the difference refers to (relative to the checked project), empty if unknown
 * @param line    the line in the file, 0 if unknown
 * @param message the description of the difference
 */
public record ApiDifference(String file, long line, String message) implements Comparable<ApiDifference> {

    /**
     * Creates a difference.
     *
     * @param file    the source file, empty if unknown
     * @param line    the line in the file, 0 if unknown
     * @param message the description
     */
    public ApiDifference {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }

    @Override
    public int compareTo(final ApiDifference other) {
        final int byFile = file.compareTo(other.file);
        if (byFile != 0) {
            return byFile;
        }
        final int byLine = Long.compare(line, other.line);
        return byLine != 0 ? byLine : message.compareTo(other.message);
    }

    @Override
    public String toString() {
        return (file.isEmpty() ? "" : file + (line > 0 ? ":" + line : "") + ": ") + message;
    }
}
