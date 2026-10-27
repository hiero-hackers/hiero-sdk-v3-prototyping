## Purpose

Define the language-neutral contract for immutable paginated results and asynchronous page navigation. SDK-specific
OpenSpec changes use this document as their behavioral starting point and add only language mapping requirements.

## ADDED Requirements

### Requirement: Immutable page state

The SDK SHALL expose a generic `Page<T>` abstraction containing immutable `data`, `size`, and `pageIndex` values.
`data` SHALL be an ordered list of `T` values and SHALL NOT be null.

#### Scenario: Page state is observed

- **Given** a page containing an ordered list of values, its declared size, and its zero-based page index.
- **When** a caller reads the page state.
- **Then** the caller receives the same ordered values, size, and page index without being able to modify the page's
  stored state.

### Requirement: Page position inspection

The SDK SHALL expose `hasNext()` to report whether another page is available and `isFirst()` to report whether the
current page is the first page.

#### Scenario: Page position is inspected

- **Given** a page returned by a paginated operation.
- **When** a caller invokes `hasNext()` and `isFirst()`.
- **Then** both operations return the page's current navigation state without initiating page retrieval.

### Requirement: Asynchronous page navigation

The SDK SHALL expose asynchronous `next()` and `first()` operations. `next()` SHALL retrieve the next page and
`first()` SHALL retrieve the first page associated with the same paginated operation.

#### Scenario: Next page is retrieved

- **Given** a page for which `hasNext()` is true.
- **When** the caller awaits `next()`.
- **Then** the operation completes with the next `Page<T>`.

#### Scenario: First page is retrieved

- **Given** any page in a paginated result.
- **When** the caller awaits `first()`.
- **Then** the operation completes with the first `Page<T>`.

### Requirement: Mirror Node navigation failures

Asynchronous page navigation SHALL report `mirror-node-error` when the associated Mirror Node request cannot produce
the requested page.

#### Scenario: Page retrieval fails

- **Given** a page whose navigation requires a Mirror Node request.
- **When** `next()` or `first()` cannot retrieve the requested page.
- **Then** the asynchronous operation terminates with `mirror-node-error`.

## Language-Neutral API Schema

```text
namespace common

abstraction Page<$$T> {
    @@immutable data: list<$$T>
    @@immutable size: int32
    @@immutable pageIndex: int32

    bool hasNext()
    bool isFirst()

    @@async
    @@throws(mirror-node-error)
    Page<$$T> next()

    @@async
    @@throws(mirror-node-error)
    Page<$$T> first()
}
```
