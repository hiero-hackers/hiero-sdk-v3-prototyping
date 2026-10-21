# Fungible Token Service API

Service definition for creating and managing fungible tokens (the Token Service).

## Description

Provides high-level operations for fungible tokens: creation, account association, minting, burning, and transfers.
Token ids are `ledger.Address` values (pure shard.realm.num); account ids are `ledger.AccountId`. Amounts are
expressed in the token's smallest unit as `int64`. A token that should support later minting/burning must be
created with a supply key.

### Defining a fungible token

Creating a token has many meaningful — and mostly optional — parameters (precision, supply policy, memo, token-level
metadata, auto-renew settings, and up to eight authorities). Expressing all of them as method parameters would
require a combinatorial explosion of overloads, while the few short overloads that exist today silently drop most of
the options.

Therefore the create operation takes a single fully immutable definition object, `FungibleTokenDef`, that carries
every attribute of the new token. This keeps one canonical, fully expressive entry point
(`createToken(def: FungibleTokenDef)`); the remaining `createToken` overloads are pure convenience shortcuts for the
most common cases and are specified in terms of an equivalent `FungibleTokenDef`. The same pattern is used for NFT
collections — see [`service-nft.md`](service-nft.md) and its `NonFungibleTokenDef`.

`FungibleTokenDef` describes only what is *definable at creation time*; the token type is implicitly
`FUNGIBLE_COMMON`. See [`transactions-tokens.md`](../consensus-node-client/transactions-tokens.md) for the underlying
`TokenCreateTransaction` and for the exact signing requirements of each authority. An authority that is *not* set at
creation time can never be added later, so `FungibleTokenDef` is also the last chance to enable minting, freezing,
pausing, wiping, KYC, or metadata updates for the token.

`decimals`, `initialSupply`, and `maxSupply` are all expressed in the token's smallest indivisible unit — for a
6-decimals token, `initialSupply = 1_000_000` means "one display unit". A `FINITE` `supplyType` together with
`maxSupply` is enforced by the protocol: every mint that would push `totalSupply` above `maxSupply` fails. The
ceiling is checked against the *current* `totalSupply`, though — burning units frees room for another mint, so
`maxSupply` bounds how many units exist *simultaneously*, not how many units are ever issued. A "only ever N units in
total" guarantee additionally requires giving up the `supplyAuthority` after the last mint.

