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
    /** The explanation of a deprecated element whose documentation does not explain the deprecation. */
    static final String DEFAULT_DEPRECATION = "Retained for compatibility; do not use it in new code.";

    /**
     * Renders a documentation comment of an element that may be deprecated. For a deprecated element the paragraph of
     * the documentation that mentions the deprecation (contains "deprecat", ignoring case) becomes the text of the
     * {@code @deprecated} tag instead of a paragraph of the description; if there is no such paragraph, the tag gets
     * {@link #DEFAULT_DEPRECATION}.
     *
     * @param indent     the indentation of the comment
     * @param paragraphs the paragraphs of the documentation (each may contain several Markdown paragraphs)
     * @param tags       the block tags (e.g. {@code @param}, {@code @throws})
     * @param deprecated whether the element is {@code @@deprecated}
     * @return the comment, or an empty string if there is nothing to document
     */
    /**
     * Returns the paragraph of a documentation that explains a deprecation (contains "deprecat", ignoring case), e.g.
     * to repeat the explanation of a deprecated attribute at its setter.
     *
     * @param documentation the documentation
     * @return the paragraph, or an empty string
     */
    static String deprecationReason(final String documentation) {
        for (final String part : documentation.strip().split("\\n\\s*\\n")) {
            if (part.toLowerCase(java.util.Locale.ROOT).contains("deprecat")) {
                return part.strip();
            }
        }
        return "";
    }

    static String render(final String indent, final List<String> paragraphs, final List<String> tags,
                         final boolean deprecated) {
        if (!deprecated) {
            return render(indent, paragraphs, tags);
        }
        final List<String> description = new ArrayList<>();
        String reason = null;
        for (final String paragraph : paragraphs) {
            for (final String part : paragraph.strip().split("\\n\\s*\\n")) {
                if (reason == null && part.toLowerCase(java.util.Locale.ROOT).contains("deprecat")) {
                    reason = part.strip();
                } else {
                    description.add(part);
                }
            }
        }
        final List<String> allTags = new ArrayList<>(tags);
        final List<String> lines = (reason == null ? DEFAULT_DEPRECATION : reason).lines().toList();
        allTags.add("@deprecated " + lines.getFirst().strip());
        lines.subList(1, lines.size()).forEach(l -> allTags.add("  " + (l.stripLeading().startsWith("@")
                ? "&#64;" + l.stripLeading().substring(1) : l.strip())));
        return render(indent, description, allTags);
    }

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
