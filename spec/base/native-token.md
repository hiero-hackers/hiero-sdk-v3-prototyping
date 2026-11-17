# Native Token API

## Description

Every Hiero network has a native token that is used, for example, to pay transaction fees. This
package provides the network-independent abstraction for it: `NativeToken` is an amount of the
native token expressed in a `NativeTokenUnit`, and can be converted to other units of the same token
or to its smallest (base) unit. Each network provides its own concrete implementation — for Hedera
this is `Hbar`. `ExchangeRate` describes the value of the native token in USD cents for a limited
period of time.

An exchange rate is a fraction of two whole numbers: `nativeTokenEquivalent` tokens, counted in the unit
`nativeTokenUnit`, are worth `centEquivalent` US cents. The unit is not the base unit: for Hedera it is HBAR
(100,000,000 tinybar). The network's rate of 30000 HBAR for 580150 cents, for example, means that 1 HBAR is worth
580150 / 30000 = 19.3383… cents. Both numbers are kept exactly as the network reports them. A rate is valid until
its `expirationTime`; the network reports the rate that follows it as the next rate.

- `rule:exchange-rate-unit` The network reports only the two numbers of a rate, not its unit. Every exchange rate
  the SDK reads from a network (from a receipt, a record or the exchange rate file) has the native token unit of the
  network of the client as its `nativeTokenUnit` (`Network.nativeTokenUnit`; for Hedera `HbarUnit.HBAR`).

**Calculating with an exchange rate.** The SDK offers no conversion between amounts of the native token and US
cents, because every conversion has to round and the right rounding depends on what the result is used for. Calculate
with the two numbers, not with their quotient, and only with whole numbers:

- An amount of `baseUnits` base units (for Hedera: tinybar) is worth `baseUnits × centEquivalent ÷
  (nativeTokenEquivalent × f)` cents, where `f` is `nativeTokenUnit.baseUnitFactor`, the number of base units in
  one unit of the rate (for Hedera: 100,000,000).
- A price of `cents` US cents is worth `cents × nativeTokenEquivalent × f ÷ centEquivalent` base units.
- Multiply first and divide last, so that only the final division rounds. Choose its rounding on purpose: an integer
  division rounds towards zero; to round up (for example for a budget that must cover a price), add the divisor
  minus 1 to the dividend first.
- The product of an amount in base units and `centEquivalent` (and of `nativeTokenEquivalent` and `f`) can exceed
  the range of an `int64`; calculate the products in a wider integer type (128 bits, or an arbitrary-precision
  integer).
- Never calculate with a floating-point number such as a `double`: it cannot hold the rate exactly, and the error
  ends up in the amount.

See the examples below.

## Design Notes

- **`ExchangeRate` is a concrete type.** It only holds data, and the network is not the only source of one: tests and
  callers that work with a known rate create it directly.
- **The rate is the fraction the protocol carries, not a number.** The protocol's `ExchangeRate` holds `hbarEquiv`
  and `centEquiv` as two `int32`. Neither `double` nor `decimal` can hold their quotient exactly (212848 / 30000 does
  not terminate), so the two numbers are the rate. There is deliberately no `double` view of the quotient, as the V2
  SDKs offer it (`exchangeRateInCents` in Java, JavaScript, Rust, Swift and C++): a monetary value calculated with a
  binary floating-point number is wrong. `nativeTokenEquivalent` is the protocol's `hbarEquiv` under a name that is
  not bound to Hedera.
- **`isExpiredAt` takes the point in time.** A method that reads the clock itself cannot be tested
  deterministically, and a caller that checks several rates against one point in time needs the same time for all.

## API Schema — Abstraction

```
namespace nativeToken

// A unit of a native token, either the smallest indivisible unit or a named multiple of it.
abstraction NativeTokenUnit {
    @@immutable symbol: string         // display symbol of the unit (e.g. "ℏ")
    @@immutable baseUnitFactor: int64  // number of base (smallest) units contained in one of this unit
}

// An amount of a network's native token, expressed in a given unit.
// $$Self is the concrete token type (e.g. Hbar), so `to(...)` returns that exact type rather than the abstraction.
abstraction NativeToken<$$Self extends NativeToken<$$Self, $$Unit>, $$Unit extends NativeTokenUnit> {
    @@immutable amount: int64          // amount expressed in `unit`
    @@immutable unit: $$Unit  // the unit `amount` is expressed in

    // Converts this amount to a different unit of the same token. Fails if the amount is not a whole number of the
    // target unit (a conversion never loses a part of the amount) or does not fit into an int64 in that unit.
    @@throws(invalid-argument-error) $$Self to(targetUnit: $$Unit)

    // Total amount expressed in base (smallest) units. Fails if the amount does not fit into an int64 of base units.
    @@throws(invalid-argument-error) int64 toBaseUnits()
}

// The value of the native token in US cents for a limited period of time: `nativeTokenEquivalent` tokens in the
// unit `nativeTokenUnit` are worth `centEquivalent` US cents until `expirationTime`. The rate is the fraction
// centEquivalent / nativeTokenEquivalent (US cents per `nativeTokenUnit`); calculate with the two numbers as whole
// numbers, never with a floating-point quotient.
@@finalType
ExchangeRate {
    // The denominator of the rate: the number of tokens, counted in `nativeTokenUnit`, whose value is
    // `centEquivalent` US cents. For Hedera the unit is HBAR, so 30000 means 30000 HBAR (3,000,000,000,000 tinybar).
    // To relate it to an amount in base units, multiply it by `nativeTokenUnit.baseUnitFactor`.
    @@immutable nativeTokenEquivalent: int32

    // The unit `nativeTokenEquivalent` is counted in: the unit the network quotes its rate in, not the base unit (for
    // Hedera: `HbarUnit.HBAR`). Its `baseUnitFactor` is the number of base units in one token of the rate.
    @@immutable nativeTokenUnit: NativeTokenUnit

    // The numerator of the rate: the value of `nativeTokenEquivalent` tokens in US cents (not dollars). With
    // `nativeTokenEquivalent` = 30000 and `centEquivalent` = 580150, 30000 HBAR are worth 580150 cents ($5,801.50),
    // i.e. 1 HBAR is worth about 19.34 cents.
    @@immutable centEquivalent: int32

    // The point in time until which the rate is valid; after it, the next rate of the network applies.
    @@immutable expirationTime: zonedDateTime

    // Returns true if the rate has expired at the given point in time, i.e. if `time` is after `expirationTime`.
    bool isExpiredAt(time: zonedDateTime)
}
```

