# System Delete / Undelete Transaction API

## Description

`SystemDelete` and `SystemUndelete` are **privileged moderation transactions** that the
governance council can use to remove or restore a file or contract that violates council
policy (illegal content, malicious bytecode, ...). They are not normal account-level
deletes:

- A *regular* `FileDelete` / `ContractDelete` requires the asset's own `keys` / `adminAuthority`
  and is irreversible.
- A `SystemDelete` requires the **privileged system-admin or system-delete key**
  (configured at the consensus-node level), bypasses the asset's keys entirely, and can be
  *reversed* by a subsequent `SystemUndelete` until `expirationTime` is reached.

Each transaction targets either a file (`fileId`) or a contract (`contractId`) — exactly one of the two:

- **File system-delete** sets the file's "deleted" flag and schedules its permanent
  removal at `expirationTime`. Reads against the file return `FILE_DELETED` during that
  window; `SystemUndelete` clears the flag if invoked before expiration.
- **Contract system-delete** marks the contract as obsolete and prevents new invocations.
  `SystemUndelete` restores access.

`SystemUndelete` does not take an `expirationTime` — it only un-marks. If the file is
already past its `expirationTime` the un-delete fails with `INVALID_FILE_ID`.

### Signing requirements

| Transaction                | Signers required                                                                                      |
|----------------------------|-------------------------------------------------------------------------------------------------------|
| `SystemDeleteTransaction`  | the *payer* **and** the network's privileged *system-admin* (or *system-delete*) key                  |
| `SystemUndeleteTransaction`| the *payer* **and** the network's privileged *system-undelete* key                                    |

The privileged keys are configured by the council out-of-band. Provide them through a `TransactionSigner` (or sign
the packed transaction externally, for example with an HSM) exactly like any other externally held key.

## Design Notes

The two flavours are distinguished by `@@oneOf(fileId, contractId)`. See
[`transactions.md`](../consensus-node-client/transactions.md) for the HSM-friendly signing flows.

## API Schema

```
namespace consensusnode.admin.system
requires {Address, ContractId} from ledger
requires {Receipt, Transaction} from consensusnode.transactions

// Marks a file or contract as deleted by privileged council action. Exactly one of `fileId` and
// `contractId` must be set. For files, expirationTime is required and marks when the deletion
// becomes permanent (a SystemUndelete before that time reverses it). For contracts,
// expirationTime is ignored.
@@finalType
@@oneOf(fileId, contractId)
SystemDeleteTransaction extends Transaction<SystemDeleteReceipt, SystemDeleteTransaction> {
    @@immutable @@nullable fileId: Address                // the file to delete; absent when contractId is set
    @@immutable @@nullable contractId: ContractId         // the contract to delete; absent when fileId is set
    @@immutable @@nullable expirationTime: zonedDateTime  // when a system-deleted file becomes permanently unrecoverable; required when fileId is set, ignored when contractId is set
}

// Receipt of a `SystemDeleteTransaction`.
@@finalType
SystemDeleteReceipt extends Receipt {
}

// Reverses a prior `SystemDeleteTransaction`. Exactly one of `fileId` and `contractId` must be set, matching the
// target of the prior system delete. Must be submitted before the file's expirationTime; afterwards
// the un-delete fails with INVALID_FILE_ID.
@@finalType
@@oneOf(fileId, contractId)
SystemUndeleteTransaction extends Transaction<SystemUndeleteReceipt, SystemUndeleteTransaction> {
    @@immutable @@nullable fileId: Address                // the file to restore; absent when contractId is set
    @@immutable @@nullable contractId: ContractId         // the contract to restore; absent when fileId is set
}

// Receipt of a `SystemUndeleteTransaction`.
@@finalType
SystemUndeleteReceipt extends Receipt {
}
```

## Examples

### System-delete a file with a 30-day reversal window

```
HieroClient client = ...;     // council operator; signer wraps the system-admin HSM key
Address offendingFile = ...;

new SystemDeleteTransaction()
    .fileId(offendingFile)
    .expirationTime(zonedDateTime.now().plus(30, DAYS))   // reversible window
    .signWithOperatorAndSubmit(client);
```

### Restore the file inside the reversal window

```
new SystemUndeleteTransaction()
    .fileId(offendingFile)
    .signWithOperatorAndSubmit(client);
```

### System-delete a contract

```
new SystemDeleteTransaction()
    .contractId(maliciousContract)
    .signWithOperatorAndSubmit(client);     // expirationTime not used for contracts
```

## Testing

Offline tests of the attributes the system delete and undelete transactions keep, written as given / when / then.
Following `@@oneOf(fileId, contractId)`, a transaction that targets a file has no contract and the other way round, and
one with both or neither is rejected with `invalid-argument-error`.
The comment above each test names the SDK tests it comes from.

