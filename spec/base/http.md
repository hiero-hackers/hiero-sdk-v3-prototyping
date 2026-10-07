# Http API

## Description

A minimal, transport-level HTTP client used by the SDK for everything that is not gRPC — today the
Mirror Node REST API. `HttpClient` is an abstraction: the SDK ships a default implementation that
wraps the language's native HTTP stack, and an application can supply its own client to control
proxies, TLS material, connection pooling, retries or tracing.

The client is payload-agnostic and does not interpret responses:

- **Bodies are raw `bytes`.** The content type is just a header; serialisation (e.g. JSON) is up to
  the caller.
- **Status codes are reported, never interpreted.** A `404` or a `500` is a successful HTTP exchange:
  `execute` completes normally and returns the `HttpResponse`. Only a transport failure fails the
  returned future.
- **No policies.** No retry, no redirect handling, no rate limiting, no circuit breaking.

`HttpConfiguration` holds what the client applies to every exchange: `defaultHeaders` are merged into
each request (a header set on the request wins on a key collision), `connectTimeout` bounds
establishing the connection, and `defaultRequestTimeout` applies whenever `HttpRequest.timeout` is
not set. `HttpRequest.url` must be an absolute, well-formed URL; this is checked when the request is
created, so a malformed URL is reported where the mistake is made and not later in `execute`. No
base-URL resolution happens at this level.

Every exchange takes a `Cancellation`: the caller's ability to end it early. A `Cancellation` is
only ever *observed* — a client can ask whether it has fired and register a callback for when it
does, but it cannot fire one. Firing is the job of a `CancellationSource`, which the caller owns and
never hands out. `Cancellation.none()` is the value for a call nobody intends to cancel; it is never
absent.

`execute` fails only when no response can be produced at all, in one of four ways: the connection
could not be established or was broken (DNS, connection refused, TLS handshake, reset or truncated
exchange; possibly transient), a connect or request timeout elapsed (possibly transient), the
cancellation fired (permanent for that call; never retry), or the client was closed before or while
the request was submitted (permanent; never retry).

A client owns resources (sockets, pools, worker threads) and must be closed. All methods of
`HttpClient` may be called concurrently, including `execute` while a `close` is in progress.
`close()` waits for in-flight exchanges, which are bounded by their timeouts; `close(closeTimeout)`
bounds that wait and aborts whatever is left — aborted exchanges fail with the "client closed"
error. `close` itself never fails and is idempotent.

## Design Notes

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

- Serialisation being the caller's business keeps this namespace usable for non-JSON payloads.
- Translating a status code into a domain error such as `not-found-error` or `mirror-node-error` is
  the job of the layer that knows what the call meant.
- Retry, redirect, rate-limit and circuit-breaker policies belong either to the concrete
  implementation or to the calling layer.
- `HttpRequest.url` is **absolute** — this type performs no base-URL resolution; that is precisely
  what `mirrornode.http.MirrorNodeHttpClient` adds on top.

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
| `cancelled-error`     | the caller's `Cancellation` fired before the response was complete      | no         |
| `client-closed-error` | the client was closed before or while the request was submitted         | no         |

`cancelled-error` is kept separate from `timeout-error` for the same reason: a caller that gave up
is not a server that was slow, and only one of the two says anything about the remote side.

`timeout-error` is kept separate from `connection-error` on purpose: "the server took too long" and
"the server was unreachable" call for different back-off strategies, and collapsing them would force
callers to guess. Note what is *absent*: `execute` has no input-validation failure mode, because
`@@urlPattern` has already ruled it out at construction. Every error it reports is a genuine runtime
condition, which is what makes the table actionable.

### Cancellation

A timeout the caller merely *waits out* is not a bound. A helper holding only an asynchronous result
can stop waiting, but it cannot end the exchange: racing a hung request against a timer leaves the
socket open and the request in flight. With a retry budget above this layer that is one leaked socket
per attempt, out of the client's own pool. The exchange therefore has to be told, and `Cancellation`
is how it is told.

Three types rather than one, because the capability splits into two halves that must not be held by
the same party:

- `Cancellation` is handed *down*, to a client the SDK may not have written. It can only observe —
  `isCancelled()` and `onCancel(...)`. If `cancel()` lived here, a third-party client could cancel
  the caller's own operation.
- `CancellationSource` is kept *up*, by whoever started the call. It owns the `cancel()`.
- `CancellationRegistration` is what `onCancel` returns, so an observer can be released. Without it,
  an application that holds one source across many calls — "cancel everything on logout", which is
  the use these types exist for — accumulates one dead callback per exchange for the lifetime of that
  source. A single-use rule would not fix it, because nothing can enforce one.

Semantics an implementation has to honour: `cancel()` is idempotent; `isCancelled()` is true *before*
any callback runs; callbacks run synchronously on the thread that called `cancel()` and must not
block; a callback that throws neither stops the others nor propagates to the caller of `cancel()`;
`onCancel` on an already-cancelled instance runs its callback immediately rather than never; and
cancelling after the exchange has finished is a no-op.

