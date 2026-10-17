# Mirror Node Http API

## Description

Low-level HTTP access to one specific Mirror Node. A `MirrorNodeHttpClient` combines a `MirrorNode` with an
`HttpClient` and executes `MirrorNodeHttpRequest`s against it. Requests contain only the **path** of the REST
endpoint, never a host name, so the same code works unchanged against mainnet, testnet, previewnet or a local
network — the `MirrorNode` decides which one is called.

How a request is resolved:

- `MirrorNode.restBaseUrl` is an absolute URL that already includes the API version segment
  (`scheme://host[:port]/api/v1`). A trailing slash on it is ignored.
- `MirrorNodeHttpRequest.path` always starts with `/` and does not repeat `/api/v1`. An absolute URL is rejected when
  the request is created.
- The final URL is the base URL followed by the path: `https://mainnet.mirrornode.hedera.com/api/v1` +
  `/accounts/0.0.1234` resolves to `https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234`. Endpoints named
  in the Mirror Node REST documentation as `/api/v1/...` are therefore called with the part after `/api/v1`.

The client returns the raw `HttpResponse`: it does not interpret status codes, does not retry and does not paginate.
Mapping a `404` to an absent result, a `5xx` to an error or following the `links.next` value of a response is up to
the caller. Because `links.next` is returned by the Mirror Node as a path, fetching the next page is just another
`execute` call. Transport failures (connection problems, timeouts, a closed `HttpClient`) are passed through
unchanged.

A `MirrorNodeHttpClient` does not own the `HttpClient` it wraps and never closes it. One `HttpClient` (and its
connection pool) can therefore be shared across several Mirror Nodes; whoever created it is responsible for closing
it.

## Design Notes

This namespace is the thin adapter that binds the generic [`http`](../base/http.md) transport to
**one specific** Mirror Node. It is the single place in the mirror-node client where a host name
exists; everything above it — `MirrorNodeClient` and the per-domain repositories in
`mirrornode.account`, `mirrornode.token`, … — works purely in **paths**.

That split is the whole point of the namespace:

- A repository states *which endpoint* it calls (`/accounts/0.0.1234?limit=25`), never *where* the
  Mirror Node lives. The same repository code therefore runs unchanged against mainnet, testnet, a
  previewnet, or a local [solo](../base/solo.md) network — the `MirrorNode` instance decides.
- `MirrorNodeHttpRequest` is structurally identical to `HttpRequest` except that `url` is replaced
  by `path`. Making that a separate type turns "no absolute URLs above this line" into a rule the
  type system enforces, instead of a convention a repository can accidentally break.

The two request types are the mirror image of each other, and both make their contract an invariant
of the field rather than a check somebody has to remember. Where `HttpRequest.url` carries
`@@urlPattern` (absolute URL required), `MirrorNodeHttpRequest.path` carries a `@@pattern` that
requires the opposite: a leading `/` and no whitespace. That single constraint rejects an
accidentally absolute `https://…` — it has no leading slash — at the moment the request is built,
which is the whole "no host names above this line" rule made mechanical.

### Path resolution

`execute` resolves `request.path` against `mirrorNode.restBaseUrl` and delegates to the wrapped
`httpClient`. The rule is plain concatenation, made unambiguous from both sides:

- `restBaseUrl` is a well-formed absolute URL (guaranteed by `@@urlPattern` on the field) and already
  includes the API version segment (`scheme://host[:port]/api/v1`). A trailing slash on it is
  stripped before concatenation, so both spellings of a base URL behave identically.
- `path` always begins with `/` (guaranteed by `@@pattern`) and must **not** repeat `/api/v1`.

So `https://mainnet.mirrornode.hedera.com/api/v1` + `/accounts/0.0.1234` resolves to
`https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234`, and the resulting string satisfies
`HttpRequest.url`'s `@@urlPattern` by construction. The `// Maps to GET /api/v1/…` comments in the
repository specs name the endpoint the way the Mirror Node REST documentation does; the
corresponding `path` is that string minus the base URL.

