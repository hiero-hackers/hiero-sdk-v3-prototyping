# Hedera API

The Hedera API acts as the specific concrete implementation mapping the generic SDK abstractions to the Hedera network context. It defines the core identifiers, constants, and the native token `HBAR` powering the Hedera public ledger.

## Constants

### Network Identifiers
String constants establishing standard references to Hedera's public networks.
- `HEDERA_MAINNET_IDENTIFIER`: `"hedera-mainnet"`
- `HEDERA_TESTNET_IDENTIFIER`: `"hedera-testnet"`

## Interfaces and Classes

### `HederaNetworkSetting`
An interface extending `NetworkSetting` (from the `ledger.config` module). Represents the structured configurations specific to Hedera.

### `HbarUnit`
A concrete implementation of `NativeTokenUnit` defining the strict denominations of HBAR. Uses the "type-safe enum" pattern to expose predefined constants.
Available units include:
- `TINYBAR` (`tℏ`, base factor 1)
- `MICROBAR` (`μℏ`, base factor 100)
- `MILLIBAR` (`mℏ`, base factor 100,000)
- `HBAR` (`ℏ`, base factor 100,000,000)
- `KILOBAR` (`kℏ`, base factor 100,000,000,000)
- `MEGABAR` (`Mℏ`, base factor 100,000,000,000,000)
- `GIGABAR` (`Gℏ`, base factor 100,000,000,000,000,000)

### `Hbar`
A concrete implementation of `NativeToken` representing a quantitative amount of Hedera's native token.
- Extends the base scaling logic inherited from `nativeToken.md`.
- `to(targetUnit: HbarUnit)`: Returns a new `Hbar` instance safely scaled to the targeted unit (fractionals are properly truncated as per standard integer division rules on base units).
- `toTinybars()`: Returns the exact underlying amount represented in `TINYBAR`s (synonymous with `toBaseUnits()`), acting as the lowest indivisible magnitude.
