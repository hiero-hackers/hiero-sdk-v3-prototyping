# Queries API

This namespace defines the base abstractions for queries against the consensus node. Concrete
query types (e.g. `AccountBalanceQuery`, `TokenInfoQuery`, `ContractCallQuery`) live in
service-specific sub-namespaces; this file defines only the two shared abstractions and the
cost-discovery shape.

## Description

A query is a read-only request to a consensus node that returns a typed result without changing ledger state. There
are two kinds of queries:

- **`Query<$$Result>`** — a free query that the network answers without charging you, for example an account
  balance or a transaction receipt.
- **`PaidQuery<$$Result>`** — a query that must be paid for in the network's native token. It adds `getCost` to
  ask for the current price and `maxQueryPayment` to cap what you are willing to pay. Paid queries are always paid
  by the client's operator account.

Both are `Submittable`, so they share the retry settings (`maxAttempts`, `minBackoff`, `maxBackoff`,
`attemptTimeout`) and are sent with `submit(client)`. Concrete query types (such as `AccountBalanceQuery` or
`TokenInfoQuery`) only add their own inputs and result type.

### Payment

You never build the payment of a paid query yourself. On every `submit(client)` the SDK first asks the network for
the current price of the query, then pays exactly that amount from the operator account to the answering node and
sends the actual query together with the payment. If `maxQueryPayment` is set and the quoted price is higher, the
call fails before anything is paid.

Use `getCost(client)` if you need to know the price before submitting, for example to ask a user for confirmation.
The returned price is a snapshot, not a reservation — the price may change before you submit. Set
`maxQueryPayment` if you need a hard upper bound.

### Responses

`submit(client)` returns the typed payload wrapped in an envelope:

- `QueryResponse<$$Result>` contains the `value` and the consensus node that answered (`answeredBy`).
- `PaidQueryResponse<$$Result>` additionally contains the `cost` that was actually paid.

If you only need the payload, read `.value`: `query.submit(client).value`. Use the envelope when you need the
metadata, for example for billing or auditing.

## Design Notes

The query is paid by the client's operator; see
[ADR-0002](../../docs/adr/0002-defer-paid-query-payer-customization.md) for the rationale behind deferring a
configurable payer.

`Query` and `PaidQuery` both extend `Submittable<$$Result>` (defined in `consensusnode.client`), the shared execution
abstraction that also backs `PackedTransaction`.

The split between `Query` and `PaidQuery` mirrors the protocol distinction the consensus node
makes between free and paid queries, and makes the difference visible at the type level rather
than at runtime. A caller holding a `Query` cannot configure payment that would have no effect;
a caller holding a `PaidQuery` is reminded by the type that payment must be considered.
Promotion from free to paid (should network policy ever change) is a breaking type change
rather than a silent behavioural one.

Payment model: the consensus node accepts two response modes for a paid query: `COST_ANSWER` returns only the
quoted price; `ANSWER_ONLY` returns the actual data and requires a signed payment transaction
attached to the request. Every `submit(client)` on a `PaidQuery` orchestrates both transparently:

1. The SDK issues a `COST_ANSWER` round-trip to obtain the network's current price for the
   query.
2. If `maxQueryPayment` is set and the quoted price exceeds it, the call fails with
   `max-query-payment-exceeded-error` and no payment is made.
