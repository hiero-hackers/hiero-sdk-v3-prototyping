# Mirror Node Account Query API

## Description

Query accounts from the Mirror Node. Use the `AccountRepository` (available as `MirrorNodeClient.accounts`) to look
up an account (`findById`, `findAll`) and to read data related to it: the staking rewards it received, the HBAR,
fungible-token and NFT allowances it has granted, and its pending airdrops (HIP-904) — both the ones it sent that are
not yet claimed (outstanding) and the ones it can still claim (pending). `getBalances` returns a snapshot of account
balances across the whole network.

HBAR amounts are given in tinybars, token amounts in the token's smallest unit.

## API Schema

```
namespace mirrornode.account
requires {Address, AccountId, EvmAddress, MirrorNode} from ledger
requires {Authority} from authority
requires {Page} from common

@@finalType
AccountInfo {
    @@immutable accountId: AccountId
    @@immutable @@nullable evmAddress: EvmAddress
    @@immutable balance: int64                                       // hbar balance in tinybars
    @@immutable @@nullable balanceTimestamp: zonedDateTime           // timestamp of the reported balance
    @@immutable ethereumNonce: int64
    @@immutable pendingReward: int64                                 // tinybars receivable in the next staking payout
    @@immutable @@nullable alias: bytes                              // HIP-32 public-key alias (serialised protobuf Key bytes)
    @@immutable @@nullable authority: Authority
    @@immutable @@nullable accountMemo: string
    @@immutable @@nullable createdTimestamp: zonedDateTime
    @@immutable @@nullable expiryTimestamp: zonedDateTime
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable receiverSignatureRequired: bool
    @@immutable @@default(false) declineReward: bool
    @@immutable @@default(false) deleted: bool
    @@immutable @@nullable stakedAccountId: AccountId                // mutually exclusive with stakedNodeId
    @@immutable @@nullable stakedNodeId: int64                       // mutually exclusive with stakedAccountId
    @@immutable @@nullable stakePeriodStart: zonedDateTime
}

// A staking-reward payout received by the account. GET /accounts/{id}/rewards.
@@finalType
StakingReward {
    @@immutable accountId: AccountId
    @@immutable amount: int64                         // tinybars paid in this reward
    @@immutable timestamp: zonedDateTime              // consensus time of the payout
}

// An HBAR spending allowance the account granted. GET /accounts/{id}/allowances/crypto.
@@finalType
CryptoAllowance {
    @@immutable owner: AccountId
    @@immutable spender: AccountId
    @@immutable amount: int64                         // remaining approved tinybars
    @@immutable amountGranted: int64                  // originally approved tinybars
    @@immutable timestamp: zonedDateTime              // when the allowance was granted / last modified
}

// A fungible-token spending allowance the account granted. GET /accounts/{id}/allowances/tokens.
@@finalType
TokenAllowance {
    @@immutable owner: AccountId
    @@immutable spender: AccountId
    @@immutable tokenId: Address
    @@immutable amount: int64                         // remaining approved amount (smallest unit)
    @@immutable amountGranted: int64                  // originally approved amount
    @@immutable timestamp: zonedDateTime
}

// An NFT approved-for-all allowance the account granted. GET /accounts/{id}/allowances/nfts.
@@finalType
NftAllowance {
    @@immutable owner: AccountId
    @@immutable spender: AccountId
    @@immutable tokenId: Address
    @@immutable approvedForAll: bool                  // true → spender may transfer every serial of the collection
    @@immutable timestamp: zonedDateTime
}

// A pending airdrop (HIP-904) the account sent (outstanding) or is to receive (pending).
// Exactly one of `amount` (fungible) / `serialNumber` (NFT) is set.
@@finalType
TokenAirdrop {
    @@immutable senderId: AccountId
    @@immutable receiverId: AccountId
    @@immutable tokenId: Address
    @@immutable @@nullable amount: int64              // fungible amount (smallest unit); null for an NFT airdrop
    @@immutable @@nullable serialNumber: int64        // NFT serial; null for a fungible airdrop
    @@immutable timestamp: zonedDateTime
}

// One token balance line inside an AccountBalance.
@@finalType
TokenBalance {
    @@immutable tokenId: Address
    @@immutable balance: int64                        // balance in the token's smallest unit
}

// An account's balance snapshot from the top-level balances endpoint. GET /balances.
@@finalType
AccountBalance {
    @@immutable account: AccountId
    @@immutable balance: int64                        // hbar balance in tinybars
    @@immutable @@default([]) tokens: list<TokenBalance>
}

abstraction AccountRepository {
    @@async @@throws(mirror-node-error)
    @@nullable AccountInfo findById(accountId: AccountId)

    // Lists account entities known to the mirror node.
    // Maps to GET /api/v1/accounts.
    @@async @@throws(mirror-node-error)
    Page<AccountInfo> findAll()

    // Staking-reward payouts received by the account.
    // Maps to GET /api/v1/accounts/{id}/rewards.
    @@async @@throws(mirror-node-error)
    Page<StakingReward> getRewards(accountId: AccountId)

    // HBAR allowances the account has granted to spenders.
    // Maps to GET /api/v1/accounts/{id}/allowances/crypto.
    @@async @@throws(mirror-node-error)
    Page<CryptoAllowance> getCryptoAllowances(accountId: AccountId)

    // Fungible-token allowances the account has granted.
    // Maps to GET /api/v1/accounts/{id}/allowances/tokens.
    @@async @@throws(mirror-node-error)
    Page<TokenAllowance> getTokenAllowances(accountId: AccountId)

    // NFT approved-for-all allowances the account has granted.
    // Maps to GET /api/v1/accounts/{id}/allowances/nfts.
    @@async @@throws(mirror-node-error)
    Page<NftAllowance> getNftAllowances(accountId: AccountId)

    // Pending airdrops the account has SENT that are not yet claimed (HIP-904).
    // Maps to GET /api/v1/accounts/{id}/airdrops/outstanding.
    @@async @@throws(mirror-node-error)
    Page<TokenAirdrop> getOutstandingAirdrops(accountId: AccountId)

    // Pending airdrops the account is to RECEIVE but has not yet claimed (HIP-904).
    // Maps to GET /api/v1/accounts/{id}/airdrops/pending.
    @@async @@throws(mirror-node-error)
    Page<TokenAirdrop> getPendingAirdrops(accountId: AccountId)

    // Account balances across the network (latest, or at a queried timestamp).
    // Maps to the top-level GET /api/v1/balances — NOT /accounts/{id}/balances.
    @@async @@throws(mirror-node-error)
    Page<AccountBalance> getBalances()
}

@@static AccountRepository createRepository(mirrorNode: MirrorNode)

```

