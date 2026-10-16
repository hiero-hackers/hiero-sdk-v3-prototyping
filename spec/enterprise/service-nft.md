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
requires {TokenSupplyType} from token

// The parameters of a new NFT type (collection): everything that can be set when it is created. Name and symbol are
// required.
@@finalType
NftTypeCreateParams {
    @@immutable @@maxLength(100) name: string
    @@immutable @@maxLength(100) symbol: string
    // The account that receives the initial supply and every mint; the operator account if absent.
    @@immutable @@nullable treasuryAccountId: AccountId
    // INFINITE or FINITE; cannot be changed later.
    @@immutable @@default(INFINITE) supplyType: TokenSupplyType
    // The highest total supply; only for a FINITE supply type.
    @@immutable @@nullable @@min(1) maxSupply: int64
    // When true, newly associated accounts start frozen; requires a freeze authority.
    @@immutable @@default(false) freezeDefault: bool
    @@immutable @@nullable @@maxLength(100) tokenMemo: string
    // Opaque metadata of the token (e.g. an IPFS CID, a URL or a JSON manifest).
    @@immutable @@default([]) metadata: bytes
    // When the token expires; the network default if absent.
    @@immutable @@nullable expirationTime: zonedDateTime
    // The renewal period applied when the expiration approaches; the network default if absent.
    @@immutable @@nullable autoRenewPeriod: seconds
    // The account that pays for the renewal.
    @@immutable @@nullable autoRenewAccount: AccountId

    // The authorities of the token. An authority that is not set when the token is created cannot be added later.

    // Allows to update and delete the token; without it the token cannot be changed.
    @@immutable @@nullable adminAuthority: Authority
    // Allows to mint and burn; without it the supply is fixed after creation.
    @@immutable @@nullable supplyAuthority: Authority
    // Allows to grant and revoke KYC; without it KYC is disabled.
    @@immutable @@nullable kycAuthority: Authority
    // Allows to freeze and unfreeze accounts; without it accounts cannot be frozen.
    @@immutable @@nullable freezeAuthority: Authority
    // Allows to wipe the balances of accounts other than the treasury; without it wiping is disabled.
    @@immutable @@nullable wipeAuthority: Authority
    // Allows to pause and unpause the token; without it the token cannot be paused.
    @@immutable @@nullable pauseAuthority: Authority
    // Allows to update the custom fee schedule; without it the fees cannot be changed.
    @@immutable @@nullable feeScheduleAuthority: Authority
    // Allows to update the metadata of single tokens (HIP-657); without it that metadata cannot be changed.
    @@immutable @@nullable metadataAuthority: Authority
}

NftService {

    // Create a new NFT type (collection); returns the metadata of the new type, including its token id.
    // The treasury defaults to the operator account; a supply authority enables minting. The same as createNftType
    // with NftTypeCreateParams that have only these values.
    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string)

    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string, supplyAuthority: Authority)

    @@throws(service-error) NftMetadata createNftType(name: string, symbol: string, treasuryAccount: AccountId, supplyAuthority: Authority)

    // Create a new NFT type with the given parameters: the general form of the methods above, for every option of
    // a new NFT type.
    @@throws(service-error) NftMetadata createNftType(params: NftTypeCreateParams)

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

## Default Instances for Testing

```
// The parameters of an NFT type with only the required name and symbol.
instance NftTypeCreateParams = NftTypeCreateParams{name: "Nft Tokens", symbol: "NT"}
```

## Testing

Tests of `NftTypeCreateParams`. The methods of `NftService` talk to the network and are not unit-tested here. A
violated validation annotation (`@@maxLength`, `@@min`) cannot be expected yet: the test language expects errors of
calls only, and creating an `NftTypeCreateParams` declares none.

