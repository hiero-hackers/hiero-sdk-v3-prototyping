# Mirror Node Account Query API

## Description

Query accounts from the Mirror Node. Use the `AccountRepository` (available as `MirrorNodeClient.accounts`) to look
up an account (`findById`, `findAll`) and to read data related to it: the staking rewards it received, its token
relationships, its hooks and their storage, the HBAR, fungible-token and NFT allowances it has granted, and its pending
airdrops (HIP-904) — both the ones it sent that are not yet claimed (outstanding) and the ones it can still claim
(pending). `getBalances` returns a snapshot of account balances across the whole network.

`findById` with all its options also returns the most recent transactions of the account.

HBAR amounts are given in tinybars, token amounts in the token's smallest unit.

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **Accounts are passed as `AccountId`.** The path parameter `idOrAliasOrEvmAddress` also accepts an alias or an EVM
  address; the repository methods take an `AccountId` only. The same holds for `account.id` of `getBalances`.
- **Renamed fields of `AccountInfo`.** `account` is `accountId`, `key` is `authority` (an `Authority`, ADR-0004),
  `memo` is `accountMemo`, `receiver_sig_required` is `receiverSignatureRequired`.
- **The `balance` object of an account is flattened.** `balance.balance` is `AccountInfo.balance`,
  `balance.timestamp` is `balanceTimestamp`, `balance.tokens` is `tokenBalances`. A missing balance is `0`.
- **Defaults instead of `null` in `AccountInfo`.** The REST API declares `ethereum_nonce` and `deleted` nullable and
  `pending_reward` optional; a missing `ethereumNonce` / `pendingReward` is `0`, a missing `deleted` is `false`.
- **`alias` is bytes.** The REST API returns the alias as RFC 4648 Base32 text; `AccountInfo.alias` holds the decoded
  bytes.
- **`AccountBalanceTransactions` wraps the account.** `GET /accounts/{id}` returns the fields of `AccountInfo` and the
  `transactions` of the account in one object; here the account is the attribute `account`. Its transactions are
  `TransactionInfo` of `mirrornode.transaction`. The `links` of the response (the next page of the transactions) are
  left out: further transactions of the account are read with the `TransactionRepository`.
- **`findById(accountId)` and `findAll()` send `transactions=false`** and return the account without transactions.
- **The type of a transaction is a `TransactionType`.** The query parameter `transactiontype` is sent as its
  `protocolName`.
- **Allowances and airdrops keep their `timestamp` as `TimestampRange`.**
- **`TokenAirdrop.amount` is `null` for an NFT airdrop.** The REST API always returns an `amount`; exactly one of
  `amount` (fungible) and `serialNumber` (NFT) is set here.
- **The response-level fields of a hook storage list are left out.** `hook_id` and `owner_id` of
  `GET /accounts/{id}/hooks/{hookId}/storage` repeat the path parameters; `getHookStorage` returns the storage slots.
- **The `timestamp` of `GET /balances` is left out.** It belongs to the whole response, not to one `AccountBalance`,
  and a `Page` has no place for it.
- **Hook storage keys and values are bytes.** The REST API returns them as `0x`-prefixed hex text.
- **`getNftAllowances` names its path parameter `account`.** The query parameter `account.id` of the endpoint is
  `accountId`, so the account of the path is `account` in the overload with all options.
- **`links` is the `Page`.** The pagination links of a list response are not an attribute; `Page.next()` follows
  them.

## API Schema

