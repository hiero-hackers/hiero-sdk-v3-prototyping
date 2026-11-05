## 1. TypeScript Project Setup

- [x] 1.1 Create the npm workspace root in `sdk-ts/` and the `hiero-sdk-base-api` package with ESM output and a single
  typed `exports` entry.
- [x] 1.2 Configure strict TypeScript compilation with declaration output and no runtime dependencies.
- [x] 1.3 Configure Vitest as a dev-only dependency for runtime and type-level tests.

## 2. Public Pagination Contract

- [x] 2.1 Add the abstract `Page<T>` class with validated, frozen, read-only state, position methods, and
  `Promise`-based navigation methods.
- [x] 2.2 Add the exported `MirrorNodeError` class with message and `cause` support.
- [x] 2.3 Export only `Page` and `MirrorNodeError` from the package entry point.
- [x] 2.4 Document every exported type, constructor, accessor, and method with TSDoc.

## 3. Contract Tests

- [x] 3.1 Verify invalid-input handling, defensive copying, and runtime immutability of page data.
- [x] 3.2 Verify public types: readonly data, accessor types, and `Promise<Page<T>>` navigation return types.
- [x] 3.3 Verify synchronous position inspection and resolved and rejected navigation using a test subclass.
- [x] 3.4 Verify through a consumer-compilation check that consumers can extend `Page<T>` and use `MirrorNodeError`
  through the published package entry point.

## 4. Final Verification

- [x] 4.1 Run type checking, build, contract tests, and consumer compilation.
- [x] 4.2 Verify that the package has no runtime dependencies and exports no implementation or transport types.
- [x] 4.3 Verify traceability to the linked common proposal, common specification, and recorded source revision.
