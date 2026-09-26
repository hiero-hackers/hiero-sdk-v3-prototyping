# NFT Service API

Service definition for creating and managing non-fungible tokens (NFTs).

## Description

Provides high-level operations for NFTs: creating an NFT type (collection), account association, minting NFTs with
metadata, burning, and transfers. An NFT type id is a `ledger.Address` (pure shard.realm.num); accounts are
`ledger.AccountId`. Individual NFTs are identified by their type plus an `int64` serial number. Metadata is an
opaque `bytes` payload.

### Defining an NFT type

Creating an NFT type has many meaningful — and mostly optional — parameters (supply policy, memo, token-level
metadata, auto-renew settings, and up to eight authorities). Expressing all of them as method parameters would
require a combinatorial explosion of overloads, while the few short overloads that exist today silently drop most of
the options.

Therefore the create operation takes a single fully immutable definition object, `NonFungibleTokenDef`, that carries
every attribute of the new type. This keeps one canonical, fully expressive entry point
(`createNftType(def: NonFungibleTokenDef)`); the remaining `createNftType` overloads are pure convenience shortcuts
for the most common cases and are specified in terms of an equivalent `NonFungibleTokenDef`.

`NonFungibleTokenDef` describes only what is *definable at creation time*. Fields that the protocol fixes for every
NFT collection are intentionally absent: the token type is always `NON_FUNGIBLE_UNIQUE`, and `decimals` /
`initialSupply` are always `0` (serials are created by `mintNft`, not by the create call). See
[`transactions-tokens.md`](../consensus-node-client/transactions-tokens.md) for the underlying
`TokenCreateTransaction` and for the exact signing requirements of each authority. An authority that is *not* set at
creation time can never be added later, so `NonFungibleTokenDef` is also the last chance to enable minting,
freezing, pausing, wiping, KYC, or per-serial metadata updates for the collection.

A `FINITE` `supplyType` together with `maxSupply` is fully supported for NFT collections and enforced by the
protocol: every mint that would push the number of NFTs above `maxSupply` fails. The ceiling is checked against the
*current* supply, though — burning an NFT frees room for another mint, while serial numbers keep increasing and are
never reused. `maxSupply` therefore bounds how many NFTs of the collection exist *simultaneously*, not how many
serials are ever issued; a "only ever N NFTs in total" guarantee additionally requires giving up the
`supplyAuthority` after the last mint. See
[`transactions-tokens.md`](../consensus-node-client/transactions-tokens.md) for the protocol-level details.

