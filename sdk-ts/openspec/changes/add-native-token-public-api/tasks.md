## 1. Public API and Core Implementation

- [x] 1.1 Define `NativeTokenUnit` interface and abstract class `NativeToken<Self, Unit>` in `src/base/nativeToken.ts`.
- [x] 1.2 Implement immutable state accessors, base unit calculation (`toBaseUnits()`), and unit conversion math (`to(targetUnit)`) in `NativeToken`.
- [x] 1.3 Implement validation checks (non-null/undefined, integer verification, positive factor, freeze) in `NativeToken`.
- [x] 1.4 Provide internal `GenericNativeToken` in `src/base/internal/nativeToken-impl.ts` and export public types from `src/base/index.ts` and `src/index.ts`.

## 2. Contract Tests and Verification

- [x] 2.1 Verify public-type shapes, generics, subclass inheritance, and consumer compilation in `tests/contract/NativeTokenPublicApiShape.test.ts`.
- [x] 2.2 Verify immutability of unit state and token state in `tests/contract/NativeTokenContract.test.ts`.
- [x] 2.3 Verify unit conversion math between multiple units and concrete return types in `tests/contract/NativeTokenContract.test.ts`.
- [x] 2.4 Verify base unit calculation across positive, zero, and negative values in `tests/contract/NativeTokenContract.test.ts`.
- [x] 2.5 Verify runtime validation (rejecting null/undefined, non-integers, non-positive unit factors) in `tests/contract/NativeTokenContract.test.ts`.
- [x] 2.6 Run full TypeScript type-checking (`tsc --noEmit`) and contract test suite (`npm test`).
