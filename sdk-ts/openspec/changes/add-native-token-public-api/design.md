## Context

The Hiero SDK V3 architecture establishes a clean separation between public API contracts and internal implementations. Common feature specifications define cross-SDK semantics, while language-specific SDKs adapt them into idiomatic constructs. For TypeScript, this requires clean ES modules, strong typing with generics (`NativeToken<Self, Unit>`), immutable state enforced via `Object.freeze()`, and accurate 64-bit integer mathematics using JavaScript `bigint`.

See `proposal.md` for motivation and source reference.

## Goals / Non-Goals

**Goals:**
- Provide `NativeTokenUnit` as a public interface and `NativeToken<Self, Unit>` as a public abstract class in `src/base/nativeToken.ts`.
- Implement `amount` and `toBaseUnits()` as `bigint` to prevent precision loss for 64-bit token values.
- Implement unit conversion `to(targetUnit: Unit): Self` that returns the exact concrete subtype.
- Enforce runtime validation: non-null and non-undefined inputs, positive base unit factors, integer check for numeric amounts, and immutable freeze.
- Export all public types and classes from `src/base/index.ts` and `src/index.ts`.

**Non-Goals:**
- Implement `Hbar` or specific Hedera denomination enums (deferred to change 13: `add-hbar-public-api`).
- Implement `ExchangeRate` (deferred to change 3: `add-exchange-rate-public-api`).
- Introduce external big-number or decimal math libraries for integer base units.

## Decisions

### 1. `NativeToken` as an Abstract Class
- **Decision:** Expose `NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit>` as an abstract class that implements common properties (`amount`, `unit`), `toBaseUnits()`, and `to(targetUnit: Unit)`.
- **Rationale:** Concrete token types (e.g. `Hbar extends NativeToken<Hbar, HbarUnit>`) inherit these standard behaviors directly without boilerplate duplication, matching the meta-language specification where `Hbar` inherits `amount`, `unit`, and `to(...)`.
- **Alternatives Considered:** Making `NativeToken` purely an `interface`. This would require every concrete token class in downstream changes to reimplement the conversion math and base unit calculation.

### 2. Concrete Type Return via `create` Template Method
- **Decision:** The abstract `NativeToken` class declares a protected abstract method `create(amount: bigint, unit: Unit): Self` which is invoked by `to(targetUnit)`.
- **Rationale:** Ensures type-safety in TypeScript so that calling `hbar.to(HbarUnit.TINYBAR)` returns an `Hbar` instance rather than a generic or abstract type.
- **Alternatives Considered:** Relying on `new (this.constructor as any)(amount, unit)`. Rejected because constructor signatures in subclasses might differ or be private.

### 3. Representation of 64-bit Integers (`int64`)
- **Decision:** Use `bigint` for the internal stored `amount` and the return value of `toBaseUnits()`. Allow constructors to accept `bigint | number` (validating that `number` is a safe integer).
- **Rationale:** In Hedera, 50 billion Hbar is 5 * 10^18 tinybars, which exceeds JavaScript's safe integer limit (`2^53 - 1` ≈ `9 * 10^15`). `bigint` handles arbitrary precision integer arithmetic without truncation or floating-point errors.
- **Alternatives Considered:** `number` only. Rejected due to precision loss beyond 2^53.

### 4. Mathematical Conversion and Truncation
- **Decision:** Convert to base units first (`amount * unit.baseUnitFactor`), then divide by `targetUnit.baseUnitFactor` using integer division (`/` on `bigint`).
- **Rationale:** `int64` in the meta-language specifies integer arithmetic. Integer division naturally performs floor division for positive values.

## Risks / Trade-offs

- **[BigInt serialization in JSON]** → `bigint` cannot be serialized by `JSON.stringify` directly. Since `NativeToken` instances are domain entities, serialization will be handled by dedicated serializers in protobuf / transport layers.
