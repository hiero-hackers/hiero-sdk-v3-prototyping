# File Queries API

This namespace defines the read-side counterpart to the file transactions in
[`transactions-files.md`](transactions-files.md). Both queries are *paid*: the consensus node
charges a fee for serving file content and metadata.

## Description

Queries for reading files stored on the network. Both queries are paid, so you can bound the spend with
`maxQueryPayment`, ask for the price up front with `getCost(client)`, and read the actually paid `cost` from the
`PaidQueryResponse`.

- **`FileContentsQuery`** — returns the raw bytes of a file. The price grows roughly with the file size; set
  `maxQueryPayment` when you fetch large files.
- **`FileInfoQuery`** — returns the file's metadata: size, expiration time, deletion flag, write-access authority
  and memo. It does not return the content; use `FileContentsQuery` for that.

Querying a deleted file does not fail: `FileInfoQuery` returns a `FileInfo` with `deleted = true` (most other fields
cleared), and `FileContentsQuery` returns empty `contents`. Check `FileInfo.deleted` instead of treating empty
contents as proof of deletion.

## Design Notes

Both queries extend `PaidQuery`; see [`queries.md`](queries.md) for the full payment / envelope semantics.

## API Schema

```
namespace consensusnode.queries.files
requires {Address} from ledger
requires {Authority} from authority
requires {PaidQuery} from consensusnode.queries

// Raw byte payload of a file. Returned by `FileContentsQuery`.
type FileContents {
    @@immutable fileId: Address                       // the file this snapshot belongs to
    @@immutable contents: bytes                       // full file content; empty for a deleted file
}

// Paid query for the raw byte content of a file. The fee scales with file size; bound
// the spend with `maxQueryPayment` for large files.
@@finalType
FileContentsQuery extends PaidQuery<FileContents> {
    @@immutable fileId: Address
}

// Full file metadata snapshot. Returned by `FileInfoQuery`.
type FileInfo {
    @@immutable fileId: Address
    @@immutable size: int64                            // current file size in bytes
    @@immutable expirationTime: zonedDateTime          // when the file expires
    @@immutable deleted: bool                          // true if the file has been deleted
    @@immutable @@nullable authority: Authority            // write-access authorization; absent if the file is immutable
    @@immutable @@nullable fileMemo: string
}

// Paid query for the metadata of a file. Does not return the file's byte content — use
// `FileContentsQuery` for that.
@@finalType
FileInfoQuery extends PaidQuery<FileInfo> {
    @@immutable fileId: Address
}
```

## Examples

### Read file contents (paid)

```
HieroClient client = ...;

FileContents contents = new FileContentsQuery()
    .fileId(fileId)
    .maxQueryPayment(NativeToken.of(...))       // bound spend for large files
    .submit(client)
    .value;

bytes payload = contents.contents;
```

### Read file metadata, branch on deleted

```
FileInfo info = new FileInfoQuery()
    .fileId(fileId)
    .submit(client)
    .value;

if (info.deleted) {
    return;                                     // file no longer exists; other fields not meaningful
}

int64 sizeInBytes = info.size;
zonedDateTime expiresAt = info.expirationTime;
```

### Confirm price up-front for a large file

```
PaidQuery<FileContents> query = new FileContentsQuery().fileId(fileId);

NativeToken<ANY, ANY> quotedCost = query.getCost(client);
// show quotedCost to the user, wait for approval, then:

FileContents contents = query.submit(client).value;
```

`getCost(client)` is non-binding — the price on the subsequent `submit(client)` may differ;
combine it with `maxQueryPayment` for a hard ceiling. See
[`queries.md`](queries.md#examples) for the full pattern.

## Questions & Comments

- **`FileInfo.key` is the `Authority` authorization type.** The single write-access
  authorization requirement (HAPI's `KeyList`) is modeled as one `Authority` (single key /
  contract / m-of-n) — see [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md)
  and [`authority.md`](../base/authority.md). This resolves the former `list<PublicKey>`
  placeholder that could not express the recursive authorization requirement HAPI returns.

- **No `ledgerId` on `FileInfo`.** HAPI's `FileGetInfoResponse.FileInfo` carries a
  `ledger_id`, but the V3 `FileInfo` (and `AccountInfo` in
  [`queries-accounts.md`](queries-accounts.md)) deliberately omit it: the caller already
  knows which ledger they queried through their `HieroClient`, and ledger-origin metadata
  belongs on the response envelope, not duplicated on every typed payload. The corresponding
  envelope-level question is opened in [`queries.md`](queries.md) *Questions & Comments*.

- **`FileInfo` carries no `autoRenewAccount` / `autoRenewPeriod`.** Matches HAPI today —
  files do not auto-renew the way accounts and tokens do. The same open question as on the
  write side (see [`transactions-files.md`](transactions-files.md) *Questions & Comments*)
  applies: stay aligned with the current protocol or anticipate a future HIP that introduces
  auto-renew for files.
