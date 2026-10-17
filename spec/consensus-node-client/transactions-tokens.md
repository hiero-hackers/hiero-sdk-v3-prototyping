# Token Transactions API

## Description

The token service (HTS — Hedera Token Service, retained as the protocol-level name in Hiero) lets
users create, manage, and operate first-class tokens directly on the consensus node — without
deploying a smart contract. A single token is identified by an `Address` (token id) and is one of
two kinds:

- **`FUNGIBLE_COMMON`** — a divisible currency-like asset with a `decimals` precision and a
  shared supply pool. Balances are tracked per account as an `int64` count of the smallest
  indivisible unit.
- **`NON_FUNGIBLE_UNIQUE`** (NFT) — an indivisible collection where each issued unit is a
  distinct serial number with its own metadata. Balances are tracked per (token, serial).

A token's supply policy is independent of its kind:

- **`INFINITE`** — no protocol-enforced ceiling on `totalSupply`. The supply key may keep minting
  forever (modulo `int64` overflow).
- **`FINITE`** — the protocol enforces `totalSupply ≤ maxSupply` at every mint. A mint that would
  exceed `maxSupply` fails atomically.

The **core lifecycle and supply** transactions are `TokenCreateTransaction`,
`TokenUpdateTransaction`, `TokenDeleteTransaction`, `TokenAssociateTransaction`,
`TokenDissociateTransaction`, `TokenMintTransaction` and `TokenBurnTransaction`. The permissioned
management transactions (wipe, freeze, KYC, pause, NFT metadata updates) and the HIP-904 airdrop
transactions are provided alongside them.

### Token keys

A token carries up to eight optional keys, each granting authority over a specific operation. None
of these can be *added* via `TokenUpdate` if they were not set at creation time — leaving a key
unset at create permanently disables that capability (HAPI: `KEY_NOT_PROVIDED`). The keys can be
*rotated* by an update if they were set originally, and (under HIP-540) cleared to an immutable
state.

| Key            | Grants the authority to                                                                          |
|----------------|--------------------------------------------------------------------------------------------------|
| `adminAuthority`     | update token metadata (`TokenUpdate`) and delete the token (`TokenDelete`). Unset → immutable.   |
| `supplyAuthority`    | mint and burn units (`TokenMint`, `TokenBurn`). Unset → fixed supply after creation.             |
| `kycAuthority`       | grant / revoke KYC (`TokenGrantKyc`, `TokenRevokeKyc`). Unset → KYC concept disabled.            |
| `freezeAuthority`    | freeze / unfreeze accounts for this token (`TokenFreeze`, `TokenUnfreeze`). Unset → no freeze.   |
| `wipeAuthority`      | wipe units from a non-treasury account (`TokenWipe`). Unset → wipe disabled.                     |
| `pauseAuthority`     | pause / unpause every operation on the token (`TokenPause`, `TokenUnpause`). Unset → unpausable. |
| `feeScheduleAuthority` | update the token's custom fee schedule (`TokenFeeScheduleUpdate`). Unset → fees immutable.     |
| `metadataAuthority`  | update NFT-level metadata after mint (`TokenUpdateNfts`, HIP-657). Unset → metadata immutable.   |

The `metadata` field on the token itself (and on individual NFT serials) is opaque bytes — typical
uses are an IPFS CID, an HTTPS URL, or an inlined JSON manifest. The token's `decimals`,
`initialSupply`, and `maxSupply` fields are expressed in the smallest indivisible unit (e.g. for a
6-decimals USDC clone, `1_000_000` means "one display unit").

### Treasury account

Every token has a `treasuryAccountId`: the account that receives `initialSupply` at create time and
that all subsequent `TokenMint` units are credited to. Burns also draw from the treasury. The
treasury account must have been associated with the token (implicitly true at create — the protocol
auto-associates the treasury). Treasury rotation via `TokenUpdate` requires both the *current* and
the *new* treasury to sign.

### Token associations

Before a non-treasury account can hold a positive balance of a token it must explicitly *associate*
with it (`TokenAssociate`). Accounts created with `maxAutomaticTokenAssociations > 0` may instead
auto-associate on first inbound transfer up to that limit (HIP-23 / HIP-904); `TokenAssociate`
remains the explicit path. `TokenDissociate` removes an association — the protocol requires the
account to hold zero balance of the token (and zero NFTs of that collection) at dissociate time,
otherwise the transaction fails.

A token's lifecycle is:

```
                                          ┌── TokenMint    ──┐
  create ── (associate | dissociate)* ── ─┤                  ├── update* ── delete
                                          └── TokenBurn    ──┘
```

### Signing requirements (token lifecycle)

| Transaction       | Signers required                                                                                                                                                                                                                                                                                                |
|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TokenCreate`     | the *payer*; **and** the `treasuryAccountId` (auto-association of the treasury implicitly binds the account); **and** the `adminAuthority` if it is set (anti-spoofing: cannot bind another party's key as admin without their consent); **and** the `autoRenewAccount` if it is set and differs from the payer / treasury. Other keys (`supplyAuthority`, `kycAuthority`, ...) do **not** need to sign creation — they gate only future permissioned operations. |
| `TokenUpdate`     | the *payer*; **and** the token's *current* `adminAuthority`; **and**, if a key field is being rotated to a new value, the *new* key (anti-spoofing); **and**, if `treasuryAccountId` is being changed, both the *current* and the *new* treasury accounts; **and**, if `autoRenewAccount` is being changed, the new account. |
| `TokenDelete`     | the *payer*; **and** the token's current `adminAuthority` (an immutable token — `adminAuthority` unset — cannot be deleted; the request fails with `TOKEN_IS_IMMUTABLE`).                                                                                                                                                   |
| `TokenAssociate`  | the *payer*; **and** the `accountId` being associated (the account opting in to hold the token). The token's keys are **not** consulted — anyone may associate to any token.                                                                                                                                    |
| `TokenDissociate` | the *payer*; **and** the `accountId` being dissociated.                                                                                                                                                                                                                                                         |
| `TokenMint`       | the *payer*; **and** the token's `supplyAuthority` (mint is impossible if `supplyAuthority` is unset — `TOKEN_HAS_NO_SUPPLY_KEY`).                                                                                                                                                                                          |
| `TokenBurn`       | the *payer*; **and** the token's `supplyAuthority`. Burns always draw from the treasury account; the treasury itself does not need to co-sign.                                                                                                                                                                        |

When the operator holds every required key (e.g. operator = treasury = admin) `signWithOperatorAndSubmit(client)`
is sufficient. Multi-key flows go through the `signWithOperator(client).sign(...)` pattern.

The general rule (as in topics): a key field on a create transaction must sign **if** binding it
would grant that key authority over a real asset (`adminAuthority`, `treasuryAccountId`,
`autoRenewAccount`). Keys that only gate *future* permissioned operations and confer no liability
right now (`supplyAuthority`, `kycAuthority`, `freezeAuthority`, `wipeAuthority`, `pauseAuthority`, `feeScheduleAuthority`,
`metadataAuthority`) do not need to sign creation.

### Mutability and "clearing" keys / auto-renew

`TokenUpdate` distinguishes "leave unchanged" from "clear":

- An optional field that is left unset means *leave unchanged*.
- Clearing an optional `Address`-typed field (`autoRenewAccount`; `treasuryAccountId` is
  non-clearable) uses `ZERO_ADDRESS` as the sentinel value.
- Each token key is an `Authority`, so it can be a single key, a contract, or an m-of-n threshold.
  Clearing a key to an irrevocably-empty state (HIP-540) is not supported yet.

## Design Notes

- The remaining HTS transactions were originally tracked in
  [`missing-features.md`](../../docs/missing-features.md) section 1.2; the management transactions are
  specified in [`transactions-tokens-management.md`](transactions-tokens-management.md) and the
  HIP-904 airdrops in [`transactions-tokens-airdrops.md`](transactions-tokens-airdrops.md), all in
  the same `consensusnode.transactions.tokens` namespace. `TokenFeeScheduleUpdate` is still missing.
- `TokenMint` / `TokenBurn` mixing fungible and NFT payloads is rejected by the network; the
  `@@oneOf` at the type level makes this statically checkable.
- The multi-signature pattern is shown in [`transactions-accounts.md`](transactions-accounts.md).
- `ZERO_ADDRESS` is defined in [`ledger.md`](../base/ledger.md).
- Each token key is the `Authority` authorization type (see [`authority.md`](../base/authority.md) /
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md)). HIP-540 added the ability to
  *clear* any lower-privilege key to an irrevocably-empty state; clearing is a future write-side
  `KeyUpdate` operation (per ADR-0004), not expressible as an `Authority` value — see
  *Questions & Comments*.

- **Mint, burn and wipe are sealed over their two variants.** A token is either `FUNGIBLE_COMMON` or
  `NON_FUNGIBLE_UNIQUE`, and the payload differs accordingly. Rather than two nullable fields with
  "exactly one of" attached, each transaction is a `@@sealed` abstraction carrying the shared fields,
  with one final type per token kind — the pattern of
  [ADR-0011](../../docs/adr/0011-model-a-choice-as-a-sealed-type.md). The NFT mint additionally has
  its own receipt, because only it carries the assigned serial numbers.

## API Schema

```
namespace consensusnode.transactions.tokens
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {TokenType, TokenSupplyType} from token
requires {Receipt, Transaction} from consensusnode.transactions

