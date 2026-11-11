# File Transactions API

## Description

The file service stores arbitrary byte content on the consensus node, addressed by an
`Address` (file id). Write access is gated by a *key list*: every key in the file's current
key list must sign any modification (`FileAppend`, `FileUpdate`, `FileDelete`) — this is an
**n-of-n** requirement, not m-of-n. The classic uses are smart-contract bytecode, system
parameters, and signed binary artifacts that need on-ledger provenance.

A file's lifecycle is:

```
  create ── (append | update)* ── delete
```

- `FileCreateTransaction` allocates a new file id, sets the initial key list, optional memo,
  expiration time, and the first slice of content.
- `FileAppendTransaction` appends bytes to the end of the file.
- `FileUpdateTransaction` replaces any subset of the metadata (keys, memo, expiration) and
  optionally *replaces* the entire content (it does not append).
- `FileDeleteTransaction` deletes the file. The file id is not reusable; reads against a deleted file
  return `FILE_DELETED`.

### Large contents

The consensus node limits the size of a transaction, so the contents of one `FileCreate`, `FileUpdate` or
`FileAppend` must fit into one transaction. A `FileAppendTransaction` is exactly one transaction: it does not split
its contents. Larger contents are written as a create followed by several appends, each a transaction of its own with
its own fee and `TransactionId`; the enterprise `FileService` does this automatically.

### Signing requirements (file)

