## Source Reference

- Common proposal: [`openspec/changes/1-add-common-pagination-public-api/proposal.md`](../../../../openspec/changes/1-add-common-pagination-public-api/proposal.md)
- Common specification: [`openspec/changes/1-add-common-pagination-public-api/spec.md`](../../../../openspec/changes/1-add-common-pagination-public-api/spec.md)
- Source revision: `1d0e998933b81136b491866bcddc467ced77ddd6`

## Why

SDK repositories need one language-neutral contract for paginated results before Java and TypeScript select their own
public type shapes and implementations. The TypeScript SDK adopts the common pagination contract so every TypeScript API
returning paginated results shares one exported, idiomatic `Page<T>` type.

## What Changes

- Define a generic page with immutable data, size, and page-index values.
- Define synchronous page-position inspection through `hasNext()` and `isFirst()`.
- Define asynchronous navigation to the next and first pages.
- Define `mirror-node-error` as the terminal failure contract for asynchronous page navigation.
- Map these behaviors to an exported TypeScript `Page<T>` abstract class, `ReadonlyArray<T>` data, `Promise<Page<T>>`
  navigation, and an exported `MirrorNodeError` error class.
- Add the minimal TypeScript package and build support required to publish and verify this contract.

## Capabilities

### New Capabilities

- `common-pagination`: Defines the shared page state and navigation contract used by APIs returning paginated results.

### Modified Capabilities

None.

## Impact

- Adds the first TypeScript package (`@hiero-ledger/sdk-base-api`) under `sdk-ts/`, with an npm workspace, TypeScript
  strict build, and Vitest contract tests.
- The published package has no runtime dependencies and contains no transport, HTTP, JSON parsing, or Mirror Node
  implementation.
- Does not modify `spec/base/common.md` or the common change.

### Intentionally Deferred

- Concrete `Page<T>` implementations, Mirror Node HTTP requests, response parsing, retries, and cursor handling.
- Async-iteration helpers (for example `Symbol.asyncIterator` over pages); not part of the common contract.
- npm publishing, release automation, and CI integration.
