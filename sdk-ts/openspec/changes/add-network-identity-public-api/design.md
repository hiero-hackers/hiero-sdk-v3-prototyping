## Source Reference

- [Common proposal](../../../../openspec/changes/add-network-identity-public-api/proposal.md)
- [Common specification](../../../../openspec/changes/add-network-identity-public-api/spec.md)
- Source revision: `bd1c16017837047432bf7cf2aa44789769a14e29`

## Context

`@hiero-ledger/sdk-base-api` already exports `NativeTokenUnit` (from `add-native-token-public-api`). This change adds one
exported class to the same package and no dependencies. Type mappings follow `guidelines/api-best-practices-js.md`:
`bytes` → `Uint8Array`, `@@nullable` → `T | null`, and `undefined` is never accepted.

## Decisions

### `Network<Unit>` is a concrete class

The common schema declares `Network` as a concrete type (not an `abstraction`), so it is exported as a non-abstract
class with a public constructor `(id: Uint8Array, name: string | null, nativeTokenUnit: Unit)`. Parameters follow the
schema's field order. State is held in ECMAScript private fields behind getter-only accessors, matching `Page` and
`NativeToken`. TypeScript has no `final`; subclassing is possible but not part of the contract.

### Id bytes are copied in and out

A non-empty `Uint8Array` cannot be frozen (`Object.freeze` throws), and there is no built-in read-only typed-array
type. The constructor stores `new Uint8Array(id)` (which also normalizes subclasses such as Node.js `Buffer` to a plain
`Uint8Array`), and the `id` getter returns `this.#id.slice()`. The trade-off is that `network.id !== network.id`;
callers compare contents, not references. Ids are small, so the copy cost is negligible.

### Validation

- `id` must be a `Uint8Array` (`TypeError`). Empty ids are accepted; the common spec adds no length rule.
- `name` must be a string or `null` (`TypeError`, including for `undefined`). Empty names are accepted.
- `nativeTokenUnit` must be an `instanceof NativeTokenUnit` (`TypeError`).

### Wildcard `Network<ANY>`

`Unit` only appears in output positions, so TypeScript treats `Network` as covariant in `Unit`: `Network<HbarUnit>` is
assignable to `Network<NativeTokenUnit>`. Later APIs taking the common `Network<ANY>` (for example
`validateChecksum`) use `Network<NativeTokenUnit>`. The type-level tests pin this so a future change does not
accidentally make `Unit` invariant.

### Exported contract

`src/index.ts` adds `Network`. The package-boundary test's expected export list is extended accordingly.

## Verification Approach

- Runtime tests for construction, state reads, defensive copying in both directions, and every validation rule.
- Type-level tests for accessor types, `string | null`, read-only accessors, and wildcard assignability.
- The consumer-compilation check constructs and reads a network through the published entry point.
- The package-boundary test asserts the extended export list and no runtime dependencies.
