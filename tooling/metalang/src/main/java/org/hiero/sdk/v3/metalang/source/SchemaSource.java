package org.hiero.sdk.v3.metalang.source;

import java.util.Objects;

/**
 * The meta-language text of one spec file together with the information needed to map positions in
 * that text back to the original Markdown file.
 *
 * @param file       the spec file name (used in diagnostics)
 * @param text       the content of the schema code block (without the fences)
 * @param lineOffset number of Markdown lines that precede the first line of {@code text}; line
 *                   {@code n} of the schema is line {@code n + lineOffset} of the Markdown file
 * @param description the Markdown text of the spec's {@code ## Description} section (empty if there is none)
 * @param descriptionLine the Markdown line of the first line of {@code description} (0 if there is none)
 */
public record SchemaSource(String file, String text, int lineOffset, String description, int descriptionLine) {

    /**
     * Creates a new schema source.
     *
     * @param file       the spec file name
     * @param text       the schema text
     * @param lineOffset line offset into the Markdown file, must not be negative
     * @param description the description section (Markdown)
     * @param descriptionLine line of the first description line or 0
     */
    public SchemaSource {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(description, "description must not be null");
        if (lineOffset < 0) {
            throw new IllegalArgumentException("lineOffset must not be negative: " + lineOffset);
        }
    }

    /**
     * Creates a schema source without a description.
     *
     * @param file       the spec file name
     * @param text       the schema text
     * @param lineOffset line offset into the Markdown file, must not be negative
     */
    public SchemaSource(final String file, final String text, final int lineOffset) {
        this(file, text, lineOffset, "", 0);
    }

    /**
     * Creates a schema source for a plain meta-language text (no surrounding Markdown).
     *
     * @param file the file name used in diagnostics
     * @param text the schema text
     * @return a schema source with a line offset of 0
     */
    public static SchemaSource ofPlainText(final String file, final String text) {
        return new SchemaSource(file, text, 0);
    }
}
