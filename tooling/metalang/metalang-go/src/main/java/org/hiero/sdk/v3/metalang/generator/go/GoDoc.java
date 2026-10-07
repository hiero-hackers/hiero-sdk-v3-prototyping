package org.hiero.sdk.v3.metalang.generator.go;

import org.hiero.sdk.v3.metalang.ast.Annotated;

/**
 * Renders Go documentation comments. A Go doc comment starts with the identifier it documents, which is what
 * {@code go doc} and the editors expect, so the generator prefixes the spec text with the generated name when the
 * text does not already start with it.
 */
final class GoDoc {

    /** How wide a documentation comment is wrapped. */
    private static final int WIDTH = 110;

    private GoDoc() {
    }

    /**
     * The doc comment of a declaration.
     *
     * @param name          the generated Go name the comment has to start with
     * @param documentation the spec documentation, possibly empty
     * @param annotated     the declaration, read for {@code @@deprecated}
     * @return the comment lines, each ending in a newline, or {@code ""} when there is nothing to say
     */
    static String of(final String name, final String documentation, final Annotated annotated) {
        final String text = documentation == null ? "" : documentation.strip();
        final StringBuilder go = new StringBuilder();
        if (!text.isEmpty()) {
            go.append(comment(text.startsWith(name) ? text : name + " " + lowerFirst(text)));
        }
        final String deprecation = deprecation(annotated);
        if (deprecation != null) {
            if (!go.isEmpty()) {
                go.append("//\n");
            }
            // the exact form `go vet` and the editors recognise
            go.append(comment("Deprecated: " + deprecation));
        }
        return go.toString();
    }

    /**
     * The {@code Deprecated:} paragraph, or {@code null} when the declaration is not deprecated.
     * {@code @@deprecated} takes no arguments, so the reason, where there is one, is part of the documentation
     * above the declaration and already in the comment.
     */
    private static String deprecation(final Annotated annotated) {
        return annotated.hasAnnotation("deprecated") ? "this declaration should no longer be used." : null;
    }

    /**
     * Lowers the first letter of a sentence so it reads on after the prepended name - unless the word is a name
     * itself, which a second upper-case letter or a following upper-case word signals.
     */
    private static String lowerFirst(final String text) {
        final int end = text.indexOf(' ') < 0 ? text.length() : text.indexOf(' ');
        final String first = text.substring(0, end);
        if (first.chars().filter(Character::isUpperCase).count() > 1) {
            return text;
        }
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }

    /**
     * Wraps text into {@code //} comment lines. A blank line separates paragraphs, and a line that is indented in
     * the source stays verbatim, because an indented block is how a Go doc comment writes code.
     *
     * @param text the text
     * @return the comment lines
     */
    static String comment(final String text) {
        final StringBuilder out = new StringBuilder();
        for (final String paragraph : text.split("\n\n+")) {
            if (!out.isEmpty()) {
                out.append("//\n");
            }
            if (paragraph.startsWith(" ") || paragraph.startsWith("\t")) {
                paragraph.lines().forEach(line -> out.append("//").append(line).append('\n'));
                continue;
            }
            final StringBuilder line = new StringBuilder("//");
            for (final String word : paragraph.replace('\n', ' ').trim().split("\\s+")) {
                if (line.length() + 1 + word.length() > WIDTH) {
                    out.append(line).append('\n');
                    line.setLength(0);
                    line.append("//");
                }
                line.append(' ').append(word);
            }
            out.append(line).append('\n');
        }
        return out.toString();
    }
}
