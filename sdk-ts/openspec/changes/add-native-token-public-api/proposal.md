## Why

SDKs need a language-idiomatic way to represent native token amounts, their units, and conversions, grounded in the
language-neutral contract defined in the common `add-native-token-public-api` change. This TypeScript change
derives the type shapes and behavioral requirements from that common specification and adds the TypeScript-specific
contract mappings needed to ship a correct, idiomatic public API.

## What Changes

- Expose a `NativeTokenUnit` abstraction with immutable `symbol` and `baseUnitFactor`.
- Expose a generic `NativeToken<Self, Unit>` abstraction with immutable `amount` (bigint) and `unit`, `to()` conversion, and `toBaseUnits()`.
- Keep concrete implementations internal; export only the public contracts.

## Capabilities

### New Capabilities

- `native-token`: TypeScript public types for native token units, amounts, and unit conversion.

### Modified Capabilities

None.

## Impact

- Derived from the common `add-native-token-public-api` proposal and specification.
- Does not introduce concrete token types (e.g., Hbar) — those belong to network-specific changes.
- `amount` maps `int64` → `bigint` to safely handle values exceeding `Number.MAX_SAFE_INTEGER`.
