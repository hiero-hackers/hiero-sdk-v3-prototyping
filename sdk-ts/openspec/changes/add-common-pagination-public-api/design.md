## Source Reference

- [Common proposal](../../../../openspec/changes/1-add-common-pagination-public-api/proposal.md)
- [Common specification](../../../../openspec/changes/1-add-common-pagination-public-api/spec.md)
- Source revision: `1d0e998933b81136b491866bcddc467ced77ddd6`

## Context

This is the first TypeScript code in the repository. The change must add only the pagination contract and the minimum
build support required to compile, document, and verify it.

## Package Structure

`sdk-ts/` is an npm workspace root (private, not published). The public contract lives in one workspace package,
`sdk-ts/hiero-sdk-base-api` (`@hiero-ledger/sdk-base-api`), mirroring the Java `hiero-sdk-base-api` module.

- ESM only (`"type": "module"`), targeting Node.js 20+ and ES2022.
- `tsconfig` with `strict`, `exactOptionalPropertyTypes`, `noUncheckedIndexedAccess`, declaration output, and
  `isolatedDeclarations`-compatible explicit types.
- Sources in `src/`, tests in `test/`. Only `dist/` is published, through a single `exports` entry with types.
- `dependencies` is empty. TypeScript and Vitest are `devDependencies` only.
- npm is used because it ships with Node.js and no package manager is established in the repository yet.

## Exported Contract vs. Implementation

The package exports exactly `Page` and `MirrorNodeError` from `src/index.ts`. No HTTP, JSON, Mirror Node client, or
concrete page type is exported or depended upon; those belong to later implementation packages that extend `Page<T>`.

## Page Abstraction

`Page<T>` is an `abstract class`, not an `interface`, so the shared state, validation, and snapshotting are enforced
once rather than re-implemented by every implementation.

- The constructor is `protected` and takes `(data: ReadonlyArray<T>, size: number, pageIndex: number)`.
- `data` is validated with `Array.isArray` (otherwise `TypeError`). `size` and `pageIndex` are validated as int32
  integers (otherwise `RangeError`); this is the TypeScript mapping of the common `int32` type, not a new constraint.
- `data` is copied and frozen with `Object.freeze([...data])`, so later changes to the source array are not visible and
  runtime mutation throws in strict mode.
- State is held in ECMAScript private fields (`#data`, `#size`, `#pageIndex`) and exposed through getter-only accessors,
  so neither callers nor subclasses can reassign it. TypeScript has no `final` modifier; a subclass could still declare
  an overriding accessor, which is documented as unsupported.
- Elements are not checked for `null`; nullability of elements is expressed by the type argument `T`.

## Navigation

`hasNext()` and `isFirst()` are abstract synchronous methods returning `boolean`. `next()` and `first()` are abstract
methods returning `Promise<Page<T>>`. The base class contains no retrieval logic.

## Error Mapping

`MirrorNodeError extends Error` with `name = "MirrorNodeError"` and a constructor `(message: string, options?:
ErrorOptions)` so the standard ES2022 `cause` is preserved. Navigation failures reject the returned promise with it.

## Verification Approach

- Runtime contract tests (Vitest) for validation, snapshotting, freezing, position inspection, and resolved and rejected
  navigation using a test subclass.
- Type-level tests (`expectTypeOf`) for readonly data, accessor types, and `Promise<Page<T>>` return types.
- A consumer-compilation check that compiles a consumer file importing the package by its published name against the
  built `dist/` declarations, including `@ts-expect-error` assertions that mutation and direct construction are
  rejected.
- A package-boundary test that asserts no runtime dependencies and only the expected exports.
