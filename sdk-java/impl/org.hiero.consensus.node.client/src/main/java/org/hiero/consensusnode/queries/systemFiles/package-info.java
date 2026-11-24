/// The network keeps some of its configuration in system files, for example the exchange rates of the native token. A
/// system file can be read as raw bytes with `FileContentsQuery`, but its content is a protobuf message; the queries of
/// this namespace read the file and return its content decoded. They are paid queries like `FileContentsQuery`, so you
/// can bound the spend with `maxQueryPayment`, ask for the price up front with `getCost(client)`, and read the actually
/// paid `cost` from the `PaidQueryResponse`.
///
/// - **`ExchangeRatesQuery`** — reads the exchange rate file of the network and returns the current and the next
///   `ExchangeRate` of the native token. Create it with `new ExchangeRatesQuery()` or
///   `FileContentsQuery.forExchangeRates()`.
///
/// A system file query has no file id to set: the number of its file is fixed, and the shard and realm are those of the
/// network the query is sent to.
///
/// **Exchange rates.**
///
/// - `rule:exchange-rates-file` `ExchangeRatesQuery` reads the file `<shard>.<realm>.112`, where shard and realm are
///   those of the account of the consensus node the query is sent to. Every consensus node of a network lives in the
///   shard and realm of that network, so the query reads the exchange rate file of the network of the client.
/// - `rule:exchange-rates-decoding` The contents of the file are the protobuf message `ExchangeRateSet`: `currentRate`
///   becomes `ExchangeRates.currentRate`, `nextRate` becomes `ExchangeRates.nextRate`; of each `ExchangeRate`,
///   `hbarEquiv` becomes `nativeTokenEquivalent`, `centEquiv` becomes `centEquivalent`, and `expirationTime` (whole
///   seconds) becomes `expirationTime`; its `nativeTokenUnit` is the one of the network
///   (`nativeToken/exchange-rate-unit`). An answer whose contents cannot be decoded, or that lacks one of the two rates,
///   is classified `REJECTED`.
package org.hiero.consensusnode.queries.systemFiles;
