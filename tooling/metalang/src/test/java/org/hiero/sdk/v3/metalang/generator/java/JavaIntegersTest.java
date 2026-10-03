package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JavaIntegersTest {

    private static BuiltinType type(final String name) {
        return BuiltinType.lookup(name).orElseThrow();
    }

    @ParameterizedTest
    @CsvSource({
            "int8,    -128,                 127,                  false",
            "int16,   -32768,               32767,                false",
            "int24,   -8388608,             8388607,              true",
            "int32,   -2147483648,          2147483647,           false",
            "int64,   -9223372036854775808, 9223372036854775807,  false",
            "uint8,   0,                    255,                  true",
            "uint16,  0,                    65535,                true",
            "uint32,  0,                    4294967295,           true",
            "uint64,  0,                    18446744073709551615, false",
            "int128,  -170141183460469231731687303715884105728, 170141183460469231731687303715884105727, true"})
    void shouldKnowRangeAndWhetherItMustBeChecked(final String name, final String min, final String max,
                                                  final boolean check) {
        assertThat(JavaIntegers.range(type(name))).isEqualTo(new JavaIntegers.Range(new BigInteger(min),
                new BigInteger(max)));
        assertThat(JavaIntegers.needsRangeCheck(type(name))).isEqualTo(check);
        assertThat(JavaIntegers.isUnsignedLong(type(name))).isEqualTo(name.equals("uint64"));
    }

    @ParameterizedTest
    @CsvSource({
            "int8,   -1,                   (byte) (-1),                                 true",
            "int8,   5,                    (byte) 5,                                    true",
            "int8,   128,                  ,                                            false",
            "uint8,  255,                  (short) 255,                                 true",
            "int32,  -2147483648,          Integer.MIN_VALUE,                           true",
            "uint32, 4294967295,           4294967295L,                                 true",
            "int64,  9223372036854775808,  ,                                            false",
            "uint64, -1,                   ,                                            false",
            "uint64, 9223372036854775807,  9223372036854775807L,                        true",
            "uint64, 18446744073709551615, Long.parseUnsignedLong(\"18446744073709551615\"), true",
            "int256, -1,                   new BigInteger(\"-1\"),                      true"})
    void shouldRenderRepresentableValues(final String name, final String value, final String literal,
                                         final boolean representable) {
        assertThat(JavaIntegers.representable(type(name), new BigInteger(value))).isEqualTo(representable);
        if (representable) {
            assertThat(JavaIntegers.literal(type(name), new BigInteger(value), new Imports("p"))).isEqualTo(literal);
        }
    }
}
