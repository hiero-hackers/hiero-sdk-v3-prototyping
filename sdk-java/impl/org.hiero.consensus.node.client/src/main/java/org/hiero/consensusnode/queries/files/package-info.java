/// Queries for reading files stored on the network. Both queries are paid, so you can bound the spend with
/// `maxQueryPayment`, ask for the price up front with `getCost(client)`, and read the actually paid `cost` from the
/// `PaidQueryResponse`.
///
/// - **`FileContentsQuery`** — returns the raw bytes of a file. The price grows roughly with the file size; set
///   `maxQueryPayment` when you fetch large files.
/// - **`FileInfoQuery`** — returns the file's metadata: size, expiration time, deletion flag, write-access authority
///   and memo. It does not return the content; use `FileContentsQuery` for that.
///
/// Some files of the network are system files with a known content, such as the exchange rates. Read them with the
/// typed queries of `consensusnode.queries.systemFiles` and the fee schedule with `FeeScheduleQuery`
/// (`consensusnode.queries.fees`), which return the content decoded; `FileContentsQuery` offers factories for them
/// (`FileContentsQuery.forExchangeRates()`, `FileContentsQuery.forFeeSchedule()`).
///
/// Querying a deleted file does not fail: `FileInfoQuery` returns a `FileInfo` with `deleted = true` (most other fields
/// cleared), and `FileContentsQuery` returns empty `contents`. Check `FileInfo.deleted` instead of treating empty
/// contents as proof of deletion.
package org.hiero.consensusnode.queries.files;
