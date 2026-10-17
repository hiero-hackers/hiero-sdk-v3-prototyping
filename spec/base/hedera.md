# Hedera API

## Description

Support for the Hedera network, the public network built on Hiero. It provides the identifiers of
the Hedera mainnet and testnet, which can be used to look up their network settings, and `Hbar`,
the native token of Hedera, together with its units (`HbarUnit`) from tinybar up to gigabar.

## API Schema — Abstraction

```
namespace hedera
requires {NetworkSetting} from ledger.config
requires {NativeToken, NativeTokenUnit} from nativeToken

constant HEDERA_MAINNET_IDENTIFIER:string = "hedera-mainnet" // identifier for the Hedera mainnet
constant HEDERA_TESTNET_IDENTIFIER:string = "hedera-testnet" // identifier for the Hedera testnet

HederaNetworkSetting extends NetworkSetting {
}

// Definition of the different units of HBAR, the native token of the Hedera network.
// `symbol` and `baseUnitFactor` are inherited from NativeTokenUnit; each value assigns its own
// normative values. `baseUnitFactor` is the number of tinybars represented by one unit.
enum HbarUnit(symbol: string, baseUnitFactor: int64) extends NativeTokenUnit {
    TINYBAR("tℏ", 1)
    MICROBAR("μℏ", 100)
    MILLIBAR("mℏ", 100_000)
    HBAR("ℏ", 100_000_000)
    KILOBAR("kℏ", 100_000_000_000)
    MEGABAR("Mℏ", 100_000_000_000_000)
    GIGABAR("Gℏ", 100_000_000_000_000_000)
}

// HBAR, the native token of the Hedera network. `amount`, `unit`, and `to(...)` are inherited from
// NativeToken.
Hbar extends NativeToken<Hbar, HbarUnit> {

    // Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits(). Fails if the amount does not fit
    // into an int64 of tinybars.
    @@throws(invalid-argument-error) int64 toTinybars()

    // Two amounts of HBAR are equal if they are the same number of tinybars, whatever their units: 1 ℏ equals
    // 100,000,000 tℏ. The amounts are compared exactly, also those that do not fit into an int64 of tinybars.
    bool equals(other: ANY)

    // Consistent with equals: equal amounts in different units have the same hash code.
    int32 hashCode()
}

```

## Default Instances for Testing

```
// One HBAR.
instance Hbar = Hbar{amount: 1, unit: HbarUnit.HBAR}

// The number of tinybars in one of each unit, and its symbol, as the units of HBAR are defined above.
values TINYBARS_PER_UNIT: map<HbarUnit, int64> = {
    HbarUnit.TINYBAR: 1,
    HbarUnit.MICROBAR: 100,
    HbarUnit.MILLIBAR: 100_000,
    HbarUnit.HBAR: 100_000_000,
    HbarUnit.KILOBAR: 100_000_000_000,
    HbarUnit.MEGABAR: 100_000_000_000_000,
    HbarUnit.GIGABAR: 100_000_000_000_000_000
}

values SYMBOLS: map<HbarUnit, string> = {
    HbarUnit.TINYBAR: "tℏ",
    HbarUnit.MICROBAR: "μℏ",
    HbarUnit.MILLIBAR: "mℏ",
    HbarUnit.HBAR: "ℏ",
    HbarUnit.KILOBAR: "kℏ",
    HbarUnit.MEGABAR: "Mℏ",
    HbarUnit.GIGABAR: "Gℏ"
}
```

## Testing

Tests of HBAR and its units, written as given / when / then. The comment above each test names its source; the units
and their factors are those of this spec, which match the HBAR denominations of the Hedera documentation.

