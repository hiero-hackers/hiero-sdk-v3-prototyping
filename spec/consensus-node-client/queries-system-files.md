# System File Queries API

This namespace defines typed queries for the system files of the network: files with a fixed number in the shard and
realm of the network whose content has a known format. Each query reads its file and returns the content decoded.

## Description

The network keeps some of its configuration in system files, for example the exchange rates of the native token. A
system file can be read as raw bytes with `FileContentsQuery`, but its content is a protobuf message; the queries of
this namespace read the file and return its content decoded. They are paid queries like `FileContentsQuery`, so you
can bound the spend with `maxQueryPayment`, ask for the price up front with `getCost(client)`, and read the actually
paid `cost` from the `PaidQueryResponse`.

- **`ExchangeRatesQuery`** — reads the exchange rate file of the network and returns the current and the next
  `ExchangeRate` of the native token. Create it with `new ExchangeRatesQuery()` or
  `FileContentsQuery.forExchangeRates()`.

A system file query has no file id to set: the number of its file is fixed, and the shard and realm are those of the
network the query is sent to.

**Exchange rates.**

- `rule:exchange-rates-file` `ExchangeRatesQuery` reads the file `<shard>.<realm>.112`, where shard and realm are
  those of the account of the consensus node the query is sent to. Every consensus node of a network lives in the
  shard and realm of that network, so the query reads the exchange rate file of the network of the client.
- `rule:exchange-rates-decoding` The contents of the file are the protobuf message `ExchangeRateSet`: `currentRate`
  becomes `ExchangeRates.currentRate`, `nextRate` becomes `ExchangeRates.nextRate`; of each `ExchangeRate`,
  `hbarEquiv` becomes `nativeTokenEquivalent`, `centEquiv` becomes `centEquivalent`, and `expirationTime` (whole
  seconds) becomes `expirationTime`; its `nativeTokenUnit` is the one of the network
  (`nativeToken/exchange-rate-unit`). An answer whose contents cannot be decoded, or that lacks one of the two rates,
  is classified `REJECTED`.

## Design Notes

All queries extend `PaidQuery`; see [`queries.md`](queries.md) for the full payment / envelope semantics.

- **A namespace of its own.** The network has several system files with a typed content (the address book `.102`,
  the fee schedule `.111`, the exchange rates `.112`, the throttle definitions `.123`, ...). Kept apart from the
  generic file queries in [`queries-files.md`](queries-files.md), the typed queries and their result types do not
  crowd the file namespace as more of them are added.
- **Only system files every user needs live here.** The exchange rates concern every user of the network. The
  address book `.102` is read by `NodeAddressBookQuery` of the admin client
  ([`queries-network.md`](../consensus-node-admin-client/queries-network.md)): its audience is operator and tooling
  code, which already depends on the admin client.
- **A system file query is no subtype of `FileContentsQuery`.** A subtype would inherit the result type
  `FileContents` (generics are invariant, so `submit` cannot return `ExchangeRates` instead) and a settable `fileId`,
  with which the query could read a file of another format. Each one is a `PaidQuery` of its own; a static factory
  on `FileContentsQuery` (`forExchangeRates()`) keeps it where a user looks for reading a file.
- **No `fileId`.** The file id depends on the shard and realm of the network (`rule:exchange-rates-file`), which the
  query only knows when it is sent, so it has no attribute for it. This follows the V2 SDKs, which compute the id
  with `FileId.getExchangeRatesFileIdFor(shard, realm)` since they support other shards and realms.
- **A decoding failure is `REJECTED`.** `submit` declares no error of its own (it is inherited from `Submittable`),
  and `AttemptOutcome.REJECTED` already covers an answer that could not be read: the retry policy gives up and
  `submit` fails with `attempts-exhausted-error` (`rule:exchange-rates-decoding`).

## API Schema

```
namespace consensusnode.queries.systemFiles
requires {PaidQuery} from consensusnode.queries
requires {ExchangeRate} from nativeToken

// The current and the next exchange rate of the native token, as stored in the exchange rate file of the network.
// Returned by `ExchangeRatesQuery`.
@@finalType
ExchangeRates {
    @@immutable currentRate: ExchangeRate   // the rate that is valid now
    @@immutable nextRate: ExchangeRate      // the rate that becomes valid when `currentRate` expires
}

// Paid query for the exchange rates of the network. Reads the exchange rate file of the network the query is sent
// to and returns its content decoded; there is no file id to set.
@@finalType
ExchangeRatesQuery extends PaidQuery<ExchangeRates> {
}
```

## Examples

### Read the exchange rates of the network (paid)

```
HieroClient client = ...;

ExchangeRates rates = new ExchangeRatesQuery()
    .submit(client)
    .value;

ExchangeRate current = rates.currentRate;
int32 cents = current.centEquivalent;              // `cents` US cents ...
int32 tokens = current.nativeTokenEquivalent;      // ... for `tokens` tokens ...
NativeTokenUnit unit = current.nativeTokenUnit;    // ... in this unit (for Hedera: HBAR)
```

How to convert an amount with these numbers is shown in the examples of the native token.

## Testing

Offline tests of the system file queries and their result types, written as given / when / then. Nothing is
submitted.

```tests
// Source: the Java SDK (ExchangeRatesTest) for the two rates: 580150 resp. 587660 cents for 30000 HBAR, expiring at
// 1645714800 resp. 1645718400, i.e. 2022-02-24T15:00:00Z resp. 16:00:00Z.
@@testId("exchange-rates-keep-the-current-and-the-next-rate")
test "exchange rates keep the current and the next rate" {
    // given
    let current = ExchangeRate{nativeTokenEquivalent: 30000, nativeTokenUnit: hedera.HbarUnit.HBAR,
            centEquivalent: 580150, expirationTime: toZonedDateTime(text: "2022-02-24T15:00:00Z")}
    let next = ExchangeRate{nativeTokenEquivalent: 30000, nativeTokenUnit: hedera.HbarUnit.HBAR,
            centEquivalent: 587660, expirationTime: toZonedDateTime(text: "2022-02-24T16:00:00Z")}

    // when
    let rates = ExchangeRates{currentRate: current, nextRate: next}

    // then
    assertEquals(expected: current, actual: rates.currentRate)
    assertEquals(expected: next, actual: rates.nextRate)
    assertNotEquals(unexpected: rates.currentRate, actual: rates.nextRate)
}

// Source: this spec (a system file query has no file id to set; a new query has no payment limit).
@@testId("a-new-exchange-rates-query-has-no-payment-limit")
test "a new exchange rates query has no payment limit" {
    // when
    let query = ExchangeRatesQuery{}

    // then
    assertNull(actual: query.maxQueryPayment)
}
```

## Questions & Comments

- **One factory per system file on `FileContentsQuery`?** `forExchangeRates()` makes the file namespace depend on
  this one, and every further system file query adds a factory there. `new ExchangeRatesQuery()` works without it.
  Keep the factories for discoverability, or drop them? — open
