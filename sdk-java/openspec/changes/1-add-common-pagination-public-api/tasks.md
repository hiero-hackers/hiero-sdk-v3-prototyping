## 1. Contract Mapping and Source Traceability

The separate design artifact is intentionally omitted at the user's request. Use the proposal, specification, and
existing Java conventions as the implementation inputs.

- [x] 1.1 Review the existing pagination API and older unnumbered change; identify the Java type shape, state accessors, validation, asynchronous return type, error mapping, and package/module boundaries to preserve or align with the specification. Resolve behavior-changing questions before proceeding.
- [x] 1.2 Record links and the source revision for the common proposal and specification under repository-root `openspec/changes/1-add-common-pagination-public-api/`; identify which existing code and tests can satisfy this numbered change.
- [x] 1.3 Document the preserved Java contract mappings in `sdk-java/README.md` without editing `proposal.md` or `spec.md`, changing shared behavior, or introducing unspecified size, page-index, or element constraints, as requested by the user.

## 2. Public Pagination Contract

- [x] 2.1 Reuse the existing Java 21 Maven API module and its main/test source layout; verify JPMS, JSpecify, and test-scoped JUnit Jupiter/AssertJ support without importing transport dependencies or unrelated API features.
- [x] 2.2 Implement or align `Page<T>` with immutable ordered `data`, declared `size`, and zero-based `pageIndex`; preserve supplied state, validate non-null data at construction, and prevent modification through either input or returned collections.
- [x] 2.3 Implement or align synchronous `hasNext()` and `isFirst()` contract methods and asynchronous `next()` and `first()` signatures with the preserved Java contract mappings documented in the README, leaving concrete retrieval, HTTP calls, parsing, retries, and executors deferred.
- [x] 2.4 Reuse or align the Java mapping of `mirror-node-error`, preserving failure messages and underlying causes in exceptional navigation results.
- [x] 2.5 Apply explicit nullability, export only the public contract package, and document public types, constructors, methods, immutability boundaries, and asynchronous failures with Javadoc.

## 3. Contract and Consumer Tests

- [x] 3.1 Cover "Page state is observed": verify ordered values, declared size, page index, null-data rejection, empty data, defensive snapshots, and immutable returned collections without inferring additional numeric restrictions.
- [x] 3.2 Cover "Page position is inspected": verify position results without triggering retrieval, using a small test provider rather than a real network.
- [x] 3.3 Cover "Next page is retrieved": verify successful asynchronous completion with the next page when another page is available.
- [x] 3.4 Cover "First page is retrieved": verify successful asynchronous completion with the first page belonging to the same paginated operation.
- [x] 3.5 Cover "Page retrieval fails": verify exceptional completion of both navigation operations with the documented Java error mapping and retained cause, using failing test providers without network access.
- [x] 3.6 Verify API shape, generic return types, constructor visibility, immutable state, nullability, and implementation-free public signatures against the approved Java mapping.
- [x] 3.7 Compile and run an independent named-module consumer that uses the public API and supplies a test provider; verify JPMS exports and that test, transport, and implementation dependencies are not required by consumers.

## 4. Final Verification and Documentation

- [x] 4.1 Run the complete Maven verification and checked Javadoc generation on Java 21; confirm new and existing pagination tests pass without regressions.
- [x] 4.2 Inspect the published JAR, dependency graph, and JPMS descriptor for unwanted implementation or test leakage; verify source traceability and explicit test coverage for all five shared scenarios.
- [x] 4.3 Update Java SDK usage documentation to reflect any API, build, or source-reference changes; update repository conventions only if this change introduces new ones.
- [x] 4.4 Run `openspec validate 1-add-common-pagination-public-api --strict`, review the final diff, and mark tasks complete only after their checks pass. Leave the change unarchived and do not commit or push without a separate request.
