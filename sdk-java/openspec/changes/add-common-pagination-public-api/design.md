## Source Reference

- [Common proposal](../../../../openspec-common-delta-changes/changes/add-common-pagination-public-api/proposal.md)
- [Common specification](../../../../openspec-common-delta-changes/changes/add-common-pagination-public-api/spec.md)
- Source revision: `544b523f3db204433ccc7d3731cde73685914fbc`

## Java API Structure

The pagination contract and its contract tests reside in one Java module. Production sources use `src/main/java`, and
tests use `src/test/java`. Test dependencies are test-scoped so they do not leak into the published API.

The public package is `org.hiero.sdk.v3.common` and is exported through JPMS. The package uses JSpecify nullness
annotations.

## Page Abstraction

Represent `Page<T>` as an abstract class. It owns the common page state while later implementations provide position
and navigation behavior.

The class exposes immutable data, size, and page-index values. Construction validates non-null data and creates a
defensive snapshot. State accessors cannot be overridden.

The design does not add constraints for size or page index that are not present in the common specification.

## Navigation

Position inspection remains synchronous through `hasNext()` and `isFirst()`.

Navigation remains asynchronous through `next()` and `first()`, represented by `CompletionStage<Page<T>>`. These
operations remain abstract and contain no transport or page-retrieval business logic.

## Error Mapping

Map the common `mirror-node-error` condition to an unchecked `MirrorNodeException`. The exception supports preserving
both a message and an underlying cause.

## Module Boundary

The public API module does not depend on HTTP, gRPC, JSON, generated protocol types, executors, or concrete pagination
implementations. Those concerns belong to later implementation modules.

## Verification Approach

Contract tests verify the public API shape, null handling, immutable state, asynchronous navigation signatures, error
mapping, consumer compilation, JPMS exports, and documentation generation.
