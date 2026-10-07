## Why

SDK repositories need a unified, language-neutral contract for native network tokens and their unit conversions before Java and TypeScript select their own public type shapes. Publishing this specification ensures that each SDK provides standard token unit representations (like Hbar vs tinybar) in a safe, type-correct manner.

## What Changes

- Define `NativeTokenUnit` abstraction representing a unit of a native token, storing its display symbol and `baseUnitFactor`.
- Define `NativeToken` abstraction representing an amount expressed in a specific unit.
- Define token conversion methods: `to(targetUnit)` for converting between arbitrary units, and `toBaseUnits()` for retrieving the smallest indivisible quantity.
- Establish that conversion methods return concrete token types instead of generic abstractions.

## Capabilities

### New Capabilities

- `native-token`: Defines the common structure for representing amounts of the network's native token and securely converting those amounts between different units.

### Modified Capabilities

None.

## Impact

- Supplies the common source for corresponding Java and TypeScript OpenSpec changes regarding native token types.
- Derives the native token delta from `spec/base/native-token.md` without modifying the source specification.
- Excludes `ExchangeRate` (which belongs in a subsequent API delta) and focuses purely on token representation and unit math.
