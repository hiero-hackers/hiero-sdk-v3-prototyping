package org.hiero.sdk.v3.metalang.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.junit.jupiter.api.Test;

class IntegerRangeTest {

    @Test
    void shouldKnowTheRangesOfTheIntegerTypes() {
        final IntegerRange uint8 = IntegerRange.of(BuiltinType.lookup("uint8").orElseThrow());
        assertThat(uint8).isEqualTo(new IntegerRange(BigInteger.ZERO, BigInteger.valueOf(255)));
        assertThat(IntegerRange.of(BuiltinType.lookup("int16").orElseThrow())).isEqualTo(new IntegerRange(
                BigInteger.valueOf(-32768), BigInteger.valueOf(32767)));
        assertThat(uint8.contains(BigInteger.ZERO)).isTrue();
        assertThat(uint8.contains(BigInteger.valueOf(256))).isFalse();
        assertThat(uint8.contains(BigInteger.valueOf(-1))).isFalse();
        assertThat(uint8.intersect(new IntegerRange(BigInteger.TEN, BigInteger.valueOf(1000))))
                .isEqualTo(new IntegerRange(BigInteger.TEN, BigInteger.valueOf(255)));
        assertThat(IntegerRange.isUnsigned(BuiltinType.lookup("uint64").orElseThrow())).isTrue();
        assertThat(IntegerRange.integer(new Type.BasicType(BuiltinType.lookup("int32").orElseThrow(), List.of())))
                .isPresent();
        assertThat(IntegerRange.integer(new Type.BasicType(BuiltinType.lookup("string").orElseThrow(), List.of())))
                .isEmpty();
        assertThat(IntegerRange.integer(new Type.DeclaredType(new QualifiedName("a", "B"), List.of()))).isEmpty();
    }
}
