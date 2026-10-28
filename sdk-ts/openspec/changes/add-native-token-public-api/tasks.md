## 1. TypeScript Project Setup

- [x] 1.1 Create `package.json` with ESM configuration, TypeScript, and Vitest dependencies.
- [x] 1.2 Create `tsconfig.json` with strict mode enabled and ES module resolution.
- [x] 1.3 Create `vitest.config.ts` and verify `npm test` runs cleanly.

## 2. Public API and Core Implementation

- [x] 2.1 Define `NativeTokenUnit` interface and abstract class `NativeToken<Self, Unit>` in `src/base/nativeToken.ts`.
- [x] 2.2 Implement immutable state accessors, `toBaseUnits()`, and `to(targetUnit)` conversion math in `NativeToken`.
- [x] 2.3 Implement constructor validation (non-null/undefined, integer check, positive factor, `Object.freeze()`).
- [x] 2.4 Provide internal `GenericNativeToken<Unit>` in `src/base/internal/nativeToken-impl.ts` and export public types from `src/base/index.ts` and `src/index.ts`.

## 3. Contract Tests and Verification

- [x] 3.1 Verify public-type shapes, generics, and subclass inheritance in `tests/contract/NativeTokenPublicApiShape.test.ts`.
- [x] 3.2 Verify immutability of unit state and token state in `tests/contract/NativeTokenContract.test.ts`.
- [x] 3.3 Verify unit conversion math between multiple units in `tests/contract/NativeTokenContract.test.ts`.
- [x] 3.4 Verify `toBaseUnits()` for positive, zero, and values exceeding `Number.MAX_SAFE_INTEGER` in `tests/contract/NativeTokenContract.test.ts`.
- [x] 3.5 Verify runtime validation (rejecting null/undefined, non-integers, non-positive unit factors) in `tests/contract/NativeTokenContract.test.ts`.
- [x] 3.6 Run full TypeScript type-checking (`tsc --noEmit`) and contract test suite (`npm test`).
