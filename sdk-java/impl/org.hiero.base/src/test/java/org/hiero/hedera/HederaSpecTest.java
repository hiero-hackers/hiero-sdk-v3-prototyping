package org.hiero.hedera;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `hedera`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("hedera")
class HederaSpecTest {

    /// The number of tinybars in one of each unit, and its symbol, as the units of HBAR are defined above.
    private static Map<HbarUnit, Long> TINYBARS_PER_UNIT() {
        return Map.of(HbarUnit.TINYBAR, 1L, HbarUnit.MICROBAR, 100L, HbarUnit.MILLIBAR, 100_000L, HbarUnit.HBAR, 100_000_000L, HbarUnit.KILOBAR, 100_000_000_000L, HbarUnit.MEGABAR, 100_000_000_000_000L, HbarUnit.GIGABAR, 100_000_000_000_000_000L);
    }

    private static Map<HbarUnit, String> SYMBOLS() {
        return Map.of(HbarUnit.TINYBAR, "tℏ", HbarUnit.MICROBAR, "μℏ", HbarUnit.MILLIBAR, "mℏ", HbarUnit.HBAR, "ℏ", HbarUnit.KILOBAR, "kℏ", HbarUnit.MEGABAR, "Mℏ", HbarUnit.GIGABAR, "Gℏ");
    }

    /// `hedera/every-unit-has-its-symbol-and-its-number-of-tinybars`
    ///
    /// Source: this spec (HbarUnit); the unit tests of the Java, JavaScript, Go, Rust and Swift SDKs.
    @Test
    @DisplayName("every unit has its symbol and its number of tinybars")
    @Tag("testId:hedera/every-unit-has-its-symbol-and-its-number-of-tinybars")
    void everyUnitHasItsSymbolAndItsNumberOfTinybars() {
        for (final HbarUnit unit : keys(TINYBARS_PER_UNIT())) {
            // then
            assertEquals(get(TINYBARS_PER_UNIT(), unit), unit.baseUnitFactor(), unit.symbol());
            assertEquals(get(SYMBOLS(), unit), unit.symbol());
        }
    }

    /// `hedera/one-of-each-unit-is-its-number-of-tinybars`
    ///
    /// Source: this spec (toTinybars is toBaseUnits); the Java, JavaScript, Go and Rust SDKs (one of each unit in tinybars).
    @Test
    @DisplayName("one of each unit is its number of tinybars")
    @Tag("testId:hedera/one-of-each-unit-is-its-number-of-tinybars")
    void oneOfEachUnitIsItsNumberOfTinybars() {
        for (final HbarUnit unit : keys(TINYBARS_PER_UNIT())) {
            // given
            final Hbar one = new Hbar(1L, unit);

            // when / then
            assertEquals(get(TINYBARS_PER_UNIT(), unit), one.toTinybars(), unit.symbol());
            assertEquals(get(TINYBARS_PER_UNIT(), unit), one.toBaseUnits(), unit.symbol());
        }
    }

    /// `hedera/an-amount-of-whole-units-converts-to-tinybars-and-back`
    ///
    /// Source: the Java, JavaScript and Go SDKs (an amount is converted to every unit and back without loss).
    @Test
    @DisplayName("an amount of whole units converts to tinybars and back")
    @Tag("testId:hedera/an-amount-of-whole-units-converts-to-tinybars-and-back")
    void anAmountOfWholeUnitsConvertsToTinybarsAndBack() {
        for (final HbarUnit unit : keys(TINYBARS_PER_UNIT())) {
            // given
            final Hbar one = new Hbar(1L, unit);

            // when
            final Hbar tinybars = one.to(HbarUnit.TINYBAR);
            final Hbar back = tinybars.to(unit);

            // then
            assertEquals(new Hbar(get(TINYBARS_PER_UNIT(), unit), HbarUnit.TINYBAR), tinybars);
            assertEquals(one, back);
        }
    }

