# Topic Transactions API

## Description

The topic service (HCS — Hiero Consensus Service) is an ordered, append-only message log
identified by a topic id. Producers submit messages; consensus order across all producers is
established by the consensus node and republished by mirror nodes for subscribers.

Each topic carries two optional keys that control how it can be modified and used:

- **`adminAuthority`** — required to update or delete the topic. When unset, the topic is
  **immutable**: nobody, not even the original creator, can change its metadata or delete it.
  The topic will then live until its expiration time and be removed by the protocol.
- **`submitAuthority`** — required to submit messages to the topic. When unset, the topic is
  **public**: any account may submit. When set, only callers who sign with this key may post.

Like accounts, topics expire and can be auto-renewed: `autoRenewPeriod` is the renewal window
the protocol applies when expiration approaches, and `autoRenewAccount` (optional) names the
account that pays the renewal fee. If `autoRenewAccount` is unset, renewal is paid by the
topic itself (from any HBAR it received via HIP-991 custom fees).

A topic's lifecycle is:

```
  create ── update* ── delete
                  │
                  └── (TopicMessageSubmit, modelled separately)
```

Submitting messages (`TopicMessageSubmit`) and the HIP-991 custom-fee fields are not covered by
these transactions yet.

### Signing requirements (topic lifecycle)

