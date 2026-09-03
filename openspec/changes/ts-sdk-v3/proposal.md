## Why

The current V2 TypeScript SDK (`temp/hiero-sdk-js`) relies on JSDoc-annotated JavaScript, mixes concerns, and carries legacy baggage that deviates from the clean architecture defined for V3. We need a pure, ground-up TypeScript implementation of the V3 SDK that strictly adheres to the language-agnostic meta-language in the `spec/` folder and the rules in the `guidelines/` folder, ensuring consistency, maintainability, and strong type safety.

## What Changes

- **Pure TypeScript implementation**: A fresh codebase written purely in TS, avoiding the JSDoc-based JS structure of V2.
- **Meta-language alignment**: Strict TS mappings for meta-language constructs like `@@sealed` (discriminated unions), `streamResult<T>`, and `@@threadSafe`.
- **Modular architecture**: The package structure will mirror the logical separation found in the `spec/` folder (e.g., `base`, `consensus-node-client`, `mirror-node-client`).
- **Elimination of bad practices**: Avoid legacy patterns from V2, such as relying on implicit types, weak nullability, or leaking transport details into the API.

## Capabilities

### New Capabilities
- `ts-sdk/mappings`: Requirements and rules for mapping the language-agnostic meta-language to idiomatic TypeScript, acting as the missing `api-best-practices-ts.md`.

### Modified Capabilities

## Impact

- Introduces a new, strongly-typed TypeScript SDK for Hiero.
- Does not modify existing Java/Rust SDKs or the core meta-language specs, but provides the reference implementation for TypeScript developers.
