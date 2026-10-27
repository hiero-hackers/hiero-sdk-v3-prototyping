## 1. Contract Mapping and Source Traceability

The separate design artifact is intentionally omitted, following the current workflow. Use the proposal,
specification, and existing Java conventions as implementation inputs. Keep `proposal.md` and `spec.md` unchanged;
record Java mappings and source traceability in `sdk-java/README.md`.

- [x] 1.1 Record links and the source revision for the shared proposal and specification under repository-root `openspec/changes/3-add-exchange-rate-public-api/`, and verify that the Java copies retain the shared contract.
- [x] 1.2 Document the Java mappings for the native-token namespace, immutable `ZonedDateTime expirationTime`, primitive `double exchangeRateInUsdCents`, read-only accessors, and primitive `boolean isExpired()` in the Java README. Preserve the specified USD-cent denomination and double-precision representation; do not substitute another monetary type or introduce unspecified numeric bounds.
- [x] 1.3 Document the contract-only boundary: own immutable state and required-input validation in the API, but leave expiration evaluation provider-defined. Exclude exchange-rate fetching, refresh, caching, currency/token conversion, network access, and token-specific implementations. Specify that expiration compares instants strictly after the expiration time, with equality returning `false` regardless of time zone.

## 2. Public Exchange Rate Contract

- [x] 2.1 Reuse the existing Java 21 Maven API module, source/test layout, exported `org.hiero.sdk.v3.nativeToken` package, JSpecify dependency, and JUnit Jupiter/AssertJ setup without regenerating pagination or native-token contracts or adding transport dependencies.
- [x] 2.2 Add an abstract `ExchangeRate` class with private final `ZonedDateTime` expiration and primitive `double` USD-cent state, a protected constructor, and final read-only `expirationTime()` and `exchangeRateInUsdCents()` accessors; preserve the supplied zone and value without normalization or mutation methods.
- [x] 2.3 Apply JSpecify nullability and reject a null expiration time at construction with a descriptive `Objects.requireNonNull` check. Do not reject past expiration times or impose positivity, finiteness, or other numeric constraints absent from the shared specification.
- [x] 2.4 Expose abstract `boolean isExpired()` with Javadoc requiring comparison of the current instant against the expiration instant, returning `true` only when strictly later. Keep production clock selection and evaluation in providers; do not add placeholder results, a public clock configuration API, or an implementation dependency.
- [x] 2.5 Document every public and protected API element, immutable-state guarantees, USD-cent units, and provider obligations. Keep the public surface limited to the shared contract and its necessary Java construction/accessor mapping.

## 3. Contract and Consumer Tests

- [x] 3.1 Cover "Exchange-rate state is observed": use a minimal test provider to verify exact expiration value including zone, stored double value, immutable fields/read-only accessors, and descriptive rejection of a null expiration time.
- [x] 3.2 Cover "Exchange rate has expired": use a test-only provider with a fixed `Clock` to verify that expiration strictly before the current instant returns `true`, including a one-nanosecond boundary case. Do not use sleeps or wall-clock timing assumptions.
- [x] 3.3 Cover "Exchange rate has not expired": use the fixed-clock test provider to verify that expiration exactly equal to or later than the current instant returns `false`; include a one-nanosecond future boundary and equivalent instants represented in different time zones.
- [x] 3.4 Verify API shape, protected construction, final immutable state/accessors, `ZonedDateTime`/primitive `double`/primitive `boolean` mappings, nullability, abstract expiration evaluation, and the absence of setters or implementation types in public signatures. Verify that representative negative and zero rate values and past expiration times are not rejected by invented validation rules.
- [x] 3.5 Compile and run an independent named-module consumer with a small exchange-rate provider; verify construction, state access, and deterministic expiration outcomes through the exported API, without requiring annotation, test, transport, or implementation libraries at runtime. Identify provider behavior tests as contract examples, not production expiration implementation.

## 4. Final Verification and Documentation

- [x] 4.1 Run the complete Maven verification and checked Javadoc generation on Java 21; confirm exchange-rate tests and existing pagination/native-token tests pass without regressions.
- [x] 4.2 Map all three shared scenarios to passing tests in the Java README, and document public API usage, source traceability, Java mappings, and deferred provider work without editing proposal/spec files or creating a design artifact.
- [x] 4.3 Inspect the public JAR, dependency graph, JPMS descriptor, and existing SBOM output for implementation or test leakage; verify that the existing native-token package export is sufficient and that only exchange-rate API types and required support were added.
- [x] 4.4 Run `openspec validate 3-add-exchange-rate-public-api --strict`, review the final diff against the shared contract, and mark tasks complete only after their checks pass. Leave the change unarchived and do not commit or push without a separate request.