### What this layer does not do

Everything the base layer does not do, this layer does not do either: the raw `HttpResponse` is
returned as-is, status codes are not interpreted, and no retry or pagination logic lives here.
Mapping a `404` to `null`, a `5xx` to `mirror-node-error`, or a `links.next` value to the next
[`Page`](../base/common.md) is the responsibility of the repository that issued the call — it is the
only party that knows what the response means. Because `links.next` is itself returned by the Mirror
Node as a path (`/api/v1/accounts?…&timestamp=lt:…`), paging is just another `execute` call through
the same client.

Errors are passed through unchanged, for the same reason: `execute` declares exactly the three ids
of `http.HttpClient.execute` — `connection-error`, `timeout-error`, `client-closed-error` — and adds
none of its own. Path resolution cannot fail (both halves are already constrained), and wrapping a
transport failure in a `mirror-node-error` here would destroy the distinction a repository needs to
decide whether retrying is worthwhile. `client-closed-error` reaches callers of this client because
the transport it wraps may be shared and closed elsewhere — see below.

Ownership of the underlying `HttpClient` stays with whoever created it: `MirrorNodeHttpClient`
exposes no lifecycle methods and does not close the client it wraps. This makes it possible — and
intended — to share one transport client, with one connection pool, across several mirror nodes.

On `MirrorNodeHttpRequest.path`: the mirror image of HttpRequest.url's @@urlPattern: this one must NOT be absolute.
The leading slash is mandatory (so resolution against restBaseUrl is plain concatenation) and an accidentally absolute
"https://…" is rejected at construction, because it has none. Unlike URL syntax, this constraint is small enough to
state as a readable regex, so the existing @@pattern is used rather than a dedicated annotation.

On `MirrorNodeHttpClient.execute`: it adds no error of its own: both halves of the URL are already constrained, so
resolution cannot fail, and the three ids it declares are exactly those of http.HttpClient.execute, propagated
unchanged so a repository can still tell a retryable transport failure from a permanent one.

## API Schema

```
namespace mirrornode.http
requires {MirrorNode} from ledger
requires {HttpMethod, HttpClient, HttpResponse} from http

MirrorNodeHttpRequest {
    @@immutable method: HttpMethod
    // Path of the REST endpoint relative to the Mirror Node's REST base URL, including any query string
    // (e.g. "/accounts/0.0.1234?limit=25"). Must start with "/", must not contain whitespace and must not
    // repeat the "/api/v1" prefix; an absolute URL is rejected when the request is created.
    @@pattern("^/[^\s]*$") @@immutable path: string
    @@nullable @@immutable body: bytes
    @@nullable @@immutable timeout: duration
    @@immutable headers: map<string, string>
}

abstraction MirrorNodeHttpClient {

    @@immutable mirrorNode: MirrorNode

    @@immutable httpClient:HttpClient

    // Resolves request.path against mirrorNode.restBaseUrl and sends the request with httpClient.
    // Throws if the connection fails, the request times out or httpClient has been closed; these
    // failures are passed through unchanged. A non-2xx status code is not an error: it is returned
    // in HttpResponse.statusCode for the caller to interpret.
    @@async
    @@threadSafe(client)
    @@throws(connection-error, timeout-error, client-closed-error)
    HttpResponse execute(request: MirrorNodeHttpRequest)
}

```

## Default Instances for Testing

