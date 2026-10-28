## Purpose

TypeScript contract mapping for the language-neutral `add-native-token-public-api` specification.
All behavioral requirements from the common spec are preserved; this document adds only TypeScript-specific
type mappings and constraints.

## Type Mappings

| Common type | TypeScript type | Notes |
|-------------|-----------------|-------|
| `int64`     | `bigint`        | Required: token amounts can exceed `Number.MAX_SAFE_INTEGER` (e.g., 50B HBAR = 5×10¹⁸ tinybars) |
| `string`    | `string`        | No change |
| `$$Self`    | generic `Self extends NativeToken<Self, Unit>` | Return-type self-reference |
| `$$Unit`    | generic `Unit extends NativeTokenUnit` | Unit type constraint |

## ADDED Requirements

### Requirement: Immutable native token unit state

The SDK SHALL expose a `NativeTokenUnit` interface with readonly `symbol: string` and `baseUnitFactor: bigint`.

#### Scenario: Unit state is observed

- **Given** a defined `NativeTokenUnit`.
- **When** a caller reads `symbol` and `baseUnitFactor`.
- **Then** the values are returned as immutable `string` and `bigint` respectively.

### Requirement: Immutable native token amount

The SDK SHALL expose a generic abstract class `NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit>` with readonly `amount: bigint` and `unit: Unit`.

#### Scenario: Token amount state is observed

- **Given** an instantiated `NativeToken` subclass.
- **When** a caller reads `amount` and `unit`.
- **Then** the values are returned as immutable `bigint` and `Unit` respectively; neither stored value can be modified externally.

### Requirement: Token unit conversion

The SDK SHALL expose a `to(targetUnit: Unit): Self` method that converts the token amount to a target unit, returning the concrete subtype.

#### Scenario: Token amount is converted to a different unit

- **Given** a token amount expressed in unit A with `baseUnitFactor` F_A.
- **When** `to(unitB)` is called where unitB has `baseUnitFactor` F_B.
- **Then** the returned token has `amount = this.amount * F_A / F_B` in unit B, as a `bigint` (integer division).

### Requirement: Base unit retrieval

The SDK SHALL expose a `toBaseUnits(): bigint` method that returns `amount * unit.baseUnitFactor`.

#### Scenario: Base units are calculated

- **Given** a token amount with `amount` A and `unit.baseUnitFactor` F.
- **When** `toBaseUnits()` is called.
- **Then** the result is `A * F` as a `bigint`.

### Requirement: Runtime validation

The SDK SHALL validate constructor arguments and reject invalid inputs with a thrown error.

#### Scenario: Null or undefined amount is rejected

- **Given** a call to construct a native token with a `null` or `undefined` amount.
- **When** the constructor runs.
- **Then** an error is thrown and no instance is created.

#### Scenario: Null or undefined unit is rejected

- **Given** a call to construct a native token with a `null` or `undefined` unit.
- **When** the constructor runs.
- **Then** an error is thrown and no instance is created.

#### Scenario: Non-integer amount is rejected

- **Given** a call to construct a native token with a non-bigint amount (e.g., a `number`).
- **When** the constructor runs.
- **Then** an error is thrown and no instance is created.

#### Scenario: Non-positive baseUnitFactor is rejected

- **Given** a unit whose `baseUnitFactor` is zero or negative.
- **When** a native token is constructed with that unit.
- **Then** an error is thrown and no instance is created.

#### Scenario: Instance is frozen after construction

- **Given** a successfully constructed native token.
- **When** a caller attempts to reassign any property.
- **Then** the assignment fails (strict mode throws, non-strict is a no-op).
