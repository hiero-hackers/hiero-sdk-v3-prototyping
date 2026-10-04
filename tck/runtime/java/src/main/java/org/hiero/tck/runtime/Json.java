package org.hiero.tck.runtime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal JSON reader and writer for the JSON-RPC messages of the TCK: objects are {@code Map<String, Object>},
 * arrays {@code List<Object>}, numbers {@code BigDecimal}, and {@code true}, {@code false}, {@code null}, strings as
 * usual.
 */
public final class Json {

    private final String text;
    private int position;

    private Json(final String text) {
        this.text = text;
    }

    /**
     * Reads a JSON value.
     *
     * @param text the JSON text
     * @return the value
     * @throws IllegalArgumentException if the text is no valid JSON
     */
    public static Object read(final String text) {
        final Json json = new Json(text);
        final Object value = json.value();
        json.whitespace();
        if (json.position != text.length()) {
            throw json.error("unexpected content");
        }
        return value;
    }

    /**
     * Writes a JSON value.
     *
     * @param value a map, list, string, number, boolean or {@code null}
     * @return the JSON text
     */
    public static String write(final Object value) {
        final StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(final Object value, final StringBuilder out) {
        switch (value) {
            case null -> out.append("null");
            case String string -> quote(string, out);
            case Boolean bool -> out.append(bool);
            case BigDecimal number -> out.append(number.toPlainString());
            case Number number -> out.append(number);
            case Map<?, ?> map -> {
                out.append('{');
                boolean first = true;
                for (final Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) {
                        out.append(',');
                    }
                    first = false;
                    quote(String.valueOf(entry.getKey()), out);
                    out.append(':');
                    write(entry.getValue(), out);
                }
                out.append('}');
            }
            case List<?> list -> {
                out.append('[');
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) {
                        out.append(',');
                    }
                    write(list.get(i), out);
                }
                out.append(']');
            }
            default -> quote(value.toString(), out);
        }
    }

    private static void quote(final String value, final StringBuilder out) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    private Object value() {
        whitespace();
        if (position >= text.length()) {
            throw error("unexpected end");
        }
        final char c = text.charAt(position);
        return switch (c) {
            case '{' -> object();
            case '[' -> array();
            case '"' -> string();
            case 't' -> literal("true", Boolean.TRUE);
            case 'f' -> literal("false", Boolean.FALSE);
            case 'n' -> literal("null", null);
            default -> number();
        };
    }

    private Map<String, Object> object() {
        final Map<String, Object> map = new LinkedHashMap<>();
        position++;
        whitespace();
        if (peek() == '}') {
            position++;
            return map;
        }
        while (true) {
            whitespace();
            final String key = string();
            whitespace();
            expect(':');
            map.put(key, value());
            whitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect('}');
                return map;
            }
        }
    }

    private List<Object> array() {
        final List<Object> list = new ArrayList<>();
        position++;
        whitespace();
        if (peek() == ']') {
            position++;
            return list;
        }
        while (true) {
            list.add(value());
            whitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect(']');
                return list;
            }
        }
    }

    private String string() {
        expect('"');
        final StringBuilder out = new StringBuilder();
        while (position < text.length()) {
            final char c = text.charAt(position++);
            if (c == '"') {
                return out.toString();
            }
            if (c == '\\') {
                final char escaped = text.charAt(position++);
                switch (escaped) {
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'u' -> {
                        out.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        position += 4;
                    }
                    default -> out.append(escaped);
                }
            } else {
                out.append(c);
            }
        }
        throw error("unterminated string");
    }

    private Object literal(final String word, final Object value) {
        if (!text.startsWith(word, position)) {
            throw error("unexpected value");
        }
        position += word.length();
        return value;
    }

    private BigDecimal number() {
        final int start = position;
        while (position < text.length() && "+-0123456789.eE".indexOf(text.charAt(position)) >= 0) {
            position++;
        }
        try {
            return new BigDecimal(text.substring(start, position));
        } catch (final NumberFormatException e) {
            throw error("invalid number");
        }
    }

    private char peek() {
        return position < text.length() ? text.charAt(position) : '\0';
    }

    private void expect(final char c) {
        if (peek() != c) {
            throw error("expected '" + c + "'");
        }
        position++;
    }

    private void whitespace() {
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
            position++;
        }
    }

    private IllegalArgumentException error(final String message) {
        return new IllegalArgumentException("Invalid JSON at " + position + ": " + message);
    }
}