```tests
// Source: this spec (HbarUnit); the unit tests of the Java, JavaScript, Go, Rust and Swift SDKs.
@@testId("every-unit-has-its-symbol-and-its-number-of-tinybars")
test "every unit has its symbol and its number of tinybars" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // then
        assertEquals(expected: TINYBARS_PER_UNIT[unit], actual: unit.baseUnitFactor, message: unit.symbol)
        assertEquals(expected: SYMBOLS[unit], actual: unit.symbol)
    }
}

// Source: this spec (toTinybars is toBaseUnits); the Java, JavaScript, Go and Rust SDKs (one of each unit in tinybars).
@@testId("one-of-each-unit-is-its-number-of-tinybars")
test "one of each unit is its number of tinybars" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // given
        let one = Hbar{amount: 1, unit: unit}

        // when / then
        assertEquals(expected: TINYBARS_PER_UNIT[unit], actual: one.toTinybars(), message: unit.symbol)
        assertEquals(expected: TINYBARS_PER_UNIT[unit], actual: one.toBaseUnits(), message: unit.symbol)
    }
}

// Source: the Java, JavaScript and Go SDKs (an amount is converted to every unit and back without loss).
@@testId("an-amount-of-whole-units-converts-to-tinybars-and-back")
test "an amount of whole units converts to tinybars and back" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // given
        let one = Hbar{amount: 1, unit: unit}

        // when
        let tinybars = one.to(targetUnit: HbarUnit.TINYBAR)
        let back = tinybars.to(targetUnit: unit)

        // then
        assertEquals(expected: Hbar{amount: TINYBARS_PER_UNIT[unit], unit: HbarUnit.TINYBAR}, actual: tinybars)
        assertEquals(expected: one, actual: back)
    }
}

// Source: the Java and JavaScript SDKs (conversions between units other than tinybar).
@@testId("an-amount-converts-between-units-by-their-factors")
test "an amount converts between units by their factors" {
    // given
    let hbar = DEFAULT(Hbar)

    // when / then
    assertEquals(expected: Hbar{amount: 1_000, unit: HbarUnit.MILLIBAR}, actual: hbar.to(targetUnit: HbarUnit.MILLIBAR))
    assertEquals(expected: Hbar{amount: 1_000_000, unit: HbarUnit.MICROBAR}, actual: hbar.to(targetUnit: HbarUnit.MICROBAR))
    assertEquals(expected: Hbar{amount: 1, unit: HbarUnit.KILOBAR},
            actual: Hbar{amount: 1_000, unit: HbarUnit.HBAR}.to(targetUnit: HbarUnit.KILOBAR))
    assertEquals(expected: Hbar{amount: 1_000_000, unit: HbarUnit.KILOBAR},
            actual: Hbar{amount: 1, unit: HbarUnit.GIGABAR}.to(targetUnit: HbarUnit.KILOBAR))
}

// Source: the Java, JavaScript, Go and Python SDKs (a negative amount, as used by the sender of a transfer).
@@testId("a-negative-amount-converts-like-a-positive-one")
test "a negative amount converts like a positive one" {
    // given
    let debit = Hbar{amount: -5, unit: HbarUnit.HBAR}

    // when / then
    assertEquals(expected: -500_000_000, actual: debit.toTinybars())
    assertEquals(expected: Hbar{amount: -5_000, unit: HbarUnit.MILLIBAR}, actual: debit.to(targetUnit: HbarUnit.MILLIBAR))
}

// Source: the Java and Go SDKs (zero HBAR).
@@testId("zero-is-zero-in-every-unit")
test "zero is zero in every unit" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // when / then
        assertEquals(expected: 0, actual: Hbar{amount: 0, unit: unit}.toTinybars())
        assertEquals(expected: Hbar{amount: 0, unit: HbarUnit.TINYBAR},
                actual: Hbar{amount: 0, unit: unit}.to(targetUnit: HbarUnit.TINYBAR))
    }
}

// Source: this spec (amounts are int64 tinybars); the largest whole number of gigabars that fits.
@@testId("the-largest-amounts-that-fit-into-int64-tinybars-convert-exactly")
test "the largest amounts that fit into int64 tinybars convert exactly" {
    // when / then
    assertEquals(expected: 9_200_000_000_000_000_000, actual: Hbar{amount: 92, unit: HbarUnit.GIGABAR}.toTinybars())
    assertEquals(expected: 9_223_372_036_854_775_807,
            actual: Hbar{amount: 9_223_372_036_854_775_807, unit: HbarUnit.TINYBAR}.toTinybars())
}

// Source: this spec (an Hbar is its amount and its unit).
@@testId("an-amount-keeps-its-amount-and-its-unit")
test "an amount keeps its amount and its unit" {
    // given
    let hbar = Hbar{amount: 42, unit: HbarUnit.KILOBAR}

    // then
    assertEquals(expected: 42, actual: hbar.amount)
    assertEquals(expected: HbarUnit.KILOBAR, actual: hbar.unit)
}

// Source: this spec (equals); the Java and JavaScript SDKs (amounts are compared in tinybars).
@@testId("amounts-of-the-same-number-of-tinybars-are-equal-whatever-their-units")
test "amounts of the same number of tinybars are equal whatever their units" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // given
        let one = Hbar{amount: 1, unit: unit}
        let inTinybars = Hbar{amount: TINYBARS_PER_UNIT[unit], unit: HbarUnit.TINYBAR}

        // then
        assertEquals(expected: inTinybars, actual: one, message: unit.symbol)
        assertEquals(expected: inTinybars.hashCode(), actual: one.hashCode(), message: unit.symbol)
        assertNotEquals(unexpected: Hbar{amount: TINYBARS_PER_UNIT[unit] + 1, unit: HbarUnit.TINYBAR}, actual: one)
    }
    assertEquals(expected: Hbar{amount: 1_000, unit: HbarUnit.HBAR}, actual: Hbar{amount: 1, unit: HbarUnit.KILOBAR})
    assertNotEquals(unexpected: Hbar{amount: -1, unit: HbarUnit.HBAR}, actual: DEFAULT(Hbar))
}

// Source: this spec (equals compares exactly): 100 gigabars and 100,000 megabars are both 10^19 tinybars, more than
// an int64 holds.
@@testId("amounts-beyond-int64-tinybars-are-compared-exactly")
test "amounts beyond int64 tinybars are compared exactly" {
    // given
    let gigabars = Hbar{amount: 100, unit: HbarUnit.GIGABAR}

    // then
    assertEquals(expected: Hbar{amount: 100_000, unit: HbarUnit.MEGABAR}, actual: gigabars)
    assertEquals(expected: Hbar{amount: 100_000, unit: HbarUnit.MEGABAR}.hashCode(), actual: gigabars.hashCode())
    assertNotEquals(unexpected: Hbar{amount: 100_001, unit: HbarUnit.MEGABAR}, actual: gigabars)
}

// Source: this spec (a conversion never loses a part of the amount).
@@testId("a-conversion-to-a-unit-of-which-the-amount-is-no-whole-number-fails")
test "a conversion to a unit of which the amount is no whole number fails" {
    // when / then
    @@expectThrows(invalid-argument-error)
    Hbar{amount: 1, unit: HbarUnit.TINYBAR}.to(targetUnit: HbarUnit.HBAR)
    @@expectThrows(invalid-argument-error)
    Hbar{amount: 150, unit: HbarUnit.HBAR}.to(targetUnit: HbarUnit.KILOBAR)
    @@expectThrows(invalid-argument-error)
    Hbar{amount: -1, unit: HbarUnit.MICROBAR}.to(targetUnit: HbarUnit.MILLIBAR)
}

// Source: this spec (amounts are int64 in every unit): 93 gigabars are more tinybars than an int64 holds.
@@testId("an-amount-that-does-not-fit-into-int64-tinybars-fails-to-convert")
test "an amount that does not fit into int64 tinybars fails to convert" {
    // given
    let tooMuch = Hbar{amount: 93, unit: HbarUnit.GIGABAR}

    // when / then
    @@expectThrows(invalid-argument-error)
    tooMuch.toTinybars()
    @@expectThrows(invalid-argument-error)
    tooMuch.toBaseUnits()
    @@expectThrows(invalid-argument-error)
    tooMuch.to(targetUnit: HbarUnit.TINYBAR)
    @@expectThrows(invalid-argument-error)
    Hbar{amount: -93, unit: HbarUnit.GIGABAR}.toTinybars()
}
```

## Questions & Comments
