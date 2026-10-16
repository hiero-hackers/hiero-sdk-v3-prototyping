# Network Freeze Transaction API

## Description

`FreezeTransaction` is the **only mechanism** by which the consensus network can be paused
or upgraded. It is authorised by the network's *freeze key* (configured at the consensus
node out-of-band — not the operator's normal account key), which is held by the governance
council and rotates independently of any application's keys. Submitting a
`FreezeTransaction` from a non-privileged payer fails with `AUTHORIZATION_FAILED`; this
transaction has no use case for end-user applications.

The transaction encodes one of five distinct operations selected by `freezeType`:

- **`FREEZE_ONLY`** — pauses consensus at `startTime`. The network remains frozen until
  manually resumed by the council. No upgrade payload is required.
- **`PREPARE_UPGRADE`** — does **not** freeze the network. Stages the upgrade payload
  identified by `updateFile` (a system file containing the upgrade archive) for a later
  `FREEZE_UPGRADE`. The network keeps running, `startTime` is ignored, and `updateFile` /
  `fileHash` are required. The consensus node rejects the transaction if the hash of the
  file's current contents does not match `fileHash`.
- **`FREEZE_UPGRADE`** — at `startTime`, freezes the network and applies the previously
  staged upgrade. Must be preceded by a matching `PREPARE_UPGRADE`, otherwise the
  consensus node rejects with `NO_UPGRADE_HAS_BEEN_PREPARED`.
- **`FREEZE_ABORT`** — cancels a pending `FREEZE_ONLY` or `FREEZE_UPGRADE` whose
  `startTime` has not yet arrived. `startTime` / `updateFile` / `fileHash` are ignored.
- **`TELEMETRY_UPGRADE`** — applies an upgrade to the telemetry / observability subsystem
  only. The consensus path stays live (no downtime). Requires `updateFile` and `fileHash`
  like `PREPARE_UPGRADE` / `FREEZE_UPGRADE`.

A typical upgrade first submits `PREPARE_UPGRADE` (the network stays live while the nodes stage the upgrade) and then
`FREEZE_UPGRADE` with a `startTime`, at which the network freezes, applies the upgrade and is restarted by the
council.

### Signing requirements

A `FreezeTransaction` must be signed by the *payer* **and** by the network's privileged *freeze key*. The freeze key
is configured at the consensus node, not on the transaction. If the freeze key is held in an HSM, sign the packed
transaction with an external signer or with externally created signatures.

## Design Notes

The `consensus-node-admin-client` module exists because of transactions like this one, which have no use case for
end-user applications.

| Transaction         | Signers required                                                                          |
|---------------------|-------------------------------------------------------------------------------------------|
| `FreezeTransaction` | the *payer* **and** the network's privileged *freeze key* (configured at the consensus node) |

The freeze key is not modelled by this spec — it lives in the network's address-book
configuration and is signed by the council's HSM. The
`PackedTransaction.sign(signer)` / `PackedTransaction.sign(signatures)` flows from
[`transactions.md`](../consensus-node-client/transactions.md) are the right entry points
for HSM-backed council signing.

## API Schema

```
namespace consensusnode.admin.freeze
requires {Address} from ledger
requires {Receipt, Transaction} from consensusnode.transactions

// Selects which of the five freeze operations to perform. Each operation requires a different set of
// `FreezeTransaction` fields; the consensus node rejects a transaction whose fields do not match the operation
// with INVALID_FREEZE_TRANSACTION_BODY.
enum FreezeType {
    FREEZE_ONLY        // pause consensus at startTime until the council resumes the network
    PREPARE_UPGRADE    // stage the upgrade in updateFile without freezing the network
    FREEZE_UPGRADE     // freeze at startTime and apply the previously staged upgrade
    FREEZE_ABORT       // cancel a pending freeze whose startTime has not yet arrived
    TELEMETRY_UPGRADE  // upgrade only the telemetry subsystem, without downtime
}

// Pauses or upgrades the consensus network. Must be signed by the network's privileged freeze key.
@@finalType
FreezeTransaction extends Transaction<FreezeReceipt, FreezeTransaction> {
    @@immutable freezeType: FreezeType                       // the operation to perform
    @@immutable @@nullable startTime: zonedDateTime          // wall-clock time at which the network freezes; required for FREEZE_ONLY, FREEZE_UPGRADE, TELEMETRY_UPGRADE; ignored for PREPARE_UPGRADE, FREEZE_ABORT
    @@immutable @@nullable updateFile: Address               // id of a file holding the upgrade payload; required for PREPARE_UPGRADE, FREEZE_UPGRADE, TELEMETRY_UPGRADE
    @@immutable @@nullable fileHash: bytes                   // SHA-384 of the upgrade file's contents; required whenever updateFile is set; the consensus node rejects with FREEZE_UPDATE_FILE_HASH_DOES_NOT_MATCH on mismatch
}

// Receipt of a `FreezeTransaction`. It confirms that the network accepted the freeze schedule, not that an
// upgrade has been applied.
@@finalType
FreezeReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// The id of the system file of the example: an upgrade file.
values UPGRADE_FILE: Address = Address{shard: 0, realm: 0, num: 150, checksum: ""}

// A SHA-384 hash has 48 bytes; the content of the hash does not matter to the transaction.
values UPGRADE_FILE_HASH: bytes = repeat(value: hex(text: "ab"), count: 48)
```

## Examples

### Prepare and execute a network upgrade

