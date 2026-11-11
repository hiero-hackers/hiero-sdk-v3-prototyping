## 1. Workspace and Tooling Setup

- [x] 1.1 Create minimal `package.json` with ESM configuration, TypeScript, and Vitest dependencies.
- [x] 1.2 Create `tsconfig.json` with strict mode enabled and ES module resolution.
- [x] 1.3 Verify build and test runners execute cleanly with `npm test`.

## 2. Public API and Internal Implementation

- [x] 2.1 Define public interfaces `NativeTokenUnit` and `Network<Unit extends NativeTokenUnit>` in `src/base/ledger.ts` and `src/base/nativeToken.ts`.
- [x] 2.2 Implement internal `NetworkImpl` in `src/base/internal/ledger-impl.ts` enforcing defensive copying on construction and access, non-undefined validation, and `Object.freeze()`.
- [x] 2.3 Provide a public factory function `createNetwork` in `src/base/ledger.ts` and export all public types from `src/base/index.ts` and `src/index.ts`.

## 3. Contract Tests and Verification

- [x] 3.1 Verify public-type shapes, generics, and compiler compatibility in consumer code (`tests/contract/PublicApiShape.test.ts`).
- [x] 3.2 Verify immutability and defensive copying against input mutation and output mutation (`tests/contract/NetworkContract.test.ts`).
- [x] 3.3 Verify null handling (validating `null` for absence and rejecting `undefined`) in `tests/contract/NetworkContract.test.ts`.
- [x] 3.4 Verify synchronous accessor behavior and contract traceability to the common specification.
- [x] 3.5 Run full TypeScript type-checking (`tsc --noEmit`) and contract test suite (`npm test`).
