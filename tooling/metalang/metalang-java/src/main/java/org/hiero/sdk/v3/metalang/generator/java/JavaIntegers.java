package org.hiero.sdk.v3.metalang.generator.java;

import java.math.BigInteger;
import org.hiero.sdk.v3.metalang.generator.IntegerRange;
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
     * Whether the type is {@code uint64}: a {@code long} with unsigned interpretation.
     *
     * @param builtin the integer type
     * @return {@code true} for {@code uint64}
     */
    static boolean isUnsignedLong(final BuiltinType builtin) {
        return IntegerRange.isUnsigned(builtin) && builtin.bits() == 64;
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
        return javaBits > 64 || !IntegerRange.of(builtin).equals(signed(javaBits));
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
            return IntegerRange.of(builtin).contains(value);
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
    private static IntegerRange signed(final int bits) {
        final int width = bits <= 8 ? 8 : bits <= 16 ? 16 : bits <= 32 ? 32 : 64;
        return new IntegerRange(BigInteger.TWO.pow(width - 1).negate(), BigInteger.TWO.pow(width - 1)
                .subtract(BigInteger.ONE));
    }

}
