package org.hiero.sdk.v3.metalang.cli;

import java.util.List;
import java.util.Map;

/**
 * Minimal, dependency-free JSON writer with stable output: two-space indentation, LF line endings, keys in insertion
 * order (callers use ordered maps), empty containers written inline.
 */
final class Json {

    private Json() {
    }

    /**
     * Serializes a tree of {@link Map} (with {@link String} keys), {@link List}, {@link String}, {@link Number},
     * {@link Boolean} and {@code null}.
     */
    static String write(final Object value) {
        final StringBuilder out = new StringBuilder();
        write(value, out, 0);
        return out.append('\n').toString();
    }

    private static void write(final Object value, final StringBuilder out, final int indent) {
        switch (value) {
            case null -> out.append("null");
            case String string -> out.append(JsonReport.quote(string));
            case Number number -> out.append(number);
            case Boolean bool -> out.append(bool);
            case Map<?, ?> map -> {
                if (map.isEmpty()) {
                    out.append("{}");
                    return;
                }
                out.append("{\n");
                int i = 0;
                for (final Map.Entry<?, ?> entry : map.entrySet()) {
                    out.append("  ".repeat(indent + 1)).append(JsonReport.quote((String) entry.getKey())).append(": ");
                    write(entry.getValue(), out, indent + 1);
                    out.append(++i < map.size() ? ",\n" : "\n");
                }
                out.append("  ".repeat(indent)).append('}');
            }
            case List<?> list -> {
                if (list.isEmpty()) {
                    out.append("[]");
                    return;
                }
                out.append("[\n");
                for (int i = 0; i < list.size(); i++) {
                    out.append("  ".repeat(indent + 1));
                    write(list.get(i), out, indent + 1);
                    out.append(i + 1 < list.size() ? ",\n" : "\n");
                }
                out.append("  ".repeat(indent)).append(']');
            }
            default -> throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass());
        }
    }
}
