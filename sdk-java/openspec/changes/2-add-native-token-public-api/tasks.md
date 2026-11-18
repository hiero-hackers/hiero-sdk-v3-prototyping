## 1. Contract Mapping and Source Traceability

The separate design artifact is intentionally omitted, following the current workflow. Use the proposal,
specification, and existing Java conventions as implementation inputs. Keep `proposal.md` and `spec.md` unchanged;
record Java mappings and source traceability in `sdk-java/README.md`.

- [x] 1.1 Record links and the source revision for the shared proposal and specification under repository-root `openspec/changes/2-add-native-token-public-api/`, and verify that the Java copies retain the shared contract.
- [x] 1.2 Document Java mappings for the native-token namespace, immutable attributes, recursive self/unit type bounds, accessors, required inputs, and conversion signatures in the Java README; preserve compatibility with the existing API module and future unit providers.
- [x] 1.3 Document the public-contract-only boundary: keep concrete conversion arithmetic, token-specific implementations, exchange rates, and network behavior deferred. Record that fractional-result, overflow, and unit-factor validation policies require a shared contract decision before implementing production arithmetic; do not silently select rounding, truncation, saturation, or new numeric constraints.

## 2. Public Native Token Contract

- [x] 2.1 Reuse the existing Java 21 Maven module, source/test layout, JSpecify dependency, and JUnit Jupiter/AssertJ setup without adding transport dependencies or regenerating unrelated pagination code.
- [x] 2.2 Add the `NativeTokenUnit` abstraction exposing immutable `String` symbol and primitive `long` base-unit factor values, with no mutation API; keep its Java representation compatible with future enum-based unit providers.
- [x] 2.3 Add the `NativeToken<SELF, UNIT>` abstraction with the source specification's recursive bounds, immutable primitive `long` amount and typed unit state, and read-only accessors; use constructor-owned state validation where applicable.
- [x] 2.4 Expose provider-defined `to(UNIT targetUnit)` returning exactly `SELF` and `long toBaseUnits()` contracts, preserving the associated unit type and leaving production conversion arithmetic abstract rather than providing placeholder results.
- [x] 2.5 Apply explicit JSpecify nullability, enforce required constructor inputs with descriptive null checks where the API owns construction, document provider obligations for other required inputs, export only the public namespace, and add Javadoc for every public API element.

## 3. Contract and Consumer Tests

- [x] 3.1 Cover "Unit state is observed": use a minimal unit provider to verify symbol and factor access, immutable stored values, and the absence of public mutation methods.
- [x] 3.2 Cover "Token amount is observed": verify the stored amount, associated typed unit, and immutable state; include zero and representative signed 64-bit values without inventing amount bounds.
- [x] 3.3 Cover "Token amount is converted to a different unit": use a small test provider with exactly representable conversion examples; verify the returned concrete token type, target unit, expected amount, and unchanged source state. Keep this explicitly a provider-contract test, not evidence that production arithmetic is implemented.
- [x] 3.4 Cover "Base units are calculated": use exactly representable test-provider examples to verify primitive `long` results in the smallest unit, including an already-base-unit amount, without choosing unspecified rounding or overflow policies.
- [x] 3.5 Verify API shape, recursive generic bounds, constructor visibility, immutable fields/accessors, primitive `long` mappings, required-input checks, nullability annotations, and the absence of implementation types in public signatures.
- [x] 3.6 Compile and run an independent named-module consumer with test token/unit providers; verify concrete self-type preservation, rejection of unrelated unit types by the compiler, and execution without annotation, test, transport, or implementation dependencies at runtime.

## 4. Final Verification and Documentation

- [x] 4.1 Run the complete Maven verification and checked Javadoc generation on Java 21; confirm native-token tests and existing pagination tests pass without regressions.
- [x] 4.2 Map all four shared scenarios to passing tests in the Java README, and update public API usage and deferred-work documentation without editing proposal/spec files or creating a design artifact.
- [x] 4.3 Inspect the public JAR, dependency graph, JPMS descriptor, and existing SBOM output for implementation or test leakage; confirm source-revision traceability and that only native-token API types and required support were added.
- [x] 4.4 Run `openspec validate 2-add-native-token-public-api --strict`, review the final diff, and mark tasks complete only after their checks pass. Leave the change unarchived and do not commit or push without a separate request.