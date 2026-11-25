## Source Reference

- [Common proposal](../../../../openspec/changes/add-native-token-public-api/proposal.md)
- [Common specification](../../../../openspec/changes/add-native-token-public-api/spec.md)
- Source revision: `65dded267c95d003d444d3e3f897c9e04b8dcd6d`

## Context

The `@hiero-ledger/sdk-base-api` package already exists (from `add-common-pagination-public-api`) with a strict
TypeScript build, Vitest, and a consumer-compilation check. This change adds two exported types to it and no new
dependencies.

## Decisions

### Numeric representation: `bigint`

All common int64 values are `bigint`. `number` is exact only up to 2^53 − 1, which would silently narrow the common
int64 range. Values are validated at runtime with a shared `requireInt64` helper (not exported): non-`bigint` input
throws `TypeError`, out-of-range input throws `RangeError`.

### `NativeTokenUnit` is an abstract class

An abstract class (rather than an interface) lets the base enforce validation once and keeps the state in ECMAScript
private fields behind getter-only accessors, matching `Page<T>`. Concrete units (for example a future `HbarUnit`) extend
it and are typically exposed as `static readonly` instances.

- `protected constructor(symbol: string, baseUnitFactor: bigint)`.
- `symbol` must be a string (`TypeError`); an empty string is allowed because the common spec does not forbid it.
- `baseUnitFactor` must be an int64 (`requireInt64`) and greater than zero (`RangeError`). This is a TypeScript
  constraint chosen because the common spec leaves it open and `to()` divides by the factor.

### `NativeToken<Self, Unit>` is an abstract class with a template method

The F-bounded generic `Self extends NativeToken<Self, Unit>` makes `to()` return the concrete subclass type.

- `protected constructor(amount: bigint, unit: Unit)` validates `amount` with `requireInt64` and `unit` with
  `instanceof NativeTokenUnit` (`TypeError`).
- `toBaseUnits(): bigint` is implemented in the base class as `amount * unit.baseUnitFactor`.
- `to(targetUnit: Unit): Self` is implemented in the base class so every token converts with the same exact semantics.
  It computes the base units, throws `RangeError` when `baseUnits % targetUnit.baseUnitFactor !== 0n`, and otherwise
  calls the subclass factory with the quotient. The quotient is validated as int64 by the new token's constructor.
- Subclasses implement one hook, `protected abstract create(amount: bigint, unit: Unit): Self`, which constructs the
  concrete type. This protected hook is a TypeScript addition needed to build `Self` without reflection; it is not part
  of the public call surface.
- `to()` always returns a new instance, including when converting to the same unit.

### Exported contract vs. implementation

`src/index.ts` adds `NativeToken` and `NativeTokenUnit` exports. `requireInt64` stays internal. The package-boundary
test's expected export list is extended accordingly.

## Open Questions

- `toBaseUnits()` does not check that its result fits in int64. For a large `amount` in a large unit the returned
  `bigint` can exceed `2^63 − 1`, although the common schema types the result as int64. A range check was considered and
  deliberately not added in this change; it should be decided in the common spec.
- Whether units with the same `symbol` and `baseUnitFactor` from different token types should be rejected by `to()` at
  runtime is left to concrete token types.

## Compile-Time Unit Separation

TypeScript is structurally typed. Two `NativeTokenUnit` subclasses that add no members are assignable to each other, so
the `Unit` type parameter alone would not stop a unit of one token being passed to another token's `to()`. Concrete
unit classes SHOULD declare a nominal member (for example a `private readonly` brand field); private members make the
classes mutually incompatible. This is documented on `NativeTokenUnit` and exercised by the type-level tests.

## Verification Approach

- Runtime tests for validation, immutability, exact conversion in both directions, rejection of inexact and
  out-of-range conversions, and creation of the concrete subclass by `to()`.
- Type-level tests for `bigint` accessors, `to()` returning `Self`, and rejection of a foreign unit type.
- The consumer-compilation check defines a concrete token and unit through the published entry point.
- The package-boundary test asserts the extended export list and no runtime dependencies.
