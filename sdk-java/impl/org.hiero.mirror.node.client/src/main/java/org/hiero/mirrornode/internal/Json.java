package org.hiero.mirrornode.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A minimal JSON parser (RFC 8259) for the responses of the Mirror Node REST API, and a read-only view of a JSON
/// object with typed accessors.
///
/// A parsed value is a `JsonObject`, a `List` of values, a `String`, a `BigDecimal`, a `Boolean` or `null`. Numbers
/// are kept exact, so 64-bit amounts and 256-bit supplies lose nothing.
public final class Json {

    private final String text;
    private int position;

    private Json(final String text) {
        this.text = text;
    }

    /// Parses `text` into a JSON value.
    ///
    /// @param text the JSON text
    /// @return the value
    /// @throws IllegalArgumentException if `text` is no well-formed JSON
    public static @Nullable Object parse(final String text) {
        Objects.requireNonNull(text, "text must not be null");
        final Json parser = new Json(text);
        parser.skipWhitespace();
        final Object value = parser.value();
        parser.skipWhitespace();
        if (parser.position != text.length()) {
            throw parser.error("unexpected trailing content");
        }
        return value;
    }

    /// Parses `text`, which must be a JSON object.
    ///
    /// @param text the JSON text
    /// @return the object
    /// @throws IllegalArgumentException if `text` is no well-formed JSON object
    public static JsonObject parseObject(final String text) {
        if (parse(text) instanceof JsonObject object) {
            return object;
        }
        throw new IllegalArgumentException("the JSON text is no object");
    }

