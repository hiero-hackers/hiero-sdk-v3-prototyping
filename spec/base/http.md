# Http API

## Description

The SDK has to speak HTTP for everything that is not gRPC — today that is the Mirror Node REST API
(see [`mirrornode.http`](../mirror-node-client/mirror-node-http.md) and the repositories above it),
tomorrow potentially any other REST/JSON service a network exposes. This namespace defines the
**transport-level** contract for that traffic, and deliberately nothing more.

`HttpClient` is an `abstraction` on purpose. Every target language already ships (or has a de-facto
standard) HTTP stack — `java.net.http.HttpClient`, `fetch`, `reqwest`, `libcurl`, `URLSession` — and
real applications need to control proxies, TLS material, connection pooling, retries, or tracing at
exactly that level. By keeping the SDK's own dependency on HTTP down to the handful of types below, a language
binding can wrap its native stack and an application can plug in its own client, without either of
them affecting the layers above.

The layer is payload-agnostic and semantics-free:

- **Bodies are raw `bytes`.** The content type is just a header. Serialisation (JSON for the Mirror
  Node) is the caller's business, which keeps this namespace usable for non-JSON payloads.
- **Status codes are reported, never interpreted.** A `404` or a `500` is a *successful* HTTP
  exchange: `execute` completes normally and hands back the `HttpResponse`. Only a transport failure
  (DNS, connect, TLS, timeout) fails the returned future. Translating a status code into a domain
  error such as `not-found-error` or `mirror-node-error` is the job of the layer that knows what the
  call meant.
- **No policies.** No retry, no redirect handling, no rate limiting, no circuit breaking. Those
  belong either to the concrete implementation or to the calling layer.

`HttpConfiguration` carries what the client applies to every exchange: `defaultHeaders` are merged
into each request (a header set on the request wins on a key collision), `connectTimeout` bounds
establishing the connection, and `defaultRequestTimeout` applies whenever `HttpRequest.timeout` is
null. `HttpRequest.url` is **absolute** — this type performs no base-URL resolution; that is
precisely what `mirrornode.http.MirrorNodeHttpClient` adds on top.

That "absolute" is not just documentation: `url` carries `@@urlPattern`, so a well-formed absolute
URL is an **invariant of the type**, enforced wherever an `HttpRequest` is built. The malformed-URL
case is therefore reported where the mistake is made, not later at a call to `execute` that may be
several layers away from whoever assembled the string — and it needs no factory, no `Url` wrapper
type and no validation code in the client. A binding enforces it by handing the string to its native
URL parser (see `@@urlPattern` in [`api-guideline.md`](../../guidelines/api-guideline.md)); the same
annotation is reusable anywhere else the SDK stores a URL.

### Failure model

An `HttpClient` fails only when it cannot produce a response at all. The three error ids are chosen
so that the caller — which is the layer that owns retry policy, since this one has none — can decide
what to do without inspecting a message string:

| Error                 | Cause                                                                   | Retryable  |
|-----------------------|-------------------------------------------------------------------------|------------|
| `connection-error`    | DNS, connection refused, TLS handshake, reset / truncated exchange      | possibly   |
| `timeout-error`       | `connectTimeout` or the effective request timeout elapsed               | possibly   |
| `client-closed-error` | the client was closed before or while the request was submitted         | no         |

`timeout-error` is kept separate from `connection-error` on purpose: "the server took too long" and
"the server was unreachable" call for different back-off strategies, and collapsing them would force
callers to guess. Note what is *absent*: `execute` has no input-validation failure mode, because
`@@urlPattern` has already ruled it out at construction. Every error it reports is a genuine runtime
condition, which is what makes the table actionable.

### Lifecycle and concurrency

Every consumer above this layer shares a single client, so `execute` and both `close` overloads are
`@@threadSafe(client)`: the SDK may call any of them concurrently, including `execute` while a
`close` is already in progress. That is exactly why `client-closed-error` exists.

A client owns resources (sockets, pools, worker threads), so it is closeable. `close()` waits for
in-flight exchanges — which are themselves bounded by their timeouts, so it terminates —
while `close(closeTimeout)` bounds that wait and aborts whatever is left. Aborting surfaces as
`client-closed-error` on the affected `execute` calls; `close` itself never fails and is idempotent,
which is why neither overload declares `@@throws`. A bounded shutdown is the guarantee it gives, not
an outcome it reports. Both are `@@async` because a graceful shutdown is itself an I/O operation.

## API Schema

