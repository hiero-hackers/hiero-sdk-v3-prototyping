## 1. Project Scaffolding

- [x] 1.1 Initialize a new TS project (e.g., `hiero-sdk-v3-ts`) outside of the old `temp/hiero-sdk-js` and configure `tsconfig.json` for strict mode. Verify that `tsc --noEmit` succeeds.
- [x] 1.2 Set up `package.json` with conditional exports pointing to `/base`, `/consensus`, etc., mirroring the spec namespaces. Verify `npm install` completes successfully.
- [x] 1.3 Add testing infrastructure (e.g. Vitest/Jest) and ESLint/Prettier. Verify a dummy test passes and formatting works.

## 2. Core Meta-Language Primitives

- [x] 2.1 Implement `StreamItem<T, E>` discriminated union in the base namespace to map the `streamResult<T>` meta-type. Verify by writing type assertions that prove exhaustiveness.
- [x] 2.2 Implement custom data types (e.g., specific ID or alias wrappers, `BigInt` numeric conversions) for the TS environment. Verify with unit tests showing successful parsing of large integers without precision loss.

## 3. Foundation (Ledger & Keys)

- [x] 3.1 Implement the `BaseAddress` hierarchy (`Address`, `ContractId`, `AccountId`) as TS classes/interfaces reflecting the `@@sealed` logic. Verify tests demonstrate pattern matching across the discriminated types.
- [x] 3.2 Implement the `Authority` sum types (`PublicKeyAuthority`, `ContractAuthority`, `AuthorityList`). Verify the recursive type structures can be safely traversed and matched via exhaustiveness tests.

## 4. Documentation & Parity Review

- [x] 4.1 Cross-reference the new implementations against `api-guideline.md` and our `ts-sdk/mappings` spec to ensure zero regressions or legacy V2 patterns. Verify by running an architectural linter or peer reviewing the codebase against the spec files.