```tests
// Source: the Java (getSetFileId, getSetExpirationTime, constructSystemDeleteTransactionFromTransactionBodyProtobuf),
// JavaScript ("should carry the file id and leave the contract id null"), C++ (GetSetFileId, GetSetExpirationTime),
// Go and Swift SystemDeleteTransaction unit tests. The expiration time is the one of the Java test
// (Instant.ofEpochSecond(1554158542), converted exactly to 2019-04-01T22:42:22Z); whole seconds, as HAPI carries it
// (see Questions & Comments).
test "a system delete of a file keeps the file and the expiration time" {
    // given
    let file = DEFAULT(Address)
    let expirationTime = toZonedDateTime(text: "2019-04-01T22:42:22Z")

    // when
    let transaction = SystemDeleteTransaction{fileId: file, contractId: null, expirationTime: expirationTime}

    // then
    assertEquals(expected: file, actual: transaction.fileId)
    assertNull(actual: transaction.contractId)
    assertEquals(expected: expirationTime, actual: transaction.expirationTime)
}

// Source: the Java (getSetContractId), JavaScript ("should carry the contract id and leave the file id null"), C++
// (GetSetContractId), Go and Swift SystemDeleteTransaction unit tests; the API schema of this spec (expirationTime is
// ignored for contracts).
test "a system delete of a contract keeps the contract and needs no expiration time" {
    // given
    let contract = DEFAULT(ContractId)

    // when
    let transaction = SystemDeleteTransaction{fileId: null, contractId: contract, expirationTime: null}

    // then
    assertNull(actual: transaction.fileId)
    assertEquals(expected: contract, actual: transaction.contractId)
    assertNull(actual: transaction.expirationTime)
}

// Source: the JavaScript SystemDeleteTransaction unit tests ("should never encode both ids at once": fileID and
// contractID are a protobuf oneof); `@@oneOf(fileId, contractId)` of this spec.
test "a system delete of both a file and a contract is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    SystemDeleteTransaction{fileId: DEFAULT(Address), contractId: DEFAULT(ContractId),
            expirationTime: toZonedDateTime(text: "2019-04-01T22:42:22Z")}
}

// Source: this spec (`@@oneOf(fileId, contractId)`: exactly one target).
test "a system delete without a file and without a contract is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    SystemDeleteTransaction{fileId: null, contractId: null, expirationTime: null}
}

// Source: the Java (shouldSerializeFile), C++, Go and Swift (test_GetSetFileId) SystemUndeleteTransaction unit tests.
test "a system undelete of a file keeps the file" {
    // given
    let file = DEFAULT(Address)

    // when
    let transaction = SystemUndeleteTransaction{fileId: file, contractId: null}

    // then
    assertEquals(expected: file, actual: transaction.fileId)
    assertNull(actual: transaction.contractId)
}

// Source: the Java (shouldSerializeContract), C++, Go and Swift (test_GetSetContractId) SystemUndeleteTransaction unit
// tests.
test "a system undelete of a contract keeps the contract" {
    // given
    let contract = DEFAULT(ContractId)

    // when
    let transaction = SystemUndeleteTransaction{fileId: null, contractId: contract}

    // then
    assertNull(actual: transaction.fileId)
    assertEquals(expected: contract, actual: transaction.contractId)
}

// Source: HAPI system_undelete.proto (fileID and contractID are a oneof); `@@oneOf(fileId, contractId)` of this spec.
test "a system undelete of both a file and a contract is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    SystemUndeleteTransaction{fileId: DEFAULT(Address), contractId: DEFAULT(ContractId)}
}

// Source: this spec (`@@oneOf(fileId, contractId)`: exactly one target).
test "a system undelete without a file and without a contract is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    SystemUndeleteTransaction{fileId: null, contractId: null}
}
```

## Questions & Comments

- **No first-class system-admin key reference.** Same gap as in
  [`transactions-freeze.md`](transactions-freeze.md): the council-held keys that authorise
  system delete / undelete are not modelled on the transaction; they reach the SDK through
  `TransactionSigner`. A future `NetworkAdminKey` type could surface the distinction
  between *system-admin*, *system-delete*, and *system-undelete* keys, but is not in scope
  today.

- **Splitting `SystemDelete` into `SystemDeleteFileTransaction` /
  `SystemDeleteContractTransaction`** — same trade-off as the `freezeType` enum question:
  the `@@oneOf` mirrors HAPI but loses some compile-time safety (`expirationTime` is only
  valid in the file branch, yet sits next to `contractId` in the type). Two concrete
  subtypes would remove the cross-field dependency. Decide before locking the API.

- **No status code yet for "expired window".** A `SystemUndelete` on a file whose
  reversal window has elapsed surfaces as `INVALID_FILE_ID`, which is indistinguishable
  from "file never existed". Whether the SDK should map this case to a dedicated
  `system-undelete-window-expired-error` is open.

- **`expirationTime` has a precision of seconds on the wire.** HAPI carries it as a `TimestampSeconds`, so the
  sub-second part of the `zonedDateTime` is lost when the transaction is serialized (the JavaScript SDK tests this:
  "should drop nanoseconds across a round trip"). Whether the API should state this (a `seconds`-precision type or a
  documented truncation) is open; a test of it would also need access to the transaction of a `PackedTransaction`.

- **Does a system delete of a contract keep an expiration time?** The API schema says that `expirationTime` is ignored
  when `contractId` is set, but not whether the transaction keeps a value that is set anyway. The Java SDK keeps and
  serializes it (SystemDeleteTransactionTest builds a contract delete with an expiration time and reads it back). The
  tests set it to `null` for a contract until the spec decides.
