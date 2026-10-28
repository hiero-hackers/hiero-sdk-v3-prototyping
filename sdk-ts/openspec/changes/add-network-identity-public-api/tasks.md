## 1. Public API and Internal Implementation

- [ ] 1.1 Define `Network<Unit extends NativeTokenUnit>` interface and `createNetwork` factory in `src/base/ledger.ts`.
- [ ] 1.2 Implement `NetworkImpl` in `src/base/internal/ledger-impl.ts` with defensive copy on construction and on every `id` read, null validation for `name`, and `Object.freeze()`.
- [ ] 1.3 Export `Network` and `createNetwork` from `src/base/index.ts` and `src/index.ts`.

## 2. Contract Tests and Verification

- [ ] 2.1 Verify public-type shapes and generics in `tests/contract/PublicApiShape.test.ts`.
- [ ] 2.2 Verify immutability — input `id` mutation does not affect stored state in `tests/contract/NetworkContract.test.ts`.
- [ ] 2.3 Verify immutability — output `id` mutation does not affect stored state in `tests/contract/NetworkContract.test.ts`.
- [ ] 2.4 Verify `name: null` when no name is provided in `tests/contract/NetworkContract.test.ts`.
- [ ] 2.5 Verify `createNetwork` rejects `undefined` as a name value in `tests/contract/NetworkContract.test.ts`.
- [ ] 2.6 Run full TypeScript type-checking (`tsc --noEmit`) and contract test suite (`npm test`).
