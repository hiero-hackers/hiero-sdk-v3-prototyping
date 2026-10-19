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

### Chunked appends

The consensus node imposes a maximum body size per transaction. A `FileAppend` whose
`contents` exceeds that limit cannot be sent as a single transaction; the SDK splits it into
N consecutive `FileAppend` transactions on the same `fileId` and submits them in order. From
the caller's perspective this remains a single `Transaction.signWithOperator…` call — the SDK
hides the split. The receipt surfaced back to the caller is the receipt of the *last* chunk.

Note that each underlying chunk is a separate consensus transaction with its own fee and its own
`TransactionId`.

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

- **Chunked appends are a leaky abstraction.** Each underlying chunk is a separate consensus
  transaction with its own fee and its own `TransactionId`. The current model intentionally keeps
  the surface simple; surfacing per-chunk receipts is deferred (see *Questions & Comments*). The
  same chunking concept applies to `TopicMessageSubmit` and other HAPI transactions that ride on the
  consensus-node body size limit.
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

// Appends `contents` to the end of the file. The SDK transparently splits this into multiple
// consensus transactions when `contents` exceeds the single-transaction body limit.
@@finalType
FileAppendTransaction extends Transaction<FileAppendReceipt, FileAppendTransaction> {
    @@immutable fileId: Address
    @@immutable contents: bytes                                // bytes to append; large payloads are chunked by the SDK
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

### Append (transparently chunked when needed)

`largeBlob` may be hundreds of kilobytes; the SDK splits it into multiple `FileAppend`
consensus transactions internally and returns the final receipt.

```
new FileAppendTransaction()
    .fileId(fileId)
    .contents(largeBlob)
    .signWithOperatorAndSubmit(client);
```

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
one transaction; chunking and the network's key checks are tested by the TCK, not here.

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

- **Chunked appends are not modelled at the type level.** `FileAppendTransaction.contents`
  accepts the full byte payload and relies on the SDK to split it into multiple consensus
  transactions when the body exceeds the single-tx size limit. The model returns a single
  `Response<FileAppendReceipt>` — there is no first-class way for advanced callers to inspect
  per-chunk receipts, control chunk size, or recover from a partially-applied append (where
  chunk K succeeded but chunk K+1 timed out). Once HIP-1300 (jumbo transactions, section
  1.11 of `docs/missing-features.md`) is specified, the choice between "explicit chunk control"
  and "always pretend it's one transaction" needs a decision; the same question applies to
  `TopicMessageSubmit`.

- **`FileUpdate.contents` replaces, not appends.** This mirrors HAPI semantics but is a
  common foot-gun: callers expecting "update" to mean "merge" will silently drop the rest of
  the file. The field comment calls this out; consider whether the API should also expose a
  dedicated `replaceContents(bytes)` / `appendContents(bytes)` split at the language-binding
  level to make intent explicit.

- **`FileCreate` does not set `shard` / `realm`.** They are inherited from the
  client's configured network. Once persistent shard / realm
  (HIP-1299, section 5.2 in `docs/missing-features.md`) is added to the client, no per-transaction
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

- **How does a chunked `FileAppendTransaction` map onto `PackedTransaction`?** A `PackedTransaction` has one
  `transactionId` and one body per target node, but each chunk of a large append is a transaction of its own with its
  own `TransactionId`. What `pack(...)`, `signableBodies()` and `toBytes()` return for contents above the body limit is
  not stated, so the chunking tests of the current SDKs (one body per chunk and node, consecutive valid starts) cannot
  be written against this API. Because of this, the Java implementation does not chunk at all: `pack(...)` rejects a
  `FileAppendTransaction` whose contents exceed 4096 bytes, so larger contents can only be appended by the caller in
  several transactions.

- **An immutable file cannot be expressed.** HAPI makes a file immutable with an empty key list, but
  `AuthorityList` requires at least one child (`@@minSize(1)`) and `authority` cannot be set to "no keys". So
  `FileCreateTransaction` can only create an immutable file by leaving `authority` unset, and `FileUpdateTransaction`
  cannot make an existing file immutable (`null` means "unchanged"). On the read side, `FileInfo.authority` of a file
  without keys is absent, while the TCK expects an empty key list (`[]`). Should there be an explicit "no keys" value
  (cf. the `KeyUpdate` operation of ADR-0004 for tokens and topics), and how should the TCK binding map an absent
  authority?
