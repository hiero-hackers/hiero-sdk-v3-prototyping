package org.hiero.sdk.v3.nativeToken.contract;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/** Provider-contract examples only: the API supplies no production conversion arithmetic. */
class NativeTokenContractTest {
    @Nested
    class UnitState {
        @Test
        void observesImmutableUnitStateWithoutMutationMethods() {
            // GIVEN
            final NativeTokenUnit unit = TestUnit.LARGE;
            // WHEN / THEN
            assertThat(unit.symbol()).isEqualTo("large");
            assertThat(unit.baseUnitFactor()).isEqualTo(100L);
            assertThat(Arrays.stream(NativeTokenUnit.class.getDeclaredMethods()).map(method -> method.getName()))
                    .containsExactlyInAnyOrder("symbol", "baseUnitFactor");
            assertThat(Arrays.stream(TestUnit.class.getDeclaredFields())
                    .filter(field -> !field.isSynthetic() && !Modifier.isStatic(field.getModifiers())))
                    .allSatisfy(field -> assertThat(field.getModifiers())
                            .matches(Modifier::isPrivate).matches(Modifier::isFinal));
            assertThat(unit.symbol()).isEqualTo("large");
            assertThat(unit.baseUnitFactor()).isEqualTo(100L);
        }

        @Test
        void providerRejectsNullSymbol() {
            // GIVEN / WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> new OtherUnit(null, 1L))
                    .withMessage("symbol must not be null");
        }

        @ParameterizedTest
        @ValueSource(longs = {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE})
        void contractDoesNotAddFactorValidation(final long factor) {
            // GIVEN / WHEN
            final NativeTokenUnit unit = new OtherUnit("unit", factor);
            // THEN
            assertThat(unit.baseUnitFactor()).isEqualTo(factor);
        }
    }

    @Nested
    class AmountState {
        @ParameterizedTest
        @ValueSource(longs = {Long.MIN_VALUE, -42, 0, 42, Long.MAX_VALUE})
        void observesStoredAmountAndAssociatedUnit(final long amount) {
            // GIVEN / WHEN
            final TestToken token = new TestToken(amount, TestUnit.LARGE);
            final TestUnit unit = token.unit();
            // THEN
            assertThat(token.amount()).isEqualTo(amount);
            assertThat(unit).isSameAs(TestUnit.LARGE);
        }

        @Test
        void constructorRejectsNullUnitImmediately() {
            // GIVEN / WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> new TestToken(0L, null))
                    .withMessage("unit must not be null");
        }
    }

    @Nested
    class Conversion {
        @ParameterizedTest
        @CsvSource({"2,LARGE,BASE,200", "200,BASE,LARGE,2", "-3,LARGE,BASE,-300", "0,BASE,LARGE,0",
                "7,BASE,BASE,7"})
        void returnsNewConcreteTokenInTargetUnitWithoutChangingSource(
                final long amount, final TestUnit sourceUnit, final TestUnit targetUnit, final long expected) {
            // GIVEN
            final TestToken source = new TestToken(amount, sourceUnit);
            // WHEN: assigning to TestToken also verifies the self type
            final TestToken converted = source.to(targetUnit);
            // THEN
            assertThat(converted).isNotSameAs(source);
            assertThat(converted.amount()).isEqualTo(expected);
            assertThat(converted.unit()).isSameAs(targetUnit);
            assertThat(source.amount()).isEqualTo(amount);
            assertThat(source.unit()).isSameAs(sourceUnit);
        }

        @Test
        void providerRejectsNullTarget() {
            // GIVEN
            final TestToken token = new TestToken(1L, TestUnit.BASE);
            // WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> token.to(null))
                    .withMessage("targetUnit must not be null");
        }
    }

    @Nested
    class BaseUnits {
        @ParameterizedTest
        @CsvSource({"2,LARGE,200", "7,BASE,7", "-3,LARGE,-300", "0,LARGE,0"})
        void calculatesExactlyRepresentableBaseUnits(
                final long amount, final TestUnit unit, final long expected) {
            // GIVEN
            final TestToken token = new TestToken(amount, unit);
            // WHEN
            final long baseUnits = token.toBaseUnits();
            // THEN
            assertThat(baseUnits).isEqualTo(expected);
            assertThat(token.amount()).isEqualTo(amount);
            assertThat(token.unit()).isSameAs(unit);
        }
    }

    private enum TestUnit implements NativeTokenUnit {
        BASE("base", 1L), LARGE("large", 100L);

        private final String symbol;
        private final long baseUnitFactor;

        TestUnit(final String symbol, final long baseUnitFactor) {
            this.symbol = Objects.requireNonNull(symbol, "symbol must not be null");
            this.baseUnitFactor = baseUnitFactor;
        }

        @Override
        public String symbol() {
            return symbol;
        }

        @Override
        public long baseUnitFactor() {
            return baseUnitFactor;
        }
    }

    private record OtherUnit(String symbol, long baseUnitFactor) implements NativeTokenUnit {
        private OtherUnit {
            Objects.requireNonNull(symbol, "symbol must not be null");
        }
    }

    private static final class TestToken extends NativeToken<TestToken, TestUnit> {
        private TestToken(final long amount, final TestUnit unit) {
            super(amount, unit);
        }

        @Override
        public TestToken to(final TestUnit targetUnit) {
            Objects.requireNonNull(targetUnit, "targetUnit must not be null");
            return new TestToken(toBaseUnits() / targetUnit.baseUnitFactor(), targetUnit);
        }

        @Override
        public long toBaseUnits() {
            return amount() * unit().baseUnitFactor();
        }
    }
}
