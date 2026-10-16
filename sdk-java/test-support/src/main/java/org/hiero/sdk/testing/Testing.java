package org.hiero.sdk.testing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Assertions;

/**
 * The built-in functions and assertions of the test language ({@code guidelines/test-language.md}, namespace
 * {@code testing}) for the tests generated from the specs.
 *
 * <p>Each method implements the semantics the guideline defines, also where Java behaves differently:
 * <ul>
 *   <li>Strings are sequences of Unicode code points: lengths and positions count code points, not UTF-16 units.</li>
 *   <li>Integers are exact: arithmetic happens on {@link BigInteger} ({@link #integer(long)},
 *       {@link #unsigned(long)}), and a value is checked against its type where it is passed to the API
 *       ({@link #checked(BigInteger, String)}).</li>
 *   <li>Equality is value equality ({@link #equal(Object, Object)}): {@code byte[]} by content, sets without order,
 *       numbers by their value, decimals without their scale, {@code double} as in IEEE 754.</li>
 *   <li>A position out of range, a missing key and text that cannot be read fail the test.</li>
 * </ul>
 *
 * <p>The generated tests import the methods statically. Names that Java reserves for other purposes are renamed:
 * {@code toString} is {@link #text(BigInteger)}.
 */
public final class Testing {

    private Testing() {
    }

    // --- assertions ------------------------------------------------------------------------------

    /**
     * Fails the test unless {@code actual} is equal to {@code expected} ({@link #equal(Object, Object)}).
     *
     * @param expected the expected value
     * @param actual   the actual value
     */
    public static void assertEquals(final Object expected, final Object actual) {
        assertEquals(expected, actual, null);
    }

