package org.hiero.sdk.v3.ledger.contract;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NetworkContractTest {
    @Test
    void observesExactIdentityNameAndConcreteUnit() {
        // GIVEN
        final Network<TestUnit> network = new Network<>(new byte[] {0, 1, -1}, "example", TestUnit.BASE);
        // WHEN
        final TestUnit unit = network.nativeTokenUnit();
        // THEN
        assertThat(network.id()).containsExactly(0, 1, -1);
        assertThat(network.name()).isEqualTo("example");
        assertThat(unit).isSameAs(TestUnit.BASE);
        assertThat(network.id()).containsExactly(0, 1, -1);
        assertThat(network.name()).isEqualTo("example");
        assertThat(network.nativeTokenUnit()).isSameAs(unit);
    }

    @ParameterizedTest
    @MethodSource("ids")
    void ownsIdIndependentlyOfInputAndEveryReturnedArray(final byte[] input) {
        // GIVEN
        final byte[] expected = input.clone();
        final Network<TestUnit> network = new Network<>(input, null, TestUnit.BASE);
        // WHEN
        final byte[] first = network.id();
        final byte[] second = network.id();
        if (input.length > 0) {
            input[0] ^= (byte) 0xff;
            first[0] = 17;
            second[second.length - 1] = 42;
        }
        // THEN: distinct copies are required even for empty IDs
        assertThat(first).isNotSameAs(input).isNotSameAs(second);
        assertThat(network.id()).containsExactly(expected).isNotSameAs(first).isNotSameAs(second);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"example", "  example  ", "   ", "réseau 🌍"})
    void preservesAbsentEmptyAndSuppliedNames(final String name) {
        // GIVEN / WHEN
        final Network<TestUnit> network = new Network<>(new byte[0], name, TestUnit.BASE);
        // THEN
        assertThat(network.name()).isSameAs(name);
    }

    @Test
    void rejectsNullIdWithDescriptiveMessage() {
        // GIVEN / WHEN / THEN
        assertThatNullPointerException().isThrownBy(() -> new Network<>(null, null, TestUnit.BASE))
                .withMessage("id must not be null");
    }

    @Test
    void rejectsNullUnitWithDescriptiveMessage() {
        // GIVEN / WHEN / THEN
        assertThatNullPointerException().isThrownBy(() -> new Network<>(new byte[0], null, null))
                .withMessage("nativeTokenUnit must not be null");
    }

    @Test
    void constructionAndReadsRetainUnitWithoutInspectingMetadata() {
        // GIVEN: instrumentation counts calls without changing the unit's symbol or factor
        final AtomicInteger reads = new AtomicInteger();
        final NativeTokenUnit unit = new NativeTokenUnit() {
            @Override
            public String symbol() {
                reads.incrementAndGet();
                return "base";
            }

            @Override
            public long baseUnitFactor() {
                reads.incrementAndGet();
                return 1L;
            }
        };
        // WHEN
        final Network<NativeTokenUnit> network = new Network<>(new byte[] {1}, "example", unit);
        final byte[] observed = network.id();
        final String name = network.name();
        final NativeTokenUnit associated = network.nativeTokenUnit();
        // THEN
        assertThat(observed).containsExactly(1);
        assertThat(name).isEqualTo("example");
        assertThat(associated).isSameAs(unit);
        assertThat(reads.get()).isZero();
        assertThat(unit.symbol()).isEqualTo("base");
        assertThat(unit.baseUnitFactor()).isEqualTo(1L);
    }

    @Test
    void javaValueEqualityComparesArrayContentsNameAndUnit() {
        // GIVEN
        final Network<TestUnit> first = new Network<>(new byte[] {0, 1}, "example", TestUnit.BASE);
        final Network<TestUnit> second = new Network<>(new byte[] {0, 1}, "example", TestUnit.BASE);
        final Network<TestUnit> third = new Network<>(new byte[] {0, 1}, "example", TestUnit.BASE);
        // WHEN / THEN
        assertThat(first).isEqualTo(first).isEqualTo(second);
        assertThat(second).isEqualTo(first).isEqualTo(third);
        assertThat(first).isEqualTo(third).hasSameHashCodeAs(second).hasSameHashCodeAs(third);
        assertThat(first.equals(null)).isFalse();
        assertThat(first.equals("example")).isFalse();
        assertThat(first).isNotEqualTo(new Network<>(new byte[] {1, 0}, "example", TestUnit.BASE));
        assertThat(first).isNotEqualTo(new Network<>(new byte[] {0, 1}, "other", TestUnit.BASE));
        assertThat(first).isNotEqualTo(new Network<>(new byte[] {0, 1}, null, TestUnit.BASE));
        assertThat(first).isNotEqualTo(new Network<>(new byte[] {0, 1}, "example", TestUnit.LARGE));
    }

    @Test
    void equalityUsesUnitValueSemanticsRatherThanItsReference() {
        // GIVEN
        final Network<ValueUnit> first = new Network<>(new byte[0], null, new ValueUnit("base", 1L));
        final Network<ValueUnit> second = new Network<>(new byte[0], null, new ValueUnit("base", 1L));
        // WHEN / THEN
        assertThat(first.nativeTokenUnit()).isNotSameAs(second.nativeTokenUnit());
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).hasToString("Network[id=[], name=null, nativeTokenUnit=ValueUnit[symbol=base, baseUnitFactor=1]]");
    }

    @Test
    void equalityHashAndTextRemainStableAfterExternalArrayMutation() {
        // GIVEN
        final byte[] input = {0, 1};
        final Network<TestUnit> network = new Network<>(input, "example", TestUnit.BASE);
        final Network<TestUnit> equal = new Network<>(input.clone(), "example", TestUnit.BASE);
        final int hash = network.hashCode();
        final String text = network.toString();
        // WHEN
        input[0] = 42;
        network.id()[1] = 42;
        // THEN
        assertThat(network).isEqualTo(equal);
        assertThat(network.hashCode()).isEqualTo(hash);
        assertThat(network.toString()).isEqualTo(text)
                .isEqualTo("Network[id=[0, 1], name=example, nativeTokenUnit=BASE]");
    }

    private static Stream<byte[]> ids() {
        return Stream.of(new byte[0], new byte[] {0, 0}, new byte[] {-1},
                new byte[] {0, 0, 1, -1, Byte.MIN_VALUE, Byte.MAX_VALUE});
    }

    private record ValueUnit(String symbol, long baseUnitFactor) implements NativeTokenUnit {
    }

    private enum TestUnit implements NativeTokenUnit {
        BASE, LARGE;

        @Override
        public String symbol() {
            return name();
        }

        @Override
        public long baseUnitFactor() {
            return this == BASE ? 1L : 100L;
        }
    }
}