```tests
// Source: this spec (@@default and @@nullable of NftTypeCreateParams).
test "NFT type parameters with only name and symbol have the defaults of the spec" {
    // when
    let params = DEFAULT(NftTypeCreateParams)

    // then
    assertEquals(expected: "Nft Tokens", actual: params.name)
    assertEquals(expected: "NT", actual: params.symbol)
    assertEquals(expected: TokenSupplyType.INFINITE, actual: params.supplyType)
    assertFalse(condition: params.freezeDefault)
    assertEquals(expected: 0, actual: length(value: params.metadata))
    assertNull(actual: params.treasuryAccountId)
    assertNull(actual: params.maxSupply)
    assertNull(actual: params.tokenMemo)
    assertNull(actual: params.expirationTime)
    assertNull(actual: params.autoRenewPeriod)
    assertNull(actual: params.autoRenewAccount)
    assertNull(actual: params.adminAuthority)
    assertNull(actual: params.supplyAuthority)
    assertNull(actual: params.kycAuthority)
    assertNull(actual: params.freezeAuthority)
    assertNull(actual: params.wipeAuthority)
    assertNull(actual: params.pauseAuthority)
    assertNull(actual: params.feeScheduleAuthority)
    assertNull(actual: params.metadataAuthority)
}

// Source: the TokenCreateTransaction unit tests of the Java (TokenCreateTransactionTest), Go
// (token_create_transaction_unit_test.go), C++ (TokenCreateTransactionUnitTests, with NON_FUNGIBLE_UNIQUE and FINITE)
// and Python (token_create_transaction_test.py, test_build_transaction_body_non_fungible) SDKs: every value that is
// set is read back unchanged.
test "NFT type parameters keep the values they are created with" {
    // given
    let authority = DEFAULT(Authority)
    let treasury = DEFAULT(AccountId)
    let metadata = utf8(text: "ipfs://bafkreigh2akiscaildc")

    // when
    let params = NftTypeCreateParams{name: "Nft Tokens", symbol: "NT", treasuryAccountId: treasury,
            supplyType: TokenSupplyType.FINITE, maxSupply: 10, freezeDefault: true, tokenMemo: "memo",
            metadata: metadata, autoRenewAccount: treasury, adminAuthority: authority, supplyAuthority: authority,
            kycAuthority: authority, freezeAuthority: authority, wipeAuthority: authority, pauseAuthority: authority,
            feeScheduleAuthority: authority, metadataAuthority: authority}

    // then
    assertEquals(expected: "Nft Tokens", actual: params.name)
    assertEquals(expected: "NT", actual: params.symbol)
    assertEquals(expected: treasury, actual: params.treasuryAccountId)
    assertEquals(expected: TokenSupplyType.FINITE, actual: params.supplyType)
    assertEquals(expected: 10, actual: params.maxSupply)
    assertTrue(condition: params.freezeDefault)
    assertEquals(expected: "memo", actual: params.tokenMemo)
    assertEquals(expected: metadata, actual: params.metadata)
    assertEquals(expected: treasury, actual: params.autoRenewAccount)
    assertEquals(expected: authority, actual: params.adminAuthority)
    assertEquals(expected: authority, actual: params.supplyAuthority)
    assertEquals(expected: authority, actual: params.kycAuthority)
    assertEquals(expected: authority, actual: params.freezeAuthority)
    assertEquals(expected: authority, actual: params.wipeAuthority)
    assertEquals(expected: authority, actual: params.pauseAuthority)
    assertEquals(expected: authority, actual: params.feeScheduleAuthority)
    assertEquals(expected: authority, actual: params.metadataAuthority)
}

// Source: this spec (@@maxLength(100) of name, symbol and tokenMemo); HAPI limits them to 100 bytes, which ASCII text
// of 100 characters is.
test "an NFT type accepts a name, a symbol and a memo of 100 characters" {
    // given
    let text = repeat(value: "a", count: 100)

    // when
    let params = NftTypeCreateParams{name: text, symbol: text, tokenMemo: text}

    // then
    assertEquals(expected: 100, actual: length(value: params.name))
    assertEquals(expected: 100, actual: length(value: params.symbol))
    assertEquals(expected: text, actual: params.tokenMemo)
}

// Source: this spec (@@min(1) of maxSupply, which applies to a FINITE supply type).
test "a finite NFT type accepts a max supply of 1" {
    // when
    let params = NftTypeCreateParams{name: "Nft Tokens", symbol: "NT", supplyType: TokenSupplyType.FINITE,
            maxSupply: 1}

    // then
    assertEquals(expected: TokenSupplyType.FINITE, actual: params.supplyType)
    assertEquals(expected: 1, actual: params.maxSupply)
}
```

## Questions & Comments

- The `supplyAuthority` parameters of `createNftType` accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled supply keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.
- **Limits in bytes, not in characters.** HAPI limits an NFT type name, a symbol and a memo to 100 bytes of UTF-8, but
  `@@maxLength(100)` counts characters: a name of 100 non-ASCII characters passes the annotation and is rejected by the
  network. The tests therefore check the limit only with ASCII text. Should the meta-language get a byte-length
  annotation (e.g. `@@maxUtf8Bytes(100)`), or should the services check the byte length themselves?
- **Size of `metadata`.** HAPI limits the metadata of a token to 100 bytes (the Python SDK rejects more in
  `set_metadata`), but `metadata` has no `@@maxSize(100)`. Should it get one?

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
