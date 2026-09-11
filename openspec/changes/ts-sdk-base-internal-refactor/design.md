## Context

The `base` namespace currently mixes public interfaces and private concrete classes in the same files or adjacent folders (like `crypto/`). 

## Goals / Non-Goals

**Goals:**
- Move concrete classes (`DefaultHttpClient`, `Ed25519PrivateKey`, etc.) to `src/base/internal/`.
- Ensure that `src/base/index.ts` only exports public types and factory methods, absolutely no internal paths.

**Non-Goals:**
- Altering the behavior of the HTTP or Cryptography implementations.

## Decisions

### 1. Internal Directory Structure
**Decision:** We will create `src/base/internal/` and mirror the domain structure within it (e.g., `src/base/internal/crypto`, `src/base/internal/http`). This keeps the internal implementations organized while hiding them from the public surface.
