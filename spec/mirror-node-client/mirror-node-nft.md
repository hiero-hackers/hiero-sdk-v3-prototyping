# Mirror Node NFT Query API

## Description

Query NFTs and NFT types (collections) from the Mirror Node. Use the `NftRepository` (available as
`MirrorNodeClient.nft`) to find NFTs by owner, by type or by type and serial number, and to look up the
`NftMetadata` (name, symbol and treasury account) of NFT types. An individual `Nft` is identified by the token id of
its type plus its serial number and carries its owner and metadata bytes.

## API Schema

```
namespace mirrornode.nft
requires {Address, AccountId, MirrorNode} from ledger
requires {Page} from common

@@finalType
NftMetadata {
    @@immutable tokenId: Address
    @@immutable name: string
    @@immutable symbol: string
    @@immutable treasuryAccountId: AccountId
}

@@finalType
Nft {
    @@immutable tokenId: Address
    @@immutable serial: int64
    @@immutable owner: AccountId
    @@immutable metadata: bytes
}

abstraction NftRepository {
    @@async @@throws(mirror-node-error)
    Page<Nft> findByOwner(ownerId: AccountId)

    @@async @@throws(mirror-node-error)
    Page<Nft> findByType(tokenId: Address)

    @@async @@throws(mirror-node-error)
    @@nullable Nft findByTypeAndSerial(tokenId: Address, serialNumber: int64)

    @@async @@throws(mirror-node-error)
    Page<Nft> findByOwnerAndType(ownerId: AccountId, tokenId: Address)

    @@async @@throws(mirror-node-error)
    Page<NftMetadata> findAllTypes()

    @@async @@throws(mirror-node-error)
    Page<NftMetadata> findTypesByOwner(ownerId: AccountId)

    @@async @@throws(mirror-node-error)
    @@nullable NftMetadata getNftMetadata(tokenId: Address)
}

@@static NftRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `NftRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: this spec (an NFT is identified by the token id of its type plus its serial number).
test "NFTs of the same type with different serial numbers are different" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let owner = DEFAULT(AccountId)
    let metadata = utf8(text: "metadata")

    // when
    let first = Nft{tokenId: token, serial: 1, owner: owner, metadata: metadata}
    let second = Nft{tokenId: token, serial: 2, owner: owner, metadata: metadata}

    // then
    assertEquals(expected: token, actual: first.tokenId)
    assertEquals(expected: 1, actual: first.serial)
    assertEquals(expected: metadata, actual: first.metadata)
    assertEquals(expected: Nft{tokenId: token, serial: 1, owner: owner, metadata: metadata}, actual: first)
    assertNotEquals(unexpected: first, actual: second)
}
```

## Questions & Comments

- **NFTs without an owner cannot be represented.** `Nft.owner` is required, but the Mirror Node returns NFTs without
  an owner — a burned NFT has `account_id = null`. The Java implementation leaves such NFTs out of every result page, so
  a page can hold fewer entries than the REST response and a burned NFT cannot be looked up at all. Should `owner` be
  `@@nullable` (perhaps together with a `deleted` flag), or is leaving these NFTs out the intended behavior?
