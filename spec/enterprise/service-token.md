# Fungible Token Service API

## Description

`FungibleTokenService` creates and manages fungible tokens (the Token Service): creation, account association,
minting, burning, transfers and lookups of token information and balances. Tokens are identified by an `Address`
(shard.realm.num), accounts by an `AccountId`. Amounts are given in the token's smallest unit. A token that should
support minting and burning later must be created with a supply authority.

## API Schema

```
namespace enterprise.service.token
requires {Page} from common
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {Token, TokenInfo, Balance} from mirrornode.token
requires {Session} from enterprise.service
requires {TokenSupplyType} from token

// The parameters of a new fungible token: everything that can be set when it is created. Name and symbol are
// required.
@@finalType
TokenCreateParams {
    @@immutable @@maxLength(100) name: string
    @@immutable @@maxLength(100) symbol: string
    // The account that receives the initial supply and every mint; the operator account if absent.
    @@immutable @@nullable treasuryAccountId: AccountId
    // The number of decimal places of the smallest unit (e.g. 6 for a currency like USDC).
    @@immutable @@default(0) decimals: int32
    // The amount minted to the treasury when the token is created, in the smallest unit.
    @@immutable @@default(0) initialSupply: int64
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

FungibleTokenService {

    // Create a fungible token and return its id. The treasury defaults to the operator account; a supply authority
    // enables minting and burning. The same as createToken with TokenCreateParams that have only these values.
    @@throws(service-error) Address createToken(name: string, symbol: string)

    @@throws(service-error) Address createToken(name: string, symbol: string, supplyAuthority: Authority)

    @@throws(service-error) Address createToken(name: string, symbol: string, treasuryAccount: AccountId, supplyAuthority: Authority)

    // Create a fungible token with the given parameters and return its id: the general form of the methods above,
    // for every option of a new token.
    @@throws(service-error) Address createToken(params: TokenCreateParams)

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

// Creates the service for the given session. With a framework integration the service is usually obtained via
// dependency injection instead.
@@static
FungibleTokenService createService(session: Session)
```

## Default Instances for Testing

```
// The parameters of a token with only the required name and symbol.
instance TokenCreateParams = TokenCreateParams{name: "Token Name", symbol: "TKN"}
```

## Testing

Tests of `TokenCreateParams`. The methods of `FungibleTokenService` talk to the network and are not unit-tested here.
A violated validation annotation (`@@maxLength`, `@@min`) cannot be expected yet: the test language expects errors of
calls only, and creating a `TokenCreateParams` declares none.

```tests
// Source: this spec (@@default and @@nullable of TokenCreateParams); the Python SDK
// (token_create_transaction_test.py, test_overwrite_defaults: decimals and initial supply are 0, no expiration time).
test "token parameters with only name and symbol have the defaults of the spec" {
    // when
    let params = DEFAULT(TokenCreateParams)

    // then
    assertEquals(expected: "Token Name", actual: params.name)
    assertEquals(expected: "TKN", actual: params.symbol)
    assertEquals(expected: 0, actual: params.decimals)
    assertEquals(expected: 0, actual: params.initialSupply)
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
// (token_create_transaction_unit_test.go), C++ (TokenCreateTransactionUnitTests), Swift
// (TokenCreateTransactionUnitTests) and Python (token_create_transaction_test.py) SDKs: every value that is set is
// read back unchanged. The expiration time and the auto-renew period are those of the Java test (spawnTestTransactionFungible:
// Instant.ofEpochSecond(1554158542) and Duration.ofHours(10), converted exactly to 2019-04-01T22:42:22Z and 36000
// seconds).
test "token parameters keep the values they are created with" {
    // given
    let authority = DEFAULT(Authority)
    let treasury = DEFAULT(AccountId)
    let metadata = utf8(text: "ipfs://bafkreigh2akiscaildc")
    let expirationTime = toZonedDateTime(text: "2019-04-01T22:42:22Z")
    let autoRenewPeriod = toSeconds(value: 36000)

    // when
    let params = TokenCreateParams{name: "USD Coin", symbol: "USDC", treasuryAccountId: treasury, decimals: 6,
            initialSupply: 1_000_000, supplyType: TokenSupplyType.FINITE, maxSupply: 10_000_000, freezeDefault: true,
            tokenMemo: "memo", metadata: metadata, expirationTime: expirationTime,
            autoRenewPeriod: autoRenewPeriod, autoRenewAccount: treasury, adminAuthority: authority,
            supplyAuthority: authority, kycAuthority: authority, freezeAuthority: authority,
            wipeAuthority: authority, pauseAuthority: authority, feeScheduleAuthority: authority,
            metadataAuthority: authority}

    // then
    assertEquals(expected: "USD Coin", actual: params.name)
    assertEquals(expected: "USDC", actual: params.symbol)
    assertEquals(expected: treasury, actual: params.treasuryAccountId)
    assertEquals(expected: 6, actual: params.decimals)
    assertEquals(expected: 1_000_000, actual: params.initialSupply)
    assertEquals(expected: TokenSupplyType.FINITE, actual: params.supplyType)
    assertEquals(expected: 10_000_000, actual: params.maxSupply)
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

// Source: this spec (@@maxLength(100) of name, symbol and tokenMemo); HAPI limits them to 100 bytes, which ASCII text
// of 100 characters is.
test "a name, a symbol and a memo of 100 characters are accepted" {
    // given
    let text = repeat(value: "a", count: 100)

    // when
    let params = TokenCreateParams{name: text, symbol: text, tokenMemo: text}

    // then
    assertEquals(expected: 100, actual: length(value: params.name))
    assertEquals(expected: 100, actual: length(value: params.symbol))
    assertEquals(expected: text, actual: params.tokenMemo)
}

// Source: this spec (@@min(1) of maxSupply, which applies to a FINITE supply type).
test "a finite token accepts a max supply of 1" {
    // when
    let params = TokenCreateParams{name: "Token Name", symbol: "TKN", supplyType: TokenSupplyType.FINITE,
            maxSupply: 1}

    // then
    assertEquals(expected: TokenSupplyType.FINITE, actual: params.supplyType)
    assertEquals(expected: 1, actual: params.maxSupply)
}
```

## Questions & Comments

- The `supplyAuthority` parameters of `createToken` accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled supply keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.
- **Limits in bytes, not in characters.** HAPI limits a token name, a symbol and a memo to 100 bytes of UTF-8, but
  `@@maxLength(100)` counts characters: a name of 100 non-ASCII characters passes the annotation and is rejected by the
  network. The tests therefore check the limit only with ASCII text. Should the meta-language get a byte-length
  annotation (e.g. `@@maxUtf8Bytes(100)`), or should the services check the byte length themselves?
- **Size of `metadata`.** HAPI limits the token metadata to 100 bytes (the Python SDK rejects more in
  `set_metadata`), but `metadata` has no `@@maxSize(100)`. Should it get one?

We need better "annotation" support to define ranges of params. For example:

```
@@throws(service-error) @@positiveValue int64 mintToken(tokenId: Address, @@positiveValue amount: int64)
```
Here `@@positiveValue` is added at 2 positions to define a better restriction.