## Default Instances for Testing

```
// The creation, expiry and balance timestamps of the Hiero Enterprise Java test fixture account-info.json
// ("1605733260.898372007", "1612733260.898372007", "1742896287.333154756"), in millisecond precision.
values TIMES: list<zonedDateTime> = [toZonedDateTime(text: "2020-11-18T21:01:00.898Z"),
        toZonedDateTime(text: "2021-02-07T21:27:40.898Z"), toZonedDateTime(text: "2025-03-25T09:51:27.333Z")]
```

## Testing

The queries of `AccountRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: this spec (`declineReward` and `deleted` default to false; the other optional values are nullable).
@@testId("an-account-info-without-optional-values-does-not-decline-rewards-and-is-not-deleted")
test "an account info without optional values does not decline rewards and is not deleted" {
    // given
    let account = DEFAULT(AccountId)

    // when
    let info = AccountInfo{accountId: account, balance: 100, ethereumNonce: 0, pendingReward: 0}

    // then
    assertEquals(expected: account, actual: info.accountId)
    assertEquals(expected: 100, actual: info.balance)
    assertFalse(condition: info.declineReward)
    assertFalse(condition: info.deleted)
    assertNull(actual: info.evmAddress)
    assertNull(actual: info.alias)
    assertNull(actual: info.stakedAccountId)
    assertNull(actual: info.stakedNodeId)
    assertNull(actual: info.balanceTimestamp)
    assertNull(actual: info.createdTimestamp)
    assertNull(actual: info.expiryTimestamp)
    assertNull(actual: info.autoRenewPeriod)
    assertNull(actual: info.stakePeriodStart)
}

// Source: the Hiero Enterprise Java test fixture account-info.json (created "1605733260.898372007", expiry
// "1612733260.898372007", balance timestamp "1742896287.333154756", auto-renew period 7000000 seconds, no stake period
// start), the values of TIMES; millisecond precision.
@@testId("an-account-info-keeps-its-timestamps-and-its-auto-renew-period")
test "an account info keeps its timestamps and its auto-renew period" {
    // given
    let created = TIMES[0]
    let expiry = TIMES[1]
    let balanceTime = TIMES[2]

    // when
    let info = AccountInfo{accountId: DEFAULT(AccountId), balance: 1663012637744658, balanceTimestamp: balanceTime,
            ethereumNonce: 0, pendingReward: 0, createdTimestamp: created, expiryTimestamp: expiry,
            autoRenewPeriod: toSeconds(value: 7000000)}

    // then
    assertEquals(expected: toZonedDateTime(text: "2020-11-18T21:01:00.898Z"), actual: info.createdTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2021-02-07T21:27:40.898Z"), actual: info.expiryTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2025-03-25T09:51:27.333Z"), actual: info.balanceTimestamp)
    assertEquals(expected: toSeconds(value: 7000000), actual: info.autoRenewPeriod)
    assertNull(actual: info.stakePeriodStart)
}

// Source: this spec (a staking reward is a payout of tinybars to the account at a consensus time); the values of
// TIMES.
@@testId("a-staking-reward-keeps-its-account-amount-and-payout-time")
test "a staking reward keeps its account, amount and payout time" {
    // given
    let account = DEFAULT(AccountId)

    // when
    let reward = StakingReward{accountId: account, amount: 100, timestamp: TIMES[0]}
    let later = StakingReward{accountId: account, amount: 100, timestamp: TIMES[1]}

    // then
    assertEquals(expected: account, actual: reward.accountId)
    assertEquals(expected: 100, actual: reward.amount)
    assertEquals(expected: TIMES[0], actual: reward.timestamp)
    assertEquals(expected: StakingReward{accountId: account, amount: 100, timestamp: TIMES[0]}, actual: reward)
    assertNotEquals(unexpected: reward, actual: later)
}

// Source: this spec (an HBAR allowance has a remaining and an originally approved amount and the time it was granted
// or last modified).
@@testId("a-crypto-allowance-keeps-its-owner-spender-remaining-and-granted-amounts-and-its-time")
test "a crypto allowance keeps its owner, spender, remaining and granted amounts and its time" {
    // given
    let owner = AccountId.fromString(value: "0.0.1001")
    let spender = AccountId.fromString(value: "0.0.1002")
    let time = DEFAULT(zonedDateTime)

    // when
    let allowance = CryptoAllowance{owner: owner, spender: spender, amount: 25, amountGranted: 100, timestamp: time}

    // then
    assertEquals(expected: owner, actual: allowance.owner)
    assertEquals(expected: spender, actual: allowance.spender)
    assertEquals(expected: 25, actual: allowance.amount)
    assertEquals(expected: 100, actual: allowance.amountGranted)
    assertEquals(expected: time, actual: allowance.timestamp)
}

// Source: this spec (a fungible-token allowance has the token, a remaining and an originally approved amount and the
// time it was granted or last modified).
@@testId("a-token-allowance-keeps-its-token-remaining-and-granted-amounts-and-its-time")
test "a token allowance keeps its token, remaining and granted amounts and its time" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let time = DEFAULT(zonedDateTime)

    // when
    let allowance = TokenAllowance{owner: AccountId.fromString(value: "0.0.1001"),
            spender: AccountId.fromString(value: "0.0.1002"), tokenId: token, amount: 0, amountGranted: 10,
            timestamp: time}

    // then
    assertEquals(expected: token, actual: allowance.tokenId)
    assertEquals(expected: 0, actual: allowance.amount)
    assertEquals(expected: 10, actual: allowance.amountGranted)
    assertEquals(expected: time, actual: allowance.timestamp)
}

// Source: this spec (an NFT allowance is approved for all serials of the collection or not).
@@testId("an-nft-allowance-keeps-whether-it-is-approved-for-the-whole-collection")
test "an NFT allowance keeps whether it is approved for the whole collection" {
    // given
    let token = Address.fromString(address: "0.0.5005")
    let time = DEFAULT(zonedDateTime)

    for approved in [false, true] {
        // when
        let allowance = NftAllowance{owner: AccountId.fromString(value: "0.0.1001"),
                spender: AccountId.fromString(value: "0.0.1002"), tokenId: token, approvedForAll: approved,
                timestamp: time}

        // then
        assertEquals(expected: approved, actual: allowance.approvedForAll)
        assertEquals(expected: token, actual: allowance.tokenId)
        assertEquals(expected: time, actual: allowance.timestamp)
    }
}

// Source: this spec (exactly one of `amount` and `serialNumber` is set; `serialNumber` is null for a fungible
// airdrop); HIP-904.
@@testId("a-fungible-airdrop-has-an-amount-and-no-serial-number")
test "a fungible airdrop has an amount and no serial number" {
    // given
    let time = DEFAULT(zonedDateTime)

    // when
    let airdrop = TokenAirdrop{senderId: AccountId.fromString(value: "0.0.1001"),
            receiverId: AccountId.fromString(value: "0.0.1002"), tokenId: Address.fromString(address: "0.0.5005"),
            amount: 10, timestamp: time}

    // then
    assertEquals(expected: 10, actual: airdrop.amount)
    assertNull(actual: airdrop.serialNumber)
    assertEquals(expected: time, actual: airdrop.timestamp)
}

// Source: this spec (exactly one of `amount` and `serialNumber` is set; `amount` is null for an NFT airdrop);
// HIP-904.
@@testId("an-nft-airdrop-has-a-serial-number-and-no-amount")
test "an NFT airdrop has a serial number and no amount" {
    // given
    let sender = AccountId.fromString(value: "0.0.1001")
    let receiver = AccountId.fromString(value: "0.0.1002")

    // when
    let airdrop = TokenAirdrop{senderId: sender, receiverId: receiver, tokenId: Address.fromString(address: "0.0.5005"),
            serialNumber: 1, timestamp: DEFAULT(zonedDateTime)}

    // then
    assertEquals(expected: sender, actual: airdrop.senderId)
    assertEquals(expected: receiver, actual: airdrop.receiverId)
    assertEquals(expected: 1, actual: airdrop.serialNumber)
    assertNull(actual: airdrop.amount)
}

// Source: this spec (`tokens` defaults to an empty list).
@@testId("an-account-balance-without-token-balances-has-an-empty-token-list")
test "an account balance without token balances has an empty token list" {
    // when
    let balance = AccountBalance{account: DEFAULT(AccountId), balance: 100}

    // then
    assertEquals(expected: 100, actual: balance.balance)
    assertEquals(expected: 0, actual: length(value: balance.tokens))
}

// Source: this spec (an account balance lists its token balances in the token's smallest unit).
@@testId("an-account-balance-keeps-its-token-balances")
test "an account balance keeps its token balances" {
    // given
    let tokens = [TokenBalance{tokenId: Address.fromString(address: "0.0.5005"), balance: 10},
            TokenBalance{tokenId: Address.fromString(address: "0.0.5006"), balance: 0}]

    // when
    let balance = AccountBalance{account: DEFAULT(AccountId), balance: 100, tokens: tokens}

    // then
    assertEquals(expected: tokens, actual: balance.tokens)
}
```

## Questions & Comments

- **Allowance `timestamp` is a single value, not a range.** The Mirror Node REST returns a
  `timestamp` *range* (from/to) on allowances; V3 surfaces the grant / last-modified instant as a
  single `zonedDateTime`, consistent with the other mirror-node types. A typed `TimestampRange`
  (for the generic range-filter model) is tracked in [`missing-features.md`](../../docs/missing-features.md) §4.3.
- **`getNftAllowances` returns approved-for-all grants only.** `GET /allowances/nfts` exposes
  collection-wide (`approvedForAll`) NFT allowances; per-serial NFT approvals are read via the NFT
  endpoints, not here.
- **`TokenAirdrop` is one shape for both directions.** *Outstanding* = sent by this account and not
  yet claimed; *pending* = receivable by this account. `amount` (fungible) and `serialNumber` (NFT)
  are mutually exclusive.
- **`getBalances()` maps to the top-level `/balances`**, not a per-account path — it returns
  balances across accounts. It is placed on `AccountRepository` because the result is
  account-balance-shaped, and is distinct from `mirrornode.token.Balance` (which is token-centric).