// Creates a new token. The token is identified by the `tokenId` returned in the receipt. All
// eight key fields are optional and follow the rule that a key not set at create time can never
// be added later (the network rejects it with `KEY_NOT_PROVIDED`); leaving e.g. `supplyAuthority`
// unset permanently fixes the token's supply at `initialSupply`.
@@finalType
TokenCreateTransaction extends Transaction<TokenCreateReceipt, TokenCreateTransaction> {
    @@immutable @@maxBytes(100) name: string                       // human-readable token name
    @@immutable @@maxBytes(100) symbol: string                     // short ticker / symbol
    @@immutable tokenType: TokenType                                // FUNGIBLE_COMMON | NON_FUNGIBLE_UNIQUE; cannot be changed later
    @@immutable @@default(0) decimals: int32                        // FUNGIBLE_COMMON only: precision of the smallest indivisible unit (e.g. 6 for USDC); MUST be 0 for NON_FUNGIBLE_UNIQUE
    @@immutable @@default(0) initialSupply: int64                   // FUNGIBLE_COMMON only: amount minted to the treasury at create (in smallest unit); MUST be 0 for NON_FUNGIBLE_UNIQUE
    @@immutable treasuryAccountId: AccountId                        // account receiving `initialSupply` and all subsequent mints; MUST sign the create transaction
    @@immutable @@default(INFINITE) supplyType: TokenSupplyType     // INFINITE | FINITE; cannot be changed later
    @@immutable @@nullable @@min(1) maxSupply: int64                // FINITE only: protocol-enforced ceiling on totalSupply; unset (and only valid) when supplyType is INFINITE
    @@immutable @@default(false) freezeDefault: bool                // when true, newly associated accounts start in the FROZEN state (require an explicit TokenUnfreeze before they can move balances); requires freezeAuthority to be set
    @@immutable @@nullable @@maxBytes(100) tokenMemo: string       // short, human-readable label
    @@immutable @@default([]) @@maxSize(100) metadata: bytes        // opaque token-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
    @@immutable @@nullable expirationTime: zonedDateTime            // when the token expires; SDK default applies when unset
    @@immutable @@nullable autoRenewPeriod: seconds                 // renewal window applied when expiration approaches; protocol default applies when unset
    @@immutable @@nullable autoRenewAccount: AccountId              // account paying for auto-renewal; MUST sign the create transaction if set and not equal to the treasury / payer

    // Optional key fields — see "Token keys" in the description above for what each one grants.
    // A key NOT set at create time CANNOT be added by TokenUpdate.

    @@immutable @@nullable adminAuthority: Authority                      // controls update / delete; unset → immutable token; MUST sign the create transaction when set (anti-spoofing)
    @@immutable @@nullable supplyAuthority: Authority                     // controls mint / burn; unset → fixed supply after creation
    @@immutable @@nullable kycAuthority: Authority                        // controls KYC grant / revoke; unset → KYC disabled
    @@immutable @@nullable freezeAuthority: Authority                     // controls freeze / unfreeze; unset → no freeze
    @@immutable @@nullable wipeAuthority: Authority                       // controls wipe of non-treasury balances; unset → wipe disabled
    @@immutable @@nullable pauseAuthority: Authority                      // controls pause / unpause; unset → unpausable
    @@immutable @@nullable feeScheduleAuthority: Authority                // controls custom-fee schedule updates; unset → fees immutable
    @@immutable @@nullable metadataAuthority: Authority                   // controls per-serial NFT metadata updates (HIP-657); unset → metadata immutable
}

@@finalType
TokenCreateReceipt extends Receipt {
    @@immutable tokenId: Address                                    // id of the newly created token
}

// Updates one or more of a token's mutable fields. Every nullable field is "leave unchanged"
// when null. `tokenType`, `supplyType`, `decimals`, and `initialSupply` are NOT updatable — they
// are fixed at create.
//
// Rotating a key field to a new key requires the new key to also sign (anti-spoofing). A key
// field that was unset at create cannot be added here (the network rejects it with
// `KEY_NOT_PROVIDED`). Clearing a key field to permanently disable the corresponding capability
// (HIP-540) is not supported yet.
@@finalType
TokenUpdateTransaction extends Transaction<TokenUpdateReceipt, TokenUpdateTransaction> {
    @@immutable tokenId: Address                                    // the token being updated
    @@immutable @@nullable @@maxBytes(100) name: string
    @@immutable @@nullable @@maxBytes(100) symbol: string
    @@immutable @@nullable treasuryAccountId: AccountId             // when set, replaces the treasury; both the OLD and the NEW treasury MUST sign
    @@immutable @@nullable @@maxBytes(100) tokenMemo: string
    @@immutable @@nullable @@maxSize(100) metadata: bytes           // when set, replaces the token-level metadata
    @@immutable @@nullable expirationTime: zonedDateTime
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable autoRenewAccount: AccountId              // when set, replaces the auto-renew account; the new account MUST also sign

    // Key rotations — each requires the new key to also sign (anti-spoofing). A key that was
    // unset at create cannot be set here.
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable supplyAuthority: Authority
    @@immutable @@nullable kycAuthority: Authority
    @@immutable @@nullable freezeAuthority: Authority
    @@immutable @@nullable wipeAuthority: Authority
    @@immutable @@nullable pauseAuthority: Authority
    @@immutable @@nullable feeScheduleAuthority: Authority
    @@immutable @@nullable metadataAuthority: Authority
}

