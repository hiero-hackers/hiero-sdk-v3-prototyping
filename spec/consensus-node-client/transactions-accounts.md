# Account Transactions API


## Description

Provides all crypto-service transactions that operate on accounts: lifecycle
(`AccountCreateTransaction` / `AccountUpdateTransaction` / `AccountDeleteTransaction`), value movement
(`TransferTransaction` — HBAR, fungible tokens, and NFTs in one atomic transaction), and
delegated-spending authorization (`AccountAllowanceApproveTransaction` /
`AccountAllowanceDeleteTransaction`).

`TransferTransaction` is the single transaction type for *all* value transfers on the network.
A single instance can mix HBAR, fungible-token, and NFT movements; the consensus node settles
all movements atomically (either every leg succeeds or none does). Each leg can be marked
`approved = true` to indicate the debit should be drawn from an existing allowance granted to
the payer rather than directly from the named account; in that case the payer must hold a
sufficient allowance from the owner, and the owner does not need to sign.

#### Signing requirements (transfer)

Every leg that *removes* value from an account requires that account's signature, unless the
leg is `approved = true` (in which case the *spender* — the transaction payer — signs in the
owner's place using a previously granted allowance):

| Leg                                  | Signer required                              |
|--------------------------------------|----------------------------------------------|
| `HbarTransfer` debit (`amount < 0`)  | the debited `accountId`, or payer if `approved` |
| `HbarTransfer` credit (`amount > 0`) | none — unless the recipient has `receiverSignatureRequired = true` |
| `TokenTransfer` debit                | the debited `accountId`, or payer if `approved` |
| `TokenTransfer` credit               | none — unless `receiverSignatureRequired = true` |
| `NftTransfer`                        | `sender`, or payer if `approved`             |
| Transaction fee                      | the *payer* (whichever account is bound by `pack` / `sign…`) |

In practice this means a transfer where the operator moves value out of someone else's account
is a **multi-signature** flow: the operator signs as payer, the owner signs to authorize the
debit. The single-call `signWithOperator…` methods are only sufficient when the operator is
the only account being debited (or every debit leg is `approved`).

`AccountAllowanceApproveTransaction` grants delegated-spend rights from an *owner* account to
a *spender* account. Three independent flavours exist:

- **HBAR allowances** — the spender may transfer up to `amount` HBAR from the owner.
- **Fungible-token allowances** — the spender may transfer up to `amount` units of `tokenId`
  from the owner.
- **NFT allowances** — an `NftGrant`, which is either an `NftSerialGrant` over specific serials of
  `tokenId` or an `NftCollectionGrant` covering every current and future serial of `tokenId` the
  owner holds. The `delegatingSpender` field supports HIP-336 sub-approvals: a spender that already
  holds a collection grant may delegate per-serial rights onward.

Re-running approve with the same `(owner, spender, asset)` triple overwrites the prior amount
(for HBAR / fungible tokens) — an amount of `0` revokes the allowance. NFT per-serial grants
accumulate; an `NftCollectionGrant(false)` revokes the collection-wide grant for that
`(owner, spender, tokenId)`.

`AccountAllowanceDeleteTransaction` is **NFT-only**: it lets the *owner* revoke previously
granted per-serial NFT allowances independently of any specific spender. HBAR and
fungible-token allowances are not deletable through this transaction; revoke them by issuing
an approve with `amount = 0`. Collection-wide grants are revoked by issuing an approve with
`NftCollectionGrant(false)` for the same `(owner, spender, tokenId)`.

## Design Notes

- **An NFT allowance carries an `NftGrant`, not two nullable fields.** What is granted — named
  serials or the whole collection — is a choice, and a choice is a type: a `@@sealed` abstraction
  with one final type per case, following
  [ADR-0011](../../docs/adr/0011-model-a-choice-as-a-sealed-type.md). The grant never appears
  outside an allowance, so it is a field of it rather than a variant of `NftAllowance` itself.

## API Schema

```
namespace consensusnode.transactions.accounts
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Receipt, Transaction} from consensusnode.transactions

// An account is staked to another account or to a node, not to both.
@@finalType
@@oneOrNoneOf(stakedAccountId, stakedNodeId)
AccountCreateTransaction extends Transaction<AccountCreateReceipt, AccountCreateTransaction> {
    @@immutable authority: Authority
    // Amount transferred from the payer to the new account. If absent, the account is created with a balance
    // of 0 in the native token of the network.
    @@immutable @@nullable initialBalance: NativeToken<ANY, ANY>
    @@immutable @@nullable @@maxBytes(100) accountMemo: string
    @@immutable @@default(false) receiverSignatureRequired: bool
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable stakedAccountId: AccountId
    @@immutable @@nullable stakedNodeId: int64
    @@immutable @@default(false) declineStakingReward: bool
    @@immutable @@nullable alias: bytes
}

@@finalType
AccountCreateReceipt extends Receipt {
    @@immutable accountId: AccountId
}

@@finalType
@@oneOrNoneOf(stakedAccountId, stakedNodeId)
AccountUpdateTransaction extends Transaction<AccountUpdateReceipt, AccountUpdateTransaction> {
    @@immutable accountId: AccountId                            // the account that is being updated
    @@immutable @@nullable authority: Authority                          // the new key (requires signatures with both old and new keys)
    @@immutable @@nullable @@maxBytes(100) accountMemo: string
    @@immutable @@nullable receiverSignatureRequired: bool
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable expirationTime: zonedDateTime
    @@immutable @@nullable stakedAccountId: AccountId            // mutually exclusive with stakedNodeId
    @@immutable @@nullable stakedNodeId: int64                          // mutually exclusive with stakedAccountId
    @@immutable @@nullable declineStakingReward: bool
}

@@finalType
AccountUpdateReceipt extends Receipt {
}

@@finalType
AccountDeleteTransaction extends Transaction<AccountDeleteReceipt, AccountDeleteTransaction> {
    @@immutable accountId: AccountId                            // the account that is being deleted (must sign)
    @@immutable transferAccountId: AccountId                    // the account that receives the remaining hbar balance
}

@@finalType
AccountDeleteReceipt extends Receipt {
}

// One leg of an HBAR transfer. The consensus node requires the sum of all `amount` values in
// `TransferTransaction.hbarTransfers` to equal zero (debits balance credits exactly).
type HbarTransfer {
    @@immutable accountId: AccountId                // the account being debited or credited
    @@immutable amount: NativeToken<ANY, ANY>       // signed amount: negative = debit, positive = credit
    @@immutable @@default(false) approved: bool     // when true, the debit is drawn from an allowance granted to the transaction payer; the owner (accountId) need not sign
}

// One leg of a fungible-token transfer. The sum of `amount` per `tokenId` across all
// `TransferTransaction.tokenTransfers` must equal zero.
type TokenTransfer {
    @@immutable tokenId: Address                    // the fungible token being moved
    @@immutable accountId: AccountId                // the account being debited or credited
    @@immutable amount: int64                       // signed amount in the token's smallest unit: negative = debit, positive = credit
    @@immutable @@nullable expectedDecimals: int32  // optional protocol-side decimals check; if set and the token's actual decimals differ, the network rejects the transaction
    @@immutable @@default(false) approved: bool     // when true, the debit is drawn from an allowance granted to the transaction payer; the owner (accountId) need not sign
}

// One NFT transfer (single serial). NFTs are non-fungible so each serial moves exactly once
// per transaction.
type NftTransfer {
    @@immutable tokenId: Address                    // the NFT collection
    @@immutable serial: int64                       // the serial number being transferred
    @@immutable sender: AccountId                   // current owner
    @@immutable receiver: AccountId                 // new owner
    @@immutable @@default(false) approved: bool     // when true, the transfer is drawn from an NFT allowance granted to the transaction payer; the sender need not sign
}

// Atomic transfer of HBAR and/or fungible tokens and/or NFTs. All legs settle together: if any
// leg fails (insufficient balance, missing signature, frozen / KYC-blocked token account, ...)
// the entire transaction is rolled back.
@@finalType
TransferTransaction extends Transaction<TransferReceipt, TransferTransaction> {
    @@immutable @@default([]) hbarTransfers: list<HbarTransfer>
    @@immutable @@default([]) tokenTransfers: list<TokenTransfer>
    @@immutable @@default([]) nftTransfers: list<NftTransfer>
}

@@finalType
TransferReceipt extends Receipt {
}

// Allowance for HBAR. `amount` is absolute, not delta: re-approving overwrites the prior
// value; an amount of zero revokes the allowance.
type HbarAllowance {
    @@immutable ownerAccountId: AccountId           // the account granting the allowance (must sign the transaction)
    @@immutable spenderAccountId: AccountId         // the account that may spend on behalf of the owner
    @@immutable amount: NativeToken<ANY, ANY>       // upper bound the spender may transfer; 0 = revoke
}

// Allowance for a fungible token. `amount` is absolute, not delta: re-approving overwrites the
// prior value; an amount of zero revokes the allowance.
type TokenAllowance {
    @@immutable tokenId: Address                    // the fungible token the allowance applies to
    @@immutable ownerAccountId: AccountId           // the account granting the allowance (must sign the transaction)
    @@immutable spenderAccountId: AccountId         // the account that may spend on behalf of the owner
    @@immutable amount: int64                       // upper bound in the token's smallest unit; 0 = revoke
}

// What a spender is granted on an NFT collection.
@@sealed(NftSerialGrant, NftCollectionGrant) abstraction NftGrant {}

// Per-serial rights to the listed serials. Additive across multiple approve transactions.
@@finalType
NftSerialGrant extends NftGrant {
    @@immutable @@minSize(1) serials: list<int64>
}

// Rights to every current and future serial of the collection the owner holds (HAPI
// `approved_for_all`).
@@finalType
NftCollectionGrant extends NftGrant {
    @@immutable approved: bool                                      // false revokes an existing collection-wide grant
}

// Allowance for an NFT collection, granting either named serials or the whole collection.
//
// `delegatingSpender` supports HIP-336: a spender that already holds an `NftCollectionGrant` may
// delegate per-serial rights onward without involving the original owner. When set, the
// transaction must be signed by `delegatingSpender`, not by `ownerAccountId`.
NftAllowance {
    @@immutable tokenId: Address
    @@immutable ownerAccountId: AccountId
    @@immutable spenderAccountId: AccountId
    @@immutable grant: NftGrant
    @@immutable @@nullable delegatingSpender: AccountId
}

// Grants delegated-spend rights from one or more owners. Per-leg semantics:
//
// - Every HBAR / fungible-token / per-serial NFT entry is independent — granting an NFT
//   allowance does not affect the owner's HBAR allowance to the same spender, etc.
// - The owner of each leg (`ownerAccountId`, or `delegatingSpender` for HIP-336 NFT
//   sub-approvals) must sign the transaction.
@@finalType
AccountAllowanceApproveTransaction extends Transaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> {
    @@immutable @@default([]) hbarAllowances: list<HbarAllowance>
    @@immutable @@default([]) tokenAllowances: list<TokenAllowance>
    @@immutable @@default([]) nftAllowances: list<NftAllowance>
}

@@finalType
AccountAllowanceApproveReceipt extends Receipt {
}

// One NFT-allowance revocation. Removes spender rights for the listed serials of `tokenId`
// regardless of which spender currently holds them; an NFT cannot be approved to more than one
// spender at a time, so the owner does not need to name the spender to revoke.
type NftAllowanceDeletion {
    @@immutable tokenId: Address                    // the NFT collection
    @@immutable ownerAccountId: AccountId           // the account revoking the allowance (must sign the transaction)
    @@immutable serials: list<int64>                // serials whose allowance is revoked
}

// Revokes previously granted NFT allowances. NFT-only — HBAR and fungible-token allowances are
// revoked by approving them with amount = 0, and collection-wide grants are revoked by approving
// with an `NftCollectionGrant(false)`.
@@finalType
AccountAllowanceDeleteTransaction extends Transaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> {
    @@immutable nftAllowanceDeletions: list<NftAllowanceDeletion>
}

@@finalType
AccountAllowanceDeleteReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// The expiration time of the account update tests of the Java SDK (AccountUpdateTransactionTest:
// Instant.ofEpochSecond(1554158543)) and the Swift SDK (AccountUpdateTransactionUnitTests:
// Timestamp(seconds: 1_554_158_543, subSecondNanos: 0)), whole seconds.
values SDK_TEST_EXPIRATION_TIME: zonedDateTime = toZonedDateTime(text: "2019-04-01T22:42:23Z")
```

## Examples

### Sender is the operator (single signature)

The operator moves value out of its own account. The operator signs as payer *and* as the
debited account in one step.

```
HieroClient client = ...;     // operator = alice
AccountId bob = ...;

Response<TransferReceipt> response = new TransferTransaction()
    .hbarTransfers([
        new HbarTransfer(client.operatorAccountId, NativeToken.of(-10, HBAR_UNIT)),
        new HbarTransfer(bob,                      NativeToken.of(+10, HBAR_UNIT)),
    ])
    .signWithOperatorAndSubmit(client);

response.queryReceipt();
```

### Sender is *not* the operator (multi-signature)

The operator pays the transaction fee, but Alice is the one being debited. Alice must add her
own signature; otherwise the network rejects the transaction with `INVALID_SIGNATURE`. The
flow goes through `PackedTransaction` so that both signatures end up on the same packed
payload.

```
HieroClient client = ...;     // operator is not Alice
Account alice = ...;          // separate account that holds the funds
AccountId bob = ...;

PackedTransaction<...> packed = new TransferTransaction()
    .hbarTransfers([
        new HbarTransfer(alice.accountId, NativeToken.of(-10, HBAR_UNIT)),
        new HbarTransfer(bob,             NativeToken.of(+10, HBAR_UNIT)),
    ])
    .signWithOperator(client)   // operator signs as payer
    .sign(alice);               // Alice signs to authorize her debit

Response<TransferReceipt> response = packed.submit(client);
```

### Atomic HBAR + fungible + NFT transfer

A single `TransferTransaction` settles HBAR, a fungible-token leg, and an NFT leg together. If
any leg fails (e.g. recipient not associated with the token) the entire transfer rolls back.
Alice is debited on every leg, so she must co-sign in addition to the operator.

```
PackedTransaction<...> packed = new TransferTransaction()
    .hbarTransfers([
        new HbarTransfer(alice.accountId, NativeToken.of(-5, HBAR_UNIT)),
        new HbarTransfer(bob,             NativeToken.of(+5, HBAR_UNIT)),
    ])
    .tokenTransfers([
        new TokenTransfer(usdcTokenId, alice.accountId, -100, 6, false),
        new TokenTransfer(usdcTokenId, bob,             +100, 6, false),
    ])
    .nftTransfers([
        new NftTransfer(nftCollectionId, /* serial */ 42L, alice.accountId, bob, false),
    ])
    .signWithOperator(client)
    .sign(alice);

packed.submit(client);
```

### Sponsored spend via allowance (`approved = true`)

Alice previously called `AccountAllowanceApproveTransaction` to grant the spender an HBAR
allowance. The spender (here: the operator) now drains it without involving Alice — Alice's
signature is *not* required because `approved = true` on the debit leg tells the network to
charge the spender's allowance instead.

```
new TransferTransaction()
    .hbarTransfers([
        new HbarTransfer(alice, NativeToken.of(-10, HBAR_UNIT), /* approved */ true),
        new HbarTransfer(bob,   NativeToken.of(+10, HBAR_UNIT)),
    ])
    .signWithOperatorAndSubmit(client);   // operator = spender, signs as payer + allowance holder
```

### Approve HBAR / fungible / NFT allowances in one transaction

The *owner* of each leg must sign. If the owner is the operator, `signWithOperatorAndSubmit`
is enough; otherwise the owner has to co-sign as in the transfer multi-signature example
above.

```
HieroClient client = ...;     // operator = alice (the owner of all legs below)

new AccountAllowanceApproveTransaction()
    .hbarAllowances([
        new HbarAllowance(client.operatorAccountId, spender, NativeToken.of(100, HBAR_UNIT)),
    ])
    .tokenAllowances([
        new TokenAllowance(usdcTokenId, client.operatorAccountId, spender, 1_000_000L),
    ])
    .nftAllowances([
        // Specific serials:
        new NftAllowance(nftCollectionId,   client.operatorAccountId, spender, new NftSerialGrant([1L, 2L, 3L]), null),
        // Or: blanket grant over every current and future serial:
        new NftAllowance(otherCollectionId, client.operatorAccountId, spender, new NftCollectionGrant(true), null),
    ])
    .signWithOperatorAndSubmit(client);
```

### Revoke allowances

```
// Revoke an HBAR or fungible allowance by approving zero (owner = operator = alice):
new AccountAllowanceApproveTransaction()
    .hbarAllowances([
        new HbarAllowance(client.operatorAccountId, spender, NativeToken.of(0, HBAR_UNIT)),
    ])
    .signWithOperatorAndSubmit(client);

// Revoke a collection-wide NFT grant with NftCollectionGrant(false):
new AccountAllowanceApproveTransaction()
    .nftAllowances([
        new NftAllowance(otherCollectionId, client.operatorAccountId, spender, new NftCollectionGrant(false), null),
    ])
    .signWithOperatorAndSubmit(client);

// Revoke specific NFT serials regardless of current spender:
new AccountAllowanceDeleteTransaction()
    .nftAllowanceDeletions([
        new NftAllowanceDeletion(nftCollectionId, client.operatorAccountId, [1L, 2L, 3L]),
    ])
    .signWithOperatorAndSubmit(client);
```

## Testing

Offline tests of the account transactions, written as given / when / then: the attributes a transaction keeps, the
defaults of the attributes it leaves out, the packed form of each transaction and the values it rejects. Submitting
and the network's checks of the transfer sums, signatures and allowances are tested by the TCK, not here. The comment
above each test names where the current SDKs test the same.

```tests
// Source: this spec (@@default(false), @@nullable); the Python SDK (test_constructor_default_values of the account
// transactions) and the Java SDK (AccountCreateTransactionTest.propertiesTest: no staked node id, declineStakingReward
// false).
test "an account create transaction leaves out what is not given" {
    // when
    let transaction = AccountCreateTransaction{authority: DEFAULT(Authority)}

    // then
    assertEquals(expected: DEFAULT(Authority), actual: transaction.authority)
    assertNull(actual: transaction.initialBalance)
    assertNull(actual: transaction.accountMemo)
    assertFalse(condition: transaction.receiverSignatureRequired)
    assertNull(actual: transaction.maxAutomaticTokenAssociations)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.stakedAccountId)
    assertNull(actual: transaction.stakedNodeId)
    assertFalse(condition: transaction.declineStakingReward)
    assertNull(actual: transaction.alias)
}

// Source: the Java SDK (AccountCreateTransactionTest.propertiesTest: 450 tinybars, "some dumb memo", 100 automatic
// associations, an auto renew period of Duration.ofHours(10) = 36000 seconds, staked to 0.0.3, the EVM address alias
// 0x5c562e90feaf0eebd33ea75d21024f249d451417) and the Swift SDK (AccountCreateTransactionUnitTests.test_Properties,
// testAutoRenewPeriod = .hours(10)).
test "an account create transaction keeps its attributes" {
    // given
    let initialBalance: NativeToken<ANY, ANY> = hedera.Hbar{amount: 450, unit: hedera.HbarUnit.TINYBAR}
    let alias = hex(text: "5c562e90feaf0eebd33ea75d21024f249d451417")

    // when
    let transaction = AccountCreateTransaction{authority: DEFAULT(Authority), initialBalance: initialBalance,
            accountMemo: "some dumb memo", receiverSignatureRequired: true, maxAutomaticTokenAssociations: 100,
            autoRenewPeriod: toSeconds(value: 36000), stakedAccountId: AccountId.fromString(value: "0.0.3"),
            declineStakingReward: true, alias: alias}

    // then
    assertEquals(expected: initialBalance, actual: transaction.initialBalance)
    assertEquals(expected: "some dumb memo", actual: transaction.accountMemo)
    assertTrue(condition: transaction.receiverSignatureRequired)
    assertEquals(expected: 100, actual: transaction.maxAutomaticTokenAssociations)
    assertEquals(expected: toSeconds(value: 36000), actual: transaction.autoRenewPeriod)
    assertEquals(expected: AccountId.fromString(value: "0.0.3"), actual: transaction.stakedAccountId)
    assertNull(actual: transaction.stakedNodeId)
    assertTrue(condition: transaction.declineStakingReward)
    assertEquals(expected: alias, actual: transaction.alias)
}

// Source: the Java SDK (AccountCreateTransactionTest.shouldBytes and shouldBytes2: staked to an account, staked to node
// 4, both with an auto renew period of Duration.ofHours(10) = 36000 seconds), the Swift SDK (test_ToFromBytes and
// test_ToFromBytes2) and the Python SDK (test_serialization_round_trip_staked_node_id and ..._staked_account_id).
test "an account create transaction survives toBytes and fromBytes, staked to an account or to a node" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")]
    let toAccount = AccountCreateTransaction{authority: DEFAULT(Authority), accountMemo: "Test Account",
            autoRenewPeriod: toSeconds(value: 36000), stakedAccountId: AccountId.fromString(value: "0.0.8")}
    let toNode = AccountCreateTransaction{authority: DEFAULT(Authority), accountMemo: "Test Account",
            autoRenewPeriod: toSeconds(value: 36000), stakedNodeId: 1}

    for transaction in [toAccount, toNode] {
        // given
        let packed = transaction.pack(payer: payer, nodes: nodes)

        // when
        let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

        // then
        assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
        assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
    }
}

// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI (CryptoCreateTransactionBody.staked_id is a
// oneof); the C++ SDK (AccountCreateTransactionUnitTests.ResetStakedAccountId and ResetStakedNodeId) and the Python SDK
// (test_set_stake_account_id_reset_stake_node_id): setting one clears the other.
test "an account create transaction rejects being staked to an account and to a node" {
    // when / then
    @@expectThrows(invalid-argument-error)
    AccountCreateTransaction{authority: DEFAULT(Authority), stakedAccountId: AccountId.fromString(value: "0.0.3"),
            stakedNodeId: 4}
}

// Source: this spec (@@nullable: an update changes only what it names); the Python SDK
// (test_constructor_without_parameters of AccountUpdateTransaction).
test "an account update transaction changes nothing but what it names" {
    // when
    let transaction = AccountUpdateTransaction{accountId: DEFAULT(AccountId)}

    // then
    assertEquals(expected: DEFAULT(AccountId), actual: transaction.accountId)
    assertNull(actual: transaction.authority)
    assertNull(actual: transaction.accountMemo)
    assertNull(actual: transaction.receiverSignatureRequired)
    assertNull(actual: transaction.maxAutomaticTokenAssociations)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.expirationTime)
    assertNull(actual: transaction.stakedAccountId)
    assertNull(actual: transaction.stakedNodeId)
    assertNull(actual: transaction.declineStakingReward)
}

// Source: the Swift SDK (AccountUpdateTransactionUnitTests.test_GetSetAutoRenewPeriod and test_GetSetExpirationTime:
// autoRenewPeriod = .hours(10) = 36000 seconds, expirationTime = Timestamp(seconds: 1_554_158_543) =
// 2019-04-01T22:42:23Z) and the Java SDK (AccountUpdateTransactionTest: Duration.ofHours(10),
// Instant.ofEpochSecond(1554158543)).
test "an account update transaction keeps its auto renew period and expiration time" {
    // when
    let transaction = AccountUpdateTransaction{accountId: DEFAULT(AccountId), autoRenewPeriod: toSeconds(value: 36000),
            expirationTime: SDK_TEST_EXPIRATION_TIME}

    // then
    assertEquals(expected: toSeconds(value: 36000), actual: transaction.autoRenewPeriod)
    assertEquals(expected: SDK_TEST_EXPIRATION_TIME, actual: transaction.expirationTime)
}

// Source: the Java SDK (AccountUpdateTransactionTest.shouldBytes: Duration.ofHours(10) = 36000 seconds,
// Instant.ofEpochSecond(1554158543) = 2019-04-01T22:42:23Z) and the Swift SDK
// (AccountUpdateTransactionUnitTests.test_ToFromBytes).
test "an account update transaction survives toBytes and fromBytes" {
    // given
    let packed = AccountUpdateTransaction{accountId: DEFAULT(AccountId), authority: DEFAULT(Authority),
            accountMemo: "Some memo", receiverSignatureRequired: true, maxAutomaticTokenAssociations: 100,
            autoRenewPeriod: toSeconds(value: 36000), expirationTime: SDK_TEST_EXPIRATION_TIME,
            stakedNodeId: 4, declineStakingReward: true}
            .pack(payer: DEFAULT(consensusnode.client.Account), nodes: [AccountId.fromString(value: "0.0.3")])

    // when
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: the Python SDK (test_constructor_with_parameters of AccountDeleteTransaction), the Java SDK
// (AccountDeleteTransactionTest.shouldBytes), the JavaScript SDK ("should serialize and deserialize correctly") and the
// Swift SDK (test_ToFromBytes).
test "an account delete transaction keeps both accounts and survives toBytes and fromBytes" {
    // given
    let transaction = AccountDeleteTransaction{accountId: AccountId.fromString(value: "0.0.1001"),
            transferAccountId: AccountId.fromString(value: "0.0.1002")}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account), nodes: [AccountId.fromString(value: "0.0.3")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: AccountId.fromString(value: "0.0.1001"), actual: transaction.accountId)
    assertEquals(expected: AccountId.fromString(value: "0.0.1002"), actual: transaction.transferAccountId)
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: this spec (@@default([])); the Python SDK (test_constructor_default_values of TransferTransaction).
test "a transfer transaction without legs has empty lists" {
    // when
    let transaction = TransferTransaction{}

    // then
    assertEquals(expected: 0, actual: length(value: transaction.hbarTransfers))
    assertEquals(expected: 0, actual: length(value: transaction.tokenTransfers))
    assertEquals(expected: 0, actual: length(value: transaction.nftTransfers))
}

// Source: this spec (@@default(false) of approved, @@nullable expectedDecimals); the Python SDK (test_add_hbar_transfer,
// test_add_token_transfer, test_add_nft_transfer, test_token_transfer_without_expected_decimals).
test "a transfer leg is not approved and has no expected decimals unless given" {
    // when
    let hbarLeg = HbarTransfer{accountId: DEFAULT(AccountId), amount: hedera.Hbar{amount: -10, unit: hedera.HbarUnit.HBAR}}
    let tokenLeg = TokenTransfer{tokenId: Address.fromString(address: "0.0.5005"), accountId: DEFAULT(AccountId),
            amount: -100}
    let nftLeg = NftTransfer{tokenId: Address.fromString(address: "0.0.5005"), serial: 1,
            sender: AccountId.fromString(value: "0.0.1001"), receiver: AccountId.fromString(value: "0.0.1002")}

    // then
    assertFalse(condition: hbarLeg.approved)
    assertFalse(condition: tokenLeg.approved)
    assertNull(actual: tokenLeg.expectedDecimals)
    assertFalse(condition: nftLeg.approved)
}

// Source: this spec (a transfer is a list of legs; nothing merges them); the Python SDK (test_multiple_nft_transfers,
// test_nft_transfer_fields_preservation, test_expected_decimals_field_preservation).
test "a transfer transaction keeps its legs in their order" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let alice = AccountId.fromString(value: "0.0.1001")
    let bob = AccountId.fromString(value: "0.0.1002")
    let hbarLegs = [HbarTransfer{accountId: alice, amount: hedera.Hbar{amount: -10, unit: hedera.HbarUnit.HBAR}},
            HbarTransfer{accountId: bob, amount: hedera.Hbar{amount: 10, unit: hedera.HbarUnit.HBAR}}]
    let tokenLegs = [TokenTransfer{tokenId: token, accountId: alice, amount: -100, expectedDecimals: 6, approved: true},
            TokenTransfer{tokenId: token, accountId: bob, amount: 100, expectedDecimals: 6}]
    let nftLegs = [NftTransfer{tokenId: token, serial: 1, sender: alice, receiver: bob},
            NftTransfer{tokenId: token, serial: 2, sender: alice, receiver: bob, approved: true}]

    // when
    let transaction = TransferTransaction{hbarTransfers: hbarLegs, tokenTransfers: tokenLegs, nftTransfers: nftLegs}

    // then
    assertEquals(expected: hbarLegs, actual: transaction.hbarTransfers)
    assertEquals(expected: tokenLegs, actual: transaction.tokenTransfers)
    assertEquals(expected: 6, actual: transaction.tokenTransfers[0].expectedDecimals)
    assertTrue(condition: transaction.tokenTransfers[0].approved)
    assertEquals(expected: nftLegs, actual: transaction.nftTransfers)
    assertEquals(expected: 2, actual: transaction.nftTransfers[1].serial)
}

// Source: the Python SDK (test_combined_transfers_reconstruction) and the Swift SDK
// (TransferTransactionUnitTests.test_ToFromBytes).
test "a transfer of HBAR, a token and an NFT survives toBytes and fromBytes" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let alice = AccountId.fromString(value: "0.0.1001")
    let bob = AccountId.fromString(value: "0.0.1002")
    let packed = TransferTransaction{
            hbarTransfers: [HbarTransfer{accountId: alice, amount: hedera.Hbar{amount: -10, unit: hedera.HbarUnit.HBAR}},
                    HbarTransfer{accountId: bob, amount: hedera.Hbar{amount: 10, unit: hedera.HbarUnit.HBAR}}],
            tokenTransfers: [TokenTransfer{tokenId: token, accountId: alice, amount: -100, expectedDecimals: 6},
                    TokenTransfer{tokenId: token, accountId: bob, amount: 100, expectedDecimals: 6}],
            nftTransfers: [NftTransfer{tokenId: token, serial: 1, sender: alice, receiver: bob, approved: true}]}
            .pack(payer: DEFAULT(consensusnode.client.Account), nodes: [AccountId.fromString(value: "0.0.3"),
                    AccountId.fromString(value: "0.0.4")])

    // when
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: this spec (@@default([])); the Python SDK (test_account_allowance_transaction_initialization,
// test_build_proto_body_empty).
test "an allowance approve transaction without allowances has empty lists" {
    // when
    let transaction = AccountAllowanceApproveTransaction{}

    // then
    assertEquals(expected: 0, actual: length(value: transaction.hbarAllowances))
    assertEquals(expected: 0, actual: length(value: transaction.tokenAllowances))
    assertEquals(expected: 0, actual: length(value: transaction.nftAllowances))
}

// Source: the Python SDK (test_mixed_allowance_types, test_approve_token_nft_allowance_multiple_serials,
// test_approve_token_nft_allowance_all_serials, test_zero_amount_allowances) and the Java SDK
// (AccountAllowanceApproveTransactionTest.propertiesTest).
test "an allowance approve transaction keeps HBAR, token, serial and collection allowances" {
    // given
    let owner = AccountId.fromString(value: "0.0.1001")
    let spender = AccountId.fromString(value: "0.0.1002")
    let token = Address.fromString(address: "0.0.5005")
    let hbarAllowance = HbarAllowance{ownerAccountId: owner, spenderAccountId: spender,
            amount: hedera.Hbar{amount: 3, unit: hedera.HbarUnit.HBAR}}
    let revokedHbarAllowance = HbarAllowance{ownerAccountId: owner, spenderAccountId: spender,
            amount: hedera.Hbar{amount: 0, unit: hedera.HbarUnit.HBAR}}
    let tokenAllowance = TokenAllowance{tokenId: token, ownerAccountId: owner, spenderAccountId: spender, amount: 6}
    let serials = NftAllowance{tokenId: token, ownerAccountId: owner, spenderAccountId: spender,
            grant: NftSerialGrant{serials: [123, 456]}}
    let collection = NftAllowance{tokenId: Address.fromString(address: "0.0.6006"), ownerAccountId: owner,
            spenderAccountId: spender, grant: NftCollectionGrant{approved: true}}

    // when
    let transaction = AccountAllowanceApproveTransaction{hbarAllowances: [hbarAllowance, revokedHbarAllowance],
            tokenAllowances: [tokenAllowance], nftAllowances: [serials, collection]}

    // then
    assertEquals(expected: [hbarAllowance, revokedHbarAllowance], actual: transaction.hbarAllowances)
    assertEquals(expected: [tokenAllowance], actual: transaction.tokenAllowances)
    assertEquals(expected: [serials, collection], actual: transaction.nftAllowances)
    assertInstanceOf(expected: NftSerialGrant, actual: transaction.nftAllowances[0].grant)
    assertInstanceOf(expected: NftCollectionGrant, actual: transaction.nftAllowances[1].grant)
    assertNull(actual: transaction.nftAllowances[0].delegatingSpender)
}

// Source: HIP-336 (a spender with a collection grant delegates serials onward); this spec (delegatingSpender).
test "an NFT allowance keeps its delegating spender" {
    // given
    let delegatingSpender = AccountId.fromString(value: "0.0.1003")

    // when
    let allowance = NftAllowance{tokenId: Address.fromString(address: "0.0.5005"),
            ownerAccountId: AccountId.fromString(value: "0.0.1001"), spenderAccountId: AccountId.fromString(value: "0.0.1002"),
            grant: NftSerialGrant{serials: [1]}, delegatingSpender: delegatingSpender}

    // then
    assertEquals(expected: delegatingSpender, actual: allowance.delegatingSpender)
}

// Source: this spec (@@minSize(1): the whole collection is granted by an NftCollectionGrant, not by an empty list of
// serials as in the current SDKs, e.g. the JavaScript SDK TokenNftAllowance "toProtobuf() with no serial numbers").
test "a serial grant rejects an empty list of serials" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NftSerialGrant{serials: []}
}

// Source: the Java SDK (AccountAllowanceApproveTransactionTest.shouldBytes), the JavaScript SDK ("should round trip
// from bytes and maintain order") and the Swift SDK (test_ToFromBytes).
test "an allowance approve transaction survives toBytes and fromBytes" {
    // given
    let owner = AccountId.fromString(value: "0.0.1001")
    let spender = AccountId.fromString(value: "0.0.1002")
    let packed = AccountAllowanceApproveTransaction{
            hbarAllowances: [HbarAllowance{ownerAccountId: owner, spenderAccountId: spender,
                    amount: hedera.Hbar{amount: 3, unit: hedera.HbarUnit.HBAR}}],
            tokenAllowances: [TokenAllowance{tokenId: Address.fromString(address: "0.0.5005"), ownerAccountId: owner,
                    spenderAccountId: spender, amount: 6}],
            nftAllowances: [NftAllowance{tokenId: Address.fromString(address: "0.0.6006"), ownerAccountId: owner,
                    spenderAccountId: spender, grant: NftSerialGrant{serials: [123, 456]}},
                    NftAllowance{tokenId: Address.fromString(address: "0.0.7007"), ownerAccountId: owner,
                    spenderAccountId: spender, grant: NftCollectionGrant{approved: false}}]}
            .pack(payer: DEFAULT(consensusnode.client.Account), nodes: [AccountId.fromString(value: "0.0.3")])

    // when
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: the Java SDK (AccountAllowanceDeleteTransactionTest.shouldBytes), the JavaScript SDK ("[from|to]Bytes"),
// the Swift SDK (test_ToFromBytes) and the C++ SDK (DeleteNftAllowances).
test "an allowance delete transaction keeps its deletions and survives toBytes and fromBytes" {
    // given
    let deletion = NftAllowanceDeletion{tokenId: Address.fromString(address: "0.0.5005"),
            ownerAccountId: AccountId.fromString(value: "0.0.1001"), serials: [1, 2, 3]}
    let transaction = AccountAllowanceDeleteTransaction{nftAllowanceDeletions: [deletion]}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account), nodes: [AccountId.fromString(value: "0.0.3")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: [deletion], actual: transaction.nftAllowanceDeletions)
    assertEquals(expected: [1, 2, 3], actual: transaction.nftAllowanceDeletions[0].serials)
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI (CryptoUpdateTransactionBody.staked_id is a
// oneof); the Java SDK (AccountUpdateTransaction.setStakedAccountId / setStakedNodeId: setting one clears the other).
test "an account update transaction rejects being staked to an account and to a node" {
    // when / then
    @@expectThrows(invalid-argument-error)
    AccountUpdateTransaction{accountId: DEFAULT(AccountId), stakedAccountId: AccountId.fromString(value: "0.0.3"),
            stakedNodeId: 4}
}

// Source: this spec (@@maxBytes(100) of accountMemo); the consensus node (transaction.maxMemoUtf8Bytes = 100,
// MEMO_TOO_LONG): 50 characters "ä" are 100 bytes of UTF-8.
test "an account create transaction accepts a memo of 100 UTF-8 bytes" {
    // given
    let memo = repeat(value: "ä", count: 50)

    // when
    let transaction = AccountCreateTransaction{authority: DEFAULT(Authority), accountMemo: memo}

    // then
    assertEquals(expected: memo, actual: transaction.accountMemo)
}

// Source: this spec (@@maxBytes(100) of accountMemo); the consensus node (transaction.maxMemoUtf8Bytes = 100,
// MEMO_TOO_LONG): 51 characters "ä" are 102 bytes of UTF-8, although far fewer than 100 characters.
test "an account create transaction rejects a memo of more than 100 UTF-8 bytes" {
    // when / then
    @@expectThrows(invalid-argument-error)
    AccountCreateTransaction{authority: DEFAULT(Authority), accountMemo: repeat(value: "ä", count: 51)}
}
```

## Questions & Comments

- **How does an update remove the staking of an account?** `null` leaves `stakedAccountId` and `stakedNodeId`
  unchanged, so there is no way to say "stake to nothing". The current SDKs send a sentinel in one of the two fields
  (`clearStakedAccountId()` sends `0.0.0`, `clearStakedNodeId()` sends `-1`; documented in HAPI
  `crypto_update.proto`), which a V3 caller can only pass as a raw value today. Should the API model the removal
  explicitly? Note that `@@min(0)` on `stakedNodeId` would rule out the `-1` sentinel. — open

- **`AccountAllowanceDeleteTransaction` is NFT-only by design.** HAPI exposes only an NFT
  removal list, and HBAR / fungible-token allowances are deleted by approving zero. We
  intentionally do not surface a fake "delete fungible allowance" affordance — it would have
  to translate to an approve(0) under the hood, hiding which signature is actually required
  (the *owner*, same as approve). <- That should be done in enterprise layer
