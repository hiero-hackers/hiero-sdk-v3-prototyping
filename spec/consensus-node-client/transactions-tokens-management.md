# Token Management Transactions API

This file extends the HTS token service specified in
[`transactions-tokens.md`](transactions-tokens.md) with the **management** transactions —
the permissioned operations gated by a token's individual keys (wipe, freeze, KYC, pause,
per-serial metadata). They share the `consensusnode.transactions.tokens` namespace, the
`Transaction<$$Receipt, $$Self>` lifecycle, and the signing model of the core lifecycle file; the split is
purely for readability.

## Description

The token management transactions are the permissioned operations gated by a token's individual
keys. Each is authorised by one specific token key (an `Authority`), set when the token is created.
A key that was left unset at creation permanently disables the corresponding capability (the
network rejects the transaction with `KEY_NOT_PROVIDED` / `TOKEN_HAS_NO_*_KEY`), so these
transactions are only valid on tokens that opted into the capability.

- **`TokenWipe`** — burns units from a **non-treasury** account (e.g. to claw back after a
  compliance event). Gated by `wipeAuthority`. Fungible: an `amount`; NFT: up to 10 `serials`.
  Wiping the treasury is rejected (`CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT`) — use `TokenBurn` for the
  treasury.
- **`TokenFreeze` / `TokenUnfreeze`** — toggle the frozen state of a specific account for a token,
  blocking / re-enabling its transfers. Gated by `freezeAuthority`.
- **`TokenGrantKyc` / `TokenRevokeKyc`** — toggle the KYC flag of a specific account for a token.
  Gated by `kycAuthority`.
- **`TokenPause` / `TokenUnpause`** — pause / resume **all** operations on the token network-wide.
  Gated by `pauseAuthority`.
- **`TokenUpdateNfts`** (HIP-657) — replace the metadata of specific NFT serials after mint. Gated
  by `metadataAuthority`.

Updating a token's custom-fee schedule (gated by `feeScheduleAuthority`) is not supported yet.

### Signing requirements

| Transaction | Signers required |
|-------------|------------------|
| `TokenWipe` | the *payer*; **and** the token's `wipeAuthority`. The wiped account does **not** sign (it is being acted upon, not consenting). |
| `TokenFreeze` / `TokenUnfreeze` | the *payer*; **and** the token's `freezeAuthority`. |
| `TokenGrantKyc` / `TokenRevokeKyc` | the *payer*; **and** the token's `kycAuthority`. |
| `TokenPause` / `TokenUnpause` | the *payer*; **and** the token's `pauseAuthority`. |
| `TokenUpdateNfts` | the *payer*; **and** the token's `metadataAuthority`. |

## Design Notes

- `TokenFeeScheduleUpdate` belongs to this group conceptually (gated by `feeScheduleAuthority`) but
  is **not specified here** — its sole payload is the custom-fee schedule, which depends on the
  write-side `CustomFee` hierarchy that does not yet exist (tracked in
  [`missing-features.md`](../../docs/missing-features.md) §3.3). It lands once that hierarchy does.
- The token key type `Authority` is specified in [`authority.md`](../base/authority.md).
- `TokenWipe` mixing fungible and NFT payloads is rejected by the network; the `@@oneOf` makes it
  statically checkable.

- **Wipe is sealed over its two variants.** Like mint and burn it follows
  [ADR-0011](../../docs/adr/0011-model-a-choice-as-a-sealed-type.md): the shared `tokenId` and
  `accountId` sit in the abstraction, the amount and the serials each in their own final type.

## API Schema

