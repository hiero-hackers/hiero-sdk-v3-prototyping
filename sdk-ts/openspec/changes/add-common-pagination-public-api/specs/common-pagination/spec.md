## Purpose

Define the TypeScript contract for immutable paginated results and asynchronous page navigation, preserving the
common pagination behavior and adding only the TypeScript language mapping.

## ADDED Requirements

### Requirement: Immutable page state

The SDK SHALL expose a generic `Page<T>` abstraction containing immutable `data`, `size`, and `pageIndex` values.
`data` SHALL be an ordered list of `T` values and SHALL NOT be null.

#### Scenario: Page state is observed

- **WHEN** a caller reads the state of a page constructed from an ordered list of values, its declared size, and its
  zero-based page index
- **THEN** the caller receives the same ordered values, size, and page index without being able to modify the page's
  stored state

### Requirement: Page position inspection

The SDK SHALL expose `hasNext()` to report whether another page is available and `isFirst()` to report whether the
current page is the first page.

#### Scenario: Page position is inspected

- **WHEN** a caller invokes `hasNext()` and `isFirst()` on a page returned by a paginated operation
- **THEN** both operations return the page's current navigation state without initiating page retrieval

### Requirement: Asynchronous page navigation

The SDK SHALL expose asynchronous `next()` and `first()` operations. `next()` SHALL retrieve the next page and
`first()` SHALL retrieve the first page associated with the same paginated operation.

#### Scenario: Next page is retrieved

- **WHEN** the caller awaits `next()` on a page for which `hasNext()` is true
- **THEN** the operation completes with the next `Page<T>`

#### Scenario: First page is retrieved

- **WHEN** the caller awaits `first()` on any page in a paginated result
- **THEN** the operation completes with the first `Page<T>`

### Requirement: Mirror Node navigation failures

Asynchronous page navigation SHALL report `mirror-node-error` when the associated Mirror Node request cannot produce
the requested page.

#### Scenario: Page retrieval fails

- **WHEN** `next()` or `first()` cannot retrieve the requested page from the Mirror Node
- **THEN** the asynchronous operation terminates with `mirror-node-error`

### Requirement: TypeScript page mapping

The TypeScript SDK SHALL export `Page<T>` as an abstract class. `data` SHALL be typed as `ReadonlyArray<T>` and SHALL
be a frozen snapshot that does not reflect later changes to the array passed at construction. `size` and `pageIndex`
SHALL be read-only `number` values restricted to the int32 range of the common schema. Constructing a page with `data`
that is not an array SHALL throw a `TypeError`; constructing a page with a `size` or `pageIndex` that is not an int32
integer SHALL throw a `RangeError`.

#### Scenario: Page data is modified after construction

- **WHEN** a caller mutates the source array after constructing a page, or attempts to mutate `page.data`
- **THEN** the page's data remains unchanged, and mutating `page.data` fails at compile time and at runtime

#### Scenario: Page is constructed with invalid state

- **WHEN** a page is constructed with null or undefined `data`, or a non-integer or out-of-int32-range `size` or
  `pageIndex`
- **THEN** construction throws a `TypeError` for invalid `data` or a `RangeError` for an invalid number

### Requirement: TypeScript navigation mapping

The TypeScript SDK SHALL type `next()` and `first()` as returning `Promise<Page<T>>`, and `hasNext()` and `isFirst()` as
returning `boolean` synchronously.

#### Scenario: Navigation is awaited

- **WHEN** a consumer awaits `next()` or `first()`
- **THEN** the awaited value is typed as `Page<T>` with the same `T` as the current page

### Requirement: TypeScript error mapping

The TypeScript SDK SHALL map `mirror-node-error` to an exported `MirrorNodeError` class that extends `Error`, has the
`name` `"MirrorNodeError"`, and preserves a message and an optional underlying `cause`.

#### Scenario: Navigation promise rejects

- **WHEN** a Mirror Node request backing `next()` or `first()` fails
- **THEN** the returned promise rejects with a `MirrorNodeError` that is detectable with `instanceof` and exposes the
  original `cause`
