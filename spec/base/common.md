# Common API

## Description

General-purpose types shared across the SDK.

`Page` represents one page of a paginated query result, for example a list of transactions returned
by a mirror node. It holds the items of the current page (`data`), the page size and the index of
the page. Use `hasNext()` and `next()` to walk forward through the result and `first()` to return to
the first page; loading another page is an asynchronous network call. If a page cannot be loaded,
the call fails with a pagination error whose cause describes the underlying failure (for example the
failed mirror node request).

## API Schema

```
namespace common

// One page of a paginated result.
abstraction Page<$$T> {
    @@immutable data: list<$$T> // the items of this page
    @@immutable size: int32 // the page size
    @@immutable pageIndex: int32 // index of this page within the result

    bool hasNext() // returns true if another page follows this one
    bool isFirst() // returns true if this is the first page

    @@async
    @@throws(pagination-error)
    Page<$$T> next() // loads the next page

    @@async
    @@throws(pagination-error)
    Page<$$T> first() // loads the first page
}

```

## Questions & Comments