The shape is required, the three type names are not. Most languages already have all four operations
and should bind to what they have: an `AbortSignal` / `AbortController` pair in TypeScript, a
`context.Context` and its `CancelFunc` in Go, a `CancellationToken` in Rust. A parameter every
language can name is what keeps one signature for the abstraction across all of them.

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

### Schema remarks

- `HttpMethod` is listed in full so the enum is closed for good — bindings that map it to a native
  closed enum (and callers that switch exhaustively over it) never face a breaking addition later.
- `HttpRequest.url`: `@@urlPattern` makes "absolute, well-formed URL" an invariant of the type rather
  than a check somebody has to remember to run: so a request that is guaranteed to fail cannot exist
  and `execute` never re-checks the string.

## API Schema

```
namespace http

// The complete set of HTTP methods: the eight defined by RFC 9110 plus PATCH (RFC 5789).
// The set is complete and will not grow.
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

// A caller's request to end an exchange early. Only observed, never fired: a client that receives
// one cannot cancel the caller's operation. Obtain one from a CancellationSource, or none() when
// there is nothing to cancel.
@@finalType
Cancellation {
    // Whether cancellation has been requested. True before any onCancel callback runs.
    @@threadSafe(cancellation)
    bool isCancelled()

    // Registers a callback that runs when cancellation is requested — immediately if it already
    // has been. Release the registration when the exchange ends, or the callback leaks.
    @@threadSafe(cancellation)
    CancellationRegistration onCancel(callback: function<void run()>)

    // The cancellation that never fires, for a call nobody intends to cancel.
    @@static
    Cancellation none()
}

// The handle of a registered callback, so that an observer can stop observing.
@@finalType
CancellationRegistration {
    // Removes the callback. Idempotent.
    @@threadSafe(cancellation)
    void release()
}

// Owns the right to cancel. Kept by whoever starts a call; only the Cancellation is handed out.
@@finalType
CancellationSource {
    @@immutable cancellation: Cancellation

    // Requests cancellation. Idempotent; a second call does nothing. Runs the registered
    // callbacks synchronously on the calling thread.
    @@threadSafe(cancellation)
    void cancel()

    @@static
    CancellationSource create()
}

// Settings an HttpClient applies to every exchange.
HttpConfiguration {
  @@immutable defaultHeaders: map<string, string> // merged into every request; a header set on the request wins
  @@immutable connectTimeout: duration // maximum time to establish a connection
  @@immutable defaultRequestTimeout: duration // request timeout used when HttpRequest.timeout is not set
}

// A single HTTP request.
HttpRequest {
    @@immutable method: HttpMethod
    // Absolute, well-formed URL of the request. Checked when the request is created, so an
    // invalid URL fails there and never reaches `execute`. No base-URL resolution happens here.
    @@urlPattern @@immutable url: string
    @@nullable @@immutable body: bytes // request body; absent for requests without a body
    @@nullable @@immutable timeout: duration // request timeout; if absent, the configuration's defaultRequestTimeout applies
    @@immutable headers: map<string, string>
}

// The response of an HTTP exchange, whatever its status code.
HttpResponse {
    @@immutable statusCode: uint16
    @@immutable body: bytes
    @@immutable headers: map<string, string>
}

// Executes HTTP exchanges. Safe for concurrent use; must be closed to release its resources.
abstraction HttpClient {

    @@immutable configuration:HttpConfiguration
    
    // Executes a single HTTP exchange. A non-2xx status code is NOT an error — it is returned
    // in HttpResponse.statusCode. The URL of the request has already been validated when the
    // request was created. Only the three failures below prevent an exchange from producing a
    // response.
    //   connection-error    — the exchange could not be carried out: DNS failure, connection
    //                         refused, TLS handshake failure, connection reset or truncated
    //                         before the response was complete. Possibly transient.
    //   timeout-error       — configuration.connectTimeout, or the effective request timeout
    //                         (request.timeout ?? configuration.defaultRequestTimeout), elapsed.
    //                         Possibly transient.
    //   cancelled-error     — the cancellation fired before the response was complete. Permanent
    //                         for this call; never retry.
    //   client-closed-error — the client was closed before or while the request was submitted.
    //                         Permanent for this client instance; never retry.
    @@async
    @@threadSafe(client)
    @@throws(connection-error, timeout-error, cancelled-error, client-closed-error)
    HttpResponse execute(request: HttpRequest, cancellation: Cancellation)

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

// Creates the default HttpClient of the SDK, wrapping the language's native HTTP stack.
@@static HttpClient createHttpClient(configuration: HttpConfiguration)
```

## Examples

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

// Nothing to cancel here, so the call passes the cancellation that never fires.
response = await client.execute(request, Cancellation.none())

// The exchange succeeded in both branches — interpreting the status code is up to the caller.
if (response.statusCode == 200) {
    accountJson = parseJson(response.body)
} else if (response.statusCode == 404) {
    accountJson = null
}

// A caller that does want to cancel keeps the source and hands out only the cancellation.
source = CancellationSource.create()
pending = client.execute(request, source.cancellation)   // not awaited yet
source.cancel()                                          // fails `pending` with cancelled-error

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