@@finalType
TokenUpdateReceipt extends Receipt {
}

// Deletes a token. Only possible if the token has an adminAuthority (an immutable token cannot be
// deleted). After deletion, the token id is not reusable; transactions referencing it return
// `TOKEN_WAS_DELETED`. Existing balances are NOT swept by this transaction — they remain
// recorded but unusable for transfer.
@@finalType
TokenDeleteTransaction extends Transaction<TokenDeleteReceipt, TokenDeleteTransaction> {
    @@immutable tokenId: Address
}

@@finalType
TokenDeleteReceipt extends Receipt {
}

// Associates an account with one or more tokens so that the account may receive non-zero
// balances of them. The account being associated MUST sign the transaction (it is opting in;
// the token's keys are not consulted). Accounts configured with
// `maxAutomaticTokenAssociations > 0` may auto-associate on first inbound transfer instead;
// `TokenAssociate` remains the explicit path for accounts that do not, or that exceeded their
// auto-association budget.
@@finalType
TokenAssociateTransaction extends Transaction<TokenAssociateReceipt, TokenAssociateTransaction> {
    @@immutable accountId: AccountId                                // the account opting in to hold the listed tokens (MUST sign)
    @@immutable @@maxSize(100) tokens: set<Address>               // tokens to associate; duplicates are protocol-rejected (TOKEN_ID_REPEATED_IN_TOKEN_LIST) and order is irrelevant — hence a set, not a list
}

@@finalType
TokenAssociateReceipt extends Receipt {
}

// Removes the association between an account and one or more tokens. The protocol requires the
// account to hold zero balance of every listed token (and zero NFTs of any listed NFT
// collection) at consensus time, otherwise the transaction fails with
// `TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES`. Use `TransferTransaction` first to drain the
// account if necessary.
@@finalType
TokenDissociateTransaction extends Transaction<TokenDissociateReceipt, TokenDissociateTransaction> {
    @@immutable accountId: AccountId                                // the account opting out (MUST sign)
    @@immutable @@maxSize(100) tokens: set<Address>               // tokens to dissociate; same set semantics as TokenAssociateTransaction.tokens
}

@@finalType
TokenDissociateReceipt extends Receipt {
}

// Mints new units of a token, crediting them to the treasury account. Requires the token's
// supplyAuthority to sign. Which of the two mint transactions applies follows from the token's
// type: TokenMintFungibleTransaction for FUNGIBLE_COMMON, TokenMintNftTransaction for
// NON_FUNGIBLE_UNIQUE.
@@sealed(TokenMintFungibleTransaction, TokenMintNftTransaction)
abstraction TokenMintTransaction<$$Receipt extends Receipt, $$Self extends TokenMintTransaction<$$Receipt, $$Self>> extends Transaction<$$Receipt, $$Self> {
    @@immutable tokenId: Address                                    // the token to mint
}

// Mints units of a FUNGIBLE_COMMON token. The treasury account receives them.
@@finalType
TokenMintFungibleTransaction extends TokenMintTransaction<TokenMintReceipt, TokenMintFungibleTransaction> {
    @@immutable @@min(1) amount: int64                              // amount to mint, in the smallest indivisible unit
}

// Mints NON_FUNGIBLE_UNIQUE tokens, one per metadata entry. The consensus node assigns sequential
// serial numbers and returns them in the receipt.
@@finalType
TokenMintNftTransaction extends TokenMintTransaction<TokenMintNftReceipt, TokenMintNftTransaction> {
    @@immutable @@minSize(1) @@maxSize(10) metadata: list<bytes>    // one metadata blob per new serial; the protocol caps the list at 10 per transaction
}

TokenMintReceipt extends Receipt {
    @@immutable totalSupply: int64                                  // the token's new totalSupply after this mint
}

// The receipt of an NFT mint: it also carries the serial numbers the network assigned.
@@finalType
TokenMintNftReceipt extends TokenMintReceipt {
    @@immutable @@default([]) serials: list<int64>                  // the serial numbers of the newly minted NFTs
}

// Burns existing units of a token from the treasury account. Requires the token's supplyAuthority
// to sign; which of the two burn transactions applies follows from the token's type.
//
// Burning NFT serials held by a non-treasury account is not possible via this transaction —
// use the wipe transactions (gated by `wipeAuthority`) instead.
@@sealed(TokenBurnFungibleTransaction, TokenBurnNftTransaction)
abstraction TokenBurnTransaction<$$Self extends TokenBurnTransaction<$$Self>> extends Transaction<TokenBurnReceipt, $$Self> {
    @@immutable tokenId: Address                                    // the token to burn from
}

// Burns units of a FUNGIBLE_COMMON token held by the treasury.
@@finalType
TokenBurnFungibleTransaction extends TokenBurnTransaction<TokenBurnFungibleTransaction> {
    @@immutable @@min(1) amount: int64                              // amount to burn, in the smallest indivisible unit
}

// Burns NON_FUNGIBLE_UNIQUE serials held by the treasury.
@@finalType
TokenBurnNftTransaction extends TokenBurnTransaction<TokenBurnNftTransaction> {
    @@immutable @@minSize(1) @@maxSize(10) serials: list<int64>     // serials to burn; the protocol caps the list at 10 per transaction
}

