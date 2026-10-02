package org.hiero.sdk.v3.metalang.semantic;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic data type of the meta-language (see "Basic data types" in the guideline).
 *
 * @param name     the type name as written, e.g. {@code int64} or {@code list}
 * @param category the category
 * @param arity    the number of type arguments the type expects
 * @param bits     the width for {@code intX}/{@code uintX}, otherwise 0
 */
public record BuiltinType(String name, Category category, int arity, int bits) {

    /** Smallest allowed integer width. */
    public static final int MIN_INT_BITS = 8;
    /** Largest allowed integer width. */
    public static final int MAX_INT_BITS = 256;

    private static final Pattern INT_PATTERN = Pattern.compile("(u?)int(\\d+)");

    /**
     * Category of a basic data type; used by validation rules that depend on the kind of value.
     */
    public enum Category {
        /** {@code intX} and {@code uintX}. */
        INTEGER,
        /** {@code double}. */
        FLOAT,
        /** {@code decimal}. */
        DECIMAL,
        /** {@code string}. */
        STRING,
        /** {@code bool}. */
        BOOL,
        /** {@code bytes}. */
        BYTES,
        /** {@code list} and {@code set}. */
        COLLECTION,
        /** {@code map}. */
        MAP,
        /** {@code type}. */
        TYPE,
        /** {@code uuid}. */
        UUID,
        /** {@code date}, {@code time}, {@code dateTime}, {@code zonedDateTime}. */
        TEMPORAL,
        /** {@code seconds}. */
        DURATION,
        /** {@code streamResult}. */
        STREAM_RESULT
    }

    private static final Map<String, BuiltinType> FIXED = Map.ofEntries(
            fixed("double", Category.FLOAT, 0),
            fixed("decimal", Category.DECIMAL, 0),
            fixed("string", Category.STRING, 0),
            fixed("bool", Category.BOOL, 0),
            fixed("bytes", Category.BYTES, 0),
            fixed("list", Category.COLLECTION, 1),
            fixed("set", Category.COLLECTION, 1),
            fixed("map", Category.MAP, 2),
            fixed("type", Category.TYPE, 0),
            fixed("uuid", Category.UUID, 0),
            fixed("date", Category.TEMPORAL, 0),
            fixed("time", Category.TEMPORAL, 0),
            fixed("dateTime", Category.TEMPORAL, 0),
            fixed("zonedDateTime", Category.TEMPORAL, 0),
            fixed("seconds", Category.DURATION, 0),
            fixed("streamResult", Category.STREAM_RESULT, 1));

    /**
     * Creates a builtin type.
     *
     * @param name     the name
     * @param category the category
     * @param arity    the expected number of type arguments
     * @param bits     the integer width or 0
     */
    public BuiltinType {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(category, "category must not be null");
    }

    private static Map.Entry<String, BuiltinType> fixed(final String name, final Category category, final int arity) {
        return Map.entry(name, new BuiltinType(name, category, arity, 0));
    }

    /**
     * Looks up a builtin type by name. {@code intX}/{@code uintX} are recognized for every
     * {@code X}; whether the width is valid is reported by {@link #hasValidWidth()}.
     *
     * @param name the simple type name
     * @return the builtin type, if the name denotes one
     */
    public static Optional<BuiltinType> lookup(final String name) {
        Objects.requireNonNull(name, "name must not be null");
        final BuiltinType fixed = FIXED.get(name);
        if (fixed != null) {
            return Optional.of(fixed);
        }
        final Matcher matcher = INT_PATTERN.matcher(name);
        if (matcher.matches() && matcher.group(2).length() <= 4) {
            return Optional.of(new BuiltinType(name, Category.INTEGER, 0, Integer.parseInt(matcher.group(2))));
        }
        return Optional.empty();
    }

    /**
     * Returns whether the given number of type arguments is valid for this type. {@code type} accepts
     * zero arguments (any type) or one argument ({@code type<T>}: T or one of its subtypes).
     *
     * @param count the number of type arguments
     * @return {@code true} if valid
     */
    public boolean acceptsArity(final int count) {
        return count == arity || (category == Category.TYPE && count == 1);
    }

    /**
     * Returns whether the type is a numeric type.
     *
     * @return {@code true} for integers, double and decimal
     */
    public boolean isNumeric() {
        return category == Category.INTEGER || category == Category.FLOAT || category == Category.DECIMAL;
    }

    /**
     * Returns whether the type is a collection type that must never be nullable.
     *
     * @return {@code true} for list, set and map
     */
    public boolean isCollection() {
        return category == Category.COLLECTION || category == Category.MAP;
    }

    /**
     * Returns whether an integer width is within the allowed range (always {@code true} for
     * non-integer types).
     *
     * @return whether the width is valid
     */
    public boolean hasValidWidth() {
        return category != Category.INTEGER || (bits >= MIN_INT_BITS && bits <= MAX_INT_BITS);
    }
}
