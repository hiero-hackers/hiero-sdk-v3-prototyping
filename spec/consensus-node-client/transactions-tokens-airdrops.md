# Token Airdrop Transactions API (HIP-904)

This file extends the HTS token service ([`transactions-tokens.md`](transactions-tokens.md)) with
the **HIP-904 frictionless-airdrop** transactions. They share the
`consensusnode.transactions.tokens` namespace and the `Transaction<$$Receipt, $$Self>` lifecycle.

## Description

A normal token transfer fails if the recipient has neither associated the token nor a free
auto-association slot. HIP-904 adds a *push* model that never fails for that reason:

- **`TokenAirdrop`** — transfers tokens to recipients exactly like a `TransferTransaction`'s token
  legs, **but** if a recipient is not associated and has no free auto-association slot, the
  transfer is parked as a **pending airdrop** instead of failing. Associated recipients (or those
  with a free slot) receive immediately. The set of pending airdrops created is reported on the
  transaction **record**, not on the receipt.
- **`TokenClaimAirdrop`** — the **recipient** accepts one or more pending airdrops, auto-associating
  and crediting the tokens. Signed by the receiver.
- **`TokenCancelAirdrop`** — the **sender** withdraws one or more of its own outstanding pending
  airdrops before they are claimed. Signed by the sender.
- **`TokenReject`** — the current **holder** returns unwanted tokens to the token's treasury at no
  cost (the anti-spam counterpart: reject already-held tokens). Signed by the holder.

Airdrop transfers reuse the same `TokenTransfer` / `NftTransfer` leg types as
`TransferTransaction`; HBAR is not airdroppable, so there is no `hbarTransfers` leg.

### Pending-airdrop identity

A pending airdrop is identified by `(senderId, receiverId, token)` — fungible by token id, or NFT
by token id + serial. It is represented by `PendingAirdrop`, whose optional `serial` is absent for a
fungible airdrop and set for a specific NFT.

### Signing requirements

| Transaction | Signers required |
|-------------|------------------|
| `TokenAirdrop` | the *payer*; **and** each debited sender (or the payer, per leg, when `approved = true` draws on an allowance) — identical to `TransferTransaction`'s token / NFT legs. |
| `TokenClaimAirdrop` | the *payer*; **and** each pending airdrop's `receiverId` (the account accepting delivery). |
| `TokenCancelAirdrop` | the *payer*; **and** each pending airdrop's `senderId` (the account withdrawing its offer). |
| `TokenReject` | the *payer*; **and** the `owner` returning the tokens (defaults to the payer when unset). |

## Design Notes

- The `TokenTransfer` / `NftTransfer` leg types are defined in
  [`transactions-accounts.md`](transactions-accounts.md) (HAPI reuses `TokenTransferList`).
- HAPI types the pending-airdrop identity as `PendingAirdropId`; V3 does not yet have that typed
  identifier (nor `NftId`), so it is modelled here as the `PendingAirdrop` shape with a
  `@@nullable serial`. Both are tracked in [`missing-features.md`](../../docs/missing-features.md) §3.1.

## API Schema

