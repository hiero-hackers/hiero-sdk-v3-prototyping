# File service

The TCK methods of the file service
([file-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/file-service)):
creating, updating, appending to and deleting files, and the file queries. File contents are UTF-8 text in the TCK
(`utf8`); the keys of a file are a list of keys that must all sign (`keyList`), which the V3 API models as an
`AuthorityList` in `authority`.

## Gaps

- **Chunking** (`maxChunks`, `chunkSize` of `appendFile`): the V3 `FileAppendTransaction` splits large contents
  itself and has no settings for it.
- **`ledgerId`** of `getFileInfo`, **`queryPayment`** of `getFileContents`: as for the other queries, the results
  have no ledger ID and a paid query only a maximum payment.
- **An empty key list** (`keys: []`, an immutable file): an `AuthorityList` needs at least one key, so `keyList`
  turns an empty list into no key. `createFile` then creates a file without keys as intended, but `updateFile` cannot
  remove the keys of a file (an absent `authority` leaves them unchanged), and `getFileInfo` of a file without keys
  leaves `keys` out instead of answering `[]`.
- The specification of `deleteFile` names the parameter `fileID`, its tests send `fileId`; the binding accepts both.

```bindings
requires {FileCreateTransaction, FileUpdateTransaction, FileAppendTransaction, FileDeleteTransaction} from consensusnode.transactions.files
requires {FileContentsQuery, FileInfoQuery} from consensusnode.queries.files

// Creates a file (FileCreateTransaction.md).
binding createFile -> FileCreateTransaction {
    authority      = keys : keyList
    contents       = contents : utf8
    expirationTime = expirationTime : timestamp
    fileMemo       = memo : string
    result fileId = receipt.fileId : address
    result status = receipt.status : status
}

// Updates a file (FileUpdateTransaction.md).
binding updateFile -> FileUpdateTransaction {
    fileId         = fileId : address
    authority      = keys : keyList
    contents       = contents : utf8
    expirationTime = expirationTime : timestamp
    fileMemo       = memo : string
    result status = receipt.status : status
}

// Appends contents to a file (FileAppendTransaction.md).
binding appendFile -> FileAppendTransaction {
    fileId   = fileId : address
    contents = contents : utf8
    unsupported maxChunks "FileAppendTransaction chunks the contents itself; it has no chunk settings"
    unsupported chunkSize "FileAppendTransaction chunks the contents itself; it has no chunk settings"
    result status = receipt.status : status
}

// Deletes a file (FileDeleteTransaction.md).
binding deleteFile -> FileDeleteTransaction {
    fileId = fileID : address | fileId : address
    result status = receipt.status : status
}

// Queries the contents of a file (FileContentsQuery.md).
binding getFileContents -> FileContentsQuery {
    fileId          = fileId : address
    maxQueryPayment = maxQueryPayment : tinybar
    unsupported queryPayment "a paid query has only a maximum payment (PaidQuery.maxQueryPayment)"
    result contents = response.contents : utf8
}

// Queries the information of a file (FileInfoQuery.md).
binding getFileInfo -> FileInfoQuery {
    fileId = fileId : address
    result fileId         = response.fileId : address
    result size           = response.size : int64
    result expirationTime = response.expirationTime : timestamp
    result isDeleted      = response.deleted : bool
    result keys           = response.authority : keyList
    result memo           = response.fileMemo : string
    unsupported result ledgerId "FileInfo has no ledger ID"
}
```