@@finalType
TokenBurnReceipt extends Receipt {
    @@immutable totalSupply: int64                                  // the token's new totalSupply after this burn
}
```

## Default Instances for Testing

```
// The eight token keys of the token tests of the Java SDK (TokenCreateTransactionTest, TokenUpdateTransactionTest), in
// the order admin, KYC, freeze, wipe, supply, fee schedule, pause and metadata: the public keys of the Ed25519 PKCS#8
// keys ending in 11 to 18.
values JAVA_SDK_TOKEN_AUTHORITIES: list<Authority> = [
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e11")).createPublicKey()},
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e12")).createPublicKey()},
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e13")).createPublicKey()},
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e14")).createPublicKey()},
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e15")).createPublicKey()},
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e16")).createPublicKey()},
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e17")).createPublicKey()},
        authority.PublicKeyAuthority{publicKey: keys.createPrivateKey(container: keys.KeyFormat.PKCS8_WITH_DER,
                value: hex(text: "302e020100300506032b657004220420"
                        + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e18")).createPublicKey()}]

// The expiration time of the token tests of the Java SDK (TokenCreateTransactionTest.spawnTestTransactionFungible and
// spawnTestTransactionNft: validStart = Instant.ofEpochSecond(1554158542)) and the Rust SDK
// (token_create_transaction.rs and token_update_transaction.rs: EXPIRATION_TIME = VALID_START, 1554158542 seconds),
// whole seconds.
values SDK_TEST_EXPIRATION_TIME: zonedDateTime = toZonedDateTime(text: "2019-04-01T22:42:22Z")

// One character beyond the @@maxBytes(100) of the token name, symbol and memo.
values TEXT_OF_101_CHARACTERS: string = repeat(value: "a", count: 101)

// One entry beyond the @@maxSize(10) of the NFT mint metadata (11 entries).
values ELEVEN_METADATA_ENTRIES: list<bytes> = [[1], [2], [3], [4], [5], [6], [7], [8], [9], [10], [11]]

// One serial beyond the @@maxSize(10) of the NFT burn serials (the serials 1 to 11).
values ELEVEN_SERIALS: list<int64> = range(start: 1, end: 12)
```

## Examples

### Create a fungible token (operator is admin, treasury, and supply key)

The simplest case: a 6-decimals stablecoin-style token with a fixed-but-mintable supply. The
operator pays, is the treasury, and holds the admin + supply keys, so a single
`signWithOperatorAndSubmit` covers every required signature.

```
HieroClient client = ...;

Response<TokenCreateReceipt> response = new TokenCreateTransaction()
    .name("Example USD")
    .symbol("EUSD")
    .tokenType(TokenType.FUNGIBLE_COMMON)
    .decimals(6)
    .initialSupply(1_000_000_000_000L)         // 1,000,000.000000 EUSD to the treasury
    .treasuryAccountId(client.operatorAccountId)
    .supplyType(TokenSupplyType.INFINITE)
    .adminAuthority(Authority.of(client.operatorPublicKey))
    .supplyAuthority(Authority.of(client.operatorPublicKey))
    .signWithOperatorAndSubmit(client);

Address tokenId = response.queryReceipt().tokenId;
```

### Create a finite-supply NFT collection

`tokenType = NON_FUNGIBLE_UNIQUE` forces `decimals = 0` and `initialSupply = 0`. Serials are
allocated by `TokenMint` calls, capped at `maxSupply` once `supplyType = FINITE`.

```
new TokenCreateTransaction()
    .name("Example Art")
    .symbol("EART")
    .tokenType(TokenType.NON_FUNGIBLE_UNIQUE)
    .treasuryAccountId(client.operatorAccountId)
    .supplyType(TokenSupplyType.FINITE)
    .maxSupply(10_000L)
    .adminAuthority(Authority.of(client.operatorPublicKey))
    .supplyAuthority(Authority.of(client.operatorPublicKey))
    .metadataAuthority(Authority.of(client.operatorPublicKey))     // allow per-serial metadata updates (HIP-657)
    .signWithOperatorAndSubmit(client);
```

### Treasury is a separate account (multi-signature)

The operator pays the fee, but a dedicated treasury account custodies the supply. The treasury
must co-sign — its signature is what binds the treasury to the new token.

```
PackedTransaction<...> packed = new TokenCreateTransaction()
    .name("Acme Loyalty")
    .symbol("ACME")
    .tokenType(TokenType.FUNGIBLE_COMMON)
    .decimals(0)
    .initialSupply(0)
    .treasuryAccountId(treasury.accountId)
    .supplyAuthority(Authority.of(supplyOwnerPublicKey))
    .adminAuthority(Authority.of(client.operatorPublicKey))
    .signWithOperator(client)                  // payer + adminAuthority (operator)
    .sign(treasury);                           // treasury authorises being bound to the token

packed.submit(client);
```

### Associate before receiving the token

Bob must associate with `tokenId` before any `TransferTransaction` can credit him. He pays his
own fee here, so a single `sign(...)` is enough.

```
new TokenAssociateTransaction()
    .accountId(bob.accountId)
    .tokens([tokenId])
    .sign(bob, nodes)
    .submit(client);
```

### Mint additional fungible supply

The treasury automatically receives the new units. The `supplyAuthority` (here the operator) must
sign.

```
Response<TokenMintReceipt> response = new TokenMintFungibleTransaction()
    .tokenId(tokenId)
    .amount(500_000_000L)                      // 500.000000 additional EUSD
    .signWithOperatorAndSubmit(client);

int64 newTotalSupply = response.queryReceipt().totalSupply;
```

### Mint NFT serials in a single batch

Up to 10 metadata blobs may be supplied per transaction; the protocol assigns sequential serial
numbers and returns them in the receipt.

```
Response<TokenMintNftReceipt> response = new TokenMintNftTransaction()
    .tokenId(nftCollectionId)
    .metadata([
        "ipfs://QmHash1".bytes,
        "ipfs://QmHash2".bytes,
        "ipfs://QmHash3".bytes,
    ])
    .signWithOperatorAndSubmit(client);

list<int64> newSerials = response.queryReceipt().serials;   // e.g. [1, 2, 3]
                                                            // only TokenMintNftReceipt has them
```

### Burn fungible / NFT supply

Burning draws from the treasury account; the treasury itself does not need to co-sign — the
supply key authorises the burn.

```
// Fungible:
new TokenBurnFungibleTransaction()
    .tokenId(tokenId)
    .amount(10_000_000L)
    .signWithOperatorAndSubmit(client);

// NFT (serials must be currently held by the treasury):
new TokenBurnNftTransaction()
    .tokenId(nftCollectionId)
    .serials([1L, 2L])
    .signWithOperatorAndSubmit(client);
```

### Rotate the supply key (multi-signature)

Both the *current* and the *new* supply key must sign, in addition to the payer and admin key.

```
PackedTransaction<...> packed = new TokenUpdateTransaction()
    .tokenId(tokenId)
    .supplyAuthority(Authority.of(newSupplyPublicKey))
    .signWithOperator(client)                  // payer + adminAuthority (operator) + current supplyAuthority (operator)
    .sign(newSupplySigner);                    // new supplyAuthority

packed.submit(client);
```

### Dissociate and delete

Bob must drain his balance first; then he can dissociate. Once the admin decides the token has
reached the end of its life, the admin key deletes it.

```
// 1. Drain Bob (omitted: TransferTransaction back to treasury).
// 2. Dissociate:
new TokenDissociateTransaction()
    .accountId(bob.accountId)
    .tokens([tokenId])
    .sign(bob, nodes)
    .submit(client);

// 3. Delete the token (admin key signs):
new TokenDeleteTransaction()
    .tokenId(tokenId)
    .signWithOperatorAndSubmit(client);
```

## Testing

Offline tests of the token lifecycle transactions, written as given / when / then: the defaults and values a
transaction keeps, the values it rejects, and that a packed transaction survives its serialized form. Nothing is submitted; what the network
does with a transaction (signature checks, supply limits, association rules) is tested by the Hiero TCK. The current
SDKs test the same behaviour with their getter / setter and `fromBytes(toBytes())` tests; the comment above each test
names where.

```tests
// Source: the @@default values of TokenCreateTransaction; the Python SDK (TokenParams: decimals 0, initial supply 0,
// supply type INFINITE, freeze default false) and HAPI TokenCreateTransactionBody (no key, no maximum supply, no memo
// and no auto-renew account unless set).
test "a token create transaction without optional values has the defaults of the spec" {
    // when
    let transaction = TokenCreateTransaction{name: "test name", symbol: "test symbol",
            tokenType: TokenType.FUNGIBLE_COMMON, treasuryAccountId: AccountId.fromString(value: "7.7.7")}

    // then
    assertEquals(expected: 0, actual: transaction.decimals)
    assertEquals(expected: 0, actual: transaction.initialSupply)
    assertEquals(expected: TokenSupplyType.INFINITE, actual: transaction.supplyType)
    assertFalse(condition: transaction.freezeDefault)
    assertEquals(expected: 0, actual: length(value: transaction.metadata))
    assertNull(actual: transaction.maxSupply)
    assertNull(actual: transaction.tokenMemo)
    assertNull(actual: transaction.expirationTime)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.autoRenewAccount)
    assertNull(actual: transaction.adminAuthority)
    assertNull(actual: transaction.kycAuthority)
    assertNull(actual: transaction.freezeAuthority)
    assertNull(actual: transaction.wipeAuthority)
    assertNull(actual: transaction.supplyAuthority)
    assertNull(actual: transaction.feeScheduleAuthority)
    assertNull(actual: transaction.pauseAuthority)
    assertNull(actual: transaction.metadataAuthority)
}

// Source: the Java SDK (TokenCreateTransactionTest, constructTokenCreateTransactionFungibleFromTransactionBodyProtobuf:
// the same values, an auto-renew period of Duration.ofHours(10) = 36000 seconds; the expiration time of
// spawnTestTransactionFungible, Instant.ofEpochSecond(1554158542) = 2019-04-01T22:42:22Z, because the protobuf test uses
// Instant.now()); the Rust SDK (token_create_transaction.rs make_transaction: an expiration time and an auto-renew
// period together); the getter / setter tests of the Python, Go and Swift SDKs.
test "a token create transaction for a fungible token keeps its values and each key in its own field" {
    // given
    let authorities = JAVA_SDK_TOKEN_AUTHORITIES

    // when
    let transaction = TokenCreateTransaction{name: "test name", symbol: "test symbol",
            tokenType: TokenType.FUNGIBLE_COMMON, decimals: 3, initialSupply: 30,
            treasuryAccountId: AccountId.fromString(value: "7.7.7"), freezeDefault: true, tokenMemo: "test memo",
            metadata: [1, 2, 3, 4, 5], expirationTime: SDK_TEST_EXPIRATION_TIME, autoRenewPeriod: toSeconds(value: 36000),
            autoRenewAccount: AccountId.fromString(value: "8.8.8"),
            adminAuthority: authorities[0], kycAuthority: authorities[1], freezeAuthority: authorities[2],
            wipeAuthority: authorities[3], supplyAuthority: authorities[4], feeScheduleAuthority: authorities[5],
            pauseAuthority: authorities[6], metadataAuthority: authorities[7]}

    // then
    assertEquals(expected: "test name", actual: transaction.name)
    assertEquals(expected: "test symbol", actual: transaction.symbol)
    assertEquals(expected: TokenType.FUNGIBLE_COMMON, actual: transaction.tokenType)
    assertEquals(expected: 3, actual: transaction.decimals)
    assertEquals(expected: 30, actual: transaction.initialSupply)
    assertEquals(expected: AccountId.fromString(value: "7.7.7"), actual: transaction.treasuryAccountId)
    assertTrue(condition: transaction.freezeDefault)
    assertEquals(expected: "test memo", actual: transaction.tokenMemo)
    assertEquals(expected: hex(text: "0102030405"), actual: transaction.metadata)
    assertEquals(expected: SDK_TEST_EXPIRATION_TIME, actual: transaction.expirationTime)
    assertEquals(expected: toSeconds(value: 36000), actual: transaction.autoRenewPeriod)
    assertEquals(expected: AccountId.fromString(value: "8.8.8"), actual: transaction.autoRenewAccount)
    assertEquals(expected: authorities[0], actual: transaction.adminAuthority)
    assertEquals(expected: authorities[1], actual: transaction.kycAuthority)
    assertEquals(expected: authorities[2], actual: transaction.freezeAuthority)
    assertEquals(expected: authorities[3], actual: transaction.wipeAuthority)
    assertEquals(expected: authorities[4], actual: transaction.supplyAuthority)
    assertEquals(expected: authorities[5], actual: transaction.feeScheduleAuthority)
    assertEquals(expected: authorities[6], actual: transaction.pauseAuthority)
    assertEquals(expected: authorities[7], actual: transaction.metadataAuthority)
}

// Source: the Java SDK (TokenCreateTransactionTest, constructTokenCreateTransactionNftFromTransactionBodyProtobuf:
// NON_FUNGIBLE_UNIQUE, FINITE, maximum supply 500); the NFT variants of the Python and Swift SDKs.
test "a token create transaction for a finite NFT collection keeps its supply type and maximum supply" {
    // when
    let transaction = TokenCreateTransaction{name: "test name", symbol: "test symbol",
            tokenType: TokenType.NON_FUNGIBLE_UNIQUE, treasuryAccountId: AccountId.fromString(value: "7.7.7"),
            supplyType: TokenSupplyType.FINITE, maxSupply: 500}

    // then
    assertEquals(expected: TokenType.NON_FUNGIBLE_UNIQUE, actual: transaction.tokenType)
    assertEquals(expected: TokenSupplyType.FINITE, actual: transaction.supplyType)
    assertEquals(expected: 500, actual: transaction.maxSupply)
    assertEquals(expected: 0, actual: transaction.decimals)
    assertEquals(expected: 0, actual: transaction.initialSupply)
}

// Source: TokenUpdateTransaction ("every nullable field is leave unchanged when null"); HAPI TokenUpdateTransactionBody
// (an unset field is not changed); the Python SDK (test_build_transaction_body: only the fields that are set).
test "a token update transaction with only a token id changes nothing" {
    // when
    let transaction = TokenUpdateTransaction{tokenId: Address.fromString(address: "4.2.0")}

    // then
    assertEquals(expected: Address.fromString(address: "4.2.0"), actual: transaction.tokenId)
    assertNull(actual: transaction.name)
    assertNull(actual: transaction.symbol)
    assertNull(actual: transaction.treasuryAccountId)
    assertNull(actual: transaction.tokenMemo)
    assertNull(actual: transaction.metadata)
    assertNull(actual: transaction.expirationTime)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.autoRenewAccount)
    assertNull(actual: transaction.adminAuthority)
    assertNull(actual: transaction.kycAuthority)
    assertNull(actual: transaction.freezeAuthority)
    assertNull(actual: transaction.wipeAuthority)
    assertNull(actual: transaction.supplyAuthority)
    assertNull(actual: transaction.feeScheduleAuthority)
    assertNull(actual: transaction.pauseAuthority)
    assertNull(actual: transaction.metadataAuthority)
}

// Source: the Java SDK (TokenUpdateTransactionTest, constructTokenUpdateTransactionFromTransactionBodyProtobuf: the
// same keys and values, an auto-renew period of Duration.ofHours(10) = 36000 seconds); the Rust SDK
// (token_update_transaction.rs: TEST_EXPIRATION_TIME = VALID_START = 2019-04-01T22:42:22Z, because the Java test uses
// Instant.now()); the getter / setter tests of the Python SDK (test_set_methods, test_single_key_fields).
test "a token update transaction keeps each rotated key in its own field" {
    // given
    let authorities = JAVA_SDK_TOKEN_AUTHORITIES

    // when
    let transaction = TokenUpdateTransaction{tokenId: Address.fromString(address: "4.2.0"), name: "test name",
            symbol: "test symbol", treasuryAccountId: AccountId.fromString(value: "7.7.7"), tokenMemo: "test memo",
            metadata: [1, 2, 3, 4, 5], expirationTime: SDK_TEST_EXPIRATION_TIME, autoRenewPeriod: toSeconds(value: 36000),
            autoRenewAccount: AccountId.fromString(value: "8.8.8"),
            adminAuthority: authorities[0], kycAuthority: authorities[1], freezeAuthority: authorities[2],
            wipeAuthority: authorities[3], supplyAuthority: authorities[4], feeScheduleAuthority: authorities[5],
            pauseAuthority: authorities[6], metadataAuthority: authorities[7]}

    // then
    assertEquals(expected: "test name", actual: transaction.name)
    assertEquals(expected: "test symbol", actual: transaction.symbol)
    assertEquals(expected: AccountId.fromString(value: "7.7.7"), actual: transaction.treasuryAccountId)
    assertEquals(expected: "test memo", actual: transaction.tokenMemo)
    assertEquals(expected: hex(text: "0102030405"), actual: transaction.metadata)
    assertEquals(expected: SDK_TEST_EXPIRATION_TIME, actual: transaction.expirationTime)
    assertEquals(expected: toSeconds(value: 36000), actual: transaction.autoRenewPeriod)
    assertEquals(expected: AccountId.fromString(value: "8.8.8"), actual: transaction.autoRenewAccount)
    assertEquals(expected: authorities[0], actual: transaction.adminAuthority)
    assertEquals(expected: authorities[1], actual: transaction.kycAuthority)
    assertEquals(expected: authorities[2], actual: transaction.freezeAuthority)
    assertEquals(expected: authorities[3], actual: transaction.wipeAuthority)
    assertEquals(expected: authorities[4], actual: transaction.supplyAuthority)
    assertEquals(expected: authorities[5], actual: transaction.feeScheduleAuthority)
    assertEquals(expected: authorities[6], actual: transaction.pauseAuthority)
    assertEquals(expected: authorities[7], actual: transaction.metadataAuthority)
}

// Source: TokenAssociateTransaction.tokens ("order is irrelevant — hence a set"); the Java SDK
// (TokenAssociateTransactionTest and TokenDissociateTransactionTest: the same account and token ids).
test "token associate and dissociate transactions keep the account and the set of tokens in any order" {
    // given
    let first = Address.fromString(address: "4.5.6")
    let second = Address.fromString(address: "7.8.9")
    let third = Address.fromString(address: "10.11.12")
    let reordered: set<Address> = [third, first, second]

    // when
    let associate = TokenAssociateTransaction{accountId: AccountId.fromString(value: "1.2.3"),
            tokens: [first, second, third]}
    let dissociate = TokenDissociateTransaction{accountId: AccountId.fromString(value: "1.2.3"),
            tokens: [first, second, third]}

    // then
    assertEquals(expected: AccountId.fromString(value: "1.2.3"), actual: associate.accountId)
    assertEquals(expected: reordered, actual: associate.tokens)
    assertEquals(expected: AccountId.fromString(value: "1.2.3"), actual: dissociate.accountId)
    assertEquals(expected: reordered, actual: dissociate.tokens)
}

// Source: the JavaScript SDK (TokenMintTransaction and TokenBurnTransaction, "should handle large bigint amounts":
// 9000000000000000000); the Java SDK (TokenMintTransactionTest, TokenBurnTransactionTest: token 4.2.0).
test "fungible mint and burn transactions keep amounts beyond the 53 bits of a JavaScript number" {
    // given
    let token = Address.fromString(address: "4.2.0")

    // when
    let mint = TokenMintFungibleTransaction{tokenId: token, amount: 9000000000000000000}
    let burn = TokenBurnFungibleTransaction{tokenId: token, amount: 9000000000000000000}

    // then
    assertEquals(expected: token, actual: mint.tokenId)
    assertEquals(expected: 9000000000000000000, actual: mint.amount)
    assertEquals(expected: token, actual: burn.tokenId)
    assertEquals(expected: 9000000000000000000, actual: burn.amount)
}

// Source: TokenMintNftTransaction ("one metadata blob per new serial"); the Java SDK (TokenMintTransactionTest,
// getSetMetadata: [1, 2, 3, 4, 5]) and the Python SDK (test_build_transaction_body_nft: several entries in order).
test "an NFT mint transaction keeps one metadata entry per new serial in order" {
    // given
    let metadata = [hex(text: "0102030405"), utf8(text: "ipfs://QmHash1"), utf8(text: "ipfs://QmHash2")]

    // when
    let mint = TokenMintNftTransaction{tokenId: Address.fromString(address: "4.2.0"), metadata: metadata}

    // then
    assertEquals(expected: 3, actual: length(value: mint.metadata))
    assertEquals(expected: metadata, actual: mint.metadata)
}

// Source: the Java SDK (TokenBurnTransactionTest: serial 420) and the Python SDK (token_burn_transaction_test).
test "an NFT burn transaction keeps its serials in order" {
    // when
    let burn = TokenBurnNftTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: [420, 1, 2]}

    // then
    assertEquals(expected: Address.fromString(address: "4.2.0"), actual: burn.tokenId)
    assertEquals(expected: [420, 1, 2], actual: burn.serials)
}

// Source: this spec (@@maxBytes(100)); the network limits a token name to 100 UTF-8 bytes (TOKEN_NAME_TOO_LONG).
test "a token create transaction rejects a name longer than 100 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCreateTransaction{name: TEXT_OF_101_CHARACTERS, symbol: "test symbol", tokenType: TokenType.FUNGIBLE_COMMON,
            treasuryAccountId: AccountId.fromString(value: "7.7.7")}
}

