package org.hiero.sdk.v3.metalang.generator.java;

import java.math.BigInteger;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * The value ranges of the meta-language integer types and of the Java types they map to ({@link JavaTypes#javaBits}).
 * A Java type that is wider than its meta-language type ({@code uint8} as {@code short}, {@code uint32} as
 * {@code long}, {@code int24} as {@code int}, {@code int256} as {@code BigInteger}) accepts values outside of the
 * range, so the generated code checks it. {@code uint64} is the exception: it is stored as {@code long} with the
 * unsigned interpretation (see {@code Long.toUnsignedString}), so every {@code long} is a valid value and comparisons
 * must be unsigned.
 */
final class JavaIntegers {

    private JavaIntegers() {
    }

    /**
     * The range of an integer type.
     *
     * @param min the smallest value
     * @param max the largest value
     */
    record Range(BigInteger min, BigInteger max) {

        /**
         * Whether the value is in the range.
         *
         * @param value the value
         * @return {@code true} if {@code min <= value <= max}
         */
        boolean contains(final BigInteger value) {
            return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
        }

        /**
         * The intersection with another range.
         *
         * @param other the other range
         * @return the intersection (empty ranges have {@code min > max})
         */
        Range intersect(final Range other) {
            return new Range(min.max(other.min), max.min(other.max));
        }
    }

    /**
     * Returns the integer builtin of a type.
     *
     * @param type the type
     * @return the builtin if the type is an {@code intX}/{@code uintX}
     */
    static Optional<BuiltinType> integer(final Type type) {
        return type instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.INTEGER
                ? Optional.of(basic.builtin()) : Optional.empty();
    }

    /**
     * Whether the type is {@code uint64}: a {@code long} with unsigned interpretation.
     *
     * @param builtin the integer type
     * @return {@code true} for {@code uint64}
     */
    static boolean isUnsignedLong(final BuiltinType builtin) {
        return unsigned(builtin) && builtin.bits() == 64;
    }

    /**
     * The values of the meta-language type.
     *
     * @param builtin the integer type
     * @return the range, e.g. {@code [0, 255]} for {@code uint8}
     */
    static Range range(final BuiltinType builtin) {
        final int bits = builtin.bits();
        if (unsigned(builtin)) {
            return new Range(BigInteger.ZERO, BigInteger.TWO.pow(bits).subtract(BigInteger.ONE));
        }
        return new Range(BigInteger.TWO.pow(bits - 1).negate(), BigInteger.TWO.pow(bits - 1).subtract(BigInteger.ONE));
    }

    /**
     * Whether the Java type can hold a value of the meta-language type outside of its range, so that the range must be
     * checked.
     *
     * @param builtin the integer type
     * @return {@code true} unless the Java type has exactly the range ({@code int8} to {@code int64}, {@code uint64})
     */
    static boolean needsRangeCheck(final BuiltinType builtin) {
        if (isUnsignedLong(builtin)) {
            return false;
        }
        final int javaBits = JavaTypes.javaBits(builtin);
        return javaBits > 64 || !range(builtin).equals(signed(javaBits));
    }

    /**
     * Whether the Java type can represent the value (for {@code uint64} with the unsigned interpretation).
     *
     * @param builtin the integer type
     * @param value   the value
     * @return {@code true} if a Java literal of the value exists
     */
    static boolean representable(final BuiltinType builtin, final BigInteger value) {
        if (isUnsignedLong(builtin)) {
            return range(builtin).contains(value);
        }
        final int javaBits = JavaTypes.javaBits(builtin);
        return javaBits > 64 || signed(javaBits).contains(value);
    }

    /**
     * Renders a value as Java expression of the Java type of the integer type.
     *
     * @param builtin the integer type
     * @param value   the value; must be {@link #representable}
     * @param imports the imports of the file
     * @return the expression, e.g. {@code (short) 255}, {@code 4294967295L}, {@code new BigInteger("…")},
     *         {@code Long.parseUnsignedLong("18446744073709551615")}
     */
    static String literal(final BuiltinType builtin, final BigInteger value, final Imports imports) {
        if (isUnsignedLong(builtin) && value.bitLength() > 63) {
            return "Long.parseUnsignedLong(\"" + value + "\")";
        }
        final int javaBits = JavaTypes.javaBits(builtin);
        if (javaBits > 64) {
            return "new " + imports.use("java.math", "BigInteger") + "(\"" + value + "\")";
        }
        if (javaBits > 32) {
            return value + "L";
        }
        if (javaBits > 16) {
            return value.equals(BigInteger.valueOf(Integer.MIN_VALUE)) ? "Integer.MIN_VALUE" : value.toString();
        }
        return (javaBits > 8 ? "(short) " : "(byte) ") + (value.signum() < 0 ? "(" + value + ")" : value);
    }

    /**
     * The values of a signed Java integer type.
     *
     * @param bits 8, 16, 32 or 64
     * @return the range
     */
    private static Range signed(final int bits) {
        final int width = bits <= 8 ? 8 : bits <= 16 ? 16 : bits <= 32 ? 32 : 64;
        return new Range(BigInteger.TWO.pow(width - 1).negate(), BigInteger.TWO.pow(width - 1)
                .subtract(BigInteger.ONE));
    }

    private static boolean unsigned(final BuiltinType builtin) {
        return builtin.name().startsWith("uint");
    }
}
