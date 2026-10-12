package org.hiero.sdk.v3.metalang.generator.python;

import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Annotated;

/**
 * Docstrings: the documentation of the specs, with the notes for {@code @@deprecated} and {@code @@threadSafe}.
 * Prose paragraphs are wrapped; lists, tables, indented lines and code blocks are kept as they are.
 */
final class PyDoc {

    private static final int WIDTH = 110;

    private PyDoc() {
    }

    /**
     * The docstring of a declaration, or an empty string when there is nothing to say.
     *
     * @param indent        the indentation of the docstring
     * @param documentation the documentation of the specs (may be {@code null})
     * @param annotated     the declaration, for its annotations
     * @return the docstring with a trailing newline, or {@code ""}
     */
    static String of(final String indent, final String documentation, final Annotated annotated) {
        final List<String> paragraphs = new ArrayList<>();
        final String text = documentation == null ? "" : documentation.strip();
        if (!text.isEmpty()) {
            paragraphs.add(text);
        }
        if (annotated != null && annotated.hasAnnotation("threadSafe")) {
            paragraphs.add("Instances are safe to use from several threads.");
        }
        if (annotated != null && annotated.hasAnnotation("deprecated")) {
            paragraphs.add("Deprecated: this declaration should no longer be used.");
        }
        return paragraphs.isEmpty() ? "" : docstring(indent, String.join("\n\n", paragraphs));
    }

    /**
     * A docstring of a text.
     *
     * @param indent the indentation
     * @param text   the text
     * @return the docstring with a trailing newline
     */
    static String docstring(final String indent, final String text) {
        final String escaped = text.strip().replace("\\", "\\\\").replace("\"\"\"", "\\\"\\\"\\\"");
        // the opening quotes share the first line
        final List<String> lines = lines(escaped, WIDTH - indent.length() - 3);
        if (lines.size() == 1 && indent.length() + lines.getFirst().length() + 6 <= WIDTH
                && !lines.getFirst().endsWith("\"")) {
            return indent + "\"\"\"" + lines.getFirst() + "\"\"\"\n";
        }
        final StringBuilder out = new StringBuilder(indent).append("\"\"\"");
        for (int i = 0; i < lines.size(); i++) {
            out.append(i == 0 ? "" : lines.get(i).isEmpty() ? "" : indent).append(lines.get(i)).append('\n');
        }
        return out.append(indent).append("\"\"\"\n").toString();
    }

    /** Wraps prose paragraphs; a paragraph with structure (list, table, code, indentation) is kept line by line. */
    private static List<String> lines(final String text, final int width) {
        final List<String> out = new ArrayList<>();
        boolean code = false;
        for (final String paragraph : text.split("\n\\s*\n")) {
            if (!out.isEmpty()) {
                out.add("");
            }
            final boolean structured = code || paragraph.contains("```") || paragraph.lines().anyMatch(line ->
                    line.startsWith(" ") || line.startsWith("\t") || line.startsWith("- ") || line.startsWith("* ")
                            || line.startsWith("|") || line.matches("\\d+\\. .*"));
            if (structured) {
                for (final String line : paragraph.lines().toList()) {
                    out.add(line.stripTrailing());
                    if (line.strip().startsWith("```")) {
                        code = !code;
                    }
                }
                continue;
            }
            final StringBuilder line = new StringBuilder();
            for (final String word : paragraph.replace('\n', ' ').trim().split("\\s+")) {
                if (!line.isEmpty() && line.length() + 1 + word.length() > width) {
                    out.add(line.toString());
                    line.setLength(0);
                }
                line.append(line.isEmpty() ? "" : " ").append(word);
            }
            out.add(line.toString());
        }
        return out;
    }
}