```
namespace consensusnode.transactions.tokens
requires {Address, AccountId} from ledger
requires {Receipt, Transaction} from consensusnode.transactions
requires {TokenTransfer, NftTransfer} from consensusnode.transactions.accounts

// Placeholder for HAPI's typed PendingAirdropId (see missing-features §3.1).

// A single pending airdrop, identified by its sender, receiver, and token. `serial` is null for a
// fungible airdrop and set for a specific NFT.
type PendingAirdrop {
    @@immutable senderId: AccountId
    @@immutable receiverId: AccountId
    @@immutable tokenId: Address
    @@immutable @@nullable serial: int64        // null → fungible; set → that NFT serial
}

// Placeholder for HAPI's TokenReference / NftId (see missing-features §3.1).

// A reference to tokens being rejected: either a whole fungible token (serial absent) or a single
// NFT serial.
type TokenReference {
    @@immutable tokenId: Address
    @@immutable @@nullable serial: int64        // null → fungible token; set → that NFT serial
}

// Transfers tokens to recipients; recipients that are not associated and have no free
// auto-association slot receive a pending airdrop instead of the transaction failing. Reuses the
// transfer leg types from TransferTransaction. The protocol caps the combined transfer list at 10.
// The pending airdrops created are reported on the record, not on this receipt.
@@finalType
TokenAirdropTransaction extends Transaction<TokenAirdropReceipt, TokenAirdropTransaction> {
    @@immutable @@default([]) tokenTransfers: list<TokenTransfer>   // fungible legs (sum per token must be zero)
    @@immutable @@default([]) nftTransfers: list<NftTransfer>       // NFT legs
}

@@finalType
TokenAirdropReceipt extends Receipt {
}

// The receiver accepts pending airdrops, auto-associating and crediting the tokens. Each receiver
// must sign. Protocol cap 10 per transaction; no duplicates.
@@finalType
TokenClaimAirdropTransaction extends Transaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> {
    @@immutable @@minSize(1) @@maxSize(10) pendingAirdrops: list<PendingAirdrop>
}

@@finalType
TokenClaimAirdropReceipt extends Receipt {
}

// The sender withdraws its own outstanding pending airdrops before they are claimed. Each sender
// must sign. Protocol cap 10 per transaction; no duplicates.
@@finalType
TokenCancelAirdropTransaction extends Transaction<TokenCancelAirdropReceipt, TokenCancelAirdropTransaction> {
    @@immutable @@minSize(1) @@maxSize(10) pendingAirdrops: list<PendingAirdrop>
}

@@finalType
TokenCancelAirdropReceipt extends Receipt {
}

// Returns unwanted, already-held tokens to their treasury at no cost to the holder. Protocol cap
// 10 references per transaction.
@@finalType
TokenRejectTransaction extends Transaction<TokenRejectReceipt, TokenRejectTransaction> {
    @@immutable @@nullable owner: AccountId                          // the holder returning the tokens; defaults to the payer when unset
    @@immutable @@minSize(1) @@maxSize(10) rejections: list<TokenReference>
}

@@finalType
TokenRejectReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// One pending airdrop beyond the @@maxSize(10) of a claim or a cancel: the ten fungible airdrops of the tokens 0.0.8000
// to 0.0.8009 and an eleventh of the token 0.0.9999 from 0.0.8001 to 0.0.8002, as in the Python SDK
// (token_airdrop_claim_test, test_cannot_exceed_max_airdrops).
values ELEVEN_PENDING_AIRDROPS: list<PendingAirdrop> = [
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8000")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8001")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8002")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8003")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8004")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8005")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8006")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8007")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8008")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.8009")},
        PendingAirdrop{senderId: AccountId.fromString(value: "0.0.8001"), receiverId: AccountId.fromString(value: "0.0.8002"),
                tokenId: Address.fromString(address: "0.0.9999")}]

// One token reference beyond the @@maxSize(10) of a token reject: the fungible tokens 0.0.8000 to 0.0.8010.
values ELEVEN_TOKEN_REFERENCES: list<TokenReference> = [
        TokenReference{tokenId: Address.fromString(address: "0.0.8000")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8001")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8002")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8003")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8004")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8005")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8006")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8007")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8008")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8009")},
        TokenReference{tokenId: Address.fromString(address: "0.0.8010")}]
```

## Examples

### Airdrop a fungible token (parks as pending if the recipient is not associated)

```
HieroClient client = ...;

new TokenAirdropTransaction()
    .tokenTransfers([
        new TokenTransfer(usdcTokenId, treasury.accountId, -100, 6, false),
        new TokenTransfer(usdcTokenId, recipient,          +100, 6, false),
    ])
    .signWithOperatorAndSubmit(client);
```

### Recipient claims a pending airdrop

```
new TokenClaimAirdropTransaction()
    .pendingAirdrops([
        new PendingAirdrop(treasury.accountId, me.accountId, usdcTokenId, /* serial */ null),
    ])
    .sign(me, nodes)
    .submit(client);
```

### Reject already-held tokens back to treasury

```
new TokenRejectTransaction()
    .owner(me.accountId)
    .rejections([
        new TokenReference(usdcTokenId, null),          // a fungible token
        new TokenReference(nftCollectionId, 42L),       // one NFT serial
    ])
    .sign(me, nodes)
    .submit(client);
```

