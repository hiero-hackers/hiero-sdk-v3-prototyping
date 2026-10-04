package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Renders documentation comments ({@code ///}) and the {@code #[deprecated]} attribute. */
final class RustDoc {

    /** The text of a deprecated element whose documentation does not explain the deprecation. */
    static final String DEFAULT_DEPRECATION = "Retained for compatibility; do not use it in new code.";

    private static final java.util.regex.Pattern LIST_ITEM = java.util.regex.Pattern.compile("^([-*+]|\\d+[.)])\\s+");

    private RustDoc() {
    }

    /**
     * Renders the documentation of an element.
     *
     * @param indent     the indentation
     * @param paragraphs the Markdown paragraphs; blank ones are skipped
     * @param deprecated whether the element is deprecated: the paragraph that mentions the deprecation becomes the
     *                   note of the {@code #[deprecated]} attribute
     * @return the comment lines and the attribute, empty if there is nothing to render
     */
    static String render(final String indent, final List<String> paragraphs, final boolean deprecated) {
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
        final StringBuilder out = new StringBuilder();
        boolean code = false;
        for (int i = 0; i < description.size(); i++) {
            if (i > 0) {
                out.append(indent).append("///\n");
            }
            int item = 0; // the indentation of the continuation lines of a list item
            for (final String line : description.get(i).lines().toList()) {
                String text = line.strip();
                if (text.startsWith("```")) {
                    // code of the specs is no Rust code: rustdoc must not compile it as doc test
                    text = code ? "```" : "```text";
                    code = !code;
                    item = 0;
                } else if (!code) {
                    final java.util.regex.Matcher marker = LIST_ITEM.matcher(text);
                    if (marker.find()) {
                        item = marker.end();
                    } else if (item > 0) {
                        text = " ".repeat(item) + text;
                    }
                    text = escape(text);
                }
                out.append(indent).append("///").append(text.isEmpty() ? "" : " " + text).append('\n');
            }
        }
        if (code) {
            out.append(indent).append("/// ```\n");
        }
        if (deprecated) {
            out.append(indent).append("#[deprecated(note = ").append(RustLiterals.quote(reason == null
                    ? DEFAULT_DEPRECATION : reason.replaceAll("\\s+", " "))).append(")]\n");
        }
        return out.toString();
    }

    /**
     * Escapes what rustdoc would read as HTML tag ({@code shard.realm.<num>}) or as link ({@code host[:port]})
     * outside of code spans.
     */
    static String escape(final String text) {
        final StringBuilder out = new StringBuilder();
        boolean code = false;
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c == '`') {
                code = !code;
            } else if (!code && c == '<') {
                out.append('\\');
            } else if (!code && c == '[') {
                final int close = text.indexOf(']', i);
                if (close < 0 || close + 1 >= text.length() || text.charAt(close + 1) != '(') {
                    out.append('\\');
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    static String render(final String indent, final List<String> paragraphs) {
        return render(indent, paragraphs, false);
    }

    /** Renders module documentation ({@code //!}). */
    static String module(final String text) {
        final StringBuilder out = new StringBuilder();
        final String doc = render("", List.of(text));
        doc.lines().forEach(l -> out.append("//!").append(l.substring(3)).append('\n'));
        return out.toString();
    }
}