| Transaction      | Signers required                                                                                              |
|------------------|---------------------------------------------------------------------------------------------------------------|
| `TopicCreate`    | the *payer*; **and** the new `adminAuthority` if it is set (anti-spoofing: cannot bind another party's key as admin without their consent); **and** the `autoRenewAccount` if it is set and differs from the payer. The `submitAuthority` does **not** need to sign creation — it gates only future submits, no spoofing risk. |
| `TopicUpdate`    | the *payer*; **and** the topic's *current* `adminAuthority`; **and**, if `adminAuthority` itself is being rotated, the *new* key; **and**, if `autoRenewAccount` is set/changed to a different account, that new account |
| `TopicDelete`    | the *payer*; **and** the topic's current `adminAuthority` (an immutable topic — `adminAuthority` unset — cannot be deleted; the request fails with `UNAUTHORIZED`) |

When the operator holds every required key, `signWithOperatorAndSubmit(client)` suffices.
Multi-key flows go through the `signWithOperator(client).sign(...)` pattern.

The general rule: a key field on a create transaction must sign **if** binding it would grant
that key authority over a real asset (admin / auto-renew payer / treasury / ...). A key field
that only gates *future* permissioned operations and confers no liability or authority right
now (here: `submitAuthority`) does not need to sign creation.

### Mutability and "clearing" keys / auto-renew

`TopicUpdate` distinguishes "leave unchanged" from "clear":

- An optional field that is left unset means *leave unchanged*.
- `adminAuthority` / `submitAuthority` are `Authority` values (a single key, a contract, or an
  m-of-n threshold). Clearing an optional key (e.g. removing `adminAuthority` to make the topic
  immutable, or removing `submitAuthority` to make the topic public) is not supported yet.
- `autoRenewAccount` can be removed by setting it to `ZERO_ADDRESS`.

## Design Notes

- `TopicMessageSubmit` and the HIP-991 custom-fee fields (`customFees`, `feeScheduleAuthority`,
  `feeExemptAuthorities`) are deliberately out of scope of this file — see *Questions & Comments*.
- The multi-signature pattern is shown in [`transactions-accounts.md`](transactions-accounts.md).
- `adminAuthority` / `submitAuthority` are the `Authority` authorization type (see
  [`authority.md`](../base/authority.md) / [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md)).
  Clearing an optional key is a future write-side `KeyUpdate` operation (per ADR-0004), not
  expressible as an `Authority` value — see *Questions & Comments*.

## API Schema

```
namespace consensusnode.transactions.topics
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {Receipt, Transaction} from consensusnode.transactions

// Creates a new topic. Both keys are optional:
//   - adminAuthority unset  → topic is immutable (no update / delete possible)
//   - submitAuthority unset → topic is public (anyone may submit messages)
@@finalType
TopicCreateTransaction extends Transaction<TopicCreateReceipt, TopicCreateTransaction> {
    @@immutable @@nullable adminAuthority: Authority          // controls update / delete; unset → immutable topic; must sign the create transaction when set (anti-spoofing)
    @@immutable @@nullable submitAuthority: Authority         // controls message submission; unset → public topic; does NOT need to sign creation
    @@immutable @@nullable @@maxBytes(100) topicMemo: string   // short human-readable label
    @@immutable @@nullable autoRenewPeriod: seconds     // protocol default applies when unset
    @@immutable @@nullable autoRenewAccount: AccountId  // pays auto-renewal; must sign the create transaction if set
}

@@finalType
TopicCreateReceipt extends Receipt {
    @@immutable topicId: Address                        // id of the newly created topic
}

// Updates one or more of a topic's mutable fields. Every nullable field is "leave unchanged"
// when null. Requires the topic's current adminAuthority to sign; an immutable topic (no adminAuthority)
// cannot be updated.
@@finalType
TopicUpdateTransaction extends Transaction<TopicUpdateReceipt, TopicUpdateTransaction> {
    @@immutable topicId: Address
    @@immutable @@nullable adminAuthority: Authority          // when set, replaces the admin key; the new key must also sign
    @@immutable @@nullable submitAuthority: Authority         // when set, replaces the submit key
    @@immutable @@nullable @@maxBytes(100) topicMemo: string
    @@immutable @@nullable expirationTime: zonedDateTime
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable autoRenewAccount: AccountId  // when set, the new account must also sign
}

@@finalType
TopicUpdateReceipt extends Receipt {
}

// Deletes a topic. Only possible if the topic has an adminAuthority (an immutable topic cannot be
// deleted). The adminAuthority must sign.
@@finalType
TopicDeleteTransaction extends Transaction<TopicDeleteReceipt, TopicDeleteTransaction> {
    @@immutable topicId: Address
}

@@finalType
TopicDeleteReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// The expiration time of the topic update tests of the Swift SDK (TopicUpdateTransactionUnitTests:
// TestConstants.validStart = Timestamp(seconds: 1_554_158_542, subSecondNanos: 0)) and the Rust SDK
// (topic_update_transaction.rs: TEST_EXPIRATION_TIME = VALID_START, 1554158542 seconds), whole seconds.
values SDK_TEST_EXPIRATION_TIME: zonedDateTime = toZonedDateTime(text: "2019-04-01T22:42:22Z")
```

## Examples

### Create a public, immutable topic

No `adminAuthority`, no `submitAuthority`. Anyone may submit messages; no one — not even the creator —
can ever change the topic's metadata or delete it. Useful for cases where the operator wants
an unalterable audit log.

```
HieroClient client = ...;

Response<TopicCreateReceipt> response = new TopicCreateTransaction()
    .topicMemo("audit-log")
    .signWithOperatorAndSubmit(client);

Address topicId = response.queryReceipt().topicId;
```

### Create a private, mutable topic (operator controls both keys)

```
new TopicCreateTransaction()
    .adminAuthority(Authority.of(client.operatorPublicKey))
    .submitAuthority(Authority.of(client.operatorPublicKey))
    .topicMemo("ops-events")
    .signWithOperatorAndSubmit(client);
```

### Rotate the admin key (multi-signature)

Both the *current* and the *new* admin key must sign, in addition to the payer.

```
PackedTransaction<...> packed = new TopicUpdateTransaction()
    .topicId(topicId)
    .adminAuthority(Authority.of(newAdminPublicKey))
    .signWithOperator(client)        // payer + current adminAuthority (operator)
    .sign(newAdminSigner);           // new adminAuthority

packed.submit(client);
```

### Delete a topic

```
new TopicDeleteTransaction()
    .topicId(topicId)
    .signWithOperatorAndSubmit(client);
```

## Testing

Offline tests of the topic transactions, written as given / when / then: the attributes a transaction keeps, the
defaults of the attributes it leaves out, and the packed form of each transaction. The network's key checks are
tested by the TCK, not here.

```tests
// Source: this spec (adminAuthority unset: an immutable topic, submitAuthority unset: a public topic); the Python SDK
// (test_constructor_default_values of TopicCreateTransaction).
test "a topic create transaction without attributes creates a public, immutable topic" {
    // when
    let transaction = TopicCreateTransaction{}

    // then
    assertNull(actual: transaction.adminAuthority)
    assertNull(actual: transaction.submitAuthority)
    assertNull(actual: transaction.topicMemo)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.autoRenewAccount)
}

// Source: the Python SDK (test_constructor of TopicCreateTransaction: "Test Topic", an admin and a submit key) and the
// Swift SDK (TopicCreateTransactionUnitTests.test_GetSetSubmitKey, test_GetSetAdminKey, test_GetSetAutoRenewAccountId
// and test_GetSetAutoRenewPeriod: testAutoRenewPeriod = .days(1) = 86400 seconds).
test "a topic create transaction keeps its authorities, memo, auto-renew period and auto-renew account" {
    // given
    let submitAuthority: Authority = authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(algorithm: keys.KeyAlgorithm.ECDSA,
            encoding: keys.ByteImportEncoding.HEX,
            value: "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048").createPublicKey()}

    // when
    let transaction = TopicCreateTransaction{adminAuthority: DEFAULT(Authority), submitAuthority: submitAuthority,
            topicMemo: "Test Topic", autoRenewPeriod: toSeconds(value: 86400),
            autoRenewAccount: AccountId.fromString(value: "0.0.5678")}

    // then
    assertEquals(expected: DEFAULT(Authority), actual: transaction.adminAuthority)
    assertEquals(expected: submitAuthority, actual: transaction.submitAuthority)
    assertEquals(expected: "Test Topic", actual: transaction.topicMemo)
    assertEquals(expected: toSeconds(value: 86400), actual: transaction.autoRenewPeriod)
    assertEquals(expected: AccountId.fromString(value: "0.0.5678"), actual: transaction.autoRenewAccount)
}

// Source: the Java SDK (TopicCreateTransactionTest.shouldBytes and shouldBytesNoSetters; auto-renew period
// Duration.ofHours(24) = 86400 seconds) and the Swift SDK (TopicCreateTransactionUnitTests.test_ToFromBytes).
test "a topic create transaction survives toBytes and fromBytes" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")]
    let withAttributes = TopicCreateTransaction{adminAuthority: DEFAULT(Authority), submitAuthority: DEFAULT(Authority),
            topicMemo: "Test Topic", autoRenewPeriod: toSeconds(value: 86400),
            autoRenewAccount: AccountId.fromString(value: "0.0.5678")}

    for transaction in [withAttributes, TopicCreateTransaction{}] {
        // given
        let packed = transaction.pack(payer: payer, nodes: nodes)

        // when
        let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

        // then
        assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
        assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
    }
}

// Source: this spec (every nullable field of an update is "leave unchanged"); the Python SDK
// (test_auto_renew_period_omitted_when_unset, test_topic_memo_serialization_distinguishes_unset_and_empty).
test "a topic update transaction changes nothing but what it names" {
    // when
    let transaction = TopicUpdateTransaction{topicId: DEFAULT(Address)}

    // then
    assertEquals(expected: DEFAULT(Address), actual: transaction.topicId)
    assertNull(actual: transaction.adminAuthority)
    assertNull(actual: transaction.submitAuthority)
    assertNull(actual: transaction.topicMemo)
    assertNull(actual: transaction.expirationTime)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.autoRenewAccount)
}

// Source: the Rust SDK (topic_update_transaction.rs get_set_expiration_time and get_set_auto_renew_period:
// TEST_EXPIRATION_TIME = VALID_START = 2019-04-01T22:42:22Z, TEST_AUTO_RENEW_PERIOD = Duration::days(1) = 86400
// seconds) and the Swift SDK (TopicUpdateTransactionUnitTests: the same values in makeTransaction2).
test "a topic update transaction keeps its expiration time and auto-renew period" {
    // when
    let transaction = TopicUpdateTransaction{topicId: DEFAULT(Address), expirationTime: SDK_TEST_EXPIRATION_TIME,
            autoRenewPeriod: toSeconds(value: 86400)}

    // then
    assertEquals(expected: SDK_TEST_EXPIRATION_TIME, actual: transaction.expirationTime)
    assertEquals(expected: toSeconds(value: 86400), actual: transaction.autoRenewPeriod)
}

// Source: the Python SDK (test_topic_update_transaction_with_all_fields: "Comprehensive update", auto-renew account
// 0.0.5678), the Java SDK (TopicUpdateTransactionTest.shouldBytes) and the Swift SDK
// (TopicUpdateTransactionUnitTests.test_ToFromBytes2: expiration time TestConstants.validStart = 2019-04-01T22:42:22Z,
// auto-renew period .days(1) = 86400 seconds).
test "a topic update transaction survives toBytes and fromBytes" {
    // given
    let packed = TopicUpdateTransaction{topicId: DEFAULT(Address), adminAuthority: DEFAULT(Authority),
            submitAuthority: DEFAULT(Authority), topicMemo: "Comprehensive update",
            expirationTime: SDK_TEST_EXPIRATION_TIME, autoRenewPeriod: toSeconds(value: 86400),
            autoRenewAccount: AccountId.fromString(value: "0.0.5678")}
            .pack(payer: DEFAULT(consensusnode.client.Account), nodes: [AccountId.fromString(value: "0.0.3")])

    // when
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: the Java SDK (TopicDeleteTransactionTest.shouldBytes) and the Swift SDK (TopicDeleteTransactionUnitTests
// .test_GetSetTopicId and test_ToFromBytes).
test "a topic delete transaction keeps its topic and survives toBytes and fromBytes" {
    // given
    let transaction = TopicDeleteTransaction{topicId: DEFAULT(Address)}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account), nodes: [AccountId.fromString(value: "0.0.3")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: DEFAULT(Address), actual: transaction.topicId)
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}
```

## Questions & Comments

- **`adminAuthority` / `submitAuthority` are the `Authority` authorization type.** Both fields are an
  `Authority` — a single key, a contract, or an m-of-n threshold (`AuthorityList`) —
  introduced in [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and
  specified in [`authority.md`](../base/authority.md), resolving the former `PublicKey`
  placeholder.

- **Clearing `adminAuthority` / `submitAuthority` / `autoRenewAccount` on update — partial coverage.**
  HAPI distinguishes "leave unchanged" from "remove" via sentinel values that the consensus
  node interprets server-side:
  - For `Address`-typed fields (`autoRenewAccount`): the sentinel is `0.0.0`, exposed as
    [`ZERO_ADDRESS`](../base/ledger.md) in the `ledger` namespace. Pass `ZERO_ADDRESS` to
    `autoRenewAccount(...)` on an update to drop the auto-renew account.
  - For the `Authority`-typed fields (`adminAuthority`, `submitAuthority`): clearing — e.g. turning a
    private topic public via update or freezing an updatable topic by removing its admin key —
    is a future write-side `KeyUpdate` operation (per
    [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md)), not expressible as
    an `Authority` value.

  V2 SDKs additionally expose dedicated `clearAutoRenewAccountId()` / `clearAdminKey()` /
  `clearSubmitKey()` methods that hide the sentinel from callers. V3 could later layer the
  same convenience methods over the sentinels (additive change) — see the placement
  decision recorded in the `ZERO_ADDRESS` comment in `ledger.md`.

- **`TopicMessageSubmit` is out of scope of this file.** Message-submit shares the chunking
  question raised in [`transactions-files.md`](transactions-files.md) (`FileAppend` chunked
  appends) and additionally interacts with HIP-991 custom-fee limits; it deserves its own
  spec rather than being smuggled into the lifecycle file. Tracked in
  [`missing-features.md`](../../docs/missing-features.md) section 1.5. Until it is specified, no message
  can be submitted to a topic at all: the Java implementation has no such transaction, and
  `TopicService.submitMessage` in [`service-topic.md`](../enterprise/service-topic.md) cannot be
  implemented either.

- **HIP-991 custom-fee fields are out of scope.** `TopicCreate` / `TopicUpdate` in HAPI
  carry `customFees: list<FixedCustomFee>`, `feeScheduleAuthority: Key`, and
  `feeExemptAuthorities: list<Key>` for revenue-generating topics. These are tracked in
  [`missing-features.md`](../../docs/missing-features.md) sections 1.5 and 2.3 and depend on a
  write-side custom-fee model that does not yet exist in the spec. Adding them here is
  additive and breaks nothing. Until then the Java implementation cannot create a revenue-generating
  topic.

- **`ZERO_ADDRESS` does not fit `autoRenewAccount`.** The description says that `autoRenewAccount` is removed by
  setting it to `ZERO_ADDRESS`, but `ZERO_ADDRESS` is an `Address` and the field is an `AccountId`; `ledger` also
  defines `ZERO_ACCOUNT_ID`. Should the text name `ZERO_ACCOUNT_ID`?

- **Who pays the renewal of a topic created without `autoRenewAccount`?** The description says the topic itself. The
  current SDKs instead set the payer (Python: the operator, Swift: `test_AutomaticallySetAutoRenewAccountId`) as
  `autoRenewAccount` when the transaction is frozen and none is given. Should `pack(...)` do the same in V3, or is
  the field sent unset? The Java implementation follows the spec and sends none, so it differs from V2 here.

- **A `TopicCreateTransaction` without `autoRenewPeriod` is rejected by the network.** The description says that
  the protocol default applies when the field is unset, but HAPI has no such default and rejects the create. The Java
  implementation sends 90 days, as the V2 SDKs do. Should the spec state that default (e.g. `@@default` on
  `autoRenewPeriod`), or make the field required?