## Testing

Offline tests of the HIP-904 airdrop transactions, written as given / when / then: the defaults and values a
transaction keeps, the identity of a pending airdrop, the list sizes a transaction rejects, and that a packed
transaction survives its serialized form. Nothing is submitted; whether an airdrop is delivered or parked as pending is
decided by the network and tested by the Hiero TCK. The comment above each test names the SDK tests it comes from.

```tests
// Source: the @@default values of TokenAirdropTransaction; the Python SDK (test_from_proto_without_nft_transfers,
// test_from_proto_without_token_transfer) and the Java SDK (TokenAirdropTransactionTest, shouldBytesNoSetters).
@@testId("an-airdrop-transaction-without-transfers-has-no-token-and-no-nft-legs")
test "an airdrop transaction without transfers has no token and no NFT legs" {
    // when
    let airdrop = TokenAirdropTransaction{}

    // then
    assertEquals(expected: 0, actual: length(value: airdrop.tokenTransfers))
    assertEquals(expected: 0, actual: length(value: airdrop.nftTransfers))
}

// Source: the Java SDK (TokenAirdropTransactionTest, spawnTestTransaction: the same legs, among them the expected
// decimals 3 and the approved legs) and the Python SDK (test_build_transaction_body_with_approved_transfer,
// test_build_transaction_body_with_expected_decimal).
@@testId("an-airdrop-transaction-keeps-its-fungible-and-nft-legs-in-order")
test "an airdrop transaction keeps its fungible and NFT legs in order" {
    // given
    let token5 = Address.fromString(address: "0.0.5")
    let account5005 = AccountId.fromString(value: "0.0.5005")
    let account5006 = AccountId.fromString(value: "0.0.5006")
    let account5007 = AccountId.fromString(value: "0.0.5007")
    let account5008 = AccountId.fromString(value: "0.0.5008")
    let tokenTransfers = [
            TokenTransfer{tokenId: token5, accountId: account5006, amount: -800, expectedDecimals: 3},
            TokenTransfer{tokenId: token5, accountId: account5007, amount: 400, expectedDecimals: 3},
            TokenTransfer{tokenId: token5, accountId: account5008, amount: 400},
            TokenTransfer{tokenId: Address.fromString(address: "0.0.4"), accountId: account5006, amount: 123,
                    approved: true}]
    let nftTransfers = [
            NftTransfer{tokenId: Address.fromString(address: "0.0.3"), serial: 2, sender: account5008,
                    receiver: account5007},
            NftTransfer{tokenId: Address.fromString(address: "0.0.4"), serial: 4, sender: account5005,
                    receiver: account5006, approved: true}]

    // when
    let airdrop = TokenAirdropTransaction{tokenTransfers: tokenTransfers, nftTransfers: nftTransfers}

    // then
    assertEquals(expected: tokenTransfers, actual: airdrop.tokenTransfers)
    assertEquals(expected: nftTransfers, actual: airdrop.nftTransfers)
    assertEquals(expected: 3, actual: airdrop.tokenTransfers[0].expectedDecimals)
    assertNull(actual: airdrop.tokenTransfers[2].expectedDecimals)
    assertTrue(condition: airdrop.tokenTransfers[3].approved)
    assertFalse(condition: airdrop.nftTransfers[0].approved)
    assertTrue(condition: airdrop.nftTransfers[1].approved)
}

// Source: PendingAirdrop ("serial is null for a fungible airdrop and set for a specific NFT"); the Java SDK
// (PendingAirdropIdTest, testConstructorWithTokenId and testConstructorWithNftId: the same accounts and tokens), the Go
// SDK (TestPendingAirdropId_FromProtobuf) and the Python SDK (test_pending_airdrop_id_constructor).
@@testId("a-fungible-pending-airdrop-has-no-serial-and-an-nft-pending-airdrop-has-its-serial")
test "a fungible pending airdrop has no serial and an NFT pending airdrop has its serial" {
    // given
    let sender = AccountId.fromString(value: "0.0.1001")
    let receiver = AccountId.fromString(value: "0.0.1002")

    // when
    let fungible = PendingAirdrop{senderId: sender, receiverId: receiver,
            tokenId: Address.fromString(address: "0.0.1003")}
    let nft = PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: Address.fromString(address: "0.0.1004"),
            serial: 1}

    // then
    assertEquals(expected: sender, actual: fungible.senderId)
    assertEquals(expected: receiver, actual: fungible.receiverId)
    assertEquals(expected: Address.fromString(address: "0.0.1003"), actual: fungible.tokenId)
    assertNull(actual: fungible.serial)
    assertEquals(expected: Address.fromString(address: "0.0.1004"), actual: nft.tokenId)
    assertEquals(expected: 1, actual: nft.serial)
}

// Source: "Pending-airdrop identity" in this spec ((senderId, receiverId, token), the NFT by token and serial); the C++
// SDK (PendingAirdropIdUnitTests, OperatorEqualsFt, OperatorEqualsNft and OperatorNotEqualsDifferentSender / Receiver /
// Token: the same accounts and tokens).
@@testId("pending-airdrops-are-equal-exactly-when-sender-receiver-token-and-serial-are-equal")
test "pending airdrops are equal exactly when sender, receiver, token and serial are equal" {
    // given
    let sender = AccountId.fromString(value: "1.2.3")
    let receiver = AccountId.fromString(value: "4.5.6")
    let token = Address.fromString(address: "7.8.9")

    // when
    let fungible = PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: token}
    let nft = PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: token, serial: 10}

    // then
    assertEquals(expected: PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: token}, actual: fungible)
    assertEquals(expected: PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: token, serial: 10},
            actual: nft)
    assertNotEquals(unexpected: PendingAirdrop{senderId: AccountId.fromString(value: "10.2.3"), receiverId: receiver,
            tokenId: token}, actual: fungible)
    assertNotEquals(unexpected: PendingAirdrop{senderId: sender, receiverId: AccountId.fromString(value: "4.5.10"),
            tokenId: token}, actual: fungible)
    assertNotEquals(unexpected: PendingAirdrop{senderId: sender, receiverId: receiver,
            tokenId: Address.fromString(address: "7.8.10")}, actual: fungible)
    assertNotEquals(unexpected: fungible, actual: nft)
}

// Source: the Python SDK (token_airdrop_claim_test, test_order_preserved_across_batched_adds: the same pending
// airdrops) and the Java SDK (TokenClaimAirdropTransactionTest and TokenCancelAirdropTransactionTest,
// testGetAndSetPendingAirdropIds).
@@testId("claim-and-cancel-airdrop-transactions-keep-their-pending-airdrops-in-order")
test "claim and cancel airdrop transactions keep their pending airdrops in order" {
    // given
    let sender = AccountId.fromString(value: "0.0.9031")
    let receiver = AccountId.fromString(value: "0.0.9032")
    let pendingAirdrops = [
            PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: Address.fromString(address: "0.0.9030")},
            PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: Address.fromString(address: "0.0.9035"),
                    serial: 1},
            PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: Address.fromString(address: "0.0.9031")},
            PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: Address.fromString(address: "0.0.9035"),
                    serial: 2}]

    // when
    let claim = TokenClaimAirdropTransaction{pendingAirdrops: pendingAirdrops}
    let cancel = TokenCancelAirdropTransaction{pendingAirdrops: pendingAirdrops}

    // then
    assertEquals(expected: pendingAirdrops, actual: claim.pendingAirdrops)
    assertEquals(expected: pendingAirdrops, actual: cancel.pendingAirdrops)
}

// Source: TokenRejectTransaction.owner ("defaults to the payer when unset"); HAPI TokenRejectTransactionBody (an unset
// owner is the payer) and the JavaScript SDK (TokenReference, "initializes fungibleToken and nft to null").
@@testId("a-token-reject-transaction-without-owner-leaves-the-owner-unset")
test "a token reject transaction without owner leaves the owner unset" {
    // when
    let reject = TokenRejectTransaction{rejections: [TokenReference{tokenId: Address.fromString(address: "1.2.3")}]}

    // then
    assertNull(actual: reject.owner)
    assertNull(actual: reject.rejections[0].serial)
}

// Source: the Java SDK (TokenRejectTransactionTest: owner 0.6.9, the tokens 1.2.3, 4.5.6 and 7.8.9 and the NFTs
// 4.5.6/2 and 7.8.9/3) and the Python SDK (test_constructor_with_parameters).
@@testId("a-token-reject-transaction-keeps-the-owner-and-its-fungible-and-nft-references-in-order")
test "a token reject transaction keeps the owner and its fungible and NFT references in order" {
    // given
    let rejections = [TokenReference{tokenId: Address.fromString(address: "1.2.3")},
            TokenReference{tokenId: Address.fromString(address: "4.5.6")},
            TokenReference{tokenId: Address.fromString(address: "7.8.9")},
            TokenReference{tokenId: Address.fromString(address: "4.5.6"), serial: 2},
            TokenReference{tokenId: Address.fromString(address: "7.8.9"), serial: 3}]

    // when
    let reject = TokenRejectTransaction{owner: AccountId.fromString(value: "0.6.9"), rejections: rejections}

    // then
    assertEquals(expected: AccountId.fromString(value: "0.6.9"), actual: reject.owner)
    assertEquals(expected: rejections, actual: reject.rejections)
}

// Source: this spec (@@minSize(1)); the network rejects an empty claim with EMPTY_PENDING_AIRDROP_ID_LIST (the Java
// SDK, TokenAirdropClaimIntegrationTest "Cannot claim the tokens with empty list") and the Python SDK
// (test_min_ids_enforced_on_build_hits_validation).
@@testId("a-claim-airdrop-transaction-rejects-an-empty-list-of-pending-airdrops")
test "a claim airdrop transaction rejects an empty list of pending airdrops" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenClaimAirdropTransaction{pendingAirdrops: []}
}

// Source: this spec (@@maxSize(10)); the Python SDK (token_airdrop_claim_test, test_cannot_exceed_max_airdrops).
@@testId("a-claim-airdrop-transaction-rejects-more-than-10-pending-airdrops")
test "a claim airdrop transaction rejects more than 10 pending airdrops" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenClaimAirdropTransaction{pendingAirdrops: ELEVEN_PENDING_AIRDROPS}
}

// Source: this spec (@@minSize(1)); the network rejects an empty cancel with EMPTY_PENDING_AIRDROP_ID_LIST (the Java
// SDK, TokenAirdropCancelIntegrationTest "Cannot cancel the tokens with empty list").
@@testId("a-cancel-airdrop-transaction-rejects-an-empty-list-of-pending-airdrops")
test "a cancel airdrop transaction rejects an empty list of pending airdrops" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCancelAirdropTransaction{pendingAirdrops: []}
}

// Source: this spec (@@maxSize(10), the same protocol cap as a claim).
@@testId("a-cancel-airdrop-transaction-rejects-more-than-10-pending-airdrops")
test "a cancel airdrop transaction rejects more than 10 pending airdrops" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCancelAirdropTransaction{pendingAirdrops: ELEVEN_PENDING_AIRDROPS}
}

// Source: this spec (@@minSize(1)); the network rejects a reject without references with EMPTY_TOKEN_REFERENCE_LIST
// (the Java SDK, TokenRejectIntegrationTest "Cannot Reject a Token when Token Or NFT ID is not set").
@@testId("a-token-reject-transaction-rejects-an-empty-list-of-references")
test "a token reject transaction rejects an empty list of references" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenRejectTransaction{rejections: []}
}

// Source: this spec (@@maxSize(10)); the network rejects 11 references with TOKEN_REFERENCE_LIST_SIZE_LIMIT_EXCEEDED
// (the Java SDK, TokenRejectIntegrationTest "Cannot Reject a Token when executing TokenReject and Token Reference List
// Size Exceeded", and the same test of the Go, JavaScript, Python, Rust and Swift SDKs).
@@testId("a-token-reject-transaction-rejects-more-than-10-references")
test "a token reject transaction rejects more than 10 references" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenRejectTransaction{rejections: ELEVEN_TOKEN_REFERENCES}
}

// Source: the Java SDK (shouldBytes / shouldBytesNoSetters of the TokenAirdrop, TokenClaimAirdrop, TokenCancelAirdrop
// and TokenReject tests: fromBytes(toBytes()) gives the same transaction), the Swift SDK (testToFromBytes) and the
// JavaScript SDK (TokenAirdropTransaction, "from | toBytes").
@@testId("every-airdrop-transaction-survives-tobytes-and-frombytes-unchanged")
test "every airdrop transaction survives toBytes and fromBytes unchanged" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")]
    let token = Address.fromString(address: "0.0.5")
    let sender = AccountId.fromString(value: "0.0.5006")
    let receiver = AccountId.fromString(value: "0.0.5008")
    let pendingAirdrops = [PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: token},
            PendingAirdrop{senderId: sender, receiverId: receiver, tokenId: Address.fromString(address: "0.0.3"),
                    serial: 2}]
    let transactions: list<consensusnode.transactions.PackedTransaction<ANY, ANY>> = [
            TokenAirdropTransaction{
                    tokenTransfers: [TokenTransfer{tokenId: token, accountId: sender, amount: -400},
                            TokenTransfer{tokenId: token, accountId: receiver, amount: 400}],
                    nftTransfers: [NftTransfer{tokenId: Address.fromString(address: "0.0.3"), serial: 2, sender: sender,
                            receiver: receiver}]}.sign(payer: payer, nodes: nodes),
            TokenClaimAirdropTransaction{pendingAirdrops: pendingAirdrops}.sign(payer: payer, nodes: nodes),
            TokenCancelAirdropTransaction{pendingAirdrops: pendingAirdrops}.sign(payer: payer, nodes: nodes),
            TokenRejectTransaction{owner: sender, rejections: [TokenReference{tokenId: token},
                    TokenReference{tokenId: Address.fromString(address: "0.0.3"), serial: 2}]}
                    .sign(payer: payer, nodes: nodes)]

    for transaction in transactions {
        // when
        let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: transaction.toBytes())

        // then
        assertEquals(expected: transaction.toBytes(), actual: restored.toBytes())
        assertEquals(expected: transaction.transactionId, actual: restored.transactionId)
        assertEquals(expected: transaction.signableBodies(), actual: restored.signableBodies())
        assertEquals(expected: 2, actual: length(value: restored.nodeSignatures))
    }
}
```

