package org.hiero.sdk.v3.nativeToken.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Test;

/** Provider-contract tests using exactly representable examples; they do not prove production arithmetic. */
class NativeTokenContractTest {
    @Test
    void unitExposesSymbolAndFactor() {
        assertThat(TestUnit.KILO.symbol()).isEqualTo("K");
        assertThat(TestUnit.KILO.baseUnitFactor()).isEqualTo(1_000L);
        assertThat(TestUnit.BASE.baseUnitFactor()).isEqualTo(1L);
    }

    @Test
    void tokenExposesAmountAndUnit() {
        final var token = new TestToken(42, TestUnit.KILO);

        assertThat(token.amount()).isEqualTo(42);
        assertThat(token.unit()).isSameAs(TestUnit.KILO);
    }

    @Test
    void acceptsZeroAndExtremeAmounts() {
        assertThat(new TestToken(0, TestUnit.BASE).amount()).isZero();
        assertThat(new TestToken(Long.MAX_VALUE, TestUnit.BASE).amount()).isEqualTo(Long.MAX_VALUE);
        assertThat(new TestToken(Long.MIN_VALUE, TestUnit.BASE).amount()).isEqualTo(Long.MIN_VALUE);
        assertThat(new TestToken(-5, TestUnit.BASE).amount()).isEqualTo(-5);
    }

    @Test
    void rejectsNullUnit() {
        assertThatNullPointerException().isThrownBy(() -> new TestToken(1, null));
    }

    @Test
    void conversionReturnsConcreteTypeAndLeavesSourceUnchanged() {
        final var source = new TestToken(2, TestUnit.KILO);

        final TestToken converted = source.to(TestUnit.BASE);

        assertThat(converted.unit()).isSameAs(TestUnit.BASE);
        assertThat(converted.amount()).isEqualTo(2_000);
        assertThat(source.amount()).isEqualTo(2);
        assertThat(source.unit()).isSameAs(TestUnit.KILO);
    }

    @Test
    void baseUnitsOfBaseAmountAreUnchanged() {
        assertThat(new TestToken(7, TestUnit.BASE).toBaseUnits()).isEqualTo(7);
    }

    @Test
    void baseUnitsOfLargerUnitAreScaled() {
        assertThat(new TestToken(3, TestUnit.KILO).toBaseUnits()).isEqualTo(3_000);
    }

    enum TestUnit implements NativeTokenUnit {
        BASE("b", 1),
        KILO("K", 1_000);

        private final String symbol;
        private final long factor;

        TestUnit(final String symbol, final long factor) {
            this.symbol = symbol;
            this.factor = factor;
        }

        @Override
        public String symbol() {
            return symbol;
        }

        @Override
        public long baseUnitFactor() {
            return factor;
        }
    }

    static final class TestToken extends NativeToken<TestToken, TestUnit> {
        TestToken(final long amount, final TestUnit unit) {
            super(amount, unit);
        }

        @Override
        public TestToken to(final TestUnit targetUnit) {
            // Exactly representable test cases only: no rounding policy is chosen here.
            return new TestToken(toBaseUnits() / targetUnit.baseUnitFactor(), targetUnit);
        }

        @Override
        public long toBaseUnits() {
            return amount() * unit().baseUnitFactor();
        }
    }
}