3. Otherwise the SDK builds and signs a payment transaction (a transfer of the quoted amount
   from the client's operator to the chosen consensus node) and issues the `ANSWER_ONLY`
   round-trip carrying that payment.

The auto-discovered price always reflects the network's current state, so there is no exact-amount escape hatch.
The query is paid by the client's operator in this revision; a configurable payer is deferred and tracked in
[ADR-0002](../../docs/adr/0002-defer-paid-query-payer-customization.md). `getCost(client)` exposes the
`COST_ANSWER` round-trip as a separate operation.

The response envelopes are structurally parallel to `Response<$$Receipt>` on the transaction side but lighter —
queries are synchronous, so the envelope is purely a metadata wrapper, not a handle to
deferred work. The free/paid distinction is reflected in the envelope hierarchy: `cost` lives only on
`PaidQueryResponse` because it would be `0` and meaningless on free queries. The same
type-level honesty as the `Query` / `PaidQuery` split itself.

## API Schema

```
namespace consensusnode.queries
requires {AccountId} from ledger
requires {HieroClient, Submittable} from consensusnode.client
requires {NativeToken} from nativeToken

// Envelope around the typed result of a `Query`. Contains the payload plus metadata about how the answer was
// obtained. Returned by `Query.submit()`.
type QueryResponse<$$T> {
    @@immutable value: $$T              // the typed result of the query
    @@immutable answeredBy: AccountId   // consensus node that produced this answer
}

// Envelope around the typed result of a `PaidQuery`. In addition to the payload it contains the amount that was
// actually paid. Returned by `PaidQuery.submit()`.
type PaidQueryResponse<$$T> extends QueryResponse<$$T> {
    @@immutable cost: NativeToken<ANY, ANY>   // amount transferred to the answering node
}

// A read-only request to a consensus node. Direct subtypes are free queries that the network answers without
// charging the caller (for example account balance or transaction receipt); queries that require payment are
// `PaidQuery` subtypes.
//
// Use the inherited retry settings to tune how the request is sent and `submit(client)` to send it. The result
// is returned wrapped in a `QueryResponse`.
abstraction Query<$$Result> extends Submittable<QueryResponse<$$Result>> {
}

// A query whose answer must be paid for in the network's native token. The price is determined from the
// network's current fee schedule on each `submit()` or `getCost()` call and paid by the client's operator.
// Use `maxQueryPayment` to bound the spend and read `cost` from the returned `PaidQueryResponse` to see what was
// actually charged.
//
// `submit()` fails without paying anything if the quoted price exceeds `maxQueryPayment`.
abstraction PaidQuery<$$Result> extends Query<$$Result> {

    // Upper bound on the price you are willing to pay. If the price quoted by the network exceeds this limit,
    // `submit()` and `getCost()` fail and no payment is made. If absent, the quoted price is always paid.
    @@nullable maxQueryPayment: NativeToken<ANY, ANY>
    
    // Sends the query, paying the quoted price from the operator account, and returns the result together with
    // the amount actually paid.
    @@async PaidQueryResponse<$$Result> submit(client: HieroClient<ANY>)

    // Asks the network for the current price of this query without executing it. The returned value is a
    // snapshot — a subsequent call may return a different price as conditions change.
    // Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @@async NativeToken<ANY, ANY> getCost(client: HieroClient<ANY>)
}
```

## Examples

The examples below reference hypothetical concrete query types (`AccountBalanceQuery`,
`AccountInfoQuery`) defined in `consensusnode.queries.accounts`. The patterns apply to every
concrete query.

### 1. Free query

```
HieroClient client = ...;

AccountBalance balance = new AccountBalanceQuery()
    .accountId(...)
    .submit(client)
    .value;
```

No payment, no cost discovery, no `maxQueryPayment`. `.value` unwraps the `QueryResponse`
envelope for callers who only need the payload.

### 2. Paid query, default payment

```
AccountInfo info = new AccountInfoQuery()
    .accountId(...)
    .submit(client)
    .value;
```

`maxQueryPayment` is unset. The SDK quotes the price via a cost round-trip and uses the
quoted amount, paid from the client's operator. If the operator has insufficient balance,
the underlying payment transaction fails and the error propagates out of `submit(...)`.

### 3. Paid query with an explicit payment ceiling

```
AccountInfo info = new AccountInfoQuery()
    .accountId(...)
    .maxQueryPayment(NativeToken.of(...))   // refuse to pay more than this
    .submit(client)
    .value;
```

The SDK still quotes via cost discovery, but aborts with `max-query-payment-exceeded-error`
if the quote exceeds the configured ceiling.

### 4. Confirm price up-front

```
PaidQuery<AccountInfo> query = new AccountInfoQuery().accountId(...);

NativeToken<ANY, ANY> cost = query.getCost(client);
// show cost to the user, wait for approval, then:

AccountInfo info = query.submit(client).value;
```

`getCost(client)` performs the cost round-trip only. The subsequent `submit(client)` performs
its own (independent) cost round-trip — the price may have changed between the two calls.
Use `maxQueryPayment` if a hard upper bound is needed.

### 5. Read envelope metadata (audit / billing)

```
PaidQueryResponse<AccountInfo> response = new AccountInfoQuery()
    .accountId(...)
    .submit(client);

AccountInfo info        = response.value;
NativeToken<ANY, ANY> c = response.cost;        // amount actually charged
AccountId answeringNode  = response.answeredBy;  // which node served the query

auditLog.record(info.accountId, c, answeringNode);
```

For free queries, `QueryResponse` carries `value` and `answeredBy`; `cost` is only present on
`PaidQueryResponse` and is therefore unreachable on free-query responses by construction.

## Questions & Comments

- **Envelope adds a `.value` unwrap step.** Every `submit(client)` callsite now ends in
  `.value` (or binds the full envelope when metadata is needed). The friction is real for
  the 95% case that only wants the payload; the wrapper is justified by giving billing /
  audit code a structured place to read `cost` and `answeredBy` without a separate API. If
  this friction proves unacceptable in practice, a future `submitValue(client)` shortcut
  could return the unwrapped payload directly — additive change, no breakage of the
  envelope-returning method.
- **`getCost()` is non-binding.** The returned amount is a quote, not a reservation. The
  network may quote a different price on the subsequent `submit(client)` round-trip. Callers
  who need a hard upper bound must set `maxQueryPayment` rather than relying on a
  `getCost()` value remaining current.
- **No explicit payment override.** PaidQuery deliberately does not expose a
  `queryPayment`-style field that would let the caller pin the exact amount transferred.
  Auto-discovery always runs, so the price reflects the network's current schedule, and
  `maxQueryPayment` covers ceiling semantics. If a real use-case appears (e.g. high-frequency
  signed-fee-cache workflows), an explicit payment field could be added back as an additive
  change — but only with the over/underpayment footguns clearly documented.
- **Free vs. paid is a type-level distinction.** A concrete query that today is free but might
  become paid would migrate from `Query` to `PaidQuery` — a breaking change by design.
- **Configurable payer is deferred.** Today every `PaidQuery` is funded by the client's
  operator. The design space for letting another account pay (paymaster, custodial, audit
  account) is captured in
  [ADR-0002](../../docs/adr/0002-defer-paid-query-payer-customization.md), which compares
  an in-memory `payer: Account` variant against an external-signer `payerId + payerSigner`
  variant and explains why both are deferred. Workaround today: instantiate a separate
  `HieroClient` with the sponsor as operator.
- **Per-query retry semantics** (e.g. `TransactionReceiptQuery` polling on `RECEIPT_NOT_FOUND`
  / `UNKNOWN` until consensus is reached) are concrete-query concerns and will be modelled
  where those queries are defined; the base abstractions deliberately do not encode them.
- **Streaming queries** (e.g. topic message subscriptions) do not fit this request/response
  shape and are modelled separately under `mirrornode.topic` / `enterprise.service.topic` as
  `@@streaming` operations.
- **Should `QueryResponse` carry the ledger origin?** HAPI's `*GetInfoResponse` messages all
  embed a `ledger_id` so a detached payload (mirrored, archived, replayed) can still be
  attributed to its source ledger. The V3 spec drops it from every Info payload (`AccountInfo`
  in [`queries-accounts.md`](queries-accounts.md), `FileInfo` in
  [`queries-files.md`](queries-files.md), and any future Info type) because the caller
  already knows the ledger via their `HieroClient` and duplicating it on every typed payload
  is bloat. The open question is whether to surface it *once* on the envelope — analogous to
  the existing `answeredBy: AccountId` — as
  `@@immutable ledgerId: bytes` (or `LedgerId`, once typed) on `QueryResponse<$$T>`. That
  keeps the metadata available for serialize-then-archive workflows without polluting every
  payload, and applies uniformly to free and paid queries. Defer until a concrete archival /
  audit use-case shows up.
