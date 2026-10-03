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

### Requirement: Navigation failures

Asynchronous page navigation SHALL report `pagination-error` when the requested page cannot be produced. The error
SHALL carry the underlying failure (for example the failed Mirror Node request) as its cause.

#### Scenario: Page retrieval fails

- **Given** a page whose navigation requires a Mirror Node request.
- **When** `next()` or `first()` cannot retrieve the requested page.
- **Then** the asynchronous operation terminates with `pagination-error`, whose cause is the failure of the request.

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
    @@throws(pagination-error)
    Page<$$T> next()

    @@async
    @@throws(pagination-error)
    Page<$$T> first()
}
```