// Source: this spec (@@maxBytes(100)); the network limits a token symbol to 100 UTF-8 bytes (TOKEN_SYMBOL_TOO_LONG).
test "a token create transaction rejects a symbol longer than 100 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCreateTransaction{name: "test name", symbol: TEXT_OF_101_CHARACTERS, tokenType: TokenType.FUNGIBLE_COMMON,
            treasuryAccountId: AccountId.fromString(value: "7.7.7")}
}

// Source: this spec (@@maxBytes(100)); the network limits a memo to 100 UTF-8 bytes (MEMO_TOO_LONG).
test "a token create transaction rejects a memo longer than 100 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCreateTransaction{name: "test name", symbol: "test symbol", tokenType: TokenType.FUNGIBLE_COMMON,
            treasuryAccountId: AccountId.fromString(value: "7.7.7"), tokenMemo: TEXT_OF_101_CHARACTERS}
}

// Source: this spec (@@min(1) of maxSupply); the network rejects a finite token without a positive maximum supply
// (INVALID_TOKEN_MAX_SUPPLY).
test "a token create transaction rejects a maximum supply of 0" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCreateTransaction{name: "test name", symbol: "test symbol", tokenType: TokenType.NON_FUNGIBLE_UNIQUE,
            treasuryAccountId: AccountId.fromString(value: "7.7.7"), supplyType: TokenSupplyType.FINITE, maxSupply: 0}
}

