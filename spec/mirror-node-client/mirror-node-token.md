# Mirror Node Token Query API

## Description

Query fungible and non-fungible tokens from the Mirror Node. Use the `TokenRepository` (available as
`MirrorNodeClient.token`) to look up the full `TokenInfo` of a token, to list the tokens associated with an account,
and to read token balances. `TokenInfo` includes the token's supply settings and its custom fees, grouped in
`CustomFee` into fixed, fractional and royalty fees. Balances are given in the token's smallest unit together with
the token's number of decimals.

## API Schema

```
namespace mirrornode.token
requires {Address, AccountId, MirrorNode} from ledger
requires {FixedFee} from mirrornode.common
requires {Page} from common
requires {TokenType, TokenSupplyType} from token

@@finalType
Balance {
    @@immutable accountId: AccountId
    @@immutable balance: int64
    @@immutable decimals: int64
}

@@finalType
RoyaltyFee {
    @@immutable numeratorAmount: int64
    @@immutable denominatorAmount: int64
    @@immutable fallbackFeeAmount: int64
    @@immutable @@nullable collectorAccountId: AccountId
    @@immutable @@nullable denominatingTokenId: Address
}

@@finalType
FractionalFee {
    @@immutable numeratorAmount: int64
    @@immutable denominatorAmount: int64
    @@immutable @@nullable collectorAccountId: AccountId
    @@immutable @@nullable denominatingTokenId: Address
}


@@finalType
CustomFee {
    @@immutable fixedFees: list<FixedFee>
    @@immutable fractionalFees: list<FractionalFee>
    @@immutable royaltyFees: list<RoyaltyFee>
}

@@finalType
TokenInfo {
    @@immutable tokenId: Address
    @@immutable type: TokenType
    @@immutable name: string
    @@immutable symbol: string
    @@immutable @@nullable memo: string
    @@immutable decimals: int64
    @@immutable metadata: bytes
    @@immutable createdTimestamp: zonedDateTime
    @@immutable modifiedTimestamp: zonedDateTime
    @@immutable @@nullable expiryTimestamp: zonedDateTime
    @@immutable supplyType: TokenSupplyType
    @@immutable initialSupply: int256
    @@immutable totalSupply: int256
    @@immutable maxSupply: int256
    @@immutable treasuryAccountId: AccountId
    @@immutable deleted: bool
    @@immutable customFees: CustomFee
}

@@finalType
Token {
    @@immutable @@nullable tokenId: Address
    @@immutable name: string
    @@immutable symbol: string
    @@immutable type: TokenType
    @@immutable decimals: int64
    @@immutable metadata: bytes
}


abstraction TokenRepository {
    @@async @@throws(mirror-node-error)
    Page<Token> findByAccount(accountId: AccountId)

    @@async @@throws(mirror-node-error)
    @@nullable TokenInfo findById(tokenId: Address)

    @@async @@throws(mirror-node-error)
    Page<Balance> getBalances(tokenId: Address)

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
    let fractional = FractionalFee{numeratorAmount: 4, denominatorAmount: 5, collectorAccountId: collector}
    let royalty = RoyaltyFee{numeratorAmount: 4, denominatorAmount: 5, fallbackFeeAmount: 6,
            collectorAccountId: collector}

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
            treasuryAccountId: AccountId.fromString(value: "0.0.1245"), deleted: true, customFees: fees}

    // then
    assertEquals(expected: toZonedDateTime(text: "2024-02-01T19:36:26.275Z"), actual: info.createdTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2024-02-01T19:40:44.138Z"), actual: info.modifiedTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2024-04-22T20:03:05.275Z"), actual: info.expiryTimestamp)
    assertEquals(expected: TokenType.FUNGIBLE_COMMON, actual: info.type)
    assertEquals(expected: TokenSupplyType.INFINITE, actual: info.supplyType)
    assertEquals(expected: 1000000, actual: info.initialSupply)
    assertEquals(expected: AccountId.fromString(value: "0.0.1245"), actual: info.treasuryAccountId)
    assertTrue(condition: info.deleted)
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
            customFees: CustomFee{fixedFees: [], fractionalFees: [], royaltyFees: []}}

    // then
    assertNull(actual: info.expiryTimestamp)
    assertNull(actual: info.memo)
    assertEquals(expected: time, actual: info.createdTimestamp)
    assertEquals(expected: time, actual: info.modifiedTimestamp)
}
```

## Questions & Comments