| Transaction         | Signers required                                                                          |
|---------------------|-------------------------------------------------------------------------------------------|
| `FileCreate`        | the *payer* **and** every key in the new `authority` authorization (anti-spoofing: cannot bind another party's key as a file modifier without their consent) |
| `FileAppend`        | the *payer* **and** every key in the file's current `authority` authorization                   |
| `FileUpdate`        | the *payer* **and** every key in the *current* `authority` authorization; if `authority` changes, every key in the *new* authorization must sign as well |
| `FileDelete`        | the *payer* **and** every key in the file's current key list                              |

When the operator is also the sole key on the file, `signWithOperatorAndSubmit(client)` is
enough. Multi-key files require the multi-signature flow: build → `signWithOperator(client)` →
`.sign(...)` per custodian → `submit(client)`.

## Design Notes

- **No chunking in the transaction.** A `FileAppendTransaction` is one transaction, so that it maps onto one
  `PackedTransaction` with one `TransactionId` and one body per node, and offline signing (`signableBodies()`,
  `toBytes()`) works as for every other transaction. Splitting large contents into several appends is a convenience
  of the enterprise layer (`FileService`), which also decides what happens when one of the appends fails. The V2
  SDKs split inside `FileAppendTransaction` (chunk size 4096 bytes, at most 20 chunks); the TCK expects that, so
  its binding splits the contents itself (`chunk` in `tooling/tck/bindings/file-service.md`). The same applies to
  `TopicMessageSubmit` once it is specified.
- The multi-signature flow for multi-key files is the one shown in
  [`transactions-accounts.md`](transactions-accounts.md).

## API Schema

```
namespace consensusnode.transactions.files
requires {Address} from ledger
requires {Authority} from authority
requires {Receipt, Transaction} from consensusnode.transactions

@@finalType
FileCreateTransaction extends Transaction<FileCreateReceipt, FileCreateTransaction> {
    @@immutable @@default([]) contents: bytes                  // initial content; further bytes via FileAppend
    @@immutable @@nullable authority: Authority                    // write-access authorization; an all-must-sign rule is an AuthorityList
    @@immutable @@nullable expirationTime: zonedDateTime       // when the file expires; SDK default if unset
    @@immutable @@nullable @@maxBytes(100) fileMemo: string    // short, human-readable label
}

@@finalType
FileCreateReceipt extends Receipt {
    @@immutable fileId: Address                                // id of the newly created file
}

// Appends `contents` to the end of the file in one transaction. The contents must fit into one transaction; larger
// contents are appended in several transactions (the enterprise `FileService` does this automatically).
@@finalType
FileAppendTransaction extends Transaction<FileAppendReceipt, FileAppendTransaction> {
    @@immutable fileId: Address
    @@immutable contents: bytes                                // bytes to append
}

@@finalType
FileAppendReceipt extends Receipt {
}

// Updates one or more of a file's mutable fields. Every nullable field is "leave unchanged"
// when null. Setting `contents` REPLACES the file contents in full — to add bytes without
// dropping the existing content, use `FileAppendTransaction` instead.
@@finalType
FileUpdateTransaction extends Transaction<FileUpdateReceipt, FileUpdateTransaction> {
    @@immutable fileId: Address
    @@immutable @@nullable contents: bytes                     // when set, replaces the entire file contents
    @@immutable @@nullable authority: Authority                    // write-access authorization; an all-must-sign rule is an AuthorityList
    @@immutable @@nullable expirationTime: zonedDateTime       // when set, extends (or sets) the expiration time
    @@immutable @@nullable @@maxBytes(100) fileMemo: string    // when set, replaces the memo
}

@@finalType
FileUpdateReceipt extends Receipt {
}

@@finalType
FileDeleteTransaction extends Transaction<FileDeleteReceipt, FileDeleteTransaction> {
    @@immutable fileId: Address
}

@@finalType
FileDeleteReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// The expiration time of the file create and update tests of the Java SDK (FileCreateTransactionTest and
// FileUpdateTransactionTest: Instant.ofEpochSecond(1554158728)) and the Swift SDK (FileCreateTransactionUnitTests and
// FileUpdateTransactionUnitTests: Timestamp(seconds: 1_554_158_728, subSecondNanos: 0)), whole seconds.
values SDK_TEST_EXPIRATION_TIME: zonedDateTime = toZonedDateTime(text: "2019-04-01T22:45:28Z")
```

## Examples

### Create a file (operator is the sole key)

```
HieroClient client = ...;          // operator's key will also gate the file

Response<FileCreateReceipt> response = new FileCreateTransaction()
    .contents("hello world".bytes)
    .authority(Authority.of(client.operatorPublicKey))
    .fileMemo("greeting")
    .signWithOperatorAndSubmit(client);

Address fileId = response.queryReceipt(client).fileId;
```

### Append

```
new FileAppendTransaction()
    .fileId(fileId)
    .contents(nextBlock)             // fits into one transaction, e.g. at most 4096 bytes
    .signWithOperatorAndSubmit(client);
```

Contents of hundreds of kilobytes are appended block by block in this way, one transaction per block; the enterprise
`FileService` does it automatically.

### Update — replace contents, rotate keys (multi-signature)

`contents` is replaced (not appended). Rotating the authorization to an all-must-sign pair
requires signatures from *both* the current and the new keys, in addition to the payer.

```
PackedTransaction<...> packed = new FileUpdateTransaction()
    .fileId(fileId)
    .contents("v2 payload".bytes)
    .authority(Authority.of(Authority.of(newKeyA), Authority.of(newKeyB)))   // all-must-sign AuthorityList
    .signWithOperator(client)        // payer + current key (operator)
    .sign(newSignerA)                // new key
    .sign(newSignerB);               // new key

packed.submit(client);
```

### Delete

```
new FileDeleteTransaction()
    .fileId(fileId)
    .signWithOperatorAndSubmit(client);
```

## Testing

Offline tests of the file transactions, written as given / when / then: the attributes a transaction keeps, the
defaults of the attributes it leaves out, and the packed form of each transaction. The contents are small enough for
one transaction; the network's key checks are tested by the TCK, not here.

```tests
// Source: this spec (@@default([]) of contents, @@nullable).
@@testId("a-file-create-transaction-without-attributes-has-empty-contents-and-nothing-else")
test "a file create transaction without attributes has empty contents and nothing else" {
    // when
    let transaction = FileCreateTransaction{}

    // then
    assertEquals(expected: 0, actual: length(value: transaction.contents))
    assertNull(actual: transaction.authority)
    assertNull(actual: transaction.expirationTime)
    assertNull(actual: transaction.fileMemo)
}

// Source: the Python SDK (test_constructor_with_parameters of FileCreateTransaction: "Test file content",
// "Test memo") and the Swift SDK (FileCreateTransactionUnitTests.test_GetSetContents and test_GetSetExpirationTime:
// Timestamp(seconds: 1_554_158_728) = 2019-04-01T22:45:28Z).
@@testId("a-file-create-transaction-keeps-its-contents-authority-expiration-time-and-memo")
test "a file create transaction keeps its contents, authority, expiration time and memo" {
    // given
    let contents = utf8(text: "Test file content")

    // when
    let transaction = FileCreateTransaction{contents: contents, authority: DEFAULT(Authority),
            expirationTime: SDK_TEST_EXPIRATION_TIME, fileMemo: "Test memo"}

    // then
    assertEquals(expected: contents, actual: transaction.contents)
    assertEquals(expected: DEFAULT(Authority), actual: transaction.authority)
    assertEquals(expected: SDK_TEST_EXPIRATION_TIME, actual: transaction.expirationTime)
    assertEquals(expected: "Test memo", actual: transaction.fileMemo)
}

// Source: the Java SDK (FileCreateTransactionTest.shouldBytes and shouldBytesNoSetters; expiration time
// Instant.ofEpochSecond(1554158728) = 2019-04-01T22:45:28Z) and the Swift SDK
// (FileCreateTransactionUnitTests.test_ToFromBytes).
@@testId("a-file-create-transaction-survives-tobytes-and-frombytes")
test "a file create transaction survives toBytes and fromBytes" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [ledger.AccountId.fromString(value: "0.0.3"), ledger.AccountId.fromString(value: "0.0.4")]
    let withAttributes = FileCreateTransaction{contents: utf8(text: "Test file content"), authority: DEFAULT(Authority),
            expirationTime: SDK_TEST_EXPIRATION_TIME, fileMemo: "Test memo"}

    for transaction in [withAttributes, FileCreateTransaction{}] {
        // given
        let packed = transaction.pack(payer: payer, nodes: nodes)

        // when
        let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

        // then
        assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
        assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
    }
}

// Source: the Python SDK (test_constructor_with_parameters of FileAppendTransaction: file 0.0.12345,
// "Test append content"), the Java SDK (FileAppendTransactionTest.shouldBytes) and the Swift SDK
// (FileAppendTransactionUnitTests.test_GetSetFileId, test_GetSetContents and test_ToFromBytes).
@@testId("a-file-append-transaction-keeps-its-file-and-contents-and-survives-tobytes-and-frombytes")
test "a file append transaction keeps its file and contents and survives toBytes and fromBytes" {
    // given
    let fileId = Address.fromString(address: "0.0.12345")
    let contents = utf8(text: "Test append content")
    let transaction = FileAppendTransaction{fileId: fileId, contents: contents}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account),
            nodes: [ledger.AccountId.fromString(value: "0.0.3")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: fileId, actual: transaction.fileId)
    assertEquals(expected: contents, actual: transaction.contents)
    assertEquals(expected: 1, actual: length(value: packed.signableBodies()))
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: this spec (every nullable field of an update is "leave unchanged"); the Python SDK
// (test_constructor_without_parameters of FileUpdateTransaction).
@@testId("a-file-update-transaction-changes-nothing-but-what-it-names")
test "a file update transaction changes nothing but what it names" {
    // when
    let transaction = FileUpdateTransaction{fileId: DEFAULT(Address)}

    // then
    assertEquals(expected: DEFAULT(Address), actual: transaction.fileId)
    assertNull(actual: transaction.contents)
    assertNull(actual: transaction.authority)
    assertNull(actual: transaction.expirationTime)
    assertNull(actual: transaction.fileMemo)
}

// Source: the Python SDK (test_constructor_with_parameters of FileUpdateTransaction: "Updated content", "Updated memo")
// and the Swift SDK (FileUpdateTransactionUnitTests.test_GetSetExpirationTime); the expiration time of the Java SDK
// (FileUpdateTransactionTest: Instant.ofEpochSecond(1554158728) = 2019-04-01T22:45:28Z).
@@testId("a-file-update-transaction-keeps-its-contents-authority-expiration-time-and-memo")
test "a file update transaction keeps its contents, authority, expiration time and memo" {
    // given
    let contents = utf8(text: "Updated content")

    // when
    let transaction = FileUpdateTransaction{fileId: DEFAULT(Address), contents: contents, authority: DEFAULT(Authority),
            expirationTime: SDK_TEST_EXPIRATION_TIME, fileMemo: "Updated memo"}

    // then
    assertEquals(expected: contents, actual: transaction.contents)
    assertEquals(expected: DEFAULT(Authority), actual: transaction.authority)
    assertEquals(expected: SDK_TEST_EXPIRATION_TIME, actual: transaction.expirationTime)
    assertEquals(expected: "Updated memo", actual: transaction.fileMemo)
}

// Source: the Python SDK (test_constructor_with_parameters of FileUpdateTransaction), the Java SDK
// (FileUpdateTransactionTest.shouldBytes: expiration time Instant.ofEpochSecond(1554158728) = 2019-04-01T22:45:28Z)
// and the Swift SDK (FileUpdateTransactionUnitTests.test_ToFromBytes).
@@testId("a-file-update-transaction-survives-tobytes-and-frombytes")
test "a file update transaction survives toBytes and fromBytes" {
    // given
    let packed = FileUpdateTransaction{fileId: DEFAULT(Address), contents: utf8(text: "Updated content"),
            authority: DEFAULT(Authority), expirationTime: SDK_TEST_EXPIRATION_TIME, fileMemo: "Updated memo"}
            .pack(payer: DEFAULT(consensusnode.client.Account), nodes: [ledger.AccountId.fromString(value: "0.0.3")])

    // when
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: the Python SDK (test_constructor_with_file_id of FileDeleteTransaction), the Java SDK
// (FileDeleteTransactionTest.shouldBytes) and the Swift SDK (FileDeleteTransactionUnitTests.test_ToFromBytes).
@@testId("a-file-delete-transaction-keeps-its-file-and-survives-tobytes-and-frombytes")
test "a file delete transaction keeps its file and survives toBytes and fromBytes" {
    // given
    let transaction = FileDeleteTransaction{fileId: DEFAULT(Address)}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account),
            nodes: [ledger.AccountId.fromString(value: "0.0.3")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: DEFAULT(Address), actual: transaction.fileId)
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}
```

## Questions & Comments

- **`authority` is a single `Authority`.** HAPI's `FileCreateTransactionBody.keys` is a single
  `KeyList` — one authorization requirement whose entries must *all* sign to modify the file —
  so V3 models it as one `authority: Authority` rather than a `list`. The all-must-sign rule is an
  `AuthorityList` with `threshold == children.size()`, and because `Authority` is recursive
  each entry may itself be a single key, a contract, or a nested threshold — e.g.
  `Authority.of(Authority.of(alice), Authority.of(2, Authority.of(bob), Authority.of(carol), Authority.ofContract(dao)))`.
  The `Authority` authorization type is introduced in
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and specified in
  [`authority.md`](../base/authority.md), resolving the former `list<PublicKey>` placeholder.

- **How large may the contents of one transaction be?** The network limits the size of the whole transaction (6 KiB,
  more with HIP-1300 jumbo transactions), not of the contents, so the spec states no maximum. The Java implementation
  rejects contents of more than 4096 bytes in `pack(...)`, the chunk size of the V2 SDKs, instead of letting the
  network answer `TRANSACTION_OVERSIZE`. Should the spec state the limit (e.g. `@@maxSize(4096)` on `contents`), or
  should the SDK leave it to the network?

- **`FileUpdate.contents` replaces, not appends.** This mirrors HAPI semantics but is a
  common foot-gun: callers expecting "update" to mean "merge" will silently drop the rest of
  the file. The field comment calls this out; consider whether the API should also expose a
  dedicated `replaceContents(bytes)` / `appendContents(bytes)` split at the language-binding
  level to make intent explicit.

- **`FileCreate` does not set `shard` / `realm`.** They are inherited from the
  client's configured network. Once persistent shard / realm
  (HIP-1299, section 6.2 in `docs/missing-features.md`) is added to the client, no per-transaction
  shard / realm fields are needed here either.

- **No `autoRenewPeriod` field.** Unlike accounts, files in HAPI do not currently carry an
  auto-renew account or auto-renew period — expiration is set explicitly. Should the v3
  surface anticipate a future HIP that adds auto-renew to files, or stay aligned with the
  current protocol shape?

- **Which expiration time does a `FileCreateTransaction` without `expirationTime` get?** The field comment says "SDK
  default if unset", but the spec does not state the default, so no test can check it. The Python SDK sets the
  current time plus 90 days (7,776,000 seconds) in the constructor. Should the default be stated (e.g. as an
  `@@default`-like rule relative to the valid start), or should "unset" mean that the network's default applies?
  The Java implementation sends the valid start of the transaction id plus 90 days, as the V2 SDKs do.

- **An immutable file cannot be expressed.** HAPI makes a file immutable with an empty key list, but
  `AuthorityList` requires at least one child (`@@minSize(1)`) and `authority` cannot be set to "no keys". So
  `FileCreateTransaction` can only create an immutable file by leaving `authority` unset, and `FileUpdateTransaction`
  cannot make an existing file immutable (`null` means "unchanged"). On the read side, `FileInfo.authority` of a file
  without keys is absent, while the TCK expects an empty key list (`[]`). Should there be an explicit "no keys" value
  (cf. the `KeyUpdate` operation of ADR-0004 for tokens and topics), and how should the TCK binding map an absent
  authority?