// Source: this spec (@@maxBytes(100)); the network limits a token name to 100 UTF-8 bytes (TOKEN_NAME_TOO_LONG).
test "a token update transaction rejects a name longer than 100 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenUpdateTransaction{tokenId: Address.fromString(address: "4.2.0"), name: TEXT_OF_101_CHARACTERS}
}

// Source: this spec (@@maxBytes(100)); the network limits a token symbol to 100 UTF-8 bytes (TOKEN_SYMBOL_TOO_LONG).
test "a token update transaction rejects a symbol longer than 100 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenUpdateTransaction{tokenId: Address.fromString(address: "4.2.0"), symbol: TEXT_OF_101_CHARACTERS}
}

// Source: this spec (@@maxBytes(100)); the network limits a memo to 100 UTF-8 bytes (MEMO_TOO_LONG).
test "a token update transaction rejects a memo longer than 100 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenUpdateTransaction{tokenId: Address.fromString(address: "4.2.0"), tokenMemo: TEXT_OF_101_CHARACTERS}
}

// Source: this spec (@@min(1) of amount).
test "a fungible mint transaction rejects an amount of 0" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenMintFungibleTransaction{tokenId: Address.fromString(address: "4.2.0"), amount: 0}
}

// Source: this spec (@@minSize(1): one metadata entry per new serial).
test "an NFT mint transaction rejects an empty list of metadata" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenMintNftTransaction{tokenId: Address.fromString(address: "4.2.0"), metadata: []}
}

