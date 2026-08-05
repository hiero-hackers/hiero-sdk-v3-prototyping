# Common API

The Common API provides core utilities and generic abstractions used uniformly across the SDK.

## Core Abstractions

### `Page<T>`
An abstraction for paginated data returned by network queries (such as Mirror Node requests). It elegantly models an immutable, sequential window into a larger dataset while exposing asynchronous methods to traverse the collection.

#### Properties
- `data`: A strictly typed `ReadonlyArray<T>` holding the results for the current page.
- `size`: The integer number of elements currently stored in `data`.
- `pageIndex`: The integer 0-based index of the current page.

#### Methods
- `hasNext()`: Returns `true` if there are subsequent pages available.
- `isFirst()`: Returns `true` if the current page is the very first page in the sequence (`pageIndex === 0`).
- `next()`: Asynchronously fetches and returns the next `Page<T>`. Throws a `MirrorNodeError` if no next page is available.
- `first()`: Asynchronously fetches and returns the first `Page<T>` of the sequence, allowing users to restart pagination gracefully.

## Error Handling

### `MirrorNodeError`
A custom error class (extending `Error`) that signals failures relating to Mirror Node operations or pagination state violations (e.g., calling `next()` when `hasNext()` is false).