```
namespace mirrornode.account
requires {Address, AccountId, EvmAddress, MirrorNode} from ledger
requires {Authority} from authority
requires {Page} from common
requires {Order, TimestampRange} from mirrornode.common
requires {TransactionInfo, TransactionType} from mirrornode.transaction

// One token balance line of an account.
@@finalType
TokenBalance {
    @@immutable tokenId: Address
    @@immutable balance: int64                        // balance in the token's smallest unit
}

@@finalType
AccountInfo {
    @@immutable accountId: AccountId
    @@immutable @@nullable evmAddress: EvmAddress
    @@immutable balance: int64                                       // hbar balance in tinybars
    @@immutable @@nullable balanceTimestamp: zonedDateTime           // timestamp of the reported balance
    @@immutable @@default([]) tokenBalances: list<TokenBalance>      // the token balances at balanceTimestamp
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
    @@immutable @@nullable delegationAddress: EvmAddress             // the EVM address the account delegates its code to
}

// An account together with its most recent transactions. GET /accounts/{id}.
@@finalType
AccountBalanceTransactions {
    @@immutable account: AccountInfo
    @@immutable @@default([]) transactions: list<TransactionInfo>    // the most recent transactions of the account
}

// The point of a transaction at which a hook is called.
enum HookExtensionPoint {
    ACCOUNT_ALLOWANCE_HOOK    // called to approve a transfer from the account in place of an allowance
}

// The kind of program a hook runs.
enum HookType {
    EVM    // an EVM smart contract
}

// A hook of an account. GET /accounts/{id}/hooks.
@@finalType
Hook {
    @@immutable @@nullable adminKey: Authority                 // the authority that may change the hook
    @@immutable contractId: Address                            // the contract that implements the hook
    @@immutable @@nullable createdTimestamp: zonedDateTime
    @@immutable deleted: bool
    @@immutable extensionPoint: HookExtensionPoint
    @@immutable hookId: int64
    @@immutable ownerId: Address                               // the entity that owns the hook
    @@immutable @@nullable timestampRange: TimestampRange      // the period this version of the hook is valid for
    @@immutable type: HookType
}

// A storage slot of a hook. GET /accounts/{id}/hooks/{hookId}/storage.
@@finalType
HookStorage {
    @@immutable key: bytes                                     // the 32-byte storage key
    @@immutable @@nullable value: bytes                        // the stored value; null if the slot was cleared
    @@immutable timestamp: zonedDateTime                       // consensus time of the last change
}

// A staking-reward payout received by the account. GET /accounts/{id}/rewards.
@@finalType
StakingReward {
    @@immutable accountId: AccountId
    @@immutable amount: int64                         // tinybars paid in this reward
    @@immutable timestamp: zonedDateTime              // consensus time of the payout
}

// Whether an account's balance of a token is frozen.
enum TokenFreezeStatus {
    NOT_APPLICABLE    // the token has no freeze key
    FROZEN
    UNFROZEN
}

// Whether KYC of an account is granted for a token.
enum TokenKycStatus {
    NOT_APPLICABLE    // the token has no KYC key
    GRANTED
    REVOKED
}

// The relationship of an account with a token it is associated with. GET /accounts/{id}/tokens.
@@finalType
TokenRelationship {
    @@immutable @@nullable automaticAssociation: bool          // true if the token was associated automatically
    @@immutable balance: int64                                 // balance in the token's smallest unit
    @@immutable @@nullable createdTimestamp: zonedDateTime     // when the association was created
    @@immutable @@nullable decimals: int64                     // the decimals of the token
    @@immutable @@nullable freezeStatus: TokenFreezeStatus
    @@immutable kycStatus: TokenKycStatus
    @@immutable tokenId: Address
}

// An HBAR spending allowance the account granted. GET /accounts/{id}/allowances/crypto.
@@finalType
CryptoAllowance {
    @@immutable owner: AccountId
    @@immutable spender: AccountId
    @@immutable amount: int64                         // remaining approved tinybars
    @@immutable amountGranted: int64                  // originally approved tinybars
    @@immutable timestamp: TimestampRange             // the period this version of the allowance is valid for
}

// A fungible-token spending allowance the account granted. GET /accounts/{id}/allowances/tokens.
@@finalType
TokenAllowance {
    @@immutable owner: AccountId
    @@immutable spender: AccountId
    @@immutable tokenId: Address
    @@immutable amount: int64                         // remaining approved amount (smallest unit)
    @@immutable amountGranted: int64                  // originally approved amount
    @@immutable timestamp: TimestampRange             // the period this version of the allowance is valid for
}

// An NFT approved-for-all allowance the account granted or received. GET /accounts/{id}/allowances/nfts.
@@finalType
NftAllowance {
    @@immutable owner: AccountId
    @@immutable spender: AccountId
    @@immutable tokenId: Address
    @@immutable approvedForAll: bool                  // true → spender may transfer every serial of the collection
    @@immutable timestamp: TimestampRange             // the period this version of the allowance is valid for
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
    @@immutable timestamp: TimestampRange             // the period this version of the airdrop is valid for
}

// An account's balance snapshot from the top-level balances endpoint. GET /balances.
@@finalType
AccountBalance {
    @@immutable account: AccountId
    @@immutable balance: int64                        // hbar balance in tinybars
    @@immutable @@default([]) tokens: list<TokenBalance>
}

abstraction AccountRepository {
    // The account, without its transactions.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}.
    @@async @@throws(mirror-node-error)
    @@nullable AccountInfo findById(accountId: AccountId)

    // The account with its most recent transactions, filtered by consensus time and transaction type;
    // `transactions` false leaves the transactions out.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}.
    @@async @@throws(mirror-node-error)
    @@nullable AccountBalanceTransactions findById(accountId: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, timestamp: list<string>, @@nullable transactionType: TransactionType,
            @@nullable transactions: bool)

    // Lists account entities known to the mirror node.
    // Maps to GET /api/v1/accounts.
    @@async @@throws(mirror-node-error)
    Page<AccountInfo> findAll()

    // Lists account entities known to the mirror node, filtered by balance, id and public key; `balance` false
    // leaves the balances out.
    // Maps to GET /api/v1/accounts.
    @@async @@throws(mirror-node-error)
    Page<AccountInfo> findAll(accountBalance: list<string>, accountId: list<string>,
            @@nullable accountPublicKey: string, @@nullable balance: bool, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order)

    // The hooks of the account.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks.
    @@async @@throws(mirror-node-error)
    Page<Hook> getHooks(accountId: AccountId)

    // The hooks of the account, filtered by hook id.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks.
    @@async @@throws(mirror-node-error)
    Page<Hook> getHooks(accountId: AccountId, @@nullable @@min(1) hookId: int64,
            @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order)

    // The storage slots of a hook of the account.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks/{hookId}/storage.
    @@async @@throws(mirror-node-error)
    Page<HookStorage> getHookStorage(accountId: AccountId, @@min(0) hookId: int64)

    // The storage slots of a hook of the account, filtered by key and consensus time.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks/{hookId}/storage.
    @@async @@throws(mirror-node-error)
    Page<HookStorage> getHookStorage(accountId: AccountId, @@min(0) hookId: int64, key: list<string>,
            @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order, timestamp: list<string>)

    // Staking-reward payouts received by the account.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/rewards.
    @@async @@throws(mirror-node-error)
    Page<StakingReward> getRewards(accountId: AccountId)

    // Staking-reward payouts received by the account, filtered by consensus time.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/rewards.
    @@async @@throws(mirror-node-error)
    Page<StakingReward> getRewards(accountId: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, timestamp: list<string>)

    // The tokens the account is associated with.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/tokens.
    @@async @@throws(mirror-node-error)
    Page<TokenRelationship> getTokenRelationships(accountId: AccountId)

    // The tokens the account is associated with, filtered by token id.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/tokens.
    @@async @@throws(mirror-node-error)
    Page<TokenRelationship> getTokenRelationships(accountId: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, tokenId: list<string>)

    // Pending airdrops the account has SENT that are not yet claimed (HIP-904).
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/outstanding.
    @@async @@throws(mirror-node-error)
    Page<TokenAirdrop> getOutstandingAirdrops(accountId: AccountId)

    // Pending airdrops the account has SENT that are not yet claimed (HIP-904), filtered by receiver, serial number
    // and token.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/outstanding.
    @@async @@throws(mirror-node-error)
    Page<TokenAirdrop> getOutstandingAirdrops(accountId: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, receiverId: list<string>, serialNumber: list<string>, tokenId: list<string>)

    // Pending airdrops the account is to RECEIVE but has not yet claimed (HIP-904).
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/pending.
    @@async @@throws(mirror-node-error)
    Page<TokenAirdrop> getPendingAirdrops(accountId: AccountId)

    // Pending airdrops the account is to RECEIVE but has not yet claimed (HIP-904), filtered by sender, serial number
    // and token.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/pending.
    @@async @@throws(mirror-node-error)
    Page<TokenAirdrop> getPendingAirdrops(accountId: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, senderId: list<string>, serialNumber: list<string>, tokenId: list<string>)

    // HBAR allowances the account has granted to spenders.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/crypto.
    @@async @@throws(mirror-node-error)
    Page<CryptoAllowance> getCryptoAllowances(accountId: AccountId)

    // HBAR allowances the account has granted, filtered by spender.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/crypto.
    @@async @@throws(mirror-node-error)
    Page<CryptoAllowance> getCryptoAllowances(accountId: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, spenderId: list<string>)

    // Fungible-token allowances the account has granted.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/tokens.
    @@async @@throws(mirror-node-error)
    Page<TokenAllowance> getTokenAllowances(accountId: AccountId)

    // Fungible-token allowances the account has granted, filtered by spender and token.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/tokens.
    @@async @@throws(mirror-node-error)
    Page<TokenAllowance> getTokenAllowances(accountId: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, spenderId: list<string>, tokenId: list<string>)

    // NFT approved-for-all allowances the account has granted.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/nfts.
    @@async @@throws(mirror-node-error)
    Page<NftAllowance> getNftAllowances(accountId: AccountId)

    // NFT approved-for-all allowances, filtered by the other account and the token. With `owner` true or null the
    // account is the owner and `accountId` filters the spenders; with `owner` false the account is the spender and
    // `accountId` filters the owners.
    // Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/nfts.
    @@async @@throws(mirror-node-error)
    Page<NftAllowance> getNftAllowances(account: AccountId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, accountId: list<string>, tokenId: list<string>, @@nullable owner: bool)

    // Account balances across the network (latest).
    // Maps to the top-level GET /api/v1/balances — NOT /accounts/{id}/balances.
    @@async @@throws(mirror-node-error)
    Page<AccountBalance> getBalances()

    // Account balances across the network, filtered by account, balance and public key, at the given consensus time.
    // Maps to the top-level GET /api/v1/balances — NOT /accounts/{id}/balances.
    @@async @@throws(mirror-node-error)
    Page<AccountBalance> getBalances(@@nullable accountId: AccountId, accountBalance: list<string>,
            @@nullable accountPublicKey: string, @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order,
            timestamp: list<string>)
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
    let time = TimestampRange{from: DEFAULT(zonedDateTime)}

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
    let time = TimestampRange{from: DEFAULT(zonedDateTime)}

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
    let time = TimestampRange{from: DEFAULT(zonedDateTime)}

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
    let time = TimestampRange{from: DEFAULT(zonedDateTime)}

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
            serialNumber: 1, timestamp: TimestampRange{from: DEFAULT(zonedDateTime)}}

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

// Source: the example of the `TokenRelationship` schema of the mirror node OpenAPI definition (automatic association,
// balance 5, created "123456789.000000001", 3 decimals, UNFROZEN, GRANTED, token 0.0.27335); millisecond precision.
@@testId("a-token-relationship-keeps-its-balance-decimals-and-statuses")
test "a token relationship keeps its balance, decimals and statuses" {
    // given
    let token = Address.fromString(address: "0.0.27335")

    // when
    let relationship = TokenRelationship{automaticAssociation: true, balance: 5,
            createdTimestamp: toZonedDateTime(text: "1973-11-29T21:33:09Z"), decimals: 3,
            freezeStatus: TokenFreezeStatus.UNFROZEN, kycStatus: TokenKycStatus.GRANTED, tokenId: token}

    // then
    assertEquals(expected: token, actual: relationship.tokenId)
    assertEquals(expected: 5, actual: relationship.balance)
    assertEquals(expected: 3, actual: relationship.decimals)
    assertEquals(expected: TokenFreezeStatus.UNFROZEN, actual: relationship.freezeStatus)
    assertEquals(expected: TokenKycStatus.GRANTED, actual: relationship.kycStatus)
    assertEquals(expected: toZonedDateTime(text: "1973-11-29T21:33:09Z"), actual: relationship.createdTimestamp)
}

// Source: this spec (`automaticAssociation`, `createdTimestamp`, `decimals` and `freezeStatus` are nullable).
@@testId("a-token-relationship-without-optional-values-has-no-decimals-and-no-freeze-status")
test "a token relationship without optional values has no decimals and no freeze status" {
    // when
    let relationship = TokenRelationship{balance: 0, kycStatus: TokenKycStatus.NOT_APPLICABLE,
            tokenId: Address.fromString(address: "0.0.27335")}

    // then
    assertNull(actual: relationship.automaticAssociation)
    assertNull(actual: relationship.createdTimestamp)
    assertNull(actual: relationship.decimals)
    assertNull(actual: relationship.freezeStatus)
}

// Source: this spec (`adminKey`, `createdTimestamp` and `timestampRange` of a hook are nullable).
@@testId("a-hook-without-optional-values-has-no-admin-key-and-no-timestamps")
test "a hook without optional values has no admin key and no timestamps" {
    // given
    let owner = Address.fromString(address: "0.0.1001")

    // when
    let hook = Hook{contractId: Address.fromString(address: "0.0.5005"), deleted: false,
            extensionPoint: HookExtensionPoint.ACCOUNT_ALLOWANCE_HOOK, hookId: 1, ownerId: owner, type: HookType.EVM}

    // then
    assertEquals(expected: owner, actual: hook.ownerId)
    assertEquals(expected: 1, actual: hook.hookId)
    assertEquals(expected: HookType.EVM, actual: hook.type)
    assertNull(actual: hook.adminKey)
    assertNull(actual: hook.createdTimestamp)
    assertNull(actual: hook.timestampRange)
}

// Source: this spec (the value of a cleared hook storage slot is null).
@@testId("a-cleared-hook-storage-slot-has-no-value")
test "a cleared hook storage slot has no value" {
    // when
    let slot = HookStorage{key: [1], timestamp: DEFAULT(zonedDateTime)}

    // then
    assertEquals(expected: [1], actual: slot.key)
    assertNull(actual: slot.value)
}

// Source: this spec (`transactions` defaults to an empty list).
@@testId("an-account-with-transactions-without-transactions-has-an-empty-transaction-list")
test "an account with transactions without transactions has an empty transaction list" {
    // given
    let info = AccountInfo{accountId: DEFAULT(AccountId), balance: 100, ethereumNonce: 0, pendingReward: 0}

    // when
    let account = AccountBalanceTransactions{account: info}

    // then
    assertEquals(expected: info, actual: account.account)
    assertEquals(expected: 0, actual: length(value: account.transactions))
    assertEquals(expected: 0, actual: length(value: info.tokenBalances))
}
```

## Questions & Comments

- **`getBalances()` is placed on `AccountRepository`.** It maps to the top-level `/balances`, not a per-account path,
  and returns balances across accounts; it is here because the result is account-balance-shaped. It is distinct from
  `mirrornode.token.Balance` (which is token-centric). Should it move to a repository of its own?
- **Response-level values of a list are lost.** The `timestamp` of `GET /balances` (the time of the balance snapshot)
  belongs to the whole response, and a `Page` has no place for it. Should `Page` (or a subtype) carry such values?
- **The transactions of `findById` cannot be paged.** `GET /accounts/{id}` returns a page of the account's
  transactions with a `links.next`; `AccountBalanceTransactions` holds the first page only. Should `transactions` be
  a `Page<TransactionInfo>`?
- **`HookType` and `HookExtensionPoint` are closed enums.** A value that a newer mirror node adds cannot be read, and
  the query fails. Should they be open like `TransactionType`?
- **Entity ids are nullable in the OpenAPI definition.** The `EntityId` schema is `[string, "null"]`; ids that are
  always present (`StakingReward.accountId`, the owner and spender of an allowance, `Hook.contractId`, ...) are not
  `@@nullable` here.
