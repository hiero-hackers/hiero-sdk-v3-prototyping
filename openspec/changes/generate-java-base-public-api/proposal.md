## Why

The language-neutral contracts under `spec/base` currently have no buildable Java API artifact that implementation
modules can compile against. Establishing the public contract first lets the project review and stabilize the Java API
independently, while leaving operational implementation to later contributors and modules.

## What Changes

- Add a self-contained Java 21 workspace under `java/`, with a public API artifact covering every namespace and
  declaration under `spec/base`.
- Translate the meta-language into idiomatic Java contracts, immutable structural value types, enums, exceptions,
  constants, and module/package documentation.
- Permit implementation only where required to preserve a value's validity, immutability, equality, and deterministic
  field-derived representation.
- Add a reviewed source-to-API mapping matrix, API-shape tests, and consumer compilation tests.
- Keep the directly maintained public API separate from future implementation/provider modules.
- Defer operational behavior—including networking, cryptography, signing, verification, parsing, serialization,
  persistence, mutable registries, retries, scheduling, provider selection, and service logic—to later changes.
- Preserve unresolved `Questions & Comments` from the source specifications rather than choosing answers during API
  translation.

## Capabilities

### New Capabilities

- `java-base-public-api`: Defines the Java public contract for all `spec/base` namespaces, its limited
  structural value behavior, and its conformance guarantees.

### Modified Capabilities

None.

## Impact

- Adds the directly maintained API module, contract-test module, build wrapper, and Java-specific documentation under
  `java/`; no specification parser or code generator is introduced and no Java deliverable is placed at repository
  root.
- Establishes `spec/base/*.md`, `guidelines/api-guideline.md`, and `guidelines/api-best-practices-java.md` as the
  authoritative inputs for reviewing and maintaining the Java base API.
- Creates the contract that later Java implementation/provider modules will implement without exposing their internal
  types through the public API.
- Introduces a Java 21 build baseline and build/test tooling, but no operational SDK or network implementation.