```
// Paths relative to the REST base URL: those that the Go SDK accepts (TestUnitMirrorRestPathAcceptsRelativePaths), the
// ".." inside a query string that the Java SDK accepts (MirrorNodeRestPathTest.allowsDotDotInsideQueryString) and the
// path of this spec's example.
values RELATIVE_PATHS: list<string> = ["/accounts/0.0.1", "/accounts/0.0.1?limit=25",
        "/accounts/0.0.1/tokens?token.id=gt:0.0.5&order=asc", "/", "/accounts/0x00000000000000000000000000000000000004d2",
        "/accounts?order=..", "/accounts/0.0.1234?limit=25"]

// Paths that @@pattern("^/[^\s]*$") rejects: those of the Go SDK (TestUnitMirrorRestPathRejectsHost) without a leading
// slash or with a space, the absolute URL and the space of the Java SDK (MirrorNodeRestPathTest.rejectsAbsoluteUrl,
// rejectsWhitespace) and the absolute URL of this spec's example. The protocol-relative and ".." paths that the SDKs also
// reject are accepted by the pattern and therefore not listed.
values REJECTED_PATHS: list<string> = ["", "accounts/0.0.1", "https://evil.example.com/steal",
        "http://evil.example.com/steal", "/accounts/0.0.1 ?limit=1", "https://evil.example/api/v1/accounts",
        "/accounts/0.0.1 2", "https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234"]
```

## Examples

The following example shows what a repository does internally when it serves
`client.accounts.findById(...)`:

```
mirrorNode = MirrorNode(restBaseUrl: "https://mainnet.mirrornode.hedera.com/api/v1")
client = createMirrorNodeClient(mirrorNode)

// A repository builds a path — never a full URL. @@pattern is checked right here: passing
// "https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234" would fail at construction.
request = MirrorNodeHttpRequest(
    method: GET,
    path: "/accounts/0.0.1234?limit=25",
    body: null,
    timeout: null,      // falls back to httpClient.configuration.defaultRequestTimeout
    headers: {})

// Resolved against mirrorNode.restBaseUrl, this executes
// GET https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234?limit=25
// and propagates connection-error / timeout-error / client-closed-error unchanged.
response = await client.mirrorNodeHttpClient.execute(request)

// Interpreting the result is the repository's job, not this layer's.
if (response.statusCode == 200) {
    accountInfo = parseAccountInfo(response.body)
} else if (response.statusCode == 404) {
    accountInfo = null                    // findById returns @@nullable AccountInfo
} else {
    throw mirror-node-error
}
```

## Testing

How a request is resolved against the base URL and sent is behaviour of `execute` and needs an `HttpClient`; it is not
tested here.

