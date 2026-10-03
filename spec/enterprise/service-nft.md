# NFT Service API

## Description

`NftService` creates and manages non-fungible tokens (NFTs): creating an NFT type (collection), account association,
minting NFTs with metadata, burning, transfers and lookups. An NFT type is identified by an `Address`
(shard.realm.num), accounts by an `AccountId`. An individual NFT is identified by its type plus its serial number.
NFT metadata is an opaque byte payload.

## API Schema

```
namespace enterprise.service.nft
requires {Page} from common
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {Nft, NftMetadata} from mirrornode.nft
requires {Session} from enterprise.service

NftService {

    // Create a new NFT type (collection); returns the metadata of the new type, including its token id.
    // The treasury defaults to the operator account; a supply authority enables minting.
    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string)

    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string, supplyAuthority: Authority)

    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string, treasuryAccount: AccountId, supplyAuthority: Authority)

    // Associate an account with one or more NFT types so it can hold them
    @@throws(service-error) void associateNft(accountId: AccountId, tokenIds: Address...)

    // Remove the association between an account and one or more NFT types
    @@throws(service-error) void dissociateNft(accountId: AccountId, tokenIds: Address...)

    // Mint a single NFT with the given metadata; returns the new NFT including its serial number
    @@throws(service-error) Nft mintNft(tokenId: Address, metadata: bytes)

    // Mint multiple NFTs in one operation; returns the new NFTs in the order of the given metadata
    @@throws(service-error) list<Nft> mintNfts(tokenId: Address, metadata: bytes...)

    // Burn a single NFT by serial number
    @@throws(service-error) void burnNft(tokenId: Address, serialNumber: int64)

    // Burn the given NFT
    @@throws(service-error) void burnNft(nft: Nft)

    // Burn multiple NFTs by serial number
    @@throws(service-error) void burnNfts(tokenId: Address, serialNumbers: set<int64>)

    // Burn the given NFTs
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

// Creates the service for the given session. With a framework integration the service is usually obtained via
// dependency injection instead.
@@static
NftService createService(session: Session)
```

## Questions & Comments

- The `supplyAuthority` parameters of `createNftType` accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled supply keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.

We should have immutable data objects as input params for the "create" methods.
That would allow full flexibility and we only need 1 method with such data object + a hand full of convenience methods.
The following code shows how it could look like in Java:


```
NonFungibleTokenDef def = NonFungibleTokenDef.builder()
    .name("Nft Tkens")
    .symbol("NT")
    .metadata(bytes)
    .supplyKey(minterKey)
    .freezeKey(freezeKey)
    .pauseKey(pasueKey)
    .build();

NftMetadata meta = nftClient.createNftType(def);
```
