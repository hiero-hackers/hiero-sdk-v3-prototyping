## Why

SDK repositories need one language-neutral contract for paginated results before Java and TypeScript select their own
public type shapes and implementations. Publishing the common proposal and specification together gives each SDK owner
the same reviewed starting point while keeping language-specific OpenSpec workflows independent.

## What Changes

- Define a generic page with immutable data, size, and page-index values.
- Define synchronous page-position inspection through `hasNext()` and `isFirst()`.
- Define asynchronous navigation to the next and first pages.
- Define `pagination-error` as the terminal failure contract for asynchronous page navigation; it carries the
  underlying failure (e.g. of the Mirror Node request) as cause.
- Require SDK owners to preserve this behavior while selecting idiomatic language-specific representations.

## Capabilities

### New Capabilities

- `common-pagination`: Defines the shared page state and navigation contract used by APIs returning paginated results.

### Modified Capabilities

None.

## Impact

- Supplies the common source for corresponding Java and TypeScript OpenSpec changes.
- Derives the pagination delta from `spec/base/common.md` without modifying or replacing that source specification.
- Does not prescribe a Java class, TypeScript interface, concrete transport, executor, or Mirror Node implementation.
