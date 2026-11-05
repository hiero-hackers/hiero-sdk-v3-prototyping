## Purpose

Define the TypeScript contract for native token amounts, their units, and unit conversion, preserving the common native
token behavior and adding only the TypeScript language mapping.

## ADDED Requirements

### Requirement: Immutable native token unit state

The SDK SHALL expose a `NativeTokenUnit` abstraction containing immutable `symbol` and `baseUnitFactor` values.

#### Scenario: Unit state is observed

- **WHEN** a caller reads the state of a defined native token unit
- **THEN** the caller receives the string `symbol` and int64 `baseUnitFactor` without being able to modify the unit's
  stored state

### Requirement: Immutable native token amount

The SDK SHALL expose a generic `NativeToken<$$Self, $$Unit>` abstraction containing immutable `amount` and `unit`
values.

#### Scenario: Token amount is observed

- **WHEN** a caller reads the state of an instantiated native token amount
- **THEN** the caller receives the int64 `amount` and its associated `NativeTokenUnit` without being able to modify the
  stored state

### Requirement: Token unit conversion

The SDK SHALL expose a `to()` method that converts a given native token amount to a target unit, returning the exact
concrete token type `$$Self`.

#### Scenario: Token amount is converted to a different unit

- **WHEN** the caller invokes `to()` on a token amount expressed in a specific unit, providing a target unit
- **THEN** the operation returns a new token amount mathematically converted to the target unit

### Requirement: Base unit retrieval

The SDK SHALL expose a `toBaseUnits()` method that returns the total amount expressed in the smallest indivisible base
units.

#### Scenario: Base units are calculated

- **WHEN** the caller invokes `toBaseUnits()` on a token amount expressed in any unit
- **THEN** the operation returns a 64-bit integer representing the total amount in base units

### Requirement: TypeScript int64 mapping

The TypeScript SDK SHALL represent `amount`, `baseUnitFactor`, and the result of `toBaseUnits()` as `bigint`.
Constructing a unit or token with a value that is not a `bigint` SHALL throw a `TypeError`, and with a `bigint` outside
the signed 64-bit range SHALL throw a `RangeError`.

#### Scenario: Value outside int64 is rejected

- **WHEN** a unit or token is constructed with a `number` instead of a `bigint`, or with a `bigint` outside
  `-(2^63)` to `2^63 - 1`
- **THEN** construction throws a `TypeError` for the wrong type or a `RangeError` for the out-of-range value

### Requirement: TypeScript unit mapping

The TypeScript SDK SHALL export `NativeTokenUnit` as an abstract class with read-only `symbol: string` and
`baseUnitFactor: bigint` accessors. `baseUnitFactor` SHALL be greater than zero; constructing a unit with a factor of
zero or less SHALL throw a `RangeError`. Constructing a unit whose `symbol` is not a string SHALL throw a `TypeError`.

#### Scenario: Non-positive factor is rejected

- **WHEN** a unit is constructed with a `baseUnitFactor` of `0n` or a negative value
- **THEN** construction throws a `RangeError`

### Requirement: TypeScript token mapping

The TypeScript SDK SHALL export `NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit>` as an
abstract class with read-only `amount: bigint` and `unit: Unit` accessors. `to(targetUnit: Unit)` SHALL be typed as
returning `Self`, and `toBaseUnits()` as returning `bigint`. Constructing a token whose `unit` is not a
`NativeTokenUnit` SHALL throw a `TypeError`.

#### Scenario: Conversion keeps the concrete type

- **WHEN** a consumer calls `to()` on an instance of a concrete token subclass
- **THEN** the result is typed and created as that same concrete subclass

### Requirement: Exact conversion

`toBaseUnits()` SHALL return `amount * unit.baseUnitFactor` exactly. `to(targetUnit)` SHALL return a new token whose
amount is `toBaseUnits() / targetUnit.baseUnitFactor` when that division has no remainder. When the division has a
remainder, `to()` SHALL throw a `RangeError` and SHALL NOT round or truncate. When the converted amount is outside the
int64 range, `to()` SHALL throw a `RangeError`.

#### Scenario: Conversion to a smaller unit

- **WHEN** an amount of `2n` in a unit with factor `100000000n` is converted to a unit with factor `1n`
- **THEN** the result has amount `200000000n` in the target unit

#### Scenario: Exact conversion to a larger unit

- **WHEN** an amount of `300n` in a unit with factor `1n` is converted to a unit with factor `100n`
- **THEN** the result has amount `3n` in the target unit

#### Scenario: Inexact conversion is rejected

- **WHEN** an amount of `150n` in a unit with factor `1n` is converted to a unit with factor `100n`
- **THEN** `to()` throws a `RangeError` and no token is returned
