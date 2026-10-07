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

## API Schema

```
namespace consensusnode.transactions.tokens
requires {Address, AccountId} from ledger
requires {Receipt, Transaction} from consensusnode.transactions

// Removes (burns) units of a token from a NON-treasury account. Requires the token's
// wipeAuthority. The payload differs by token kind:
//   - FUNGIBLE_COMMON     → set `amount` (count in the smallest unit, must be > 0).
//   - NON_FUNGIBLE_UNIQUE → set `serials` (the serials held by `accountId`, max 10).
// Exactly one of `amount` and `serials` must be set.
@@oneOf(amount, serials)
@@finalType
TokenWipeTransaction extends Transaction<TokenWipeReceipt, TokenWipeTransaction> {
    @@immutable tokenId: Address
    @@immutable accountId: AccountId                                // the non-treasury account to wipe from (MUST NOT be the treasury)
    @@immutable @@nullable @@min(1) amount: int64                   // FUNGIBLE_COMMON only
    @@immutable @@nullable @@minSize(1) @@maxSize(10) serials: list<int64>   // NON_FUNGIBLE_UNIQUE only; protocol cap 10 per transaction
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
PackedTransaction<...> packed = new TokenWipeTransaction()
    .tokenId(nftCollectionId)
    .accountId(holder.accountId)
    .serials([7L, 8L])
    .signWithOperator(client)        // payer
    .sign(wipeKeySigner);            // wipeAuthority

int64 newTotalSupply = packed.submit(client).queryReceipt().totalSupply;
```

### Update NFT metadata (HIP-657)

```
new TokenUpdateNftsTransaction()
    .tokenId(nftCollectionId)
    .serials([1L, 2L, 3L])
    .metadata("ipfs://QmUpdatedManifest".bytes)
    .signWithOperatorAndSubmit(client);
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