```
namespace consensusnode.transactions.tokens
requires {Address, AccountId} from ledger
requires {Receipt, Transaction} from consensusnode.transactions

// Removes (burns) units of a token from a NON-treasury account. Requires the token's
// wipeAuthority. Which of the two wipe transactions applies follows from the token's type:
// TokenWipeFungibleTransaction for FUNGIBLE_COMMON, TokenWipeNftTransaction for
// NON_FUNGIBLE_UNIQUE.
@@sealed(TokenWipeFungibleTransaction, TokenWipeNftTransaction)
abstraction TokenWipeTransaction<$$Self extends TokenWipeTransaction<$$Self>> extends Transaction<TokenWipeReceipt, $$Self> {
    @@immutable tokenId: Address                                    // the token to wipe
    @@immutable accountId: AccountId                                // the non-treasury account to wipe from (MUST NOT be the treasury)
}

// Wipes units of a FUNGIBLE_COMMON token from the account.
@@finalType
TokenWipeFungibleTransaction extends TokenWipeTransaction<TokenWipeFungibleTransaction> {
    @@immutable @@min(1) amount: int64                              // amount to wipe, in the smallest indivisible unit
}

// Wipes NON_FUNGIBLE_UNIQUE serials held by the account.
@@finalType
TokenWipeNftTransaction extends TokenWipeTransaction<TokenWipeNftTransaction> {
    @@immutable @@minSize(1) @@maxSize(10) serials: list<int64>     // serials held by accountId; the protocol caps the list at 10 per transaction
}

@@finalType
TokenWipeReceipt extends Receipt {
    @@immutable totalSupply: int64                                  // the token's new totalSupply after the wipe
}

// Freezes a specific account for a token: until unfrozen it cannot send or receive the token.
// Requires the token's freezeAuthority (unset → TOKEN_HAS_NO_FREEZE_KEY).
@@finalType
TokenFreezeTransaction extends Transaction<TokenFreezeReceipt, TokenFreezeTransaction> {
    @@immutable tokenId: Address
    @@immutable accountId: AccountId
}

@@finalType
TokenFreezeReceipt extends Receipt {
}

// Reverses a freeze. Requires the token's freezeAuthority.
@@finalType
TokenUnfreezeTransaction extends Transaction<TokenUnfreezeReceipt, TokenUnfreezeTransaction> {
    @@immutable tokenId: Address
    @@immutable accountId: AccountId
}

@@finalType
TokenUnfreezeReceipt extends Receipt {
}

// Grants KYC to a specific account for a token, enabling it to transact the token.
// Requires the token's kycAuthority (unset → TOKEN_HAS_NO_KYC_KEY).
@@finalType
TokenGrantKycTransaction extends Transaction<TokenGrantKycReceipt, TokenGrantKycTransaction> {
    @@immutable tokenId: Address
    @@immutable accountId: AccountId
}

@@finalType
TokenGrantKycReceipt extends Receipt {
}

// Revokes KYC from a specific account for a token. Requires the token's kycAuthority.
@@finalType
TokenRevokeKycTransaction extends Transaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> {
    @@immutable tokenId: Address
    @@immutable accountId: AccountId
}

@@finalType
TokenRevokeKycReceipt extends Receipt {
}

// Pauses every operation on the token network-wide (transfers, mints, burns, ...) until unpaused.
// Requires the token's pauseAuthority (unset → TOKEN_HAS_NO_PAUSE_KEY).
@@finalType
TokenPauseTransaction extends Transaction<TokenPauseReceipt, TokenPauseTransaction> {
    @@immutable tokenId: Address
}

@@finalType
TokenPauseReceipt extends Receipt {
}

// Resumes a paused token. Requires the token's pauseAuthority.
@@finalType
TokenUnpauseTransaction extends Transaction<TokenUnpauseReceipt, TokenUnpauseTransaction> {
    @@immutable tokenId: Address
}

@@finalType
TokenUnpauseReceipt extends Receipt {
}

// Replaces the metadata of specific NFT serials after mint (HIP-657). Requires the token's
// metadataAuthority (unset → metadata is immutable). Applies the same `metadata` to every listed
// serial; null metadata leaves it unchanged.
@@finalType
TokenUpdateNftsTransaction extends Transaction<TokenUpdateNftsReceipt, TokenUpdateNftsTransaction> {
    @@immutable tokenId: Address
    @@immutable @@minSize(1) @@maxSize(10) serials: list<int64>   // serials to update; protocol cap 10 per transaction
    @@immutable @@nullable @@maxSize(100) metadata: bytes           // new per-serial metadata (≤ 100 bytes); null → leave unchanged
}

@@finalType
TokenUpdateNftsReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// One serial beyond the @@maxSize(10) of the serials of an NFT wipe and an NFT metadata update (the serials 1 to 11).
values ELEVEN_SERIALS: list<int64> = range(start: 1, end: 12)
```

