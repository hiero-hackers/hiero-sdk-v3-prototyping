package org.hiero.sdk.v3.metalang.generator.go;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lays a list of declarations out in columns, the way {@code gofmt} does.
 *
 * <p>{@code gofmt} aligns the attributes of a struct, the entries of a composite literal and the constants of a
 * {@code const} block, but not the statements in a function body. The generator cannot call the Go toolchain -
 * nothing in the build may depend on it - so it aligns these blocks itself, at the places where it knows it is
 * emitting a declaration list. {@code gofmt -l} over the generated module is what proves the result agrees.
 */
final class GoFormat {

    /** A declaration with two cells: indentation, the first cell, the separating blanks, and the rest. */
    private static final Pattern CELLS = Pattern.compile("^(\t*)(?!//)([^\t ]+)( +)(\\S.*)$");

    private GoFormat() {
    }

    /**
     * Aligns a block of declarations. A line without two cells - a comment, a blank line - ends the column and
     * starts a new one after it, which is what {@code gofmt} does: a constant that carries its own comment is
     * not aligned with the constants above it.
     *
     * @param lines the declarations, each without its trailing newline
     * @return the aligned lines, joined, each ending in a newline, or {@code ""} for an empty block
     */
    static String block(final List<String> lines) {
        final StringBuilder go = new StringBuilder();
        final List<String> run = new ArrayList<>();
        for (final String line : lines) {
            if (CELLS.matcher(line).matches()) {
                run.add(line);
                continue;
            }
            go.append(aligned(run)).append(line).append('\n');
            run.clear();
        }
        return go.append(aligned(run)).toString();
    }

    /** Pads the first cell of every line of one run to the widest one. */
    private static String aligned(final List<String> run) {
        int width = 0;
        for (final String line : run) {
            final Matcher matcher = CELLS.matcher(line);
            matcher.matches();
            width = Math.max(width, matcher.group(2).length());
        }
        final StringBuilder go = new StringBuilder();
        for (final String line : run) {
            final Matcher matcher = CELLS.matcher(line);
            matcher.matches();
            go.append(matcher.group(1)).append(matcher.group(2))
                    .append(" ".repeat(width - matcher.group(2).length() + 1))
                    .append(matcher.group(4)).append('\n');
        }
        return go.toString();
    }

    /**
     * Splits a text into lines and aligns them as a block.
     *
     * @param text the lines, separated and terminated by newlines
     * @return the aligned text
     */
    static String block(final String text) {
        if (text.isEmpty()) {
            return text;
        }
        final List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
        if (lines.getLast().isEmpty()) {
            lines.removeLast();
        }
        return block(lines);
    }
}
