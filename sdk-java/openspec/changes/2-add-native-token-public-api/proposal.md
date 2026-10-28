## Why

SDK repositories need one language-neutral contract for native token amounts, units, and conversions before individual
SDKs select their own public type shapes and implementations. Publishing the common proposal and specification
together gives each SDK owner the same reviewed starting point while keeping language-specific OpenSpec workflows
independent.

## What Changes

- Define units of native tokens with their display symbol and relationship to the base unit.
- Define token amounts associated with a specific unit.
- Define conversion of token amounts between different units of the same token.
- Define conversion of token amounts to the base (smallest) unit.
- Require SDK owners to preserve this behavior while selecting idiomatic language-specific representations.

## Capabilities

### New Capabilities

- `native-token`: Defines the shared native token unit, amount, and conversion contract.

### Modified Capabilities

None.

## Impact

- Supplies the common source for corresponding language-specific OpenSpec changes across all SDK implementations.
- Derives the native token delta from `spec/base/native-token.md` without modifying or replacing that source specification.
- Does not prescribe language-specific types, interfaces, numeric representations, or concrete token implementations.
