# Schedule Queries API

This namespace defines the read-side counterpart to the schedule transactions in
[`transactions-schedule.md`](transactions-schedule.md). One paid query is exposed:
`ScheduleInfoQuery` returns the current state of a stored schedule.

## Description

`ScheduleInfoQuery` is a paid query that returns the current state of a stored schedule: who created it, who pays
the fee of the scheduled transaction, the scheduled transaction itself, the keys that have signed so far, and whether
the schedule is still pending, has executed or was deleted.

### Pending, executed or deleted

A schedule is always in exactly one of three states, which you can read from `executionTime` and `deletionTime`
(at most one of them is set):

- **pending** — neither timestamp is set; the schedule is still collecting signatures (or, for a long-term
  schedule, waiting for `expirationTime`).
- **executed** — `executionTime` is the consensus time at which the scheduled transaction ran after its required
  signatures were collected.
- **deleted** — `deletionTime` is the consensus time at which the schedule was deleted before it could execute.

A deleted or executed schedule remains queryable for a while, so its state is part of the result rather than a
query failure.

### The scheduled transaction

`scheduledTransaction` is a read-only reconstruction of the stored transaction, of the same type you pass when
creating a schedule. It is never packed or signed. To read the *outcome* of an executed schedule, pass
`scheduledTransactionId` to `Transaction.getResponse(...)`.

### Signers and admin key

`signers` lists the individual public keys whose signatures have already been credited to the scheduled
transaction. `adminKey`, in contrast, is an `Authority`: the requirement that must be met to delete the schedule.

## Design Notes

This namespace is the read-side counterpart to the schedule transactions in
[`transactions-schedule.md`](transactions-schedule.md). The query extends `PaidQuery`; see
[`queries.md`](queries.md) for the full payment / envelope semantics.

The state is captured by `@@oneOrNoneOf(executionTime, deletionTime)`. Unlike `TopicInfo` (where a removed topic
disappears from the consensus node), a deleted or executed schedule remains queryable until it is reaped, so the
state is modelled as data on the snapshot rather than as a query failure.

The captured inner transaction is exposed as a `Transaction<ANY, ANY>` — the same builder type `ScheduleCreate`
accepts (see [`transactions-schedule.md`](transactions-schedule.md)). To read the outcome of an executed schedule,
use `scheduledTransactionId` with the `Transaction.getResponse(...)` factory in [`transactions.md`](transactions.md),
exactly as shown in the schedule examples.

`signers` is `list<PublicKey>` (concrete keys that have signed), not `list<Authority>` — a signer is a single key
that produced a signature, whereas an `Authority` is an authorization *requirement* (see
[`authority.md`](../base/authority.md)).

## API Schema

```
namespace consensusnode.queries.schedule
requires {Address, AccountId, TransactionId} from ledger
requires {PublicKey} from keys
requires {Authority} from authority
requires {PaidQuery} from consensusnode.queries
requires {Transaction} from consensusnode.transactions

// Full metadata snapshot of a schedule. Returned by `ScheduleInfoQuery`. At most one of `executionTime` and
// `deletionTime` is set; if neither is set, the schedule is still pending.
@@oneOrNoneOf(executionTime, deletionTime)
type ScheduleInfo {
    @@immutable scheduleId: Address                        // the id of the schedule
    @@immutable creatorAccountId: AccountId                // account that created the schedule
    @@immutable payerAccountId: AccountId                  // pays the inner transaction's fee when it executes
    @@immutable scheduledTransaction: Transaction<ANY, ANY>     // read-only reconstruction of the captured inner transaction
    @@immutable scheduledTransactionId: TransactionId      // the id the inner transaction carries when it executes (scheduled flag set)
    @@immutable @@default([]) signers: list<PublicKey>     // public keys whose signatures have been credited so far
    @@immutable @@nullable adminKey: Authority             // may delete the schedule; absent if the schedule is immutable
    @@immutable @@nullable scheduleMemo: string            // free-form memo on the schedule entity
    @@immutable expirationTime: zonedDateTime              // when the schedule expires (the requested long-term expiry, or the default expiry)
    @@immutable @@default(false) waitForExpiry: bool       // if true, execute only at expirationTime even if all signatures are collected earlier (long-term schedule, HIP-423)
    @@immutable @@nullable executionTime: zonedDateTime    // set once the inner transaction has executed
    @@immutable @@nullable deletionTime: zonedDateTime     // set once the schedule has been deleted before execution
}

// Paid query for the current state of a stored schedule.
@@finalType
ScheduleInfoQuery extends PaidQuery<ScheduleInfo> {
    @@immutable scheduleId: Address
}
```

## Examples

### Read a schedule's state (paid)

```
HieroClient client = ...;

ScheduleInfo info = new ScheduleInfoQuery()
    .scheduleId(scheduleId)
    .submit(client)
    .value;

list<PublicKey> signed = info.signers;            // keys credited so far
Authority       admin  = info.adminKey;            // null if the schedule is immutable
```

### Branch on pending / executed / deleted

```
ScheduleInfo info = new ScheduleInfoQuery()
    .scheduleId(scheduleId)
    .submit(client)
    .value;

if (info.executionTime != null) {
    // executed — read the inner transaction's outcome with its type token
    Response<TransferReceipt> innerResponse =
        Transaction.getResponse(info.scheduledTransactionId, TransferTransaction, client);
    TransferReceipt innerReceipt = innerResponse.queryReceipt();
} else if (info.deletionTime != null) {
    // deleted before execution
} else {
    // still pending — collect more signatures via ScheduleSign
}
```

## Questions & Comments

- **`signers` is `list<PublicKey>`, not `list<Authority>`.** A credited signer is always a single
  concrete public key that produced a valid signature; the recursive `Authority` sum type
  (single key / contract / m-of-n) models an authorization *requirement*, not a signature that has
  been collected. HAPI returns `signers` as a `KeyList`, but every entry is a simple key, so the
  flatter `list<PublicKey>` is both honest and lighter. `adminKey` stays an `Authority` because it
  *is* a requirement (matching `ScheduleCreateTransaction.adminKey` in
  [`transactions-schedule.md`](transactions-schedule.md)).

- **The inner transaction is `Transaction<ANY, ANY>`.** Same modelling choice as
  `ScheduleCreateTransaction.scheduledTransaction`: the captured body has no compile-time receipt
  type. Reading its execution outcome therefore re-supplies the inner transaction's type token to
  `Transaction.getResponse(...)` (see [`transactions.md`](transactions.md) and the schedule
  examples). The snapshot exposes the *captured body*; it is never packed or signed.

- **No `deleted` boolean — state is the `@@oneOrNoneOf` timestamp pair.** Rather than a flag plus a
  timestamp, the executed/deleted/pending state is read directly from which (if either) of
  `executionTime` / `deletionTime` is set, matching HAPI's `oneof { execution_time, deletion_time }`.
  A pending schedule has neither.

- **No `ledgerId` on `ScheduleInfo`.** Matches `TopicInfo` / `AccountInfo` / `FileInfo` — the caller
  already knows the ledger via their `HieroClient`, and ledger-origin metadata belongs on the
  response envelope, not on every payload. See the open envelope-level question in
  [`queries.md`](queries.md) *Questions & Comments*.

- **`customFeeLimits` (HIP-991) is absent**, consistent with its absence on
  `ScheduleCreateTransaction` — it depends on the write-side custom-fee model not yet specified in
  V3 (§3.3 in [`missing-features.md`](../../docs/missing-features.md)). Additive once that lands.