Since `FungibleTokenDef` is immutable and has many optional fields, languages should offer their idiomatic
construction pattern for it — a builder in Java (see
[Builder Pattern](../../guidelines/api-best-practices-java.md#builder-pattern)), an options object in JavaScript, a
struct literal with defaults in Rust, keyword arguments in Python.

## API Schema

```
namespace enterprise.service.token
requires {Page} from common
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {TokenSupplyType} from token
requires {Token, TokenInfo, Balance} from mirrornode.token
requires {Session} from enterprise.service

// Immutable definition of a new fungible token. Every field except `name` and `symbol` is optional; unset fields
// fall back to the documented default (or to a value derived from the session's operator account). The token type
// is implicitly FUNGIBLE_COMMON. All amounts are in the token's smallest indivisible unit.
@@finalType
FungibleTokenDef {
    @@immutable @@maxLength(100) name: string                       // human-readable token name
    @@immutable @@maxLength(100) symbol: string                     // short ticker / symbol
    @@immutable @@default(0) decimals: int32                        // precision of the smallest indivisible unit (e.g. 6 for a USDC-style token)
    @@immutable @@default(0) initialSupply: int64                   // amount minted to the treasury at create time
    @@immutable @@nullable treasuryAccount: AccountId               // account that receives `initialSupply` and every later mint; defaults to the operator account
    @@immutable @@default(INFINITE) supplyType: TokenSupplyType     // INFINITE | FINITE; cannot be changed later
    @@immutable @@nullable @@min(1) maxSupply: int64                // FINITE only: protocol-enforced ceiling on the simultaneously existing totalSupply; must be unset when supplyType is INFINITE
    @@immutable @@default(false) freezeDefault: bool                // when true, newly associated accounts start FROZEN; requires freezeAuthority to be set
    @@immutable @@nullable @@maxLength(100) memo: string            // short, human-readable label on the token
    @@immutable @@default([]) metadata: bytes                       // opaque token-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
    @@immutable @@nullable expirationTime: zonedDateTime            // when the token expires; SDK default applies when unset
    @@immutable @@nullable autoRenewPeriod: seconds                 // renewal window applied when expiration approaches; protocol default applies when unset
    @@immutable @@nullable autoRenewAccount: AccountId              // account paying for auto-renewal; defaults to the treasury account

    // Optional authorities — an authority NOT set at creation time can never be added later.
    @@immutable @@nullable adminAuthority: Authority                // controls update / delete; unset → immutable token
    @@immutable @@nullable supplyAuthority: Authority               // controls mint / burn; unset → supply fixed at initialSupply
    @@immutable @@nullable kycAuthority: Authority                  // controls KYC grant / revoke; unset → KYC disabled
    @@immutable @@nullable freezeAuthority: Authority               // controls freeze / unfreeze; unset → no freeze
    @@immutable @@nullable wipeAuthority: Authority                 // controls wipe of non-treasury balances; unset → wipe disabled
    @@immutable @@nullable pauseAuthority: Authority                // controls pause / unpause; unset → unpausable
    @@immutable @@nullable feeScheduleAuthority: Authority          // controls custom-fee schedule updates; unset → fees immutable
    @@immutable @@nullable metadataAuthority: Authority             // controls token metadata updates; unset → metadata immutable
}

FungibleTokenService {

    // Create a fungible token from a full definition; returns the token id of the new token.
    // This is the canonical create operation — the overloads below are convenience shortcuts for it.
    @@throws(service-error) Address createToken(def: FungibleTokenDef)

    // Convenience: FungibleTokenDef with only `name` and `symbol` set — 0 decimals, no initial supply,
    // no mintable supply, treasury is the operator account.
    @@throws(service-error) Address createToken(name: string, symbol: string)

    // Convenience: as above plus a supply authority, so units can be minted later.
    @@throws(service-error) Address createToken(name: string, symbol: string, supplyAuthority: Authority)

    // Convenience: as above with an explicit treasury account instead of the operator account.
    @@throws(service-error) Address createToken(name: string, symbol: string, treasuryAccount: AccountId, supplyAuthority: Authority)

    // Associate an account with one or more tokens so it can hold them
    @@throws(service-error) void associateToken(accountId: AccountId, tokenIds: Address...)

    // Remove the association between an account and one or more tokens
    @@throws(service-error) void dissociateToken(accountId: AccountId, tokenIds: Address...)

    // Mint new units into the treasury; returns the new total supply
    @@throws(service-error) int64 mintToken(tokenId: Address, amount: int64)

    // Burn units from the treasury; returns the new total supply
    @@throws(service-error) int64 burnToken(tokenId: Address, amount: int64)

    // Transfer units from the operator account to a recipient
    @@throws(service-error) void transferToken(tokenId: Address, toAccountId: AccountId, amount: int64)

    // Transfer units from a specific account to a recipient
    @@throws(service-error) void transferToken(tokenId: Address, fromAccountId: AccountId, toAccountId: AccountId, amount: int64)

    // Return the full token information for the given token id
    @@throws(service-error) @@nullable TokenInfo findById(tokenId: Address)

    // Return all tokens that the given account is associated with
    @@throws(service-error) Page<Token> findByAccount(accountId: AccountId)

    // Return the balance of the given token held by every account that holds it
    @@throws(service-error) Page<Balance> getBalances(tokenId: Address)

    // Return the balance of the given token for a specific account
    @@throws(service-error) Page<Balance> getBalancesForAccount(tokenId: Address, accountId: AccountId)
}

// Factory method to create the service (not needed for real framework integration where injection is used)
@@static
FungibleTokenService createService(session: Session)
```

## Examples

### Create a token from a definition (Java)

`FungibleTokenDef` is immutable with many optional fields, so Java maps it to a record-like type with a builder (see
[Builder Pattern](../../guidelines/api-best-practices-java.md#builder-pattern)). The treasury defaults to the
operator account; setting `supplyAuthority` is what makes the token mintable, and `freezeAuthority` /
`pauseAuthority` enable the corresponding lifecycle operations.

```
FungibleTokenService tokenService = ...;

FungibleTokenDef def = FungibleTokenDef.builder()
    .name("Example USD")
    .symbol("EUSD")
    .decimals(6)
    .initialSupply(1_000_000_000_000L)                   // 1,000,000.000000 EUSD to the treasury
    .metadata(metadataBytes)
    .supplyAuthority(Authority.of(minterPublicKey))
    .freezeAuthority(Authority.of(freezePublicKey))
    .pauseAuthority(Authority.of(pausePublicKey))
    .build();

Address tokenId = tokenService.createToken(def);
```

### Create a finite-supply token with a dedicated treasury (Java)

```
FungibleTokenDef def = FungibleTokenDef.builder()
    .name("Acme Loyalty")
    .symbol("ACME")
    .treasuryAccount(treasuryAccountId)
    .supplyType(TokenSupplyType.FINITE)
    .maxSupply(10_000_000L)
    .memo("Acme loyalty points")
    .adminAuthority(Authority.of(adminPublicKey))
    .supplyAuthority(Authority.of(minterPublicKey))
    .build();

Address tokenId = tokenService.createToken(def);
```

### Create a minimal token without a definition object (Java)

For the trivial case the convenience overloads avoid building a definition at all:

```
Address tokenId = tokenService.createToken("Example USD", "EUSD", Authority.of(minterPublicKey));
```

## Questions & Comments

- The `supplyAuthority` parameters of `createToken` accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled supply keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.
- The `maxSupply` / `supplyType` dependency ("`maxSupply` may only be set when `supplyType` is `FINITE`") is
  currently only prose — the same open question as in [`service-nft.md`](service-nft.md): the meta-language has no
  annotation for a field constraint that depends on another field's *value*.
- `FungibleTokenDef` deliberately has no field for HIP-18 custom fees (fixed / fractional fees). Custom fees are not
  yet specified anywhere in `consensusnode.transactions.tokens`; once they are, `FungibleTokenDef` should gain a
  `customFees` field rather than a separate create overload.

We need better "annotation" support to define ranges of params. For example:

```
@@throws(service-error) @@positiveValue int64 mintToken(tokenId: Address, @@positiveValue amount: int64)
```
Here `@@positiveValue` is added at 2 positions to define a better restriction.