// Source: this spec (@@maxSize(10): the network caps a mint at 10 NFTs, TOKEN_MAX_BATCH_SIZE_REACHED).
test "an NFT mint transaction rejects more than 10 metadata entries" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenMintNftTransaction{tokenId: Address.fromString(address: "4.2.0"), metadata: ELEVEN_METADATA_ENTRIES}
}

// Source: this spec (@@min(1) of amount).
test "a fungible burn transaction rejects an amount of 0" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenBurnFungibleTransaction{tokenId: Address.fromString(address: "4.2.0"), amount: 0}
}

// Source: this spec (@@minSize(1)).
test "an NFT burn transaction rejects an empty list of serials" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenBurnNftTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: []}
}

// Source: this spec (@@maxSize(10): the network caps a burn at 10 NFTs, TOKEN_MAX_BATCH_SIZE_REACHED).
test "an NFT burn transaction rejects more than 10 serials" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenBurnNftTransaction{tokenId: Address.fromString(address: "4.2.0"), serials: ELEVEN_SERIALS}
}

// Source: the Java SDK (shouldBytes / shouldBytesFungible / shouldBytesNft of the TokenCreate, TokenUpdate,
// TokenDelete, TokenAssociate, TokenDissociate, TokenMint and TokenBurn tests: fromBytes(toBytes()) gives the same
// transaction; TokenCreate with the expiration time validStart = Instant.ofEpochSecond(1554158542) = 2019-04-01T22:42:22Z,
// which clears its auto-renew period there, TokenUpdate with the auto-renew period Duration.ofHours(10) = 36000
// seconds); the Rust SDK (token_update_transaction.rs to_from_bytes: the expiration time VALID_START); the JavaScript
// SDK ("should round trip from bytes") and the to / from bytes tests of the Go SDK.
test "every token lifecycle transaction survives toBytes and fromBytes unchanged" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")]
    let token = Address.fromString(address: "4.2.0")
    let account = AccountId.fromString(value: "0.6.9")
    let transactions: list<consensusnode.transactions.PackedTransaction<ANY, ANY>> = [
            TokenCreateTransaction{name: "test name", symbol: "test symbol", tokenType: TokenType.FUNGIBLE_COMMON,
                    decimals: 3, initialSupply: 30, treasuryAccountId: account, expirationTime: SDK_TEST_EXPIRATION_TIME,
                    adminAuthority: DEFAULT(Authority)}.sign(payer: payer, nodes: nodes),
            TokenUpdateTransaction{tokenId: token, name: "test name", expirationTime: SDK_TEST_EXPIRATION_TIME,
                    autoRenewPeriod: toSeconds(value: 36000)}.sign(payer: payer, nodes: nodes),
            TokenDeleteTransaction{tokenId: token}.sign(payer: payer, nodes: nodes),
            TokenAssociateTransaction{accountId: account, tokens: [token]}.sign(payer: payer, nodes: nodes),
            TokenDissociateTransaction{accountId: account, tokens: [token]}.sign(payer: payer, nodes: nodes),
            TokenMintFungibleTransaction{tokenId: token, amount: 10}.sign(payer: payer, nodes: nodes),
            TokenMintNftTransaction{tokenId: token, metadata: [[1, 2, 3, 4, 5]]}.sign(payer: payer, nodes: nodes),
            TokenBurnFungibleTransaction{tokenId: token, amount: 69}.sign(payer: payer, nodes: nodes),
            TokenBurnNftTransaction{tokenId: token, serials: [420]}.sign(payer: payer, nodes: nodes)]

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

// Source: this spec (@@maxSize(100) of metadata); HIP-646 and the consensus node (tokens.maxMetadataBytes = 100,
// METADATA_TOO_LONG); the Python SDK (TokenCreateTransaction.set_metadata rejects more than 100 bytes).
test "a token create transaction rejects metadata longer than 100 bytes" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCreateTransaction{name: "test name", symbol: "test symbol", tokenType: TokenType.FUNGIBLE_COMMON,
            treasuryAccountId: AccountId.fromString(value: "7.7.7"), metadata: repeat(value: hex(text: "78"), count: 101)}
}

// Source: this spec (@@maxSize(100) of metadata); HIP-646 and the consensus node (tokens.maxMetadataBytes = 100).
test "a token update transaction rejects metadata longer than 100 bytes" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenUpdateTransaction{tokenId: Address.fromString(address: "4.2.0"),
            metadata: repeat(value: hex(text: "78"), count: 101)}
}