    /**
     * Fails the test unless {@code actual} is equal to {@code expected} ({@link #equal(Object, Object)}).
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @param message  the explanation for the failure report, may be {@code null}
     */
    public static void assertEquals(final Object expected, final Object actual, final String message) {
        if (!equal(expected, actual)) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(display(expected))
                    .actual(display(actual)).buildAndThrow();
        }
    }

    /**
     * Fails the test if {@code actual} is equal to {@code unexpected} ({@link #equal(Object, Object)}).
     *
     * @param unexpected the value {@code actual} must not be equal to
     * @param actual     the actual value
     */
    public static void assertNotEquals(final Object unexpected, final Object actual) {
        assertNotEquals(unexpected, actual, null);
    }

    /**
     * Fails the test if {@code actual} is equal to {@code unexpected} ({@link #equal(Object, Object)}).
     *
     * @param unexpected the value {@code actual} must not be equal to
     * @param actual     the actual value
     * @param message    the explanation for the failure report, may be {@code null}
     */
    public static void assertNotEquals(final Object unexpected, final Object actual, final String message) {
        if (equal(unexpected, actual)) {
            AssertionFailureBuilder.assertionFailure().message(message)
                    .reason("expected: not equal but was: <" + display(actual) + ">").buildAndThrow();
        }
    }

    /**
     * Fails the test unless the condition is true.
     *
     * @param condition the condition
     */
    public static void assertTrue(final boolean condition) {
        assertTrue(condition, null);
    }

    /**
     * Fails the test unless the condition is true.
     *
     * @param condition the condition
     * @param message   the explanation for the failure report, may be {@code null}
     */
    public static void assertTrue(final boolean condition, final String message) {
        if (!condition) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(true).actual(false).buildAndThrow();
        }
    }

    /**
     * Fails the test unless the condition is false.
     *
     * @param condition the condition
     */
    public static void assertFalse(final boolean condition) {
        assertFalse(condition, null);
    }

    /**
     * Fails the test unless the condition is false.
     *
     * @param condition the condition
     * @param message   the explanation for the failure report, may be {@code null}
     */
    public static void assertFalse(final boolean condition, final String message) {
        if (condition) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(false).actual(true).buildAndThrow();
        }
    }

    /**
     * Fails the test unless the value is {@code null}.
     *
     * @param actual the value
     */
    public static void assertNull(final Object actual) {
        assertNull(actual, null);
    }

    /**
     * Fails the test unless the value is {@code null}.
     *
     * @param actual  the value
     * @param message the explanation for the failure report, may be {@code null}
     */
    public static void assertNull(final Object actual, final String message) {
        if (actual != null) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(null).actual(display(actual))
                    .buildAndThrow();
        }
    }

    /**
     * Fails the test if the value is {@code null}.
     *
     * @param actual the value
     */
    public static void assertNotNull(final Object actual) {
        assertNotNull(actual, null);
    }

    /**
     * Fails the test if the value is {@code null}.
     *
     * @param actual  the value
     * @param message the explanation for the failure report, may be {@code null}
     */
    public static void assertNotNull(final Object actual, final String message) {
        if (actual == null) {
            AssertionFailureBuilder.assertionFailure().message(message).reason("expected: not <null>")
                    .buildAndThrow();
        }
    }

    /**
     * Fails the test unless the value is an instance of the type.
     *
     * @param expected the type
     * @param actual   the value
     */
    public static void assertInstanceOf(final Class<?> expected, final Object actual) {
        assertInstanceOf(expected, actual, null);
    }

    /**
     * Fails the test unless the value is an instance of the type.
     *
     * @param expected the type
     * @param actual   the value
     * @param message  the explanation for the failure report, may be {@code null}
     */
    public static void assertInstanceOf(final Class<?> expected, final Object actual, final String message) {
        if (!expected.isInstance(actual)) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(expected.getName())
                    .actual(actual == null ? null : actual.getClass().getName()).buildAndThrow();
        }
    }

    /**
     * Fails the test unless {@code actual} differs from {@code expected} by at most {@code tolerance}.
     *
     * @param expected  the expected value
     * @param actual    the actual value
     * @param tolerance the largest allowed difference
     */
    public static void assertApprox(final double expected, final double actual, final double tolerance) {
        assertApprox(expected, actual, tolerance, null);
    }

    /**
     * Fails the test unless {@code actual} differs from {@code expected} by at most {@code tolerance}.
     *
     * @param expected  the expected value
     * @param actual    the actual value
     * @param tolerance the largest allowed difference
     * @param message   the explanation for the failure report, may be {@code null}
     */
    public static void assertApprox(final double expected, final double actual, final double tolerance,
                                    final String message) {
        if (!approx(actual, expected, tolerance)) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(expected).actual(actual)
                    .reason("expected: <" + expected + "> ± " + tolerance + " but was: <" + actual + ">")
                    .buildAndThrow();
        }
    }

    /**
     * Fails the test.
     *
     * @param message the explanation for the failure report
     */
    public static void fail(final String message) {
        AssertionFailureBuilder.assertionFailure().message(message).buildAndThrow();
    }

    /**
     * The comparison of the contract tests generated per type: arrays of bytes by their content, everything else with
     * {@link Object#equals(Object)}. Unlike {@link #assertEquals(Object, Object)} it does not use the value equality
     * of the test language: a contract test checks that a value is passed through unchanged.
     *
     * @param expected the expected value, may be {@code null}
     * @param actual   the actual value, may be {@code null}
     */
    public static void assertValue(final Object expected, final Object actual) {
        if (expected instanceof byte[] bytes && actual instanceof byte[] other) {
            Assertions.assertArrayEquals(bytes, other, () -> "expected " + Arrays.toString(bytes) + " but was "
                    + Arrays.toString(other));
        } else {
            Assertions.assertEquals(expected, actual);
        }
    }

    // --- equality --------------------------------------------------------------------------------

    /**
     * Value equality of the test language ({@code ==}): {@code byte[]} by content, lists in order, sets without
     * order, maps by their entries, integers by their value (whatever their Java type), decimals without their
     * scale, {@code double} as in IEEE 754 ({@code NaN} is not equal to itself), everything else with
     * {@link Object#equals(Object)}.
     *
     * @param a a value, may be {@code null}
     * @param b a value, may be {@code null}
     * @return whether the values are equal
     */
    public static boolean equal(final Object a, final Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a instanceof byte[] x && b instanceof byte[] y) {
            return Arrays.equals(x, y);
        }
        if (isInteger(a) && isInteger(b)) {
            return toBigInteger(a).equals(toBigInteger(b));
        }
        if ((a instanceof Double || a instanceof Float) && (b instanceof Double || b instanceof Float)) {
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        }
        if (a instanceof BigDecimal x && b instanceof BigDecimal y) {
            return x.compareTo(y) == 0;
        }
        if (a instanceof List<?> x && b instanceof List<?> y) {
            if (x.size() != y.size()) {
                return false;
            }
            for (int i = 0; i < x.size(); i++) {
                if (!equal(x.get(i), y.get(i))) {
                    return false;
                }
            }
            return true;
        }
        if (a instanceof Set<?> x && b instanceof Set<?> y) {
            return x.size() == y.size() && x.stream().allMatch(e -> contains(y, e));
        }
        if (a instanceof Map<?, ?> x && b instanceof Map<?, ?> y) {
            return x.size() == y.size() && x.entrySet().stream().allMatch(e -> y.entrySet().stream()
                    .anyMatch(o -> equal(e.getKey(), o.getKey()) && equal(e.getValue(), o.getValue())));
        }
        return a.equals(b);
    }

    private static boolean isInteger(final Object value) {
        return value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof BigInteger;
    }

    private static BigInteger toBigInteger(final Object value) {
        return value instanceof BigInteger big ? big : BigInteger.valueOf(((Number) value).longValue());
    }

    private static Object display(final Object value) {
        return value instanceof byte[] bytes ? "0x" + toHex(bytes) : value;
    }

    // --- integers --------------------------------------------------------------------------------

    /**
     * An integer of a signed Java type as exact integer.
     *
     * @param value the value
     * @return the value
     */
    public static BigInteger integer(final long value) {
        return BigInteger.valueOf(value);
    }

    /**
     * An exact integer.
     *
     * @param value the value
     * @return the value
     */
    public static BigInteger integer(final BigInteger value) {
        return Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * A {@code uint64} — a {@code long} with unsigned interpretation — as exact integer.
     *
     * @param value the value
     * @return the value, between 0 and 2^64 - 1
     */
    public static BigInteger unsigned(final long value) {
        return new BigInteger(Long.toUnsignedString(value));
    }

    /**
     * Checks that an integer fits a type of the meta-language before it is passed to the API.
     *
     * @param value the value
     * @param type  the type, {@code int8} to {@code int256} or {@code uint8} to {@code uint256}
     * @return the value
     */
    public static BigInteger checked(final BigInteger value, final String type) {
        final boolean unsigned = type.startsWith("u");
        final int bits = Integer.parseInt(type.substring(unsigned ? 4 : 3));
        final BigInteger min = unsigned ? BigInteger.ZERO : BigInteger.ONE.shiftLeft(bits - 1).negate();
        final BigInteger max = unsigned ? BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE)
                : BigInteger.ONE.shiftLeft(bits - 1).subtract(BigInteger.ONE);
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            fail(value + " is out of the range of " + type + " (" + min + " to " + max + ")");
        }
        return value;
    }

    // --- strings ---------------------------------------------------------------------------------

    /**
     * The number of code points of a string.
     *
     * @param value the string
     * @return the number of code points
     */
    public static long length(final String value) {
        return value.codePointCount(0, value.length());
    }

    /**
     * The code points from {@code start} (inclusive) to {@code end} (exclusive).
     *
     * @param value the string
     * @param start the first position
     * @param end   the position after the last one
     * @return the part of the string
     */
    public static String substring(final String value, final long start, final long end) {
        checkRange(start, end, length(value));
        return value.substring(value.offsetByCodePoints(0, (int) start), value.offsetByCodePoints(0, (int) end));
    }

    /**
     * Whether a string contains another one.
     *
     * @param value the string
     * @param part  the string to find
     * @return whether {@code value} contains {@code part}
     */
    public static boolean contains(final String value, final String part) {
        return value.contains(part);
    }

    /**
     * Whether a string starts with a prefix.
     *
     * @param value  the string
     * @param prefix the prefix
     * @return whether {@code value} starts with {@code prefix}
     */
    public static boolean startsWith(final String value, final String prefix) {
        return value.startsWith(prefix);
    }

    /**
     * Whether a string ends with a suffix.
     *
     * @param value  the string
     * @param suffix the suffix
     * @return whether {@code value} ends with {@code suffix}
     */
    public static boolean endsWith(final String value, final String suffix) {
        return value.endsWith(suffix);
    }

    /**
     * A string repeated.
     *
     * @param value the string
     * @param count the number of repetitions
     * @return the repeated string
     */
    public static String repeat(final String value, final long count) {
        return value.repeat(Math.toIntExact(count));
    }

    // --- bytes -----------------------------------------------------------------------------------

    /**
     * The number of bytes.
     *
     * @param value the bytes
     * @return the number of bytes
     */
    public static long length(final byte[] value) {
        return value.length;
    }

    /**
     * The bytes from {@code start} (inclusive) to {@code end} (exclusive).
     *
     * @param value the bytes
     * @param start the first position
     * @param end   the position after the last one
     * @return the part of the bytes
     */
    public static byte[] slice(final byte[] value, final long start, final long end) {
        checkRange(start, end, value.length);
        return Arrays.copyOfRange(value, (int) start, (int) end);
    }

    /**
     * Whether bytes contain a sequence of bytes.
     *
     * @param value the bytes
     * @param part  the sequence to find
     * @return whether {@code value} contains {@code part}
     */
    public static boolean contains(final byte[] value, final byte[] part) {
        for (int i = 0; i + part.length <= value.length; i++) {
            if (Arrays.equals(value, i, i + part.length, part, 0, part.length)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether bytes start with a prefix.
     *
     * @param value  the bytes
     * @param prefix the prefix
     * @return whether {@code value} starts with {@code prefix}
     */
    public static boolean startsWith(final byte[] value, final byte[] prefix) {
        return prefix.length <= value.length && Arrays.equals(value, 0, prefix.length, prefix, 0, prefix.length);
    }

    /**
     * Whether bytes end with a suffix.
     *
     * @param value  the bytes
     * @param suffix the suffix
     * @return whether {@code value} ends with {@code suffix}
     */
    public static boolean endsWith(final byte[] value, final byte[] suffix) {
        return suffix.length <= value.length && Arrays.equals(value, value.length - suffix.length, value.length,
                suffix, 0, suffix.length);
    }

    /**
     * Bytes repeated.
     *
     * @param value the bytes
     * @param count the number of repetitions
     * @return the repeated bytes
     */
    public static byte[] repeat(final byte[] value, final long count) {
        final byte[] result = new byte[Math.multiplyExact(value.length, Math.toIntExact(count))];
        for (int i = 0; i < count; i++) {
            System.arraycopy(value, 0, result, i * value.length, value.length);
        }
        return result;
    }

    /**
     * Two byte sequences one after the other ({@code a + b}).
     *
     * @param a the first bytes
     * @param b the second bytes
     * @return the concatenation
     */
    public static byte[] concat(final byte[] a, final byte[] b) {
        final byte[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Bytes from hex text: case-insensitive, whitespace between the bytes is allowed.
     *
     * @param text the hex text
     * @return the bytes
     */
    public static byte[] hex(final String text) {
        final String digits = text.replaceAll("\\s+", "");
        try {
            return HexFormat.of().parseHex(digits);
        } catch (final IllegalArgumentException e) {
            fail("'" + text + "' is no hex text: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Bytes from Base64 text (RFC 4648, with padding).
     *
     * @param text the Base64 text
     * @return the bytes
     */
    public static byte[] base64(final String text) {
        try {
            return Base64.getDecoder().decode(text);
        } catch (final IllegalArgumentException e) {
            fail("'" + text + "' is no Base64 text: " + e.getMessage());
            throw e;
        }
    }

    /**
     * The UTF-8 encoding of a string.
     *
     * @param text the string
     * @return the bytes
     */
    public static byte[] utf8(final String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Lowercase hex of bytes, without separators.
     *
     * @param value the bytes
     * @return the hex text
     */
    public static String toHex(final byte[] value) {
        return HexFormat.of().formatHex(value);
    }

    // --- collections -----------------------------------------------------------------------------

    /**
     * The number of elements of a list or set.
     *
     * @param value the list or set
     * @return the number of elements
     */
    public static long length(final Collection<?> value) {
        return value.size();
    }

    /**
     * The number of entries of a map.
     *
     * @param value the map
     * @return the number of entries
     */
    public static long length(final Map<?, ?> value) {
        return value.size();
    }

    /**
     * The elements from {@code start} (inclusive) to {@code end} (exclusive).
     *
     * @param value the list
     * @param start the first position
     * @param end   the position after the last one
     * @param <T>   the element type
     * @return the part of the list
     */
    public static <T> List<T> slice(final List<T> value, final long start, final long end) {
        checkRange(start, end, value.size());
        return List.copyOf(value.subList((int) start, (int) end));
    }

    /**
     * Whether a list or set contains an element equal to the given one ({@link #equal(Object, Object)}).
     *
     * @param value   the list or set
     * @param element the element
     * @return whether an equal element exists
     */
    public static boolean contains(final Collection<?> value, final Object element) {
        return value.stream().anyMatch(e -> equal(e, element));
    }

    /**
     * Whether a map contains a key ({@link #equal(Object, Object)}).
     *
     * @param value the map
     * @param key   the key
     * @return whether the key exists
     */
    public static boolean containsKey(final Map<?, ?> value, final Object key) {
        return value.keySet().stream().anyMatch(k -> equal(k, key));
    }

    /**
     * The element of a list at a position ({@code list[i]}).
     *
     * @param value the list
     * @param index the position
     * @param <T>   the element type
     * @return the element
     */
    public static <T> T get(final List<T> value, final long index) {
        checkRange(index, index + 1, value.size());
        return value.get((int) index);
    }

    /**
     * The value of a map for a key ({@code map[key]}); a missing key fails the test.
     *
     * @param value the map
     * @param key   the key
     * @param <K>   the key type
     * @param <V>   the value type
     * @return the value
     */
    public static <K, V> V get(final Map<K, V> value, final K key) {
        for (final Map.Entry<K, V> entry : value.entrySet()) {
            if (equal(entry.getKey(), key)) {
                return entry.getValue();
            }
        }
        fail("The map has no key " + display(key));
        throw new IllegalStateException("unreachable");
    }

    /**
     * Two lists one after the other ({@code a + b}).
     *
     * @param a   the first list
     * @param b   the second list
     * @param <T> the element type
     * @return the concatenation
     */
    public static <T> List<T> concat(final List<T> a, final List<T> b) {
        final List<T> result = new ArrayList<>(a);
        result.addAll(b);
        return List.copyOf(result);
    }

    /**
     * The keys of a map, in an unspecified order.
     *
     * @param value the map
     * @param <K>   the key type
     * @return the keys
     */
    public static <K> List<K> keys(final Map<K, ?> value) {
        return List.copyOf(value.keySet());
    }

    /**
     * The values of a map, in an unspecified order.
     *
     * @param value the map
     * @param <V>   the value type
     * @return the values
     */
    public static <V> List<V> values(final Map<?, V> value) {
        return List.copyOf(value.values());
    }

    /**
     * The integers from {@code start} (inclusive) to {@code end} (exclusive).
     *
     * @param start the first integer
     * @param end   the integer after the last one
     * @return the integers
     */
    public static List<Long> range(final long start, final long end) {
        return LongStream.range(start, end).boxed().toList();
    }

    // --- numbers ---------------------------------------------------------------------------------

    /**
     * The absolute value.
     *
     * @param value the value
     * @return the absolute value
     */
    public static BigInteger abs(final BigInteger value) {
        return value.abs();
    }

    /**
     * The absolute value.
     *
     * @param value the value
     * @return the absolute value
     */
    public static double abs(final double value) {
        return Math.abs(value);
    }

    /**
     * The absolute value.
     *
     * @param value the value
     * @return the absolute value
     */
    public static BigDecimal abs(final BigDecimal value) {
        return value.abs();
    }

    /**
     * The smaller of two values.
     *
     * @param a a value
     * @param b a value
     * @return the smaller one
     */
    public static BigInteger min(final BigInteger a, final BigInteger b) {
        return a.min(b);
    }

    /**
     * The smaller of two values.
     *
     * @param a a value
     * @param b a value
     * @return the smaller one
     */
    public static double min(final double a, final double b) {
        return Math.min(a, b);
    }

    /**
     * The smaller of two values.
     *
     * @param a a value
     * @param b a value
     * @return the smaller one
     */
    public static BigDecimal min(final BigDecimal a, final BigDecimal b) {
        return a.min(b);
    }

    /**
     * The larger of two values.
     *
     * @param a a value
     * @param b a value
     * @return the larger one
     */
    public static BigInteger max(final BigInteger a, final BigInteger b) {
        return a.max(b);
    }

    /**
     * The larger of two values.
     *
     * @param a a value
     * @param b a value
     * @return the larger one
     */
    public static double max(final double a, final double b) {
        return Math.max(a, b);
    }

    /**
     * The larger of two values.
     *
     * @param a a value
     * @param b a value
     * @return the larger one
     */
    public static BigDecimal max(final BigDecimal a, final BigDecimal b) {
        return a.max(b);
    }

    /**
     * Whether {@code actual} differs from {@code expected} by at most {@code tolerance}.
     *
     * @param actual    the actual value
     * @param expected  the expected value
     * @param tolerance the largest allowed difference
     * @return whether the values are close enough
     */
    public static boolean approx(final double actual, final double expected, final double tolerance) {
        return Math.abs(actual - expected) <= tolerance;
    }

    /**
     * Whether a value is {@code NaN}.
     *
     * @param value the value
     * @return whether it is {@code NaN}
     */
    public static boolean isNaN(final double value) {
        return Double.isNaN(value);
    }

    /**
     * The quotient of two decimals, rounded half-even to {@code scale} digits after the point.
     *
     * @param dividend the dividend
     * @param divisor  the divisor
     * @param scale    the number of digits after the point
     * @return the quotient
     */
    public static BigDecimal divide(final BigDecimal dividend, final BigDecimal divisor, final long scale) {
        if (divisor.signum() == 0) {
            fail("Division by zero");
        }
        return dividend.divide(divisor, Math.toIntExact(scale), RoundingMode.HALF_EVEN);
    }

    // --- conversions -----------------------------------------------------------------------------

    /**
     * Decimal text of an integer without grouping ({@code toString} of the test language).
     *
     * @param value the integer
     * @return the text, e.g. {@code -1234}
     */
    public static String text(final BigInteger value) {
        return value.toString();
    }

    /**
     * Decimal text of a decimal without grouping and exponent ({@code toString} of the test language).
     *
     * @param value the decimal
     * @return the text, e.g. {@code 0.5}
     */
    public static String text(final BigDecimal value) {
        return value.toPlainString();
    }

    /**
     * Parses decimal text; text that is no integer fails the test.
     *
     * @param text the text
     * @return the integer
     */
    public static BigInteger toInt(final String text) {
        try {
            return new BigInteger(text);
        } catch (final NumberFormatException e) {
            fail("'" + text + "' is no integer");
            throw e;
        }
    }

    /**
     * The nearest {@code double}.
     *
     * @param value the integer
     * @return the {@code double}
     */
    public static double toDouble(final BigInteger value) {
        return value.doubleValue();
    }

    /**
     * The nearest {@code double}.
     *
     * @param value the decimal
     * @return the {@code double}
     */
    public static double toDouble(final BigDecimal value) {
        return value.doubleValue();
    }

    /**
     * The exact value as decimal.
     *
     * @param value the integer
     * @return the decimal
     */
    public static BigDecimal toDecimal(final BigInteger value) {
        return new BigDecimal(value);
    }

    /**
     * The exact value as decimal.
     *
     * @param value the {@code double}
     * @return the decimal
     */
    public static BigDecimal toDecimal(final double value) {
        return new BigDecimal(value);
    }

    private static void checkRange(final long start, final long end, final long length) {
        if (start < 0 || end > length || start > end) {
            fail("The positions " + start + " to " + end + " are out of the range 0 to " + length);
        }
    }
}
