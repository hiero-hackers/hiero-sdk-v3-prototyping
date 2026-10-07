## Why

SDK repositories need a unified, language-neutral representation of HBAR, the native token of the Hedera network. By formalizing `HbarUnit` and `Hbar` on top of the foundational `NativeToken` abstractions, we ensure that all SDKs provide type-safe, standardized currency math (especially tinybar conversions) without hardcoding arbitrary multipliers throughout the codebase.

## What Changes

- Define `HbarUnit` as a concrete enum implementing `NativeTokenUnit`, containing predefined units like `TINYBAR`, `MICROBAR`, `MILLIBAR`, `HBAR`, `KILOBAR`, `MEGABAR`, and `GIGABAR` alongside their `symbol` and `baseUnitFactor`.
- Define `Hbar` as a concrete subtype of `NativeToken` strictly bound to `HbarUnit`.
- Expose a `toTinybars()` convenience method on `Hbar` that behaves equivalently to `toBaseUnits()`.

## Capabilities

### New Capabilities

- `hbar-token`: Defines the concrete implementations of the Hedera network's native token and its standard unit denominations.

### Modified Capabilities

None.

## Impact

- Supplies the common source for corresponding Java and TypeScript OpenSpec changes regarding HBAR.
- Derives the HBAR token delta from `spec/base/hedera.md` without modifying the source specification.
- Excludes `HederaNetworkSetting` and mainnet/testnet identifiers (which belong in a subsequent API delta) to focus purely on the token logic.
