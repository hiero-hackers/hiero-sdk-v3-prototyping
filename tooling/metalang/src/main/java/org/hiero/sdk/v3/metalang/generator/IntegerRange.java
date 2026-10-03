package org.hiero.sdk.v3.metalang.generator;

import java.math.BigInteger;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * A range of integer values, e.g. the values of a meta-language integer type ({@code uint8}: 0 to 255).
 *
 * @param min the smallest value
 * @param max the largest value
 */
public record IntegerRange(BigInteger min, BigInteger max) {

    /**
     * Whether the value is in the range.
     *
     * @param value the value
     * @return {@code true} if {@code min <= value <= max}
     */
    public boolean contains(final BigInteger value) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * The intersection with another range.
     *
     * @param other the other range
     * @return the intersection (empty ranges have {@code min > max})
     */
    public IntegerRange intersect(final IntegerRange other) {
        return new IntegerRange(min.max(other.min), max.min(other.max));
    }

    /**
     * The values of a meta-language integer type.
     *
     * @param builtin the integer type
     * @return the range, e.g. {@code [0, 255]} for {@code uint8}
     */
    public static IntegerRange of(final BuiltinType builtin) {
        final int bits = builtin.bits();
        if (isUnsigned(builtin)) {
            return new IntegerRange(BigInteger.ZERO, BigInteger.TWO.pow(bits).subtract(BigInteger.ONE));
        }
        return new IntegerRange(BigInteger.TWO.pow(bits - 1).negate(),
                BigInteger.TWO.pow(bits - 1).subtract(BigInteger.ONE));
    }

    /**
     * Returns the integer builtin of a type.
     *
     * @param type the type
     * @return the builtin if the type is an {@code intX}/{@code uintX}
     */
    public static Optional<BuiltinType> integer(final Type type) {
        return type instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.INTEGER
                ? Optional.of(basic.builtin()) : Optional.empty();
    }

    /**
     * Whether the integer type is unsigned.
     *
     * @param builtin the integer type
     * @return {@code true} for {@code uintX}
     */
    public static boolean isUnsigned(final BuiltinType builtin) {
        return builtin.name().startsWith("uint");
    }
}
