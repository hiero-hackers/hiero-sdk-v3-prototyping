## Purpose

Define the TypeScript contract for native token amounts, their units, and the standard behaviors for unit conversion.

## ADDED Requirements

### Requirement: Immutable native token unit state

The SDK SHALL expose a `NativeTokenUnit` abstraction containing immutable `symbol` and `baseUnitFactor` values.

#### Scenario: Unit state is observed

- **Given** a defined native token unit.
- **When** a caller reads the unit state.
- **Then** the caller receives the string `symbol` and int64 `baseUnitFactor` without being able to modify the unit's stored state.

### Requirement: Immutable native token amount

The SDK SHALL expose a generic `NativeToken<Self, Unit>` abstraction containing immutable `amount` and `unit` values.

#### Scenario: Token amount is observed

- **Given** an instantiated native token amount.
- **When** a caller reads the token state.
- **Then** the caller receives the int64 `amount` and its associated `NativeTokenUnit` without being able to modify the stored state.

### Requirement: Token unit conversion

The SDK SHALL expose a `to()` method that converts a given native token amount to a target unit, returning the exact concrete token type `Self`.

#### Scenario: Token amount is converted to a different unit

- **Given** a token amount expressed in a specific unit.
- **When** the caller invokes `to()` providing a target unit.
- **Then** the operation returns a new token amount mathematically converted to the target unit.

### Requirement: Base unit retrieval

The SDK SHALL expose a `toBaseUnits()` method that returns the total amount expressed in the smallest indivisible base units.

#### Scenario: Base units are calculated

- **Given** a token amount expressed in any unit.
- **When** the caller invokes `toBaseUnits()`.
- **Then** the operation returns a 64-bit integer representing the total amount in base units.

## TypeScript API Mapping

| Common Meta-Language | TypeScript Contract |
| --- | --- |
| `NativeTokenUnit` abstraction | `interface NativeTokenUnit { readonly symbol: string; readonly baseUnitFactor: bigint \| number; }` |
| `NativeToken<$$Self, $$Unit>` abstraction | `abstract class NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit>` |
| `amount: int64` (immutable) | `readonly amount: bigint` |
| `unit: $$Unit` (immutable) | `readonly unit: Unit` |
| `to(targetUnit: $$Unit): $$Self` | `to(targetUnit: Unit): Self` |
| `toBaseUnits(): int64` | `toBaseUnits(): bigint` |
