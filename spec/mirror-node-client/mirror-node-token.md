# Mirror Node Token Query API

## Description

Query fungible and non-fungible tokens from the Mirror Node. Use the `TokenRepository` (available as
`MirrorNodeClient.token`) to list the tokens of the network, to look up the full `TokenInfo` of a token, to list the
tokens associated with an account, and to read token balances. `TokenInfo` includes the token's keys, its supply
settings and its custom fees, grouped in `CustomFee` into fixed, fractional and royalty fees. Balances are given in
the token's smallest unit together with the token's number of decimals.

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **Required attributes where the REST API declares none.** The schemas `TokenInfo`, `CustomFees`, `FractionalFee`
  and `RoyaltyFee` have no `required` list. An attribute that the mirror node always sets (the ids, names, supplies,
  timestamps and flags of a token, the amounts of a fee) is required here; an attribute whose REST type admits `null`
  (`[x, "null"]`, a `Key`, an `EntityId` of an optional entity) is `@@nullable`. `TokenInfo.memo` is `@@nullable`
  as well.
- **Keys are `Authority`.** `admin_key`, `fee_schedule_key`, `freeze_key`, `kyc_key`, `metadata_key`, `pause_key`,
  `supply_key` and `wipe_key` (a `Key` with `_type` and `key`) are `Authority` (ADR-0004).
- **Typed numbers.** `TokenInfo.decimals` is a string in the REST API and an `int64` here; `initial_supply`,
  `max_supply` and `total_supply` are strings in the REST API and `int256` here. `auto_renew_period` (seconds) is
  `seconds`.
- **`expiry_timestamp` is a `zonedDateTime`.** The REST API gives it in nanoseconds since the epoch, unlike the
  other timestamps (`seconds.nanoseconds`).
- **Typed enums.** `type` is a `TokenType`, `supply_type` a `TokenSupplyType` (both of `token`), `pause_status` a
  `TokenPauseStatus`.
- **`custom_fees` is `CustomFee`.** The REST schema is named `CustomFees`; a token without custom fees has three empty
  fee lists.
- **The `amount` of a fractional and a royalty fee is flattened.** `amount.numerator` is `numeratorAmount`,
  `amount.denominator` is `denominatorAmount`.
- **The `fallback_fee` of a royalty fee is flattened.** `fallback_fee.amount` is `RoyaltyFee.fallbackFeeAmount`,
  `fallback_fee.denominating_token_id` is `RoyaltyFee.denominatingTokenId`; both are `null` without a fallback fee.
- **`Balance.account` is an `AccountId`.**
- **The `timestamp` of `GET /tokens/{tokenId}/balances` is left out.** It belongs to the whole response, not to one
  `Balance`, and a `Page` has no place for it.
- **The query parameter `type` of `GET /tokens` is a `list<TokenType>`.** The REST value `ALL` is not offered: an
  empty list (all types) means the same.
- **`findByAccount(accountId)` and `getBalancesForAccount(tokenId, accountId)` are shortcuts** for the overloads with
  all options and a single `account.id`.
- **`links` is the `Page`.** The pagination links of a list response are not an attribute; `Page.next()` follows
  them.

## API Schema

