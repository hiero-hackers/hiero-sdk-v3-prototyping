# Mirror Node Schedule Query API

## Description

Query the schedules of the network from the Mirror Node. A schedule holds a scheduled transaction until it has
collected the signatures it needs, or until it expires. Use the `ScheduleRepository` (available as
`MirrorNodeClient.schedules`) to look up a schedule by its id (`findById`) and to list the schedules of the network
(`findAll`), executed and not yet executed ones, optionally filtered by account and schedule id.

A `Schedule` carries the scheduled transaction body, who created it and who pays for it, the signatures collected so
far, and when it was created, executed or expires.

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **Keys are `Authority`.** `admin_key` (a `Key` with `_type` and `key`) is an `Authority` (ADR-0004).
- **Entity ids are required.** `creator_account_id`, `payer_account_id` and `schedule_id` are `EntityId`, which the
  OpenAPI definition declares as nullable in general; a schedule always has them, so they are not `@@nullable` here.
- **`type` of a signature is the enum `SignatureType`.** Its values are the ones of the OpenAPI definition.
- **`links` is the `Page`.** The pagination links of a list response are not an attribute; `Page.next()` follows
  them.

## API Schema

```
namespace mirrornode.schedule
requires {Address, AccountId, MirrorNode} from ledger
requires {Authority} from authority
requires {Page} from common
requires {Order} from mirrornode.common

// The kind of key a signature was made with.
enum SignatureType {
    CONTRACT
    ED25519
    RSA_3072
    ECDSA_384
    ECDSA_SECP256K1
    UNKNOWN
}

// A signature collected by a schedule.
@@finalType
ScheduleSignature {
    @@immutable consensusTimestamp: zonedDateTime       // consensus time at which the signature was added
    @@immutable publicKeyPrefix: bytes                  // prefix of the public key the signature belongs to
    @@immutable signature: bytes
    @@immutable type: SignatureType
}

// A schedule of a scheduled transaction.
@@finalType
Schedule {
    @@immutable @@nullable adminKey: Authority                      // the key that can delete the schedule
    @@immutable consensusTimestamp: zonedDateTime                   // consensus time at which the schedule was created
    @@immutable creatorAccountId: AccountId
    @@immutable deleted: bool
    @@immutable @@nullable executedTimestamp: zonedDateTime         // consensus time of the execution; null if not executed
    @@immutable @@nullable expirationTime: zonedDateTime
    @@immutable memo: string
    @@immutable payerAccountId: AccountId                           // the account that pays for the scheduled transaction
    @@immutable scheduleId: Address
    @@immutable @@default([]) signatures: list<ScheduleSignature>   // the signatures collected so far
    @@immutable transactionBody: bytes                              // the scheduled transaction body (protobuf)
    @@immutable waitForExpiry: bool                                 // whether the transaction is executed at expiration time only (HIP-423)
}

abstraction ScheduleRepository {
    // The schedule with the given id, or null if there is none. Maps to GET /api/v1/schedules/{scheduleId}.
    @@async @@throws(mirror-node-error)
    @@nullable Schedule findById(scheduleId: Address)

    // The schedules of the network. Maps to GET /api/v1/schedules.
    @@async @@throws(mirror-node-error)
    Page<Schedule> findAll()

    // The schedules of the network, filtered by the creating account and the schedule id. Maps to
    // GET /api/v1/schedules.
    @@async @@throws(mirror-node-error)
    Page<Schedule> findAll(accountId: list<string>, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, scheduleId: list<string>)
}

@@static ScheduleRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `ScheduleRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: the examples of the ScheduleSignature schema of the mirror node OpenAPI definition (public key prefix
// "AAEBAwuqAwzB", signature "3q2+7wABAQMLqgMMwQ==", type ED25519).
@@testId("a-schedule-signature-keeps-its-public-key-prefix-signature-and-type")
test "a schedule signature keeps its public key prefix, signature and type" {
    // given
    let prefix = base64(text: "AAEBAwuqAwzB")
    let bytes = base64(text: "3q2+7wABAQMLqgMMwQ==")

    // when
    let signature = ScheduleSignature{consensusTimestamp: DEFAULT(zonedDateTime), publicKeyPrefix: prefix,
            signature: bytes, type: SignatureType.ED25519}

    // then
    assertEquals(expected: prefix, actual: signature.publicKeyPrefix)
    assertEquals(expected: bytes, actual: signature.signature)
    assertEquals(expected: SignatureType.ED25519, actual: signature.type)
}

// Source: the examples of the Schedule schema of the mirror node OpenAPI definition (memo "created on 02/10/2021",
// transaction body "Kd6tvu8=", not deleted, payer and creator "0.0.2"); this spec (`signatures` defaults to an empty
// list, a schedule that has not been executed has no execution time).
@@testId("a-schedule-without-signatures-and-execution-has-an-empty-signature-list-and-no-execution-time")
test "a schedule without signatures and execution has an empty signature list and no execution time" {
    // given
    let account = AccountId.fromString(value: "0.0.2")

    // when
    let schedule = Schedule{consensusTimestamp: DEFAULT(zonedDateTime), creatorAccountId: account, deleted: false,
            memo: "created on 02/10/2021", payerAccountId: account, scheduleId: Address.fromString(address: "0.0.2"),
            transactionBody: base64(text: "Kd6tvu8="), waitForExpiry: false}

    // then
    assertEquals(expected: "created on 02/10/2021", actual: schedule.memo)
    assertEquals(expected: base64(text: "Kd6tvu8="), actual: schedule.transactionBody)
    assertEquals(expected: account, actual: schedule.payerAccountId)
    assertEquals(expected: 0, actual: length(value: schedule.signatures))
    assertNull(actual: schedule.executedTimestamp)
    assertNull(actual: schedule.adminKey)
    assertFalse(condition: schedule.deleted)
}
```

## Questions & Comments

- **`transactionBody` is raw protobuf bytes.** The mirror-node layer is independent of `consensus-node-client`, so it
  cannot decode the scheduled transaction into a typed transaction. Should the enterprise layer offer a decoded view?
- **`SignatureType` overlaps with the key types of `keys`.** `ED25519` and `ECDSA_SECP256K1` are key algorithms that
  `keys` already models; `CONTRACT`, `RSA_3072` and `ECDSA_384` are not. Should the mirror node client map to the key
  algorithms of `keys` where it can, or keep its own enum?