Since `NonFungibleTokenDef` is immutable and has many optional fields, languages should offer their idiomatic
construction pattern for it — a builder in Java (see
[Builder Pattern](../../guidelines/api-best-practices-java.md#builder-pattern)), an options object in JavaScript, a
struct literal with defaults in Rust, keyword arguments in Python.

## API Schema

```
namespace enterprise.service.nft
requires {Page} from common
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {TokenSupplyType} from token
requires {Nft, NftMetadata} from mirrornode.nft
requires {Session} from enterprise.service

// Immutable definition of a new NFT type (collection). Every field except `name` and `symbol` is optional;
// unset fields fall back to the documented default (or to a value derived from the session's operator account).
// The token type is implicitly NON_FUNGIBLE_UNIQUE and `decimals` / `initialSupply` are implicitly 0.
@@finalType
NonFungibleTokenDef {
    @@immutable @@maxLength(100) name: string                       // human-readable name of the collection
    @@immutable @@maxLength(100) symbol: string                     // short ticker / symbol
    @@immutable @@nullable treasuryAccount: AccountId               // account that receives every minted serial; defaults to the operator account
    @@immutable @@default(INFINITE) supplyType: TokenSupplyType     // INFINITE | FINITE; cannot be changed later
    @@immutable @@nullable @@min(1) maxSupply: int64                // FINITE only: protocol-enforced ceiling on the number of simultaneously existing NFTs; must be unset when supplyType is INFINITE
    @@immutable @@default(false) freezeDefault: bool                // when true, newly associated accounts start FROZEN; requires freezeAuthority to be set
    @@immutable @@nullable @@maxLength(100) memo: string            // short, human-readable label on the collection
    @@immutable @@default([]) metadata: bytes                       // opaque collection-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
    @@immutable @@nullable expirationTime: zonedDateTime            // when the collection expires; SDK default applies when unset
    @@immutable @@nullable autoRenewPeriod: seconds                 // renewal window applied when expiration approaches; protocol default applies when unset
    @@immutable @@nullable autoRenewAccount: AccountId              // account paying for auto-renewal; defaults to the treasury account

    // Optional authorities — an authority NOT set at creation time can never be added later.
    @@immutable @@nullable adminAuthority: Authority                // controls update / delete; unset → immutable collection
    @@immutable @@nullable supplyAuthority: Authority               // controls mint / burn; unset → no serial can ever be minted
    @@immutable @@nullable kycAuthority: Authority                  // controls KYC grant / revoke; unset → KYC disabled
    @@immutable @@nullable freezeAuthority: Authority               // controls freeze / unfreeze; unset → no freeze
    @@immutable @@nullable wipeAuthority: Authority                 // controls wipe of non-treasury holdings; unset → wipe disabled
    @@immutable @@nullable pauseAuthority: Authority                // controls pause / unpause; unset → unpausable
    @@immutable @@nullable feeScheduleAuthority: Authority          // controls custom-fee schedule updates; unset → fees immutable
    @@immutable @@nullable metadataAuthority: Authority             // controls per-serial NFT metadata updates (HIP-657); unset → metadata immutable
}

NftService {

    // Create a new NFT type (collection) from a full definition; returns the metadata of the new type.
    // This is the canonical create operation — the overloads below are convenience shortcuts for it.
    @@throws(service-error) NftMetadata createNftType(def: NonFungibleTokenDef)

    // Convenience: definition with only `name` and `symbol` set — an immutable collection with no mintable supply.
    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string)

    // Convenience: as above plus a supply authority, so serials can be minted later.
    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string, supplyAuthority: Authority)

    // Convenience: as above with an explicit treasury account instead of the operator account.
    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string, treasuryAccount: AccountId, supplyAuthority: Authority)

    // Associate an account with one or more NFT types so it can hold them
    @@throws(service-error) void associateNft(accountId: AccountId, tokenIds: Address...)

    // Remove the association between an account and one or more NFT types
    @@throws(service-error) void dissociateNft(accountId: AccountId, tokenIds: Address...)

    // Mint a single NFT with the given metadata; returns the new serial number
    @@throws(service-error) Nft mintNft(tokenId: Address, metadata: bytes)

    // Mint multiple NFTs in one operation; returns the new serial numbers in order
    @@throws(service-error) list<Nft> mintNfts(tokenId: Address, metadata: bytes...)

    // Burn a single NFT by serial number
    @@throws(service-error) void burnNft(tokenId: Address, serialNumber: int64)

    // Burn a single NFT by serial number
    @@throws(service-error) void burnNft(nft: Nft)

    // Burn multiple NFTs by serial number
    @@throws(service-error) void burnNfts(tokenId: Address, serialNumbers: set<int64>)

    // Burn multiple NFTs by serial number
    @@throws(service-error) void burnNfts(nfts: set<Nft>)

    // Transfer a single NFT to another account
    @@throws(service-error) void transferNft(tokenId: Address, serialNumber: int64, fromAccountId: AccountId, toAccountId: AccountId)

    @@throws(service-error) void transferNfts(nft: Nft, fromAccountId: AccountId, toAccountId: AccountId)


    // Transfer multiple NFTs of the same type to another account
    @@throws(service-error) void transferNfts(tokenId: Address, serialNumbers: list<int64>, fromAccountId: AccountId, toAccountId: AccountId)

    @@throws(service-error) void transferNfts(nfts: set<Nft>, fromAccountId: AccountId, toAccountId: AccountId)

    // Return all known NFT types (collections)
    @@throws(service-error) Page<NftMetadata> findAllTypes()

    // Return all NFT types that the given account holds at least one NFT of
    @@throws(service-error) Page<NftMetadata> findTypesByOwner(ownerId: AccountId)

    // Return the metadata of a single NFT type
    @@throws(service-error) @@nullable NftMetadata findTypeById(tokenId: Address)

    // Return all NFTs owned by the given account
    @@throws(service-error) Page<Nft> findByOwner(ownerId: AccountId)

    // Return all NFTs of the given type
    @@throws(service-error) Page<Nft> findByType(tokenId: Address)

    // Return a single NFT identified by type and serial number
    @@throws(service-error) @@nullable Nft findByTypeAndSerial(tokenId: Address, serialNumber: int64)

    // Return all NFTs of the given type that are owned by the given account
    @@throws(service-error) Page<Nft> findByOwnerAndType(ownerId: AccountId, tokenId: Address)

    // Return a single NFT identified by owner, type, and serial number
    @@throws(service-error) @@nullable Nft findByOwnerAndTypeAndSerial(ownerId: AccountId, tokenId: Address, serialNumber: int64)
}

// Factory method to create the service (not needed for real framework integration where injection is used)
@@static
NftService createService(session: Session)
```

## Examples

### Create an NFT type from a definition (Java)

`NonFungibleTokenDef` is immutable with many optional fields, so Java maps it to a record-like type with a builder
(see [Builder Pattern](../../guidelines/api-best-practices-java.md#builder-pattern)). The treasury defaults to the
operator account; setting `supplyAuthority` is what makes the collection mintable, and `freezeAuthority` /
`pauseAuthority` enable the corresponding lifecycle operations.

```
NftService nftService = ...;

NonFungibleTokenDef def = NonFungibleTokenDef.builder()
    .name("Nft Tokens")
    .symbol("NT")
    .metadata(metadataBytes)
    .supplyAuthority(Authority.of(minterPublicKey))
    .freezeAuthority(Authority.of(freezePublicKey))
    .pauseAuthority(Authority.of(pausePublicKey))
    .build();

NftMetadata meta = nftService.createNftType(def);
```

### Create a finite-supply collection with a dedicated treasury (Java)

```
NonFungibleTokenDef def = NonFungibleTokenDef.builder()
    .name("Example Art")
    .symbol("EART")
    .treasuryAccount(treasuryAccountId)
    .supplyType(TokenSupplyType.FINITE)
    .maxSupply(10_000L)
    .memo("Limited art drop")
    .adminAuthority(Authority.of(adminPublicKey))
    .supplyAuthority(Authority.of(minterPublicKey))
    .metadataAuthority(Authority.of(adminPublicKey))     // allow per-serial metadata updates (HIP-657)
    .build();

Address tokenId = nftService.createNftType(def).tokenId;
```

### Create a minimal collection without a definition object (Java)

For the trivial case the convenience overloads avoid building a definition at all:

```
NftMetadata meta = nftService.createNftType("Nft Tokens", "NT", Authority.of(minterPublicKey));
```

## Questions & Comments

- The `supplyAuthority` parameters of `createNftType` accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled supply keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.
- The `maxSupply` / `supplyType` dependency ("`maxSupply` may only be set when `supplyType` is `FINITE`") is
  currently only prose. The meta-language has no annotation for a conditional field constraint — `@@oneOf` does not
  fit, since the constraint couples a field to another field's *value*. Should the guideline gain something like
  `@@requiredIf(field, value)` / `@@forbiddenIf(field, value)` so the invariant is machine-checkable here and in
  `TokenCreateTransaction`?
- The definition-object pattern is now also used by `FungibleTokenService.createToken` (`FungibleTokenDef`, see
  [`service-token.md`](service-token.md)). Should it be extended to the remaining enterprise services with wide
  create surfaces — `TopicService`, `AccountService`, `FileService`? Keeping the shape uniform across the whole
  enterprise layer seems preferable to having it for tokens only.
- `NonFungibleTokenDef` deliberately has no field for HIP-18 custom fees (royalty fees are the common NFT case).
  Custom fees are not yet specified anywhere in `consensusnode.transactions.tokens`; once they are,
  `NonFungibleTokenDef` should gain a `customFees` field rather than a separate create overload.