```
namespace mirrornode.token
requires {Address, AccountId, MirrorNode} from ledger
requires {Authority} from authority
requires {FixedFee, Order} from mirrornode.common
requires {Page} from common
requires {TokenType, TokenSupplyType} from token

// The balance of a token held by one account, in the token's smallest unit.
@@finalType
Balance {
    @@immutable account: AccountId
    @@immutable balance: int64
    @@immutable @@nullable decimals: int64      // the number of decimals of the token
}

// A fee that charges a fraction of the NFT exchange value, with an optional fixed fallback fee if no value is
// exchanged.
@@finalType
RoyaltyFee {
    @@immutable numeratorAmount: int64
    @@immutable denominatorAmount: int64
    @@immutable @@nullable fallbackFeeAmount: int64             // the amount of the fallback fee; null without one
    @@immutable @@nullable collectorAccountId: AccountId
    @@immutable @@nullable denominatingTokenId: Address         // the token of the fallback fee; null if it is charged in HBAR or there is none
    @@immutable allCollectorsAreExempt: bool                    // whether all fee collectors are exempt from the fee
}

// A fee that charges a fraction of the transferred units of a fungible token.
@@finalType
FractionalFee {
    @@immutable numeratorAmount: int64
    @@immutable denominatorAmount: int64
    @@immutable @@nullable collectorAccountId: AccountId
    @@immutable @@nullable denominatingTokenId: Address
    @@immutable allCollectorsAreExempt: bool                    // whether all fee collectors are exempt from the fee
    @@immutable @@nullable maximum: int64                       // the maximum fee; null if there is none
    @@immutable minimum: int64                                  // the minimum fee
    @@immutable netOfTransfers: bool                            // whether the fee is charged to the receiver instead of the sender
}

// The custom fees of a token, grouped by kind.
@@finalType
CustomFee {
    @@immutable @@default([]) fixedFees: list<FixedFee>
    @@immutable @@default([]) fractionalFees: list<FractionalFee>
    @@immutable @@default([]) royaltyFees: list<RoyaltyFee>
    @@immutable @@nullable createdTimestamp: zonedDateTime      // consensus time at which the fees were set
}

// Whether a token is paused.
enum TokenPauseStatus {
    NOT_APPLICABLE  // the token has no pause key
    PAUSED
    UNPAUSED
}

// The full information of a token.
@@finalType
TokenInfo {
    @@immutable tokenId: Address
    @@immutable type: TokenType
    @@immutable name: string
    @@immutable symbol: string
    @@immutable @@nullable memo: string
    @@immutable decimals: int64
    @@immutable @@nullable metadata: bytes
    @@immutable createdTimestamp: zonedDateTime
    @@immutable modifiedTimestamp: zonedDateTime
    @@immutable @@nullable expiryTimestamp: zonedDateTime
    @@immutable supplyType: TokenSupplyType
    @@immutable initialSupply: int256
    @@immutable totalSupply: int256
    @@immutable maxSupply: int256
    @@immutable treasuryAccountId: AccountId
    @@immutable @@nullable deleted: bool
    @@immutable customFees: CustomFee
    @@immutable @@nullable adminKey: Authority
    @@immutable @@nullable autoRenewAccount: AccountId
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable feeScheduleKey: Authority
    @@immutable freezeDefault: bool                             // whether accounts are frozen for the token by default
    @@immutable @@nullable freezeKey: Authority
    @@immutable @@nullable kycKey: Authority
    @@immutable @@nullable metadataKey: Authority               // the authority that may change the metadata of the token and of its NFTs
    @@immutable @@nullable pauseKey: Authority
    @@immutable pauseStatus: TokenPauseStatus
    @@immutable @@nullable supplyKey: Authority
    @@immutable @@nullable wipeKey: Authority
}

// A token as listed by the token list.
@@finalType
Token {
    @@immutable @@nullable tokenId: Address
    @@immutable name: string
    @@immutable symbol: string
    @@immutable type: TokenType
    @@immutable decimals: int64
    @@immutable @@nullable metadata: bytes
    @@immutable @@nullable adminKey: Authority
}

abstraction TokenRepository {
    // The tokens of the network. Maps to GET /api/v1/tokens.
    @@async @@throws(mirror-node-error)
    Page<Token> findAll()

    // The tokens of the network, filtered by associated account, name, public key, id and type. `name` (a partial or
    // full name) cannot be combined with `accountId` or `tokenId`, and its results are not paginated.
    // Maps to GET /api/v1/tokens.
    @@async @@throws(mirror-node-error)
    Page<Token> findAll(accountId: list<string>, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable @@minLength(3) @@maxLength(100) name: string, @@nullable order: Order,
            @@nullable publicKey: string, tokenId: list<string>, type: list<TokenType>)

    // The tokens associated with the account. Maps to GET /api/v1/tokens.
    @@async @@throws(mirror-node-error)
    Page<Token> findByAccount(accountId: AccountId)

    // The token, or null if the mirror node does not know it. Maps to GET /api/v1/tokens/{tokenId}.
    @@async @@throws(mirror-node-error)
    @@nullable TokenInfo findById(tokenId: Address)

    // The token as it was at the given consensus time (operators eq, lt and lte), or null if the mirror node does not
    // know it. Maps to GET /api/v1/tokens/{tokenId}.
    @@async @@throws(mirror-node-error)
    @@nullable TokenInfo findById(tokenId: Address, timestamp: list<string>)

    // The balances of the token held by the accounts. Maps to GET /api/v1/tokens/{tokenId}/balances.
    @@async @@throws(mirror-node-error)
    Page<Balance> getBalances(tokenId: Address)

    // The balances of the token, filtered by balance, account and public key, at the given consensus time.
    // Maps to GET /api/v1/tokens/{tokenId}/balances.
    @@async @@throws(mirror-node-error)
    Page<Balance> getBalances(tokenId: Address, accountBalance: list<string>, accountId: list<string>,
            @@nullable accountPublicKey: string, @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order,
            timestamp: list<string>)

    // The balance of the token held by the account. Maps to GET /api/v1/tokens/{tokenId}/balances.
    @@async @@throws(mirror-node-error)
    Page<Balance> getBalancesForAccount(tokenId: Address, accountId: AccountId)
}

@@static TokenRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `TokenRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: the Java SDK (CustomFixedFeeTest, CustomFractionalFeeTest and CustomRoyaltyFeeTest: collector 1.2.3, amount
// 4, fraction 4/5, fallback fee 6); this spec (CustomFee groups the fees into fixed, fractional and royalty fees).
@@testId("custom-fees-keep-their-fixed-fractional-and-royalty-fees")
test "custom fees keep their fixed, fractional and royalty fees" {
    // given
    let collector = AccountId.fromString(value: "1.2.3")
    let fixed = FixedFee{amount: 4, collectorAccountId: collector}
    let fractional = FractionalFee{numeratorAmount: 4, denominatorAmount: 5, collectorAccountId: collector,
            allCollectorsAreExempt: false, minimum: 0, netOfTransfers: false}
    let royalty = RoyaltyFee{numeratorAmount: 4, denominatorAmount: 5, fallbackFeeAmount: 6,
            collectorAccountId: collector, allCollectorsAreExempt: false}

    // when
    let fees = CustomFee{fixedFees: [fixed], fractionalFees: [fractional], royaltyFees: [royalty]}

    // then
    assertEquals(expected: [fixed], actual: fees.fixedFees)
    assertEquals(expected: [fractional], actual: fees.fractionalFees)
    assertEquals(expected: [royalty], actual: fees.royaltyFees)
    assertEquals(expected: 4, actual: fees.fractionalFees[0].numeratorAmount)
    assertEquals(expected: 5, actual: fees.fractionalFees[0].denominatorAmount)
    assertEquals(expected: 6, actual: fees.royaltyFees[0].fallbackFeeAmount)
    assertNull(actual: fees.royaltyFees[0].denominatingTokenId)
}

// Source: the API guideline (a collection is never null: a token without custom fees has empty fee lists).
@@testId("custom-fees-without-fees-have-three-empty-lists")
test "custom fees without fees have three empty lists" {
    // when
    let fees = CustomFee{fixedFees: [], fractionalFees: [], royaltyFees: []}

    // then
    assertEquals(expected: 0, actual: length(value: fees.fixedFees))
    assertEquals(expected: 0, actual: length(value: fees.fractionalFees))
    assertEquals(expected: 0, actual: length(value: fees.royaltyFees))
}

// Source: the Hiero Enterprise Java test fixture token-info.json (token 0.0.1267, fungible, infinite supply, 10
// decimals, initial supply 1000000, treasury 0.0.1245, deleted, created "1706816186.275486248", modified
// "1706816444.138081003", expiry 1713816185275486248 ns); millisecond precision.
@@testId("a-token-info-keeps-its-created-modified-and-expiry-timestamps")
test "a token info keeps its created, modified and expiry timestamps" {
    // given
    let created = toZonedDateTime(text: "2024-02-01T19:36:26.275Z")
    let modified = toZonedDateTime(text: "2024-02-01T19:40:44.138Z")
    let expiry = toZonedDateTime(text: "2024-04-22T20:03:05.275Z")
    let fees = CustomFee{fixedFees: [], fractionalFees: [], royaltyFees: []}

    // when
    let info = TokenInfo{tokenId: Address.fromString(address: "0.0.1267"), type: TokenType.FUNGIBLE_COMMON,
            name: "fungible_name", symbol: "fungible", decimals: 10, metadata: [], createdTimestamp: created,
            modifiedTimestamp: modified, expiryTimestamp: expiry, supplyType: TokenSupplyType.INFINITE,
            initialSupply: 1000000, totalSupply: 0, maxSupply: 0,
            treasuryAccountId: AccountId.fromString(value: "0.0.1245"), deleted: true, customFees: fees,
            freezeDefault: false, pauseStatus: TokenPauseStatus.NOT_APPLICABLE}

    // then
    assertEquals(expected: toZonedDateTime(text: "2024-02-01T19:36:26.275Z"), actual: info.createdTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2024-02-01T19:40:44.138Z"), actual: info.modifiedTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2024-04-22T20:03:05.275Z"), actual: info.expiryTimestamp)
    assertEquals(expected: TokenType.FUNGIBLE_COMMON, actual: info.type)
    assertEquals(expected: TokenSupplyType.INFINITE, actual: info.supplyType)
    assertEquals(expected: 1000000, actual: info.initialSupply)
    assertEquals(expected: AccountId.fromString(value: "0.0.1245"), actual: info.treasuryAccountId)
    assertEquals(expected: true, actual: info.deleted)
    assertEquals(expected: fees, actual: info.customFees)
}

