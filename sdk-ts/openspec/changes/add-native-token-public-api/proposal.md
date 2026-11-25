## Source Reference

- Common proposal: [`openspec/changes/add-native-token-public-api/proposal.md`](../../../../openspec/changes/add-native-token-public-api/proposal.md)
- Common specification: [`openspec/changes/add-native-token-public-api/spec.md`](../../../../openspec/changes/add-native-token-public-api/spec.md)
- Source revision: `65dded267c95d003d444d3e3f897c9e04b8dcd6d`

## Why

SDK repositories need one language-neutral contract for native token amounts, units, and conversions before individual
SDKs select their own public type shapes and implementations. The TypeScript SDK adopts this contract so every network's
native token (for example HBAR) shares one exported, exact, and type-safe amount abstraction.

## What Changes

- Define units of native tokens with their display symbol and relationship to the base unit.
- Define token amounts associated with a specific unit.
- Define conversion of token amounts between different units of the same token.
- Define conversion of token amounts to the base (smallest) unit.
- Map these behaviors to exported TypeScript `NativeTokenUnit` and `NativeToken<Self, Unit>` abstract classes, with
  `bigint` for every int64 value.
- Define the TypeScript behavior the common specification leaves open: inexact conversions throw a `RangeError`, and a
  unit's `baseUnitFactor` must be positive.

## Capabilities

### New Capabilities

- `native-token`: Defines the shared native token unit, amount, and conversion contract.

### Modified Capabilities

None.

## Impact

- Adds `NativeTokenUnit` and `NativeToken` to the existing `@hiero-ledger/sdk-base-api` package. No new runtime
  dependencies.
- Does not modify `spec/base/native-token.md` or the common change.

### Intentionally Deferred

- Concrete token implementations such as `HbarUnit` and `Hbar` (common delta `add-hbar-public-api`).
- `ExchangeRate` (common delta `add-exchange-rate-public-api`).
- Arithmetic, comparison, parsing, and formatting of token amounts; not part of the common contract.
- An int64 range check on the result of `toBaseUnits()` (see the design's open questions).
