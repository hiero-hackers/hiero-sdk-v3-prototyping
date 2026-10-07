## Purpose

Define the language-neutral contract for HBAR amounts and units. SDK-specific OpenSpec changes use this document as their behavioral starting point and add only language mapping requirements.

## ADDED Requirements

### Requirement: HBAR unit enumeration

The SDK SHALL expose an `HbarUnit` enumeration that implements `NativeTokenUnit` containing the standard Hedera denominations: `TINYBAR`, `MICROBAR`, `MILLIBAR`, `HBAR`, `KILOBAR`, `MEGABAR`, and `GIGABAR`.

#### Scenario: HBAR units are evaluated

- **Given** the `HbarUnit` enumeration.
- **When** a caller reads the predefined units.
- **Then** the units provide their associated immutable string `symbol` (e.g. "ℏ") and int64 `baseUnitFactor` (e.g. 100_000_000 for `HBAR`).

### Requirement: Concrete HBAR token amount

The SDK SHALL expose an `Hbar` concrete type that extends `NativeToken<Hbar, HbarUnit>`. 

#### Scenario: HBAR token is instantiated

- **Given** an instantiated `Hbar` amount.
- **When** a caller reads the token state.
- **Then** the caller receives the int64 `amount` and its associated `HbarUnit` without being able to modify the stored state.

### Requirement: Tinybar conversion

The SDK SHALL expose a `toTinybars()` convenience method on `Hbar`.

#### Scenario: HBAR is converted to tinybars

- **Given** an `Hbar` amount expressed in any `HbarUnit`.
- **When** the caller invokes `toTinybars()`.
- **Then** the operation returns a 64-bit integer representing the exact total amount in tinybars, producing the identical result as `toBaseUnits()`.

## Language-Neutral API Schema

```text
namespace hedera
requires {NativeToken, NativeTokenUnit} from nativeToken

// Definition of the different units of HBAR, the native token of the Hedera network.
// `symbol` and `baseUnitFactor` are inherited from NativeTokenUnit; each constant provides its own
// normative values below. `baseUnitFactor` is the number of tinybars represented by one unit.
enum HbarUnit extends NativeTokenUnit {
    TINYBAR  // symbol: "tℏ", baseUnitFactor: 1
    MICROBAR // symbol: "μℏ", baseUnitFactor: 100
    MILLIBAR // symbol: "mℏ", baseUnitFactor: 100_000
    HBAR     // symbol: "ℏ", baseUnitFactor: 100_000_000
    KILOBAR  // symbol: "kℏ", baseUnitFactor: 100_000_000_000
    MEGABAR  // symbol: "Mℏ", baseUnitFactor: 100_000_000_000_000
    GIGABAR  // symbol: "Gℏ", baseUnitFactor: 100_000_000_000_000_000
}

// HBAR, the native token of the Hedera network. `amount`, `unit`, and `to(...)` are inherited from
// NativeToken.
Hbar extends NativeToken<Hbar, HbarUnit> {

    // Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits().
    int64 toTinybars()
}
```