```tests
// Source: the Go SDK (TestUnitMirrorRestPathAcceptsRelativePaths), the Java SDK
// (MirrorNodeRestPathTest.allowsDotDotInsideQueryString) and this spec's example; the values of RELATIVE_PATHS.
@@testId("a-request-keeps-a-path-relative-to-the-rest-base-url-unchanged")
test "a request keeps a path relative to the REST base URL unchanged" {
    for path in RELATIVE_PATHS {
        // when
        let request = MirrorNodeHttpRequest{method: HttpMethod.GET, path: path, headers: {}}

        // then
        assertEquals(expected: path, actual: request.path)
        assertEquals(expected: HttpMethod.GET, actual: request.method)
        assertNull(actual: request.body)
        assertNull(actual: request.timeout)
        assertEquals(expected: 0, actual: length(value: request.headers))
    }
}

// Source: the Java SDK (MirrorNodeHttpClientTest: the POST body and the caller's headers are passed on unchanged).
@@testId("a-post-request-keeps-its-body-and-headers")
test "a POST request keeps its body and headers" {
    // given
    let body = utf8(text: "{\"data\":\"0xabc\"}")

    // when
    let request = MirrorNodeHttpRequest{method: HttpMethod.POST, path: "/contracts/call", body: body,
            headers: {"authorization": "Bearer test"}}

    // then
    assertEquals(expected: HttpMethod.POST, actual: request.method)
    assertEquals(expected: body, actual: request.body)
    assertEquals(expected: {"authorization": "Bearer test"}, actual: request.headers)
}

// Source: the Java SDK (MirrorNodeHttpClientTest: calls with a deadline of 10 seconds); this spec (`timeout` is
// optional, a request without one falls back to the default request timeout of the HttpClient).
@@testId("a-request-keeps-its-timeout")
test "a request keeps its timeout" {
    // when
    let request = MirrorNodeHttpRequest{method: HttpMethod.GET, path: "/accounts/0.0.1",
            timeout: toDuration(text: "PT10S"), headers: {}}

    // then
    assertEquals(expected: toDuration(text: "PT10S"), actual: request.timeout)
    assertNotEquals(unexpected: toDuration(text: "PT10.001S"), actual: request.timeout)
}

// Source: the API guideline (`duration` has millisecond precision).
@@testId("a-request-keeps-a-timeout-with-millisecond-precision")
test "a request keeps a timeout with millisecond precision" {
    // when
    let request = MirrorNodeHttpRequest{method: HttpMethod.GET, path: "/accounts/0.0.1",
            timeout: toDuration(text: "PT1.5S"), headers: {}}
    let timeout = request.timeout

    // then
    assertEquals(expected: toDuration(text: "PT1.5S"), actual: timeout)
    assertNotNull(actual: timeout)
    assertTrue(condition: timeout < toDuration(text: "PT1.501S"))
    assertTrue(condition: timeout > toDuration(text: "PT1.499S"))
}

// Source: the Go SDK (TestUnitMirrorRestPathRejectsHost), the Java SDK (MirrorNodeRestPathTest.rejectsAbsoluteUrl,
// rejectsWhitespace), this spec's example and this spec (@@pattern("^/[^\s]*$")); the values of REJECTED_PATHS.
@@testId("a-request-rejects-a-path-that-is-no-relative-rest-path")
test "a request rejects a path that is no relative REST path" {
    for path in REJECTED_PATHS {
        // when / then
        @@expectThrows(invalid-argument-error)
        MirrorNodeHttpRequest{method: HttpMethod.GET, path: path, headers: {}}
    }
}

// Source: the Go SDK (TestUnitMirrorRestPathRejectsHost: "embedded newline"); this spec (@@pattern("^/[^\s]*$")).
@@testId("a-request-rejects-a-path-with-an-embedded-newline")
test "a request rejects a path with an embedded newline" {
    // when / then
    @@expectThrows(invalid-argument-error)
    MirrorNodeHttpRequest{method: HttpMethod.GET, path: "/accounts\n/0.0.1", headers: {}}
}

// Source: the Go SDK (TestUnitMirrorRestPathRejectsHost: "embedded tab"); this spec (@@pattern("^/[^\s]*$")).
@@testId("a-request-rejects-a-path-with-an-embedded-tab")
test "a request rejects a path with an embedded tab" {
    // when / then
    @@expectThrows(invalid-argument-error)
    MirrorNodeHttpRequest{method: HttpMethod.GET, path: "/accounts\t0.0.1", headers: {}}
}
```

## Questions & Comments

- **`MirrorNodeHttpRequest.timeout` uses `duration`**, which is not a declared basic type — same
  open point as in [`http.md`](../base/http.md).
- **Who creates the `MirrorNodeHttpClient`?** `mirrornode.createMirrorNodeClient(mirrorNode)` takes
  only a `MirrorNode`, so the `HttpClient` behind it is created implicitly. There is currently no
  way for an application to pass in its own transport client (proxy, mTLS, tracing) — an overload
  taking an `HttpClient`, or a factory in this namespace, is probably needed.
- **`links.next` repeats `/api/v1`.** The Mirror Node returns `links.next` as `/api/v1/accounts?…`, but
  `MirrorNodeHttpRequest.path` must not repeat the `/api/v1` prefix, so the next page is *not* "just another `execute`
  call" with that value: the prefix has to be stripped exactly once first. The Java SDK
  (`MirrorNodeRestPath.fromNextLink`) and the Go SDK (`nextPagePath`) do this and reject a `links.next` that names
  another host. Who strips it — the repository, or a function of this namespace that could then be tested — is open.
- **`@@pattern("^/[^\s]*$")` accepts paths that the Java and Go SDKs reject:** a protocol-relative `//evil.example/steal`
  and `..` segments such as `/accounts/../../etc/passwd` (the Java SDK accepts `..` only inside the query string).
  Concatenation keeps both on the configured host, but should the pattern reject them anyway?