## Default Instances for Testing

```
// The current rate of the exchange rate file of the Java SDK's test (ExchangeRatesTest): 30000 HBAR are worth
// 580150 US cents.
instance ExchangeRate = ExchangeRate{nativeTokenEquivalent: 30000, nativeTokenUnit: hedera.HbarUnit.HBAR,
        centEquivalent: 580150, expirationTime: DEFAULT}
```

## Examples

The examples use the rate of 30000 HBAR for 580150 US cents. All values are whole numbers; `int128` stands for any
integer type wide enough for the products (in Java `BigInteger`, in Rust `i128`, in TypeScript `bigint`).

### The value of an amount of HBAR in US cents

```
ExchangeRate rate = rates.currentRate;
Hbar amount = new Hbar(5, HbarUnit.HBAR);                        // 500,000,000 tinybar

int128 dividend = int128(amount.toTinybars()) * rate.centEquivalent;
int128 divisor  = int128(rate.nativeTokenEquivalent) * rate.nativeTokenUnit.baseUnitFactor;

int128 centsRoundedDown = dividend / divisor;                    // 96 (the exact value is 96.6916… cents)
int128 centsRoundedUp   = (dividend + divisor - 1) / divisor;    // 97
```

### The amount of HBAR a price in US cents costs

```
int64 priceInCents = 100;                                        // $1.00

int128 dividend = int128(priceInCents) * rate.nativeTokenEquivalent * rate.nativeTokenUnit.baseUnitFactor;
int128 divisor  = rate.centEquivalent;

int128 tinybarRoundedDown = dividend / divisor;                  // 517,107,644 tinybar (5.17107644 ℏ)
int128 tinybarRoundedUp   = (dividend + divisor - 1) / divisor;  // 517,107,645 tinybar; enough to pay the price
```

## Testing

```tests
// Source: the Java SDK (ExchangeRatesTest, the current rate of its exchange rate file
// 0a1008b0ea0110b6b4231a0608f0bade9006121008b0ea01108cef231a060880d7de9006): 580150 cents for 30000 HBAR until
// epoch second 1645714800. The Swift SDK (ExchangeRatesUnitTests) decodes the same values.
@@testId("an-exchange-rate-keeps-its-two-numbers-exactly")
test "an exchange rate keeps its two numbers exactly" {
    // when
    let rate = ExchangeRate{nativeTokenEquivalent: 30000, nativeTokenUnit: hedera.HbarUnit.HBAR,
            centEquivalent: 580150, expirationTime: toZonedDateTime(text: "2022-02-24T15:00:00Z")}

    // then
    assertEquals(expected: 30000, actual: rate.nativeTokenEquivalent)
    assertEquals(expected: "ℏ", actual: rate.nativeTokenUnit.symbol)
    assertEquals(expected: 100_000_000, actual: rate.nativeTokenUnit.baseUnitFactor)
    assertEquals(expected: 580150, actual: rate.centEquivalent)
    assertEquals(expected: toZonedDateTime(text: "2022-02-24T15:00:00Z"), actual: rate.expirationTime)
}

// Source: this spec (a rate has expired if the point in time is after its expiration time; at the expiration time
// it is still valid).
@@testId("an-exchange-rate-expires-after-its-expiration-time")
test "an exchange rate expires after its expiration time" {
    // given
    let rate = ExchangeRate{nativeTokenEquivalent: 30000, nativeTokenUnit: hedera.HbarUnit.HBAR,
            centEquivalent: 580150, expirationTime: toZonedDateTime(text: "2022-02-24T15:00:00Z")}

    // then
    assertFalse(condition: rate.isExpiredAt(time: toZonedDateTime(text: "2022-02-24T14:59:59Z")))
    assertFalse(condition: rate.isExpiredAt(time: toZonedDateTime(text: "2022-02-24T15:00:00Z")))
    assertTrue(condition: rate.isExpiredAt(time: toZonedDateTime(text: "2022-02-24T15:00:01Z")))
}
```

## Questions & Comments

- **No validation of the two numbers.** A rate of the network has a positive `nativeTokenEquivalent`, but nothing
  enforces it, and a rate with `nativeTokenEquivalent = 0` is no rate. Should both
  numbers carry `@@min(1)`, or must a rate decoded from the network be accepted as it is? — open
