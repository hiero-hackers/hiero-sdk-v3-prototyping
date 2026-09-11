## Context

We are implementing the remaining files from `spec/base/` in pure TS (`keys.md`, `native-token.md`, `common.md`, `token.md`). See proposal.

## Goals / Non-Goals

**Goals:**
- Provide 1:1 mappings of the base spec primitives in TS.
- Strictly separate public API definitions from internal private implementations.

**Non-Goals:**
- Implementing actual cryptographic signing logic for keys. We only need the type hierarchies and signatures to match the spec.

## Decisions

### 1. Cryptographic Key Hierarchy
**Decision:** `PrivateKey` and `PublicKey` will be modeled as `abstract` classes or discriminated unions, depending on how `keys.md` models algorithms (ED25519 vs ECDSA). We will favor TS discriminated unions where `@@sealed` is specified.

### 2. Encapsulation and Private Implementation
**Decision:** Private implementations must remain completely separate and non-exportable. Only the public interfaces and types mandated by the meta-language will be exposed via the module's `index.ts`. Internal implementation details will be encapsulated (e.g., using `#private` fields in classes) and internal module paths will not be accessible through the package's exports map.

## Risks / Trade-offs
- No major risks; this is a pure data-modeling exercise following strict guidelines.