    private @Nullable Object value() {
        if (position >= text.length()) {
            throw error("unexpected end of the JSON text");
        }
        final char c = text.charAt(position);
        return switch (c) {
            case '{' -> object();
            case '[' -> array();
            case '"' -> string();
            case 't' -> literal("true", Boolean.TRUE);
            case 'f' -> literal("false", Boolean.FALSE);
            case 'n' -> literal("null", null);
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield number();
                }
                throw error("unexpected character '" + c + "'");
            }
        };
    }

    private JsonObject object() {
        position++;
        final Map<String, @Nullable Object> members = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return new JsonObject(members);
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("expected a member name");
            }
            final String name = string();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            members.put(name, value());
            skipWhitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect('}');
                return new JsonObject(members);
            }
        }
    }

    private List<@Nullable Object> array() {
        position++;
        final List<@Nullable Object> elements = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return Collections.unmodifiableList(elements);
        }
        while (true) {
            skipWhitespace();
            elements.add(value());
            skipWhitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect(']');
                return Collections.unmodifiableList(elements);
            }
        }
    }

    private String string() {
        position++;
        final StringBuilder result = new StringBuilder();
        while (true) {
            if (position >= text.length()) {
                throw error("unterminated string");
            }
            final char c = text.charAt(position++);
            if (c == '"') {
                return result.toString();
            }
            if (c == '\\') {
                if (position >= text.length()) {
                    throw error("unterminated escape sequence");
                }
                final char escaped = text.charAt(position++);
                switch (escaped) {
                    case '"', '\\', '/' -> result.append(escaped);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> {
                        if (position + 4 > text.length()) {
                            throw error("incomplete unicode escape");
                        }
                        try {
                            result.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        } catch (final NumberFormatException e) {
                            throw error("malformed unicode escape");
                        }
                        position += 4;
                    }
                    default -> throw error("unknown escape sequence '\\" + escaped + "'");
                }
            } else if (c < 0x20) {
                throw error("control character in string");
            } else {
                result.append(c);
            }
        }
    }

    private BigDecimal number() {
        final int start = position;
        while (position < text.length() && "+-0123456789.eE".indexOf(text.charAt(position)) >= 0) {
            position++;
        }
        try {
            return new BigDecimal(text.substring(start, position));
        } catch (final NumberFormatException e) {
            throw error("malformed number");
        }
    }

    private @Nullable Object literal(final String literal, final @Nullable Object value) {
        if (!text.startsWith(literal, position)) {
            throw error("unexpected literal");
        }
        position += literal.length();
        return value;
    }

    private char peek() {
        if (position >= text.length()) {
            throw error("unexpected end of the JSON text");
        }
        return text.charAt(position);
    }

    private void expect(final char expected) {
        if (peek() != expected) {
            throw error("expected '" + expected + "'");
        }
        position++;
    }

    private void skipWhitespace() {
        while (position < text.length() && " \t\r\n".indexOf(text.charAt(position)) >= 0) {
            position++;
        }
    }

    private IllegalArgumentException error(final String message) {
        return new IllegalArgumentException("malformed JSON at position " + position + ": " + message);
    }

    /// A JSON object with typed accessors. An accessor of a required member fails with `IllegalArgumentException`
    /// if the member is absent, `null` or of another type; an `opt…` accessor returns `null` for an absent or `null`
    /// member.
    ///
    /// @param members the members of the object, in the order of the JSON text
    public record JsonObject(Map<String, @Nullable Object> members) {

        /// Creates the view of `members`.
        ///
        /// @throws NullPointerException if `members` is `null`
        public JsonObject {
            members = Collections.unmodifiableMap(new LinkedHashMap<>(members));
        }

        /// Whether the member `name` is present and not `null`.
        ///
        /// @param name the member name
        /// @return `true` if the member has a value
        public boolean has(final String name) {
            return members.get(name) != null;
        }

        /// Returns the value of the member `name`, or `null`.
        ///
        /// @param name the member name
        /// @return the value
        public @Nullable Object opt(final String name) {
            return members.get(name);
        }

        /// Returns the string member `name`.
        ///
        /// @param name the member name
        /// @return the value
        public String string(final String name) {
            return required(name, optString(name));
        }

        /// Returns the string member `name`, or `null`. A number is returned in its plain text form.
        ///
        /// @param name the member name
        /// @return the value
        public @Nullable String optString(final String name) {
            final Object value = members.get(name);
            return switch (value) {
                case null -> null;
                case String text -> text;
                case BigDecimal number -> number.toPlainString();
                case Boolean flag -> flag.toString();
                default -> throw new IllegalArgumentException("member '" + name + "' is no string");
            };
        }

        /// Returns the integer member `name`; a string of digits is accepted as well.
        ///
        /// @param name the member name
        /// @return the value
        public long longValue(final String name) {
            return required(name, optLong(name));
        }

        /// Returns the integer member `name`, or `null`; a string of digits is accepted as well.
        ///
        /// @param name the member name
        /// @return the value
        public @Nullable Long optLong(final String name) {
            final BigInteger value = optInteger(name);
            return value == null ? null : value.longValueExact();
        }

        /// Returns the integer member `name`, or `null`, of any size; a string of digits is accepted as well.
        ///
        /// @param name the member name
        /// @return the value
        public @Nullable BigInteger optInteger(final String name) {
            final Object value = members.get(name);
            try {
                return switch (value) {
                    case null -> null;
                    case BigDecimal number -> number.toBigIntegerExact();
                    case String text -> new BigInteger(text.trim());
                    default -> throw new IllegalArgumentException("member '" + name + "' is no integer");
                };
            } catch (final ArithmeticException | NumberFormatException e) {
                throw new IllegalArgumentException("member '" + name + "' is no integer: " + value, e);
            }
        }

        /// Returns the number member `name`.
        ///
        /// @param name the member name
        /// @return the value
        public double doubleValue(final String name) {
            final Object value = members.get(name);
            return switch (value) {
                case BigDecimal number -> number.doubleValue();
                case String text -> Double.parseDouble(text);
                case null, default -> throw new IllegalArgumentException("member '" + name + "' is no number");
            };
        }

        /// Returns the boolean member `name`, or `null`.
        ///
        /// @param name the member name
        /// @return the value
        public @Nullable Boolean optBoolean(final String name) {
            final Object value = members.get(name);
            return switch (value) {
                case null -> null;
                case Boolean flag -> flag;
                default -> throw new IllegalArgumentException("member '" + name + "' is no boolean");
            };
        }

        /// Returns the boolean member `name`, or `fallback` if it is absent or `null`.
        ///
        /// @param name the member name
        /// @param fallback the value of an absent member
        /// @return the value
        public boolean bool(final String name, final boolean fallback) {
            final Boolean value = optBoolean(name);
            return value == null ? fallback : value;
        }

        /// Returns the object member `name`.
        ///
        /// @param name the member name
        /// @return the value
        public JsonObject object(final String name) {
            return required(name, optObject(name));
        }

        /// Returns the object member `name`, or `null`.
        ///
        /// @param name the member name
        /// @return the value
        public @Nullable JsonObject optObject(final String name) {
            final Object value = members.get(name);
            return switch (value) {
                case null -> null;
                case JsonObject object -> object;
                default -> throw new IllegalArgumentException("member '" + name + "' is no object");
            };
        }

        /// Returns the objects of the array member `name`; an absent or `null` member is an empty list.
        ///
        /// @param name the member name
        /// @return the objects
        public List<JsonObject> objects(final String name) {
            final Object value = members.get(name);
            if (value == null) {
                return List.of();
            }
            if (!(value instanceof List<?> elements)) {
                throw new IllegalArgumentException("member '" + name + "' is no array");
            }
            final List<JsonObject> result = new ArrayList<>(elements.size());
            for (final Object element : elements) {
                if (!(element instanceof JsonObject object)) {
                    throw new IllegalArgumentException("member '" + name + "' holds an element that is no object");
                }
                result.add(object);
            }
            return List.copyOf(result);
        }

        private static <T> T required(final String name, final @Nullable T value) {
            if (value == null) {
                throw new IllegalArgumentException("member '" + name + "' is missing");
            }
            return value;
        }
    }
}