## Examples

### Freeze, then unfreeze an account (operator holds the freeze key)

```
HieroClient client = ...;

new TokenFreezeTransaction()
    .tokenId(tokenId)
    .accountId(bob.accountId)
    .signWithOperatorAndSubmit(client);

// ... later ...
new TokenUnfreezeTransaction()
    .tokenId(tokenId)
    .accountId(bob.accountId)
    .signWithOperatorAndSubmit(client);
```

### Wipe NFT serials from a holder (multi-signature)

The operator pays; a dedicated wipe-key holder authorises the claw-back.

```
PackedTransaction<...> packed = new TokenWipeNftTransaction()
    .tokenId(nftCollectionId)
    .accountId(holder.accountId)
    .serials([7L, 8L])
    .signWithOperator(client)        // payer
    .sign(wipeKeySigner);            // wipeAuthority

int64 newTotalSupply = packed.submit(client).queryReceipt(client).totalSupply;
```

### Update NFT metadata (HIP-657)

```
new TokenUpdateNftsTransaction()
    .tokenId(nftCollectionId)
    .serials([1L, 2L, 3L])
    .metadata("ipfs://QmUpdatedManifest".bytes)
    .signWithOperatorAndSubmit(client);
```

## Testing

Offline tests of the token management transactions, written as given / when / then: the values a transaction keeps,
the values it rejects, and that a packed transaction survives its serialized form. Nothing is submitted; whether the network accepts a
transaction (the token has the key, the account is no treasury) is tested by the Hiero TCK. The current SDKs test the
same behaviour with their getter / setter and `fromBytes(toBytes())` tests; the comment above each test names where.

