package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders Markdown as a Java Markdown documentation comment ({@code ///}, JEP 467, Java 23+).
 *
 * <p>The Markdown is taken over verbatim with one exception: outside of fenced code blocks, a line whose first
 * non-blank character is {@code @} would start a Javadoc block tag, so that {@code @} is written as the HTML entity
 * {@code &#64;} (rendered as {@code @}).
 */
final class MarkdownComment {

    private static final Pattern FENCE = Pattern.compile("^ {0,3}(`{3,}|~{3,}).*$");

    private MarkdownComment() {
    }

    /**
     * Renders the given Markdown paragraphs (separated by an empty comment line) as comment lines.
     *
     * @param indent     indentation in front of every line
     * @param paragraphs the Markdown blocks; blank ones are skipped
     * @return the comment, each line terminated by a line break; empty if all paragraphs are blank
     */
    static String render(final String indent, final List<String> paragraphs) {
        return render(indent, paragraphs, List.of());
    }

    /**
     * Renders Markdown paragraphs followed by Javadoc block tags. The paragraphs are escaped (they come from the
     * spec), the tags are written as they are (they are produced by the generator), e.g.
     * {@code @throws IllegalArgumentException}.
     *
     * @param indent     indentation in front of every line
     * @param paragraphs the Markdown blocks; blank ones are skipped
     * @param tags       block tags, one per line
     * @return the comment, each line terminated by a line break; empty if there is neither text nor a tag
     */
    static String render(final String indent, final List<String> paragraphs, final List<String> tags) {
        Objects.requireNonNull(indent, "indent must not be null");
        final List<String> lines = new ArrayList<>();
        for (final String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                continue;
            }
            if (!lines.isEmpty()) {
                lines.add("");
            }
            lines.addAll(escape(paragraph.strip().lines().toList()));
        }
        if (!tags.isEmpty() && !lines.isEmpty()) {
            lines.add("");
        }
        lines.addAll(tags);
        final StringBuilder out = new StringBuilder();
        for (final String line : lines) {
            out.append(indent).append(line.isBlank() ? "///" : "/// " + line.stripTrailing()).append('\n');
        }
        return out.toString();
    }

    private static List<String> escape(final List<String> lines) {
        final List<String> result = new ArrayList<>();
        String fence = null;
        for (final String line : lines) {
            final Matcher fenceMatcher = FENCE.matcher(line);
            if (fence == null && fenceMatcher.matches()) {
                fence = fenceMatcher.group(1);
            } else if (fence != null && isClosingFence(line, fence)) {
                fence = null;
            } else if (fence == null && line.stripLeading().startsWith("@")) {
                final int at = line.indexOf('@');
                result.add(line.substring(0, at) + "&#64;" + line.substring(at + 1));
                continue;
            }
            result.add(line);
        }
        return result;
    }

    private static boolean isClosingFence(final String line, final String fence) {
        final String stripped = line.strip();
        return stripped.length() >= fence.length() && stripped.chars().allMatch(c -> c == fence.charAt(0));
    }
}
