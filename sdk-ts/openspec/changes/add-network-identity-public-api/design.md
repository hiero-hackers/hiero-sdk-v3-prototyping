## Context

The Hiero SDK V3 architecture establishes a strict separation between public API contracts and internal implementations. Common feature specifications define cross-SDK semantics, while language-specific SDKs adapt them into idiomatic constructs. For TypeScript, this requires clean ES modules, strong typing with TypeScript generics, immutable state with runtime defensive copying, and no leakage of private classes.

See `proposal.md` for motivation and source reference.

## Goals / Non-Goals

**Goals:**
- Provide `Network<Unit extends NativeTokenUnit>` and `NativeTokenUnit` as public interfaces exported from the package entry point.
- Implement an internal concrete class or factory that enforces runtime immutability and defensive copying for `id` (`Uint8Array`).
- Ensure `name` accepts `string | null` and treats absence intentionally with `null` in adherence to JS/TS API guidelines.
- Set up a clean, minimal TypeScript workspace with compilation (`tsc`) and testing (`vitest`) capabilities.

**Non-Goals:**
- Implement checksum validation (e.g. LedgerId / Address checksums), which depends on subsequent delta changes.
- Implement network connection, gRPC channels, or Mirror Node querying.
- Introduce heavy runtime dependencies for basic types.

## Decisions

### 1. Interface-First Public Surface
- **Decision:** Expose `Network<Unit extends NativeTokenUnit>` as a TypeScript `interface` rather than exporting a concrete class directly. Provide a public creation function or factory `createNetwork(...)` or constructor pattern that returns the interface.
- **Rationale:** Decouples consumer code from internal instantiation logic, matching the project encapsulation guidelines ("keep private implementation separate and non-exportable").
- **Alternatives Considered:** Exporting a concrete class `Network` directly. While simple, it exposes internal class mechanics and makes future SPI / mocking harder.

### 2. Immutability and Defensive Copying of `Uint8Array`
- **Decision:** `Uint8Array` is mutable by default in JavaScript. The internal implementation copies the `id` buffer upon construction (`new Uint8Array(id)`) and upon access via getter, and marks the instance with `Object.freeze()`.
- **Rationale:** Satisfies the scenario requirement that neither mutating the input array nor mutating the returned array affects the stored state of `Network`.
- **Alternatives Considered:** Relying solely on `readonly Uint8Array` TypeScript type modifier. TypeScript `readonly` is compile-time only and does not prevent runtime mutations like `net.id[0] = 0`.

### 3. Null Handling for `name`
- **Decision:** Use `string | null` for `name`. Reject `undefined` in constructors/factories in accordance with `api-best-practices-js.md` ("Never use undefined in public APIs; use null for intentional absence").
- **Rationale:** Preserves consistency across JavaScript and Java SDK nullability models.
- **Alternatives Considered:** Allowing `string | undefined`. Rejected because it violates the repository's strict null-handling guidelines.

### 4. Minimal Project Tooling
- **Decision:** Use standard `package.json` with ES modules (`"type": "module"`), TypeScript (`tsconfig.json`), and Vitest for fast, isolated unit and contract testing.
- **Rationale:** Standard modern Node.js / TS stack, aligning with existing prototyping conventions in the repository.

## Risks / Trade-offs

- **[Performance overhead of defensive copying]** → `id` is a ledger ID (typically small, e.g. 1 to 32 bytes), so copying overhead is negligible compared to the guarantee of immutability.
- **[Separate package workspace setup]** → Setting up `sdk-ts` independently ensures it doesn't conflict with any other modules and matches `sdk-java`.
