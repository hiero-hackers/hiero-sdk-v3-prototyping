# Fee Schedule Queries API

This namespace defines the query for the fee schedule of the network and the types of its content: the prices the
network charges per resource for every transaction and query type.

## Description

The network computes the fee of a transaction or query from the resources it uses (bytes, signature verifications,
storage, gas, ...) and the price of each resource. The prices are kept in the fee schedule file of the network, a
system file. `FeeScheduleQuery` reads it and returns the content decoded. It is a paid query like
`FileContentsQuery`, so you can bound the spend with `maxQueryPayment`, ask for the price up front with
`getCost(client)`, and read the actually paid `cost` from the `PaidQueryResponse`.

- **`FeeScheduleQuery`** — reads the fee schedule file and returns the current and the next `FeeSchedule` as
  `FeeSchedules`. Create it with `new FeeScheduleQuery()` or `FileContentsQuery.forFeeSchedule()`. It has no file id
  to set.
- **`FeeSchedule`** — the prices of every request type until `expirationTime`: one `TransactionFeeSchedule` per
  `RequestType`, which holds one `FeeData` per `FeeSubType` (for example a token mint of a fungible and of a
  non-fungible token).
- **`FeeData`** — the prices for the node that receives the request, for the network that brings it to consensus,
  and for the service that executes it, each as `FeeComponents`.

The prices are not amounts of the native token: they are given in thousandths of a tinycent (1 tinycent =
10<sup>-8</sup> US cents) and converted with the exchange rate of the network (`ExchangeRatesQuery`) when a fee is
charged.

The request type of an entry is a `RequestType` (`consensusnode.requestTypes`), the request types the SDK knows from
its registered `RequestTypeSupport`s. `FeeSubType` is open as well: a network of a newer version can name sub types
this SDK does not know yet; `BasicFeeSubType` names the ones of the protocol the SDK is built for.

**Fee schedule.**

- `rule:fee-schedule-file` `FeeScheduleQuery` reads the file `<shard>.<realm>.111`, where shard and realm are those
  of the account of the consensus node the query is sent to. Every consensus node of a network lives in the shard and
  realm of that network, so the query reads the fee schedule file of the network of the client.
- `rule:fee-schedule-decoding` The contents of the file are the protobuf message `CurrentAndNextFeeSchedule`:
  `currentFeeSchedule` becomes `FeeSchedules.currentSchedule`, `nextFeeSchedule` becomes `FeeSchedules.nextSchedule`.
  Of each `FeeSchedule`, `transactionFeeSchedule` becomes `transactionFeeSchedules` and `expiryTime` (whole seconds)
  becomes `expirationTime`. Of each `TransactionFeeSchedule`, `hederaFunctionality` becomes `requestType` and `fees`
  becomes `fees`; if `fees` is empty, the deprecated `feeData`, if set, is its only entry. Of each `FeeData`,
  `nodedata`, `networkdata` and `servicedata` become `nodeData`, `networkData` and `serviceData`, `subType` becomes
  `subType`; a missing `FeeComponents` message becomes one with every price 0. An answer whose contents cannot be
  decoded, or that lacks one of the two schedules, is classified `REJECTED`.
- `rule:fee-schedule-request-types` The request type of an entry is the one `getRequestType(code)` returns for its
  code, i.e. the request type of the registered `RequestTypeSupport` that names it.
- `rule:fee-schedule-unknown-codes` A `TransactionFeeSchedule` whose request type code no registered
  `RequestTypeSupport` names, and a `FeeData` whose sub type code is not a `BasicFeeSubType`, are left out of the
  result; the rest of the schedule is returned. The query does not fail because the network knows more request types
  than the SDK.

## Design Notes

All queries extend `PaidQuery`; see [`queries.md`](queries.md) for the full payment / envelope semantics. The query
follows `ExchangeRatesQuery` ([`queries-system-files.md`](queries-system-files.md)): no subtype of
`FileContentsQuery`, no `fileId`, a factory on `FileContentsQuery`, and a decoding failure classified `REJECTED`.

- **A namespace of its own, not `consensusnode.queries.systemFiles`.** The fee schedule brings six types of its own
  (`FeeSchedules`, `FeeSchedule`, `TransactionFeeSchedule`, `FeeData`, `FeeComponents`, `FeeSubType` and its enum).
  They are a model of the fees of the network,
  which a later fee estimation can reuse without depending on the system file queries.
- **`FeeSchedules`, not `CurrentAndNextFeeSchedule`.** The result type is named like `ExchangeRates` (current and
  next rate), and its fields like `ExchangeRates.currentRate` / `nextRate`.
- **`RequestType` is not a type of the fees.** The request types are a concept of the protocol beyond fees and are
  extended by modules and custom services, so they live in `consensusnode.requestTypes` and are registered through
  the SPI (`RequestTypeSupport`), like the status codes.
