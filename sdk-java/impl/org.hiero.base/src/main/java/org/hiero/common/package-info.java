/// General-purpose types shared across the SDK.
///
/// `Page` represents one page of a paginated query result, for example a list of transactions returned
/// by a mirror node. It holds the items of the current page (`data`), the page size and the index of
/// the page. Use `hasNext()` and `next()` to walk forward through the result and `first()` to return to
/// the first page; loading another page is an asynchronous network call. If a page cannot be loaded,
/// the call fails with a pagination error whose cause describes the underlying failure (for example the
/// failed mirror node request).
///
/// `SemanticVersion` is a version number as defined by [semver.org](https://semver.org): `major.minor.patch` with an
/// optional pre-release label and optional build metadata, for example the version of the protocol or of the software of
/// a network node. `toString()` returns its canonical text, such as `0.49.1-rc.1+exp.sha.5114f85`.
package org.hiero.common;
