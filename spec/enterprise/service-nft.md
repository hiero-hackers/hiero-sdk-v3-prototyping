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
    @@immutable @@maxBytes(100) name: string
    @@immutable @@maxBytes(100) symbol: string
    // The account that receives the initial supply and every mint; the operator account if absent.
    @@immutable @@nullable treasuryAccountId: AccountId
    // INFINITE or FINITE; cannot be changed later.
    @@immutable @@default(INFINITE) supplyType: TokenSupplyType
    // The highest total supply; only for a FINITE supply type.
    @@immutable @@nullable @@min(1) maxSupply: int64
    // When true, newly associated accounts start frozen; requires a freeze authority.
    @@immutable @@default(false) freezeDefault: bool
    @@immutable @@nullable @@maxBytes(100) tokenMemo: string
    // Opaque metadata of the token (e.g. an IPFS CID, a URL or a JSON manifest).
    @@immutable @@default([]) @@maxSize(100) metadata: bytes
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
    @@throws(service-error) Nft mintNft(tokenId: Address, @@maxSize(100) metadata: bytes)

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
value that violates a validation annotation (`@@maxBytes`, `@@min`) is rejected with `invalid-argument-error`.

```tests
// Source: this spec (@@default and @@nullable of NftTypeCreateParams).
@@testId("nft-type-parameters-with-only-name-and-symbol-have-the-defaults-of-the-spec")
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
// set is read back unchanged. The expiration time and the auto-renew period are those of the Java test
// (spawnTestTransactionNft: Instant.ofEpochSecond(1554158542) and Duration.ofHours(10), converted exactly to
// 2019-04-01T22:42:22Z and 36000 seconds).
@@testId("nft-type-parameters-keep-the-values-they-are-created-with")
test "NFT type parameters keep the values they are created with" {
    // given
    let authority = DEFAULT(Authority)
    let treasury = DEFAULT(AccountId)
    let metadata = utf8(text: "ipfs://bafkreigh2akiscaildc")
    let expirationTime = toZonedDateTime(text: "2019-04-01T22:42:22Z")
    let autoRenewPeriod = toSeconds(value: 36000)

    // when
    let params = NftTypeCreateParams{name: "Nft Tokens", symbol: "NT", treasuryAccountId: treasury,
            supplyType: TokenSupplyType.FINITE, maxSupply: 10, freezeDefault: true, tokenMemo: "memo",
            metadata: metadata, expirationTime: expirationTime, autoRenewPeriod: autoRenewPeriod,
            autoRenewAccount: treasury, adminAuthority: authority, supplyAuthority: authority, kycAuthority: authority,
            freezeAuthority: authority, wipeAuthority: authority, pauseAuthority: authority,
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
    assertEquals(expected: expirationTime, actual: params.expirationTime)
    assertEquals(expected: autoRenewPeriod, actual: params.autoRenewPeriod)
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

// Source: this spec (@@maxBytes(100) of name, symbol and tokenMemo); HAPI limits them to 100 bytes, which ASCII text
// of 100 characters is.
@@testId("an-nft-type-accepts-a-name-a-symbol-and-a-memo-of-100-characters")
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
@@testId("a-finite-nft-type-accepts-a-max-supply-of-1")
test "a finite NFT type accepts a max supply of 1" {
    // when
    let params = NftTypeCreateParams{name: "Nft Tokens", symbol: "NT", supplyType: TokenSupplyType.FINITE,
            maxSupply: 1}

    // then
    assertEquals(expected: TokenSupplyType.FINITE, actual: params.supplyType)
    assertEquals(expected: 1, actual: params.maxSupply)
}

// Source: this spec (@@maxBytes(100) of name); HAPI limits a token name to 100 bytes.
@@testId("nft-type-parameters-reject-a-name-of-101-characters")
test "NFT type parameters reject a name of 101 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NftTypeCreateParams{name: repeat(value: "a", count: 101), symbol: "NT"}
}

// Source: this spec (@@maxBytes(100) of symbol); HAPI limits a token symbol to 100 bytes.
@@testId("nft-type-parameters-reject-a-symbol-of-101-characters")
test "NFT type parameters reject a symbol of 101 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NftTypeCreateParams{name: "Nft Tokens", symbol: repeat(value: "a", count: 101)}
}

// Source: this spec (@@maxBytes(100) of tokenMemo); HAPI limits a memo to 100 bytes.
@@testId("nft-type-parameters-reject-a-memo-of-101-characters")
test "NFT type parameters reject a memo of 101 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NftTypeCreateParams{name: "Nft Tokens", symbol: "NT", tokenMemo: repeat(value: "a", count: 101)}
}

// Source: this spec (@@min(1) of maxSupply).
@@testId("a-finite-nft-type-rejects-a-max-supply-of-0")
test "a finite NFT type rejects a max supply of 0" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NftTypeCreateParams{name: "Nft Tokens", symbol: "NT", supplyType: TokenSupplyType.FINITE, maxSupply: 0}
}

// Source: this spec (@@maxSize(100) of metadata); HIP-646 and the consensus node (tokens.maxMetadataBytes = 100).
@@testId("nft-type-parameters-reject-metadata-longer-than-100-bytes")
test "NFT type parameters reject metadata longer than 100 bytes" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NftTypeCreateParams{name: "Nft Tokens", symbol: "NT", metadata: repeat(value: hex(text: "78"), count: 101)}
}
```

## Questions & Comments

- The `supplyAuthority` parameters of `createNftType` accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled supply keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.
- **`burnNfts(nfts)` is not atomic.** The NFTs of the set can belong to different NFT types, and a `TokenBurn` burns
  serials of one token only, so the Java implementation sends one transaction per NFT type. If one of them fails, the
  NFTs of the earlier types are already burned. Should the method require NFTs of a single type, use an atomic batch
  (HIP-551), or document the partial outcome?

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