- **Unknown codes are dropped, not rejected.** `TransactionStatus` rejects an unknown code (`unknown-status-error`)
  because the status decides how a request goes on. The fee schedule is data: one entry of a request type the SDK
  cannot name must not make the prices of every other request type unreadable (`rule:fee-schedule-unknown-codes`).
- **`FeeSubType`, not `SubType`.** `SubType` alone does not say of what; the type is only used by fees. Its values
  are the ones of `SubType` in `basic_types.proto`, taken over mechanically like the request types. It has no support
  in the SPI: the sub types are a detail of the fee calculation of the network, not of a service.
- **Field names of `FeeComponents`.** The protocol abbreviates (`bpt`, `vpt`, `rbh`, `sbh`, `tv`, `bpr`, `sbpr`);
  the fields spell them out. `transferVolume` drops the `Hbar` of the V2 name
  (`transferVolumeHbar`), since the native token depends on the network.
- **Sets, not lists.** A fee schedule has at most one entry per request type, a request type at most one entry per sub
  type, and their order in the file has no meaning.
- **`int64` for every price.** Like every monetary value, the prices are whole numbers, never floating point.

## API Schema

```
namespace consensusnode.queries.fees
requires {PaidQuery} from consensusnode.queries
requires {RequestType} from consensusnode.requestTypes

// A variant of a request type with prices of its own, for example the mint of a fungible and of a non-fungible
// token. The set of sub types is open: a network of a newer version can define sub types this SDK does not know.
abstraction FeeSubType {
    @@immutable code: int32 // the numeric code of the sub type in the protocol
}

// The fee sub types of the protocol the SDK is built for, with their codes.
enum BasicFeeSubType(code: int32) extends FeeSubType {
    // The resource cost for the transaction type has no additional attributes
    DEFAULT(0)
    // The resource cost for the transaction type includes an operation on a fungible/common token
    TOKEN_FUNGIBLE_COMMON(1)
    // The resource cost for the transaction type includes an operation on a non-fungible/unique token
    TOKEN_NON_FUNGIBLE_UNIQUE(2)
    // The resource cost for the transaction type includes an operation on a fungible/common token with a custom fee
    // schedule
    TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES(3)
    // The resource cost for the transaction type includes an operation on a non-fungible/unique token with a custom
    // fee schedule
    TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES(4)
    // The resource cost for the transaction type includes a ScheduleCreate containing a ContractCall.
    SCHEDULE_CREATE_CONTRACT_CALL(5)
    // The resource cost for the transaction type includes a TopicCreate with custom fees.
    TOPIC_CREATE_WITH_CUSTOM_FEES(6)
    // The resource cost for the transaction type includes a ConsensusSubmitMessage for a topic with custom fees.
    SUBMIT_MESSAGE_WITH_CUSTOM_FEES(7)
    // The resource cost for the transaction type that includes a CryptoTransfer with hook invocations
    CRYPTO_TRANSFER_WITH_HOOKS(8)
}

// The prices of the resources a request uses, in thousandths of a tinycent (1 tinycent = 10^-8 US cents). The fee is
// the sum of each resource's use times its price, plus `constantFee`, bounded by `minimumFee` and `maximumFee`; it
// is converted to the native token with the exchange rate of the network.
@@finalType
FeeComponents {
    @@immutable @@min(0) minimumFee: int64                 // the lowest total fee
    @@immutable @@min(0) maximumFee: int64                 // the highest total fee
    @@immutable @@min(0) constantFee: int64                // the constant part of the fee
    @@immutable @@min(0) transactionBandwidthByte: int64   // price per byte of the request
    @@immutable @@min(0) transactionVerification: int64    // price per signature verification
    @@immutable @@min(0) transactionRamByteHour: int64     // price per byte-hour of memory
    @@immutable @@min(0) transactionStorageByteHour: int64 // price per byte-hour of storage
    @@immutable @@min(0) contractTransactionGas: int64     // price per unit of gas of a contract execution
    @@immutable @@min(0) transferVolume: int64             // price per unit of native token transferred
    @@immutable @@min(0) responseMemoryByte: int64         // price per byte of the response read from memory
    @@immutable @@min(0) responseDiskByte: int64           // price per byte of the response read from storage
}

// The prices of one sub type of a request type, split by who is paid.
@@finalType
FeeData {
    @@immutable nodeData: FeeComponents    // paid to the consensus node that receives the request
    @@immutable networkData: FeeComponents // paid to the network for bringing the request to consensus
    @@immutable serviceData: FeeComponents // paid to the network for executing the request and keeping its state
    @@immutable subType: FeeSubType        // the variant of the request type these prices apply to
}

// The prices of one request type: one `FeeData` per sub type.
@@finalType
TransactionFeeSchedule {
    @@immutable requestType: RequestType // the request type these prices apply to
    @@immutable fees: set<FeeData>       // one entry per sub type; empty if the network names none the SDK knows
}

// The prices of every request type until `expirationTime`.
@@finalType
FeeSchedule {
    // One entry per request type; leaves out the request types the SDK does not know.
    @@immutable transactionFeeSchedules: set<TransactionFeeSchedule>
    @@immutable expirationTime: zonedDateTime // when the schedule expires and the next one becomes valid
}

// The current and the next fee schedule of the network, as stored in its fee schedule file. Returned by
// `FeeScheduleQuery`.
@@finalType
FeeSchedules {
    @@immutable currentSchedule: FeeSchedule // the schedule that is valid now
    @@immutable nextSchedule: FeeSchedule    // the schedule that becomes valid when `currentSchedule` expires
}

// Paid query for the fee schedule of the network. Reads the fee schedule file of the network the query is sent to
// and returns its content decoded; there is no file id to set.
@@finalType
FeeScheduleQuery extends PaidQuery<FeeSchedules> {
}
```

