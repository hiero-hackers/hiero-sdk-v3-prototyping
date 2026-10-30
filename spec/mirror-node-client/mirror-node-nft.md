# Mirror Node NFT Query API

## Description

Query NFTs and NFT types (collections) from the Mirror Node. Use the `NftRepository` (available as
`MirrorNodeClient.nft`) to find NFTs by owner, by type or by type and serial number, to read the transfer history of
an NFT, and to look up the `NftMetadata` (name, symbol and treasury account) of NFT types. An individual `Nft` is
identified by the token id of its type plus its serial number and carries its owner, its spenders and its metadata
bytes. A burned NFT has no owner.

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **Required attributes where the REST API declares none.** The schema `Nft` has no `required` list. `tokenId`,
  `serialNumber` and `deleted` are always set by the mirror node and are required here; every other attribute is
  `@@nullable`.
- **Owners are passed as `AccountId`.** The path parameter `idOrAliasOrEvmAddress` of `GET /accounts/{id}/nfts` also
  accepts an alias or an EVM address; the repository methods take an `AccountId` only.
- **`NftTransactionTransfer.transactionId` is a `TransactionId`.** The REST API returns the transaction id as text
  (`0.0.19789-1618591023-997420021`) and the nonce separately; the `TransactionId` holds both. `nonce` is kept as an
  attribute as well.
- **The type of a transaction is a `TransactionType`** (of `mirrornode.transaction`), the REST value is its
  `protocolName`.
- **`NftMetadata` is no REST schema.** It is the part of a `TokenInfo` (`GET /api/v1/tokens/{tokenId}`) that
  describes an NFT type; `findAllTypes` and `findTypesByOwner` list the NFT types with `GET /api/v1/tokens` and read
  the `TokenInfo` of each type, because the token list has no treasury account.
- **`findByOwnerAndType(ownerId, tokenId)` is a shortcut** for the overload of `findByOwner` with all options and a
  single `token.id`.
- **`links` is the `Page`.** The pagination links of a list response are not an attribute; `Page.next()` follows
  them.

## API Schema

```
namespace mirrornode.nft
requires {Address, AccountId, MirrorNode, TransactionId} from ledger
requires {Page} from common
requires {Order} from mirrornode.common
requires {TransactionType} from mirrornode.transaction

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
    @@immutable serialNumber: int64
    @@immutable @@nullable accountId: AccountId                 // the owner; null for a burned NFT
    @@immutable @@nullable metadata: bytes
    @@immutable @@nullable createdTimestamp: zonedDateTime
    @@immutable @@nullable modifiedTimestamp: zonedDateTime
    @@immutable @@nullable delegatingSpender: AccountId         // the account that granted the spender its allowance
    @@immutable @@nullable spender: AccountId                   // the account allowed to transfer the NFT
    @@immutable deleted: bool                                   // whether the NFT or its token has been deleted
}

// One transfer of an NFT (including its mint and its burn) in the transfer history of the NFT.
@@finalType
NftTransactionTransfer {
    @@immutable consensusTimestamp: zonedDateTime
    @@immutable isApproval: bool                                // whether the transfer used an allowance
    @@immutable @@min(0) nonce: int32
    @@immutable @@nullable receiverAccountId: AccountId         // null for a burn
    @@immutable @@nullable senderAccountId: AccountId           // null for a mint
    @@immutable transactionId: TransactionId
    @@immutable type: TransactionType
}

abstraction NftRepository {
    // The NFTs owned by the account. Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/nfts.
    @@async @@throws(mirror-node-error)
    Page<Nft> findByOwner(ownerId: AccountId)

    // The NFTs owned by the account, filtered by serial number, spender and token. A `serialNumber` condition needs
    // a single `tokenId`. Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/nfts.
    @@async @@throws(mirror-node-error)
    Page<Nft> findByOwner(ownerId: AccountId, @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order,
            serialNumber: list<string>, spenderId: list<string>, tokenId: list<string>)

    // The NFTs of the type. Maps to GET /api/v1/tokens/{tokenId}/nfts.
    @@async @@throws(mirror-node-error)
    Page<Nft> findByType(tokenId: Address)

    // The NFTs of the type, filtered by owner and serial number. Maps to GET /api/v1/tokens/{tokenId}/nfts.
    @@async @@throws(mirror-node-error)
    Page<Nft> findByType(tokenId: Address, accountId: list<string>, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, serialNumber: list<string>)

    // The NFT, or null if the mirror node does not know it.
    // Maps to GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}.
    @@async @@throws(mirror-node-error)
    @@nullable Nft findByTypeAndSerial(tokenId: Address, @@min(1) serialNumber: int64)

    // The NFTs of the type owned by the account. Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/nfts.
    @@async @@throws(mirror-node-error)
    Page<Nft> findByOwnerAndType(ownerId: AccountId, tokenId: Address)

    // The transfer history of the NFT. Maps to GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}/transactions.
    @@async @@throws(mirror-node-error)
    Page<NftTransactionTransfer> findTransactionHistory(tokenId: Address, @@min(1) serialNumber: int64)

    // The transfer history of the NFT in the given period.
    // Maps to GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}/transactions.
    @@async @@throws(mirror-node-error)
    Page<NftTransactionTransfer> findTransactionHistory(tokenId: Address, @@min(1) serialNumber: int64,
            @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order, timestamp: list<string>)

    // The NFT types of the network. Maps to GET /api/v1/tokens (type NON_FUNGIBLE_UNIQUE) and
    // GET /api/v1/tokens/{tokenId} per type.
    @@async @@throws(mirror-node-error)
    Page<NftMetadata> findAllTypes()

    // The NFT types associated with the account. Maps to GET /api/v1/tokens (type NON_FUNGIBLE_UNIQUE) and
    // GET /api/v1/tokens/{tokenId} per type.
    @@async @@throws(mirror-node-error)
    Page<NftMetadata> findTypesByOwner(ownerId: AccountId)

    // The metadata of the NFT type, or null if the token is unknown or no NFT type.
    // Maps to GET /api/v1/tokens/{tokenId}.
    @@async @@throws(mirror-node-error)
    @@nullable NftMetadata getNftMetadata(tokenId: Address)
}

@@static NftRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `NftRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: this spec (an NFT is identified by the token id of its type plus its serial number).
@@testId("nfts-of-the-same-type-with-different-serial-numbers-are-different")
test "NFTs of the same type with different serial numbers are different" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let owner = DEFAULT(AccountId)
    let metadata = utf8(text: "metadata")

    // when
    let first = Nft{tokenId: token, serialNumber: 1, accountId: owner, metadata: metadata, deleted: false}
    let second = Nft{tokenId: token, serialNumber: 2, accountId: owner, metadata: metadata, deleted: false}

    // then
    assertEquals(expected: token, actual: first.tokenId)
    assertEquals(expected: 1, actual: first.serialNumber)
    assertEquals(expected: metadata, actual: first.metadata)
    assertEquals(expected: Nft{tokenId: token, serialNumber: 1, accountId: owner, metadata: metadata, deleted: false},
            actual: first)
    assertNotEquals(unexpected: first, actual: second)
}

// Source: the mirror node REST API (`Nft.account_id` is a nullable `EntityId`: a burned NFT has no owner).
@@testId("a-burned-nft-has-no-owner")
test "a burned NFT has no owner" {
    // when
    let burned = Nft{tokenId: Address.fromString(address: "0.0.5005"), serialNumber: 1, deleted: true}

    // then
    assertNull(actual: burned.accountId)
    assertNull(actual: burned.spender)
    assertTrue(condition: burned.deleted)
}
```

## Questions & Comments