    /// `hedera/an-amount-converts-between-units-by-their-factors`
    ///
    /// Source: the Java and JavaScript SDKs (conversions between units other than tinybar).
    @Test
    @DisplayName("an amount converts between units by their factors")
    @Tag("testId:hedera/an-amount-converts-between-units-by-their-factors")
    void anAmountConvertsBetweenUnitsByTheirFactors() {
        // given
        final Hbar hbar = defaultHbar();

        // when / then
        assertEquals(new Hbar(1_000L, HbarUnit.MILLIBAR), hbar.to(HbarUnit.MILLIBAR));
        assertEquals(new Hbar(1_000_000L, HbarUnit.MICROBAR), hbar.to(HbarUnit.MICROBAR));
        assertEquals(new Hbar(1L, HbarUnit.KILOBAR), new Hbar(1_000L, HbarUnit.HBAR).to(HbarUnit.KILOBAR));
        assertEquals(new Hbar(1_000_000L, HbarUnit.KILOBAR), new Hbar(1L, HbarUnit.GIGABAR).to(HbarUnit.KILOBAR));
    }

    /// `hedera/a-negative-amount-converts-like-a-positive-one`
    ///
    /// Source: the Java, JavaScript, Go and Python SDKs (a negative amount, as used by the sender of a transfer).
    @Test
    @DisplayName("a negative amount converts like a positive one")
    @Tag("testId:hedera/a-negative-amount-converts-like-a-positive-one")
    void aNegativeAmountConvertsLikeAPositiveOne() {
        // given
        final Hbar debit = new Hbar(-5L, HbarUnit.HBAR);

        // when / then
        assertEquals(-500_000_000L, debit.toTinybars());
        assertEquals(new Hbar(-5_000L, HbarUnit.MILLIBAR), debit.to(HbarUnit.MILLIBAR));
    }

    /// `hedera/zero-is-zero-in-every-unit`
    ///
    /// Source: the Java and Go SDKs (zero HBAR).
    @Test
    @DisplayName("zero is zero in every unit")
    @Tag("testId:hedera/zero-is-zero-in-every-unit")
    void zeroIsZeroInEveryUnit() {
        for (final HbarUnit unit : keys(TINYBARS_PER_UNIT())) {
            // when / then
            assertEquals(0L, new Hbar(0L, unit).toTinybars());
            assertEquals(new Hbar(0L, HbarUnit.TINYBAR), new Hbar(0L, unit).to(HbarUnit.TINYBAR));
        }
    }

    /// `hedera/the-largest-amounts-that-fit-into-int64-tinybars-convert-exactly`
    ///
    /// Source: this spec (amounts are int64 tinybars); the largest whole number of gigabars that fits.
    @Test
    @DisplayName("the largest amounts that fit into int64 tinybars convert exactly")
    @Tag("testId:hedera/the-largest-amounts-that-fit-into-int64-tinybars-convert-exactly")
    void theLargestAmountsThatFitIntoInt64TinybarsConvertExactly() {
        // when / then
        assertEquals(9_200_000_000_000_000_000L, new Hbar(92L, HbarUnit.GIGABAR).toTinybars());
        assertEquals(9_223_372_036_854_775_807L, new Hbar(9_223_372_036_854_775_807L, HbarUnit.TINYBAR).toTinybars());
    }

    /// `hedera/an-amount-keeps-its-amount-and-its-unit`
    ///
    /// Source: this spec (an Hbar is its amount and its unit).
    @Test
    @DisplayName("an amount keeps its amount and its unit")
    @Tag("testId:hedera/an-amount-keeps-its-amount-and-its-unit")
    void anAmountKeepsItsAmountAndItsUnit() {
        // given
        final Hbar hbar = new Hbar(42L, HbarUnit.KILOBAR);

        // then
        assertEquals(42L, hbar.amount());
        assertEquals(HbarUnit.KILOBAR, hbar.unit());
    }

