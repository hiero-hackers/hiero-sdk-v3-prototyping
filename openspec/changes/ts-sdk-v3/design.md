## Context

This design outlines the technical approach to building the V3 TypeScript SDK, pivoting away from the older JSDoc-based JS structure used in `temp/hiero-sdk-js`. See `proposal.md` for the overarching motivation. We will construct a new architecture mapping cleanly to the `spec/` structure.

## Goals / Non-Goals

**Goals:**
- Implement the base primitives (Ledger, Keys, NativeToken) in pure TypeScript.
- Define a project architecture that scales to support the consensus and mirror node specs without muddying dependency trees.
- Set up strict compilation rules (strict mode, ESLint, Prettier) to enforce pure TS.

**Non-Goals:**
- Automatically generating TypeScript code from the Markdown specs using AST parsing. We will hand-write the implementation to ensure idiomatic code quality.
- Porting all legacy helper functions from `temp/hiero-sdk-js` that don't fit the new meta-language schema.

## Decisions

### 1. Monorepo vs. Single Package
**Decision:** We will use a single package but export paths using the `exports` field in `package.json` to mirror the namespaces (e.g., `@hiero/sdk/base`, `@hiero/sdk/consensus`).
**Rationale:** A single package is easier to publish and consume for a unified SDK, but explicit exports prevent monolithic imports and align with the namespace requirements in `api-best-practices-js.md`.
**Alternatives:** pnpm workspaces (monorepo). Rejected for now because it adds overhead for users who just want to install one SDK package.

### 2. Handling `streamResult<T>`
**Decision:** We will use a standard discriminated union wrapper: `type StreamItem<T, E> = { ok: true, value: T } | { ok: false, error: E }` and return an `AsyncIterable<StreamItem<T, E>>` from `@@streaming` methods.
**Rationale:** This maps perfectly to TS discriminated unions and allows consumers to use standard `for await` loops without throwing non-terminal exceptions.
**Alternatives:** Yielding values and throwing exceptions. Rejected because `@@streaming` spec strictly separates terminal and non-terminal errors.

## Risks / Trade-offs

- **Risk:** Type conversions between `BigInt` and `number` in legacy browser environments might cause compatibility issues. -> **Mitigation:** Enforce a modern target (ES2022+) as mandated by `api-best-practices-js.md` and document the `BigInt` requirement clearly.
