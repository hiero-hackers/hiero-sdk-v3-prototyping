## 1. Contract Mapping and Source Traceability

The separate design artifact is intentionally omitted, following the current workflow. Use the proposal,
specification, and existing Java conventions as implementation inputs. Keep `proposal.md` and `spec.md` unchanged;
record Java mappings and source traceability in `sdk-java/README.md`.

- [x] 1.1 Record links and the source revision for the shared proposal and specification under repository-root `openspec/changes/4-add-network-identity-public-api/`, and verify that the Java copies retain the shared contract.
- [x] 1.2 Document the Java mapping to `org.hiero.sdk.v3.ledger.Network<UNIT extends NativeTokenUnit>`, immutable `byte[] id`, nullable `String name`, and required typed `UNIT nativeTokenUnit`, with read-only `id()`, `name()`, and `nativeTokenUnit()` accessors. Select an immutable value-type representation consistent with Java API conventions and document its construction and equality semantics without treating those mappings as new common requirements.
- [x] 1.3 Confirm reuse of the existing `NativeTokenUnit` contract from `2-add-native-token-public-api`. Keep this change limited to identity storage, defensive copying, and required-input checks; defer predefined network instances, ID assignment rules, checksums, address parsing, node addresses/discovery, network settings, refresh, and transport behavior.

## 2. Public Network Identity Contract

- [x] 2.1 Reuse the existing Java 21 Maven API module, source/test layout, JSpecify dependency, and JUnit Jupiter/AssertJ setup without regenerating unrelated pagination, native-token, or exchange-rate contracts or adding implementation dependencies.
- [x] 2.2 Add the immutable public `Network<UNIT extends NativeTokenUnit>` value type in `org.hiero.sdk.v3.ledger`, storing the supplied ID, optional name, and associated unit with no setters or other mutation API. Preserve the concrete unit type in construction and accessors rather than exposing raw types or erasing it to `NativeTokenUnit`.
- [x] 2.3 Clone the required ID array on construction and on every `id()` call so neither the source array nor any returned array can modify stored network identity. Preserve byte order and contents exactly, including empty arrays and leading zero bytes; do not introduce unspecified ID length, format, or allowed-value restrictions.
- [x] 2.4 Apply `@NullMarked` package conventions and explicit JSpecify nullability. Reject null `id` and `nativeTokenUnit` at construction using descriptive `Objects.requireNonNull` checks; accept and preserve a null name as absence and preserve supplied empty or nonempty names without trimming, normalization, or invented validation. Reuse immutable unit providers without copying or converting their associated unit.
- [x] 2.5 Export only the public ledger package from the existing JPMS module, and document all public API elements, byte-array ownership, nullable-name semantics, and the requirement that unit providers remain immutable. If the chosen representation supplies value equality, keep `equals`, `hashCode`, and `toString` consistent with defensive array ownership and document array-content handling.

## 3. Contract and Consumer Tests

- [x] 3.1 Cover "Network identity state is observed": verify exact ID contents, a supplied name, and the associated concrete native-token unit using a small immutable test unit provider; verify repeated reads preserve stored state and expose no mutation methods.
- [x] 3.2 Verify ID ownership by mutating the original constructor array and arrays obtained from separate accessor calls, then checking that the stored ID remains unchanged. Include empty IDs, leading zeros, and representative byte values without imposing unspecified ID constraints.
- [x] 3.3 Verify that null ID and unit inputs fail with descriptive messages, while absent, empty, and nonempty names are preserved. Verify that constructing or observing a network does not fetch metadata, modify its unit, or access the network.
- [x] 3.4 Verify API shape, generic bounds, immutable state, typed read-only accessors, nullability annotations, package exports, and absence of implementation types in public signatures. Test any documented Java value/equality behavior, including arrays, without introducing a shared network-ID uniqueness or identity policy.
- [x] 3.5 Compile and run an independent named-module consumer against the exported network and native-token APIs; verify typed construction, optional-name handling, and defensive ID access without annotation, test, transport, or implementation libraries at runtime. Include negative compilation checks rejecting non-`NativeTokenUnit` type arguments and assignment to an unrelated concrete unit type.

## 4. Final Verification and Documentation

- [x] 4.1 Run the complete Maven verification and checked Javadoc generation on Java 21; confirm network-identity tests and existing pagination, native-token, and exchange-rate tests pass without regressions.
- [x] 4.2 Map the shared "Network identity state is observed" scenario to passing tests in the Java README, and document usage, Java mappings, source traceability, defensive-copy guarantees, nullable-name behavior, and deferred work without editing proposal/spec files or creating a design artifact.
- [x] 4.3 Inspect the public JAR, dependency graph, JPMS descriptor, and existing SBOM output for implementation or test leakage; verify that the new ledger export contains only the intended public API and that the existing native-token contract is reused without new runtime dependencies.
- [x] 4.4 Run `openspec validate 4-add-network-identity-public-api --strict`, review the final diff against the shared contract, and mark tasks complete only after their checks pass. Leave the change unarchived and do not commit or push without a separate request.
