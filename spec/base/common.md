# Common API

## Description

General-purpose types shared across the SDK.

`Page` represents one page of a paginated query result, for example a list of transactions returned
by a mirror node. It holds the items of the current page (`data`), the page size and the index of
the page. Use `hasNext()` and `next()` to walk forward through the result and `first()` to return to
the first page; loading another page is an asynchronous network call. If a page cannot be loaded,
the call fails with a pagination error whose cause describes the underlying failure (for example the
failed mirror node request).

`SemanticVersion` is a version number as defined by [semver.org](https://semver.org): `major.minor.patch` with an
optional pre-release label and optional build metadata, for example the version of the protocol or of the software of
a network node. `toString()` returns its canonical text, such as `0.49.1-rc.1+exp.sha.5114f85`.

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

// A semantic version (major.minor.patch) with optional pre-release and build metadata labels, as defined by
// semver.org.
@@finalType
SemanticVersion {
    @@immutable major: int32                          // major version
    @@immutable minor: int32                          // minor version
    @@immutable patch: int32                          // patch version
    @@immutable @@default("") preReleaseLabel: string // pre-release label such as "rc.1"; empty for a release version
    @@immutable @@default("") buildMetadata: string   // build metadata such as "exp.sha.5114f85"; empty if none

    // Canonical semver string, e.g. "0.49.1-rc.1+exp.sha.5114f85"
    string toString()
}
```

## Default Instances for Testing

```
// The version of the example of `SemanticVersion.toString()`: with a pre-release label and build metadata.
instance SemanticVersion = SemanticVersion{major: 0, minor: 49, patch: 1, preReleaseLabel: "rc.1",
        buildMetadata: "exp.sha.5114f85"}

// Versions and their canonical semver text: the known answers of the Rust (semantic_version display tests) and Swift
// (SemanticVersionUnitTests) SDKs, the example of `SemanticVersion.toString()`, and examples of
// semver.org (items 9 and 10 of the specification).
values SEMANTIC_VERSION_TEXTS: map<string, SemanticVersion> = {
    "1.2.3": SemanticVersion{major: 1, minor: 2, patch: 3, preReleaseLabel: "", buildMetadata: ""},
    "3.1.4-15.92": SemanticVersion{major: 3, minor: 1, patch: 4, preReleaseLabel: "15.92", buildMetadata: ""},
    "1.41.1+6535asd": SemanticVersion{major: 1, minor: 41, patch: 1, preReleaseLabel: "", buildMetadata: "6535asd"},
    "0.1.4-0.9a2+sha.25531c": SemanticVersion{major: 0, minor: 1, patch: 4, preReleaseLabel: "0.9a2",
            buildMetadata: "sha.25531c"},
    "0.49.1-rc.1+exp.sha.5114f85": SemanticVersion{major: 0, minor: 49, patch: 1, preReleaseLabel: "rc.1",
            buildMetadata: "exp.sha.5114f85"},
    "1.0.0-x.7.z.92": SemanticVersion{major: 1, minor: 0, patch: 0, preReleaseLabel: "x.7.z.92", buildMetadata: ""},
    "1.0.0+20130313144700": SemanticVersion{major: 1, minor: 0, patch: 0, preReleaseLabel: "",
            buildMetadata: "20130313144700"},
    "1.0.0-beta+exp.sha.5114f85": SemanticVersion{major: 1, minor: 0, patch: 0, preReleaseLabel: "beta",
            buildMetadata: "exp.sha.5114f85"}
}
```

## Testing

Offline tests of `SemanticVersion`, written as given / when / then. The comment above each test names its source.

```tests
// Source: the Rust (semantic_version display tests) and Swift (test_Basic, test_WithPrerelease, test_WithBuild,
// test_WithPrereleaseAndBuild) SDKs; the example of `SemanticVersion.toString()`; semver.org.
@@testId("a-semantic-version-is-written-in-its-canonical-semver-form")
test "a semantic version is written in its canonical semver form" {
    for text in keys(value: SEMANTIC_VERSION_TEXTS) {
        // when
        let written = SEMANTIC_VERSION_TEXTS[text].toString()

        // then
        assertEquals(expected: text, actual: written)
    }
}

// Source: the JavaScript ("constructor stores major, minor and patch"), C++ (ConstructWithValues) and Swift
// (test_ParseWithPrereleaseAndBuild) SemanticVersion unit tests.
@@testId("a-semantic-version-keeps-its-numbers-and-labels")
test "a semantic version keeps its numbers and labels" {
    // when
    let version = SemanticVersion{major: 1, minor: 2, patch: 0, preReleaseLabel: "beta.10", buildMetadata: "d13fe780"}

    // then
    assertEquals(expected: 1, actual: version.major)
    assertEquals(expected: 2, actual: version.minor)
    assertEquals(expected: 0, actual: version.patch)
    assertEquals(expected: "beta.10", actual: version.preReleaseLabel)
    assertEquals(expected: "d13fe780", actual: version.buildMetadata)
}

// Source: the Swift SemanticVersion unit tests (test_ParseMajorMinorPatch, test_ParseZeroMinorPatch: a release has no
// labels); the API schema of this spec (both labels are empty for a release version).
@@testId("a-release-version-has-no-labels-and-is-written-as-major-minor-patch")
test "a release version has no labels and is written as major.minor.patch" {
    // when
    let version = SemanticVersion{major: 0, minor: 2, patch: 0, preReleaseLabel: "", buildMetadata: ""}

    // then
    assertEquals(expected: "", actual: version.preReleaseLabel)
    assertEquals(expected: "", actual: version.buildMetadata)
    assertEquals(expected: "0.2.0", actual: version.toString())
}
```

## Questions & Comments