```
namespace http

// The complete set of HTTP methods: the eight defined by RFC 9110 plus PATCH (RFC 5789).
// Listed in full so the enum is closed for good — bindings that map it to a native closed
// enum (and callers that switch exhaustively over it) never face a breaking addition later.
enum HttpMethod {
    GET,
    HEAD,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE,
    CONNECT   // proxy tunnelling; normally issued by the HTTP stack itself, not by a caller
}

HttpConfiguration {
  @@immutable defaultHeaders: map<string, string> 
  @@immutable connectTimeout: duration
  @@immutable defaultRequestTimeout: duration
}

HttpRequest {
    @@immutable method: HttpMethod
    // @@urlPattern makes "absolute, well-formed URL" an invariant of the type rather than a
    // check somebody has to remember to run: it is enforced wherever an HttpRequest is built,
    // so a request that is guaranteed to fail cannot exist and `execute` never re-checks the
    // string. No base-URL resolution happens here — that is what
    // mirrornode.http.MirrorNodeHttpClient adds on top.
    @@urlPattern @@immutable url: string
    @@nullable @@immutable body: bytes
    @@nullable @@immutable timeout: duration
    @@immutable headers: map<string, string>
}

HttpResponse {
    @@immutable statusCode: uint16
    @@immutable body: bytes
    @@immutable headers: map<string, string>
}

abstraction HttpClient {

    @@immutable configuration:HttpConfiguration
    
    // Executes a single HTTP exchange. A non-2xx status code is NOT an error — it is returned
    // in HttpResponse.statusCode. `request.url` needs no checking here — @@urlPattern already
    // guarantees it. Only the three failures below prevent an exchange from producing a
    // response.
    //   connection-error    — the exchange could not be carried out: DNS failure, connection
    //                         refused, TLS handshake failure, connection reset or truncated
    //                         before the response was complete. Possibly transient.
    //   timeout-error       — configuration.connectTimeout, or the effective request timeout
    //                         (request.timeout ?? configuration.defaultRequestTimeout), elapsed.
    //                         Possibly transient.
    //   client-closed-error — the client was closed before or while the request was submitted.
    //                         Permanent for this client instance; never retry.
    @@async
    @@threadSafe(client)
    @@throws(connection-error, timeout-error, client-closed-error)
    HttpResponse execute(request: HttpRequest)

    // Closes the client and releases its resources. Idempotent: closing an already-closed
    // client completes normally. Waits for in-flight exchanges to finish — they are bounded
    // by their own timeouts, so this terminates.
    @@async
    @@threadSafe(client)
    void close()

    // Same as close(), but waits at most closeTimeout for in-flight exchanges before aborting
    // them. Aborted exchanges fail their own execute() with client-closed-error; close itself
    // still completes normally — a bounded shutdown is the guarantee, not a possible failure.
    @@async
    @@threadSafe(client)
    void close(closeTimeout: duration)
}

@@static HttpClient createHttpClient(configuration: HttpConfiguration)
```

## Example

The following example performs a single GET request and shows that a `404` is a normal result of
this layer, not an error:

```
configuration = HttpConfiguration(
    defaultHeaders: {"accept": "application/json"},
    connectTimeout: 5s,
    defaultRequestTimeout: 10s)

// The binding's default implementation, wrapping its native HTTP stack. An application is
// free to supply its own HttpClient instead (proxy, mTLS, tracing).
client = createHttpClient(configuration)

// @@urlPattern is checked here, at construction — a malformed URL fails now, not later
// inside execute.
request = HttpRequest(
    method: GET,
    url: "https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234",
    body: null,          // no body on a GET
    timeout: null,       // falls back to configuration.defaultRequestTimeout
    headers: {})         // configuration.defaultHeaders still apply

response = await client.execute(request)

// The exchange succeeded in both branches — interpreting the status code is up to the caller.
if (response.statusCode == 200) {
    accountJson = parseJson(response.body)
} else if (response.statusCode == 404) {
    accountJson = null
}

await client.close(5s)
```

A request with a body, its own timeout and its own headers:

```
request = HttpRequest(
    method: POST,
    url: "https://example.org/api/v1/submit",
    body: toJsonBytes(payload),
    timeout: 30s,
    headers: {"content-type": "application/json"})
```

## Questions & Comments

- **`duration` is not a declared basic type.** `connectTimeout`, `defaultRequestTimeout`,
  `HttpRequest.timeout` and `close(closeTimeout)` use `duration`, but
  [`api-guideline.md`](../../guidelines/api-guideline.md) only defines `seconds` (whole-second
  precision, justified by the HAPI wire format). HTTP timeouts realistically need sub-second
  precision, so either the guideline gains a finer-grained duration type or these fields have to be
  expressed differently. Same question applies to `mirrornode.http.MirrorNodeHttpRequest.timeout`.
