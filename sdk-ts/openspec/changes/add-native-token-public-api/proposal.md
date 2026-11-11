## Why

SDK repositories need a unified, language-neutral contract for native token amounts, units, and conversions before building currency math and transaction fee calculations on top of them. This proposal adapts the common `add-native-token-public-api` change to provide an idiomatic, strongly-typed TypeScript representation for `NativeTokenUnit` and `NativeToken<Self, Unit>`.

## Source Reference

- Common proposal: `../../openspec/changes/add-native-token-public-api/proposal.md`
- Common spec: `../../openspec/changes/add-native-token-public-api/spec.md`
- Source revision: `65dded267c95d003d444d3e3f897c9e04b8dcd6d`

## What Changes

- Define `NativeTokenUnit` interface representing a unit of a native token, storing its display symbol and `baseUnitFactor`.
- Define `NativeToken<Self, Unit>` abstraction with immutable `amount` (`bigint`) and `unit` (`Unit`).
- Implement unit conversion via `to(targetUnit: Unit): Self` that returns the concrete token type.
- Implement base unit retrieval via `toBaseUnits(): bigint`.
- Provide an abstract base class `NativeToken` (and internal factory support) that enforces immutability, `Object.freeze()`, and non-null validation.
- Export public contracts and types from `src/base/index.ts` and `src/index.ts`.

## Capabilities

### New Capabilities

- `native-token`: Defines the TypeScript public API contracts for native token units, token amounts, and unit conversion math.

### Modified Capabilities

None.

## Impact

- Adds `NativeToken` abstraction and strengthens `NativeTokenUnit` in `src/base/nativeToken.ts`.
- Exports native token contracts from `src/base/index.ts`.
- Defers concrete token implementations (like `Hbar` in change 13) and `ExchangeRate` (in change 3) to subsequent changes.
