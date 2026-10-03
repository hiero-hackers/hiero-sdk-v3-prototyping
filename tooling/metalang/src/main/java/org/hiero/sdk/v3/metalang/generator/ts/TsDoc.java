package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders TSDoc comments ({@code /** ... * /}) from the documentation of the specs (Markdown) and the tags the
 * generator adds ({@code @param}, {@code @returns}, {@code @throws}, {@code @deprecated}).
 */
final class TsDoc {

    /** The text of a deprecated element whose documentation does not explain the deprecation. */
    static final String DEFAULT_DEPRECATION = "Retained for compatibility; do not use it in new code.";

    private TsDoc() {
    }

    /**
     * Renders a comment.
     *
     * @param indent     the indentation
     * @param paragraphs the Markdown paragraphs; blank ones are skipped
     * @param tags       the block tags
     * @param deprecated whether the element is deprecated: the paragraph that mentions the deprecation becomes the
     *                   text of the {@code @deprecated} tag
     * @return the comment, empty if there is nothing to document
     */
    static String render(final String indent, final List<String> paragraphs, final List<String> tags,
                         final boolean deprecated) {
        final List<String> description = new ArrayList<>();
        String reason = null;
        for (final String paragraph : paragraphs) {
            if (paragraph == null || paragraph.isBlank()) {
                continue;
            }
            for (final String part : paragraph.strip().split("\\n\\s*\\n")) {
                if (deprecated && reason == null && part.toLowerCase(Locale.ROOT).contains("deprecat")) {
                    reason = part.strip();
                } else {
                    description.add(part.strip());
                }
            }
        }
        final List<String> allTags = new ArrayList<>(tags);
        if (deprecated) {
            allTags.add("@deprecated " + (reason == null ? DEFAULT_DEPRECATION : reason));
        }
        if (description.isEmpty() && allTags.isEmpty()) {
            return "";
        }
        final StringBuilder out = new StringBuilder(indent).append("/**\n");
        for (int i = 0; i < description.size(); i++) {
            if (i > 0) {
                out.append(indent).append(" *\n");
            }
            description.get(i).lines().forEach(l -> line(out, indent, escape(l)));
        }
        if (!allTags.isEmpty()) {
            if (!description.isEmpty()) {
                out.append(indent).append(" *\n");
            }
            for (final String tag : allTags) {
                final List<String> lines = tag.lines().toList();
                line(out, indent, lines.getFirst().replace("*/", "*\\/"));
                lines.subList(1, lines.size()).forEach(l -> line(out, indent, "  " + escape(l)));
            }
        }
        return out.append(indent).append(" */\n").toString();
    }

    static String render(final String indent, final List<String> paragraphs) {
        return render(indent, paragraphs, List.of(), false);
    }

    private static void line(final StringBuilder out, final String indent, final String text) {
        out.append(indent).append(" *").append(text.isBlank() ? "" : " " + text.stripTrailing()).append('\n');
    }

    /** Escapes what TSDoc would read as tag or as end of the comment. */
    private static String escape(final String line) {
        final String text = line.replace("*/", "*\\/");
        return text.stripLeading().startsWith("@") ? text.replaceFirst("@", "\\\\@") : text;
    }
}