## Questions & Comments

- **Pending airdrops are a *record* field, not a receipt field.** HAPI returns the set of created
  pending airdrops in `TransactionRecord.new_pending_airdrops`, not the receipt. V3 does not yet
  type the record per transaction, so `TokenAirdropReceipt` is empty and the pending-airdrop result
  is part of the open base-`Record` design decision tracked in
  [`missing-features.md`](../../docs/missing-features.md) §3.5. Add a `pendingAirdrops` field to a typed
  `TokenAirdropRecord` once that lands.

- **`PendingAirdrop` / `TokenReference` are placeholders for typed identifiers.** They stand in for
  HAPI's `PendingAirdropId` and `TokenReference` / `NftId`, which are not yet typed in V3 (§3.1).
  The `@@nullable serial` discriminates fungible vs NFT until `NftId` lands, at which point both
  shapes should be retyped.

- **Reusing `TokenTransfer` / `NftTransfer` from `consensusnode.transactions.accounts`.** HAPI
  shares `TokenTransferList` between `CryptoTransfer` and `TokenAirdrop`; V3 currently defines those
  leg types in [`transactions-accounts.md`](transactions-accounts.md), so this file imports them
  from there. Arguably those leg types belong in a shared location (they are not crypto-service
  specific); if they move, this import follows. The dependency is one-directional
  (`...tokens → ...accounts`) and acyclic.

- **`TokenReject` `owner` defaults to the payer.** Matching HAPI, an unset `owner` means the payer
  is rejecting its own tokens; set it only when a separate account (which must then sign) is the
  holder.

- **`pendingAirdrops` is a list, but duplicates are not allowed.** The comments of
  `TokenClaimAirdropTransaction` and `TokenCancelAirdropTransaction` say "no duplicates", and the Python SDK rejects
  a duplicate pending airdrop before submitting; the order has no meaning to the network. Like
  `TokenAssociateTransaction.tokens`, this reads like a `set<PendingAirdrop>`. Should the type change, or should the
  order be kept and a duplicate be an error of the SDK? — open