// Source: this spec (@@maxBytes(100) of name); the consensus node (tokens.maxTokenNameUtf8Bytes = 100,
// TOKEN_NAME_TOO_LONG): 51 characters "ä" are 102 bytes of UTF-8.
test "a token create transaction rejects a name of more than 100 UTF-8 bytes" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TokenCreateTransaction{name: repeat(value: "ä", count: 51), symbol: "test symbol",
            tokenType: TokenType.FUNGIBLE_COMMON, treasuryAccountId: AccountId.fromString(value: "7.7.7")}
}
```

## Questions & Comments

- **All key fields are the `Authority` authorization type.** Each token key (admin, supply,
  kyc, freeze, wipe, pause, feeSchedule, metadata) is an `Authority` — a single key, a
  contract, or an m-of-n threshold (`AuthorityList`) — introduced in
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and specified in
  [`authority.md`](../base/authority.md). This resolves the former `PublicKey` placeholder
  and is particularly load-bearing for tokens: real-world deployments routinely guard
  `supplyAuthority` with a treasury-DAO threshold or a contract id, not a single public key.

- **Clearing a key field on `TokenUpdate` — not yet expressible.** HIP-540 lets an admin (or
  the key holder itself) clear any of the seven lower-privilege key fields to an
  irrevocably-empty state (e.g. drop the `adminAuthority` to make a token immutable, drop the
  `wipeAuthority` to permanently disable wipe). Clearing is a future write-side `KeyUpdate`
  operation (per [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md)), not
  expressible as an `Authority` value. For `Address`-typed fields,
  [`ZERO_ADDRESS`](../base/ledger.md) is the existing sentinel —
  `autoRenewAccount(ZERO_ADDRESS)` drops the auto-renew account. `treasuryAccountId` is **not**
  clearable: a token must always have a treasury.

- **`customFees` / `feeScheduleAuthority` interaction is partial.** This file exposes `feeScheduleAuthority`
  on `TokenCreate` / `TokenUpdate` because the field is independent and cheap to model, but the
  write-side `CustomFee` hierarchy needed by `TokenCreate.customFees` and by
  `TokenFeeScheduleUpdate` is **not** specified here. Read-side custom-fee shapes live in
  [`mirror-node-token.md`](../mirror-node-client/mirror-node-token.md); the write-side builder is
  tracked in [`missing-features.md`](../../docs/missing-features.md) section 3.3 and depends on the
  same `Key` sum type. Adding `customFees: list<CustomFee>` here once the write-side model lands
  is additive and breaks nothing. Until then neither the Java implementation nor the TCK can create a
  token with custom fees: the TCK binding declares the `customFees` parameter of `createToken` as
  unsupported, and the TCK tests that use it cannot pass.

- **`TokenWipe`, `Freeze` / `Unfreeze`, `Pause` / `Unpause`, `GrantKyc` / `RevokeKyc`,
  `FeeScheduleUpdate`, `UpdateNfts` are deliberately out of scope of this file.** They live in
  the same namespace and follow the same lifecycle / signing model as the seven transactions
  here — splitting the file keeps the core lifecycle readable. Each gets its own file as it
  lands. Tracked in [`missing-features.md`](../../docs/missing-features.md) section 1.2.

- **HIP-904 airdrop transactions are deliberately out of scope of this file.**
  `TokenAirdrop` / `TokenClaimAirdrop` / `TokenCancelAirdrop` / `TokenReject` reuse the
  association rules described here but interact with `PendingAirdropId` and the new
  `pendingAirdrops` mirror-node domain; they deserve their own spec rather than being smuggled
  into the lifecycle file. Tracked in [`missing-features.md`](../../docs/missing-features.md) section
  1.2.

- **`TokenMint` / `TokenBurn` cap at 10 NFTs per transaction.** That is the current HAPI limit
  (`TOKEN_MAX_BATCH_SIZE_REACHED`) and is encoded as `@@maxSize(10)` on the `metadata` and
  `serials` lists. If a future HIP raises the cap (HIP-1300 jumbo transactions touch this area),
  the annotation widens — callers that respected the previous cap remain valid.
- **Clearing `autoRenewAccount` with `ZERO_ADDRESS` or `ZERO_ACCOUNT_ID`?** The description and the item above
  name `ZERO_ADDRESS` as the sentinel for `autoRenewAccount`, but the field is an `AccountId` and cannot hold an
  `Address`; [`ledger.md`](../base/ledger.md) defines `ZERO_ACCOUNT_ID` for exactly this field. Should the text name
  `ZERO_ACCOUNT_ID`? — open

- **No size limit on the metadata of a minted NFT.** The network limits the metadata of each new serial to 100 bytes
  (`tokens.nfts.maxMetadataBytes`), like the token metadata, which carries `@@maxSize(100)`. The entries of
  `TokenMintNftTransaction.metadata` cannot be annotated today (no annotation on a type argument), and the same holds
  for the serial lists, whose entries the network requires to be at least 1. — open

- **Cross-field rules are not expressed as annotations.** `decimals` and `initialSupply` MUST be 0 for
  `NON_FUNGIBLE_UNIQUE`, `maxSupply` is only valid with `FINITE`, and `freezeDefault = true` requires a
  `freezeAuthority`; today these rules live only in comments, so neither the generated contract tests nor the spec
  tests can check them, and the network rejects them only on submit. Should they be checked by the SDK (and then
  declared, e.g. as a validation annotation or a `@@throws` of `pack` / `sign`), or stay network-side? — open

- **`expirationTime` together with `autoRenewPeriod` on `TokenCreateTransaction`.** The spec allows both, and the
  tests keep both. The Java SDK instead clears `autoRenewPeriod` when `setExpirationTime` is called, because a set
  auto-renew period overrides the expiration time on the network; the Rust SDK keeps both. Is setting both a
  meaningful combination in V3, should one of them win (and be documented), or should they be `@@oneOrNoneOf`?
  Also, "SDK default applies when unset" for `expirationTime` names no default (the Java and Rust SDKs set an
  auto-renew period of 90 days instead), so no test can check it. — open

- **A `TokenCreateTransaction` without `expirationTime` and `autoRenewPeriod` is rejected by the network.** The
  description says that the protocol default applies when `autoRenewPeriod` is unset, but HAPI has no such default
  and rejects a create that sets neither field. The Java implementation follows V2: it sends an `autoRenewPeriod` of
  90 days, and whenever the period is non-zero and no `autoRenewAccount` is given, it sets the payer as auto-renew
  account. This contradicts both the description and `TokenInfo.autoRenewAccount` in
  [`queries-tokens.md`](queries-tokens.md) ("if absent, the token itself pays"). Should the spec state these defaults
  (e.g. as `@@default` on `autoRenewPeriod` and a rule for `autoRenewAccount`), or should `pack(...)` reject a create
  without either field? — open

- **`tokenType` has no default.** `TokenCreateTransaction.tokenType` is required, while the V2 SDKs default to
  `FUNGIBLE_COMMON` and the TCK binding sends `"ft"` when the TCK request leaves the type out. Should the field get
  `@@default(FUNGIBLE_COMMON)`, or stay required (and the binding keep supplying the value)? — open

- **`tokens` of `TokenAssociate` / `TokenDissociate` is a `set`, which the TCK contradicts.** Semantically the field
  is a set — order does not matter and a duplicate is meaningless — so the type follows the meaning. But the TCK has
  tests that send a token twice and expect the network status `TOKEN_ID_REPEATED_IN_TOKEN_LIST`; with a `set` the
  duplicate collapses before the transaction is built, the transaction succeeds, and these tests cannot pass. The Java
  implementation keeps the `set`. Is `set` the right choice (and the TCK tests are out of scope for V3), or does the
  API have to be able to send the duplicates? — open

- **Splitting mint / burn / wipe by token type removes a request the network rejects.** `TokenMint`, `TokenBurn` and
  `TokenWipe` (in [`transactions-tokens-management.md`](transactions-tokens-management.md)) have a fungible variant
  with an `amount` and an NFT variant with `metadata` / `serials`, so a request with neither an amount nor serials
  cannot be expressed. Where the V2 SDKs send such a request and the network rejects it, the Java implementation
  fails in the SDK (no variant can be chosen). Is the earlier, SDK-side failure the intended behavior, and which
  error should it be? — open