```tests
// Source: the Java SDK (TokenWipeTransactionTest: token 4.2.0, account 0.6.9, amount 4) and the JavaScript SDK
// (TokenWipeTransaction, "should handle large bigint amounts": 9000000000000000000).
@@testId("a-fungible-wipe-transaction-keeps-the-token-the-account-and-the-amount")
test "a fungible wipe transaction keeps the token, the account and the amount" {
    // given
    let token = Address.fromString(address: "4.2.0")
    let account = AccountId.fromString(value: "0.6.9")

    // when
    let wipe = TokenWipeFungibleTransaction{tokenId: token, accountId: account, amount: 4}
    let largeWipe = TokenWipeFungibleTransaction{tokenId: token, accountId: account, amount: 9000000000000000000}

    // then
    assertEquals(expected: token, actual: wipe.tokenId)
    assertEquals(expected: account, actual: wipe.accountId)
    assertEquals(expected: 4, actual: wipe.amount)
    assertEquals(expected: 9000000000000000000, actual: largeWipe.amount)
}

// Source: the Java SDK (TokenWipeTransactionTest: serials 8, 9 and 10) and the Python SDK
// (test_constructor_with_serial_numbers).
@@testId("an-nft-wipe-transaction-keeps-its-serials-in-order")
test "an NFT wipe transaction keeps its serials in order" {
    // when
    let wipe = TokenWipeNftTransaction{tokenId: Address.fromString(address: "4.2.0"),
            accountId: AccountId.fromString(value: "0.6.9"), serials: [8, 9, 10]}

    // then
    assertEquals(expected: Address.fromString(address: "4.2.0"), actual: wipe.tokenId)
    assertEquals(expected: AccountId.fromString(value: "0.6.9"), actual: wipe.accountId)
    assertEquals(expected: [8, 9, 10], actual: wipe.serials)
}

// Source: the Java SDK (TokenFreezeTransactionTest and TokenUnfreezeTransactionTest: token 6.5.4, account 0.0.222;
// TokenGrantKycTransactionTest and TokenRevokeKycTransactionTest: token 4.2.0, account 6.9.0).
@@testId("freeze-unfreeze-grant-kyc-and-revoke-kyc-transactions-keep-the-token-and-the-account")
test "freeze, unfreeze, grant KYC and revoke KYC transactions keep the token and the account" {
    // given
    let frozenToken = Address.fromString(address: "6.5.4")
    let frozenAccount = AccountId.fromString(value: "0.0.222")
    let kycToken = Address.fromString(address: "4.2.0")
    let kycAccount = AccountId.fromString(value: "6.9.0")

    // when
    let freeze = TokenFreezeTransaction{tokenId: frozenToken, accountId: frozenAccount}
    let unfreeze = TokenUnfreezeTransaction{tokenId: frozenToken, accountId: frozenAccount}
    let grantKyc = TokenGrantKycTransaction{tokenId: kycToken, accountId: kycAccount}
    let revokeKyc = TokenRevokeKycTransaction{tokenId: kycToken, accountId: kycAccount}

    // then
    assertEquals(expected: frozenToken, actual: freeze.tokenId)
    assertEquals(expected: frozenAccount, actual: freeze.accountId)
    assertEquals(expected: frozenToken, actual: unfreeze.tokenId)
    assertEquals(expected: frozenAccount, actual: unfreeze.accountId)
    assertEquals(expected: kycToken, actual: grantKyc.tokenId)
    assertEquals(expected: kycAccount, actual: grantKyc.accountId)
    assertEquals(expected: kycToken, actual: revokeKyc.tokenId)
    assertEquals(expected: kycAccount, actual: revokeKyc.accountId)
}

// Source: the Java SDK (TokenPauseTransactionTest and TokenUnpauseTransactionTest: token 4.2.0) and the Python SDK
// (test__from_proto_restores_token_id).
@@testId("pause-and-unpause-transactions-keep-the-token")
test "pause and unpause transactions keep the token" {
    // when
    let pause = TokenPauseTransaction{tokenId: Address.fromString(address: "4.2.0")}
    let unpause = TokenUnpauseTransaction{tokenId: Address.fromString(address: "4.2.0")}

    // then
    assertEquals(expected: Address.fromString(address: "4.2.0"), actual: pause.tokenId)
    assertEquals(expected: Address.fromString(address: "4.2.0"), actual: unpause.tokenId)
}

// Source: TokenUpdateNftsTransaction.metadata ("null → leave unchanged"); the JavaScript SDK
// (TokenUpdateNftsTransaction, "should _metadata equal to null").
@@testId("an-nft-metadata-update-without-metadata-leaves-the-metadata-unchanged")
test "an NFT metadata update without metadata leaves the metadata unchanged" {
    // when
    let update = TokenUpdateNftsTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: [8, 9, 10]}

    // then
    assertNull(actual: update.metadata)
}

// Source: the Java SDK (TokenUpdateNftsTransactionTest: token 4.2.0, serials 8, 9 and 10, metadata 1, 2, 3, 4, 5) and
// the Python SDK (test_constructor_with_parameters).
@@testId("an-nft-metadata-update-keeps-the-token-the-serials-and-the-metadata")
test "an NFT metadata update keeps the token, the serials and the metadata" {
    // when
    let update = TokenUpdateNftsTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: [8, 9, 10],
            metadata: [1, 2, 3, 4, 5]}

    // then
    assertEquals(expected: Address.fromString(address: "4.2.0"), actual: update.tokenId)
    assertEquals(expected: [8, 9, 10], actual: update.serials)
    assertEquals(expected: hex(text: "0102030405"), actual: update.metadata)
}

// Source: this spec (@@min(1) of amount).
@@testId("a-fungible-wipe-transaction-rejects-an-amount-of-0")
test "a fungible wipe transaction rejects an amount of 0" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenWipeFungibleTransaction{tokenId: Address.fromString(address: "4.2.0"),
            accountId: AccountId.fromString(value: "0.6.9"), amount: 0}
}

// Source: this spec (@@minSize(1)).
@@testId("an-nft-wipe-transaction-rejects-an-empty-list-of-serials")
test "an NFT wipe transaction rejects an empty list of serials" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenWipeNftTransaction{tokenId: Address.fromString(address: "4.2.0"),
            accountId: AccountId.fromString(value: "0.6.9"), serials: []}
}

// Source: this spec (@@maxSize(10): the network caps a wipe at 10 NFTs).
@@testId("an-nft-wipe-transaction-rejects-more-than-10-serials")
test "an NFT wipe transaction rejects more than 10 serials" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenWipeNftTransaction{tokenId: Address.fromString(address: "4.2.0"),
            accountId: AccountId.fromString(value: "0.6.9"), serials: ELEVEN_SERIALS}
}

// Source: this spec (@@minSize(1)); the Python SDK (test_build_transaction_body_validation_errors of
// TokenUpdateNftsTransaction: "Missing serial numbers").
@@testId("an-nft-metadata-update-rejects-an-empty-list-of-serials")
test "an NFT metadata update rejects an empty list of serials" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenUpdateNftsTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: []}
}

// Source: this spec (@@maxSize(10): the network caps an update at 10 NFTs).
@@testId("an-nft-metadata-update-rejects-more-than-10-serials")
test "an NFT metadata update rejects more than 10 serials" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenUpdateNftsTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: ELEVEN_SERIALS}
}

// Source: this spec (@@maxSize(100)); HIP-657 (NFT metadata of at most 100 bytes); the Python SDK
// (test_build_transaction_body_validation_errors of TokenUpdateNftsTransaction: b"x" * 101).
@@testId("an-nft-metadata-update-rejects-metadata-longer-than-100-bytes")
test "an NFT metadata update rejects metadata longer than 100 bytes" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenUpdateNftsTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: [8, 9, 10],
            metadata: repeat(value: hex(text: "78"), count: 101)}
}

// Source: the Java SDK (shouldBytes / shouldBytesFungible / shouldBytesNft of the TokenWipe, TokenFreeze,
// TokenUnfreeze, TokenGrantKyc, TokenRevokeKyc, TokenPause, TokenUnpause and TokenUpdateNfts tests:
// fromBytes(toBytes()) gives the same transaction); the JavaScript SDK (TokenWipeTransaction, "should round trip from
// bytes") and the to / from bytes tests of the Go SDK.
@@testId("every-token-management-transaction-survives-tobytes-and-frombytes-unchanged")
test "every token management transaction survives toBytes and fromBytes unchanged" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")]
    let token = Address.fromString(address: "4.2.0")
    let account = AccountId.fromString(value: "0.6.9")
    let transactions: list<consensusnode.transactions.PackedTransaction<ANY, ANY>> = [
            TokenWipeFungibleTransaction{tokenId: token, accountId: account, amount: 4}
                    .sign(payer: payer, nodes: nodes),
            TokenWipeNftTransaction{tokenId: token, accountId: account, serials: [8, 9, 10]}
                    .sign(payer: payer, nodes: nodes),
            TokenFreezeTransaction{tokenId: token, accountId: account}.sign(payer: payer, nodes: nodes),
            TokenUnfreezeTransaction{tokenId: token, accountId: account}.sign(payer: payer, nodes: nodes),
            TokenGrantKycTransaction{tokenId: token, accountId: account}.sign(payer: payer, nodes: nodes),
            TokenRevokeKycTransaction{tokenId: token, accountId: account}.sign(payer: payer, nodes: nodes),
            TokenPauseTransaction{tokenId: token}.sign(payer: payer, nodes: nodes),
            TokenUnpauseTransaction{tokenId: token}.sign(payer: payer, nodes: nodes),
            TokenUpdateNftsTransaction{tokenId: token, serials: [8, 9, 10], metadata: [1, 2, 3, 4, 5]}
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

- **`TokenFeeScheduleUpdate` is intentionally absent.** Its only payload is the custom-fee
  schedule, and the write-side `CustomFee` hierarchy is not yet specified (read-side models exist
  on the mirror node — see [`mirror-node-token.md`](../mirror-node-client/mirror-node-token.md)).
  Tracked in [`missing-features.md`](../../docs/missing-features.md) §3.3; it lands in this same
  namespace once the `CustomFee` builder does.

- **No `KeyUpdate` / key-clearing here.** These transactions consume the token keys but do not
  change them — key rotation / clearing is `TokenUpdate` (core file) plus the deferred write-side
  `KeyUpdate` operation (see [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md)).

- **`TokenWipe` cannot target the treasury.** The treasury's supply is adjusted with `TokenBurn`
  (core file), which is gated by `supplyAuthority`, not `wipeAuthority`. The two are deliberately
  distinct capabilities.

- **`TokenPause` is token-wide, not per-account.** Unlike freeze (per-account), pause halts the
  entire token. There is no per-account variant.