## Examples

### Read the price of a transfer (paid)

```
HieroClient client = ...;

FeeSchedules schedules = new FeeScheduleQuery()
    .submit(client)
    .value;

for (TransactionFeeSchedule entry : schedules.currentSchedule.transactionFeeSchedules) {
    if (entry.requestType == BasicRequestType.CRYPTO_TRANSFER) {
        for (FeeData data : entry.fees) {
            if (data.subType == BasicFeeSubType.DEFAULT) {
                int64 constantPart = data.nodeData.constantFee
                    + data.networkData.constantFee
                    + data.serviceData.constantFee;   // thousandths of a tinycent
            }
        }
    }
}
```

## Testing

Offline tests of the fee schedule query and its types, written as given / when / then. Nothing is submitted.

```tests
// Source: SubType of the HAPI (basic_types.proto).
@@testId("the-basic-fee-sub-types-have-the-codes-of-the-protocol")
test "the basic fee sub types have the codes of the protocol" {
    // when / then
    assertEquals(expected: 0, actual: BasicFeeSubType.DEFAULT.code)
    assertEquals(expected: 1, actual: BasicFeeSubType.TOKEN_FUNGIBLE_COMMON.code)
    assertEquals(expected: 2, actual: BasicFeeSubType.TOKEN_NON_FUNGIBLE_UNIQUE.code)
    assertEquals(expected: 8, actual: BasicFeeSubType.CRYPTO_TRANSFER_WITH_HOOKS.code)
}

// Source: this spec (a fee schedule keeps its prices per request type and sub type).
@@testId("a-fee-schedule-keeps-its-prices")
test "a fee schedule keeps its prices" {
    // given
    let components = FeeComponents{minimumFee: 0, maximumFee: 100, constantFee: 1, transactionBandwidthByte: 2,
            transactionVerification: 3, transactionRamByteHour: 4, transactionStorageByteHour: 5, contractTransactionGas: 6,
            transferVolume: 7, responseMemoryByte: 8, responseDiskByte: 9}
    let data = FeeData{nodeData: components, networkData: components, serviceData: components,
            subType: BasicFeeSubType.DEFAULT}

    // when
    let entry = TransactionFeeSchedule{requestType: consensusnode.requestTypes.BasicRequestType.CRYPTO_TRANSFER,
            fees: [data]}

    // then
    assertEquals(expected: 1, actual: entry.requestType.code)
    assertEquals(expected: 0, actual: data.subType.code)
    assertEquals(expected: 1, actual: data.nodeData.constantFee)
    assertEquals(expected: 9, actual: data.serviceData.responseDiskByte)
}

// Source: this spec (FeeSchedules keeps the current and the next schedule).
@@testId("fee-schedules-keep-the-current-and-the-next-schedule")
test "fee schedules keep the current and the next schedule" {
    // given
    let current = FeeSchedule{transactionFeeSchedules: [],
            expirationTime: toZonedDateTime(text: "2022-02-24T15:00:00Z")}
    let next = FeeSchedule{transactionFeeSchedules: [],
            expirationTime: toZonedDateTime(text: "2022-02-24T16:00:00Z")}

    // when
    let schedules = FeeSchedules{currentSchedule: current, nextSchedule: next}

    // then
    assertEquals(expected: current, actual: schedules.currentSchedule)
    assertEquals(expected: next, actual: schedules.nextSchedule)
    assertNotEquals(unexpected: schedules.currentSchedule, actual: schedules.nextSchedule)
}

// Source: this spec (a system file query has no file id to set; a new query has no payment limit).
@@testId("a-new-fee-schedule-query-has-no-payment-limit")
test "a new fee schedule query has no payment limit" {
    // when
    let query = FeeScheduleQuery{}

    // then
    assertNull(actual: query.maxQueryPayment)
}
```

## Questions & Comments

- **Drop unknown entries or keep their code?** `rule:fee-schedule-unknown-codes` leaves out what the SDK cannot name,
  so a caller cannot see the prices of a newer request type at all. The alternative is a concrete type that carries an
  unknown code. — open
- **Lookup by request type.** Finding the prices of one request type means iterating `transactionFeeSchedules`. A
  `map<RequestType, TransactionFeeSchedule>` or a lookup method would be shorter, but repeats the key of each entry.
  — open
