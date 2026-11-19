/// The network computes the fee of a transaction or query from the resources it uses (bytes, signature verifications,
/// storage, gas, ...) and the price of each resource. The prices are kept in the fee schedule file of the network, a
/// system file. `FeeScheduleQuery` reads it and returns the content decoded. It is a paid query like
/// `FileContentsQuery`, so you can bound the spend with `maxQueryPayment`, ask for the price up front with
/// `getCost(client)`, and read the actually paid `cost` from the `PaidQueryResponse`.
///
/// - **`FeeScheduleQuery`** — reads the fee schedule file and returns the current and the next `FeeSchedule` as
///   `FeeSchedules`. Create it with `new FeeScheduleQuery()` or `FileContentsQuery.forFeeSchedule()`. It has no file id
///   to set.
/// - **`FeeSchedule`** — the prices of every request type until `expirationTime`: one `TransactionFeeSchedule` per
///   `RequestType`, which holds one `FeeData` per `FeeSubType` (for example a token mint of a fungible and of a
///   non-fungible token).
/// - **`FeeData`** — the prices for the node that receives the request, for the network that brings it to consensus,
///   and for the service that executes it, each as `FeeComponents`.
///
/// The prices are not amounts of the native token: they are given in thousandths of a tinycent (1 tinycent =
/// 10<sup>-8</sup> US cents) and converted with the exchange rate of the network (`ExchangeRatesQuery`) when a fee is
/// charged.
///
/// The request type of an entry is a `RequestType` (`consensusnode.requestTypes`), the request types the SDK knows from
/// its registered `RequestTypeSupport`s. `FeeSubType` is open as well: a network of a newer version can name sub types
/// this SDK does not know yet; `BasicFeeSubType` names the ones of the protocol the SDK is built for.
///
/// **Fee schedule.**
///
/// - `rule:fee-schedule-file` `FeeScheduleQuery` reads the file `<shard>.<realm>.111`, where shard and realm are those
///   of the account of the consensus node the query is sent to. Every consensus node of a network lives in the shard and
///   realm of that network, so the query reads the fee schedule file of the network of the client.
/// - `rule:fee-schedule-decoding` The contents of the file are the protobuf message `CurrentAndNextFeeSchedule`:
///   `currentFeeSchedule` becomes `FeeSchedules.currentSchedule`, `nextFeeSchedule` becomes `FeeSchedules.nextSchedule`.
///   Of each `FeeSchedule`, `transactionFeeSchedule` becomes `transactionFeeSchedules` and `expiryTime` (whole seconds)
///   becomes `expirationTime`. Of each `TransactionFeeSchedule`, `hederaFunctionality` becomes `requestType` and `fees`
///   becomes `fees`; if `fees` is empty, the deprecated `feeData`, if set, is its only entry. Of each `FeeData`,
///   `nodedata`, `networkdata` and `servicedata` become `nodeData`, `networkData` and `serviceData`, `subType` becomes
///   `subType`; a missing `FeeComponents` message becomes one with every price 0. An answer whose contents cannot be
///   decoded, or that lacks one of the two schedules, is classified `REJECTED`.
/// - `rule:fee-schedule-request-types` The request type of an entry is the one `getRequestType(code)` returns for its
///   code, i.e. the request type of the registered `RequestTypeSupport` that names it.
/// - `rule:fee-schedule-unknown-codes` A `TransactionFeeSchedule` whose request type code no registered
///   `RequestTypeSupport` names, and a `FeeData` whose sub type code is not a `BasicFeeSubType`, are left out of the
///   result; the rest of the schedule is returned. The query does not fail because the network knows more request types
///   than the SDK.
package org.hiero.consensusnode.queries.fees;