// Source: this spec (`expiryTimestamp` and `memo` are nullable).
@@testId("a-token-info-without-expiry-has-no-expiry-timestamp-and-no-memo")
test "a token info without expiry has no expiry timestamp and no memo" {
    // given
    let time = DEFAULT(zonedDateTime)

    // when
    let info = TokenInfo{tokenId: Address.fromString(address: "0.0.1267"), type: TokenType.NON_FUNGIBLE_UNIQUE,
            name: "nft_name", symbol: "nft", decimals: 0, metadata: [], createdTimestamp: time,
            modifiedTimestamp: time, supplyType: TokenSupplyType.FINITE, initialSupply: 0, totalSupply: 0,
            maxSupply: 100, treasuryAccountId: DEFAULT(AccountId), deleted: false,
            customFees: CustomFee{fixedFees: [], fractionalFees: [], royaltyFees: []}, freezeDefault: false,
            pauseStatus: TokenPauseStatus.NOT_APPLICABLE}

    // then
    assertNull(actual: info.expiryTimestamp)
    assertNull(actual: info.memo)
    assertEquals(expected: time, actual: info.createdTimestamp)
    assertEquals(expected: time, actual: info.modifiedTimestamp)
}
```

## Questions & Comments

- **Response-level values of a list.** `GET /tokens/{tokenId}/balances` returns the consensus `timestamp` of the
  balances next to the list; a `Page` has no place for it, so it is left out. Should `Page` (or a page type of the
  mirror node client) carry such values?