```
HieroClient client = ...;           // operator signer wraps the council freeze key (HSM)

Address upgradeFile = ...;           // system file (e.g. 0.0.150) containing the upgrade archive
bytes   upgradeHash = ...;           // SHA-384 of the file contents

// Step 1 — stage the upgrade; the network keeps running.
new FreezeTransaction()
    .freezeType(FreezeType.PREPARE_UPGRADE)
    .updateFile(upgradeFile)
    .fileHash(upgradeHash)
    .signWithOperatorAndSubmit(client);

// Step 2 — freeze and apply at startTime.
new FreezeTransaction()
    .freezeType(FreezeType.FREEZE_UPGRADE)
    .startTime(...)
    .updateFile(upgradeFile)
    .fileHash(upgradeHash)
    .signWithOperatorAndSubmit(client);
```

### Schedule a pause and then abort it

```
// Schedule a pause:
new FreezeTransaction()
    .freezeType(FreezeType.FREEZE_ONLY)
    .startTime(...)
    .signWithOperatorAndSubmit(client);

// Decide it was too aggressive — abort while startTime is still in the future:
new FreezeTransaction()
    .freezeType(FreezeType.FREEZE_ABORT)
    .signWithOperatorAndSubmit(client);
```

## Testing

Offline tests of the attributes a `FreezeTransaction` keeps, written as given / when / then. Whether the fields fit
the `freezeType` is checked by the consensus node, not by the SDK, so the tests do not expect an error for a
missing field. The comment above each test names the SDK tests it comes from.

```tests
// Source: the Java (getSetFreezeType, getSetStartTime, getSetFileId, getSetFileHash), Python
// (test_constructor_with_parameters), C++, Go and Swift FreezeTransaction unit tests.
test "a freeze upgrade keeps its type, start time, update file and file hash" {
    // given
    let startTime = DEFAULT(ledger.TransactionId).validStart

    // when
    let transaction = FreezeTransaction{freezeType: FreezeType.FREEZE_UPGRADE, startTime: startTime,
            updateFile: UPGRADE_FILE, fileHash: UPGRADE_FILE_HASH}

    // then
    assertEquals(expected: FreezeType.FREEZE_UPGRADE, actual: transaction.freezeType)
    assertEquals(expected: startTime, actual: transaction.startTime)
    assertEquals(expected: UPGRADE_FILE, actual: transaction.updateFile)
    assertEquals(expected: UPGRADE_FILE_HASH, actual: transaction.fileHash)
}

// Source: the Python (test_build_transaction_body_with_none_values) and Java (shouldBytesNoSetters) FreezeTransaction
// unit tests; the Description of this spec (PREPARE_UPGRADE ignores startTime).
test "a prepared upgrade needs no start time" {
    // when
    let transaction = FreezeTransaction{freezeType: FreezeType.PREPARE_UPGRADE, startTime: null,
            updateFile: UPGRADE_FILE, fileHash: UPGRADE_FILE_HASH}

    // then
    assertEquals(expected: FreezeType.PREPARE_UPGRADE, actual: transaction.freezeType)
    assertNull(actual: transaction.startTime)
    assertEquals(expected: UPGRADE_FILE, actual: transaction.updateFile)
    assertEquals(expected: UPGRADE_FILE_HASH, actual: transaction.fileHash)
}

// Source: the Python FreezeTransaction unit test (test_build_proto_body_with_none_fields); the Description of this
// spec (FREEZE_ONLY needs no upgrade payload).
test "a freeze without upgrade needs no update file and no file hash" {
    // given
    let startTime = DEFAULT(ledger.TransactionId).validStart

    // when
    let transaction = FreezeTransaction{freezeType: FreezeType.FREEZE_ONLY, startTime: startTime, updateFile: null,
            fileHash: null}

    // then
    assertEquals(expected: FreezeType.FREEZE_ONLY, actual: transaction.freezeType)
    assertEquals(expected: startTime, actual: transaction.startTime)
    assertNull(actual: transaction.updateFile)
    assertNull(actual: transaction.fileHash)
}

// Source: the Go (TestUnitFreezeTypeString) and Python (test_freeze_type_values, test_round_trip_conversion)
// FreezeType unit tests: every freeze type is kept as given.
test "a freeze transaction keeps every freeze type" {
    for freezeType in [FreezeType.FREEZE_ONLY, FreezeType.PREPARE_UPGRADE, FreezeType.FREEZE_UPGRADE,
            FreezeType.FREEZE_ABORT, FreezeType.TELEMETRY_UPGRADE] {
        // when
        let transaction = FreezeTransaction{freezeType: freezeType, startTime: null, updateFile: null, fileHash: null}

        // then
        assertEquals(expected: freezeType, actual: transaction.freezeType)
    }
}
```

## Questions & Comments

- **Per-`freezeType` field requirements are validated server-side, not in the type
  system.** A more type-safe alternative would be five concrete subtypes
  (`FreezeOnlyTransaction`, `PrepareUpgradeTransaction`, `FreezeUpgradeTransaction`,
  `FreezeAbortTransaction`, `TelemetryUpgradeTransaction`). That would also remove the
  three `@@nullable` fields whose validity depends on `freezeType`. The trade-off is that
  the current single-type / single-enum surface matches HAPI 1-to-1, which simplifies
  protobuf round-tripping for tooling. Decide before locking the API.

- **The freeze key is not modelled on the transaction.** The privileged signer is
  configured at the consensus-node level (council-controlled), not declared per
  transaction.

- **No streaming progress signal for `FREEZE_UPGRADE`.** The receipt confirms only that
  the consensus node accepted the freeze schedule, not that the upgrade has been applied.
  Observing the actual restart still requires mirror-node / health-check polling outside
  this transaction's surface. Whether the admin client should expose a typed
  "upgrade-applied" event is open.