    /// `hedera/amounts-of-the-same-number-of-tinybars-are-equal-whatever-their-units`
    ///
    /// Source: this spec (equals); the Java and JavaScript SDKs (amounts are compared in tinybars).
    @Test
    @DisplayName("amounts of the same number of tinybars are equal whatever their units")
    @Tag("testId:hedera/amounts-of-the-same-number-of-tinybars-are-equal-whatever-their-units")
    void amountsOfTheSameNumberOfTinybarsAreEqualWhateverTheirUnits() {
        for (final HbarUnit unit : keys(TINYBARS_PER_UNIT())) {
            // given
            final Hbar one = new Hbar(1L, unit);
            final Hbar inTinybars = new Hbar(get(TINYBARS_PER_UNIT(), unit), HbarUnit.TINYBAR);

            // then
            assertEquals(inTinybars, one, unit.symbol());
            assertEquals(inTinybars.hashCode(), one.hashCode(), unit.symbol());
            assertNotEquals(new Hbar(checked(integer(get(TINYBARS_PER_UNIT(), unit)).add(integer(1L)), "int64").longValueExact(), HbarUnit.TINYBAR), one);
        }
        assertEquals(new Hbar(1_000L, HbarUnit.HBAR), new Hbar(1L, HbarUnit.KILOBAR));
        assertNotEquals(new Hbar(-1L, HbarUnit.HBAR), defaultHbar());
    }

    /// `hedera/amounts-beyond-int64-tinybars-are-compared-exactly`
    ///
    /// Source: this spec (equals compares exactly): 100 gigabars and 100,000 megabars are both 10^19 tinybars, more than
    /// an int64 holds.
    @Test
    @DisplayName("amounts beyond int64 tinybars are compared exactly")
    @Tag("testId:hedera/amounts-beyond-int64-tinybars-are-compared-exactly")
    void amountsBeyondInt64TinybarsAreComparedExactly() {
        // given
        final Hbar gigabars = new Hbar(100L, HbarUnit.GIGABAR);

        // then
        assertEquals(new Hbar(100_000L, HbarUnit.MEGABAR), gigabars);
        assertEquals(new Hbar(100_000L, HbarUnit.MEGABAR).hashCode(), gigabars.hashCode());
        assertNotEquals(new Hbar(100_001L, HbarUnit.MEGABAR), gigabars);
    }

    /// `hedera/a-conversion-to-a-unit-of-which-the-amount-is-no-whole-number-fails`
    ///
    /// Source: this spec (a conversion never loses a part of the amount).
    @Test
    @DisplayName("a conversion to a unit of which the amount is no whole number fails")
    @Tag("testId:hedera/a-conversion-to-a-unit-of-which-the-amount-is-no-whole-number-fails")
    void aConversionToAUnitOfWhichTheAmountIsNoWholeNumberFails() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new Hbar(1L, HbarUnit.TINYBAR).to(HbarUnit.HBAR));
        assertThrows(IllegalArgumentException.class, () -> new Hbar(150L, HbarUnit.HBAR).to(HbarUnit.KILOBAR));
        assertThrows(IllegalArgumentException.class, () -> new Hbar(-1L, HbarUnit.MICROBAR).to(HbarUnit.MILLIBAR));
    }

    /// `hedera/an-amount-that-does-not-fit-into-int64-tinybars-fails-to-convert`
    ///
    /// Source: this spec (amounts are int64 in every unit): 93 gigabars are more tinybars than an int64 holds.
    @Test
    @DisplayName("an amount that does not fit into int64 tinybars fails to convert")
    @Tag("testId:hedera/an-amount-that-does-not-fit-into-int64-tinybars-fails-to-convert")
    void anAmountThatDoesNotFitIntoInt64TinybarsFailsToConvert() {
        // given
        final Hbar tooMuch = new Hbar(93L, HbarUnit.GIGABAR);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> tooMuch.toTinybars());
        assertThrows(IllegalArgumentException.class, () -> tooMuch.toBaseUnits());
        assertThrows(IllegalArgumentException.class, () -> tooMuch.to(HbarUnit.TINYBAR));
        assertThrows(IllegalArgumentException.class, () -> new Hbar(-93L, HbarUnit.GIGABAR).toTinybars());
    }

    private static Hbar defaultHbar() {
        return new Hbar(1L, HbarUnit.HBAR);
    }
}
