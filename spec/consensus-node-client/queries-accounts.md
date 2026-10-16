# Account Queries API

This namespace defines account-related queries against the consensus node. It is the first
concrete usage of the abstractions defined in [`queries.md`](queries.md) and serves as a
worked example of the free / paid split.

## Description

Queries for reading the state of accounts directly from a consensus node:

- **`AccountBalanceQuery`** — a free query that returns the native-token balance and the token balances of an
  account or smart contract. Set either `accountId` or `contractId`.
- **`AccountInfoQuery`** — a paid query that returns the full state of an account: its authority (key), memo,
  expiration, staking configuration, EVM-address alias and more. The price is determined automatically and paid by
  the client's operator account.
- **`AccountRecordsQuery`** — a paid query that returns the recent transaction records in which the account was the
  effective payer. On current networks this list is almost always empty; use the mirror node to read an account's
  transaction history.

The free query returns a `QueryResponse`, the paid queries return a `PaidQueryResponse` that also contains the cost
that was actually paid.

## Design Notes

This namespace is the first concrete usage of the abstractions defined in [`queries.md`](queries.md) and serves as
a worked example of the free / paid split established by `consensusnode.queries`. `AccountRecordsQuery` is a legacy
("threshold records") surface kept for V2 parity (see *Questions & Comments*).

## API Schema

```
namespace consensusnode.queries.accounts
requires {Address, AccountId, ContractId, EvmAddress} from ledger
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {FreeQuery, PaidQuery} from consensusnode.queries
requires {Receipt, Record} from consensusnode.transactions

// Current balance snapshot of an account or contract. Returned by `AccountBalanceQuery`.
type AccountBalance {
    @@immutable accountId: AccountId                     // the account or contract this snapshot belongs to
    @@immutable balance: NativeToken<ANY, ANY>           // native-token balance
    @@immutable tokenBalances: map<Address, int64>       // tokenId -> balance in the token's smallest unit
}

// Free query for the current balance of an account or smart contract.
// Exactly one of accountId or contractId must be set.
@@finalType
@@oneOf(accountId, contractId)
AccountBalanceQuery extends FreeQuery<AccountBalance> {
    @@immutable @@nullable accountId: AccountId
    @@immutable @@nullable contractId: ContractId
}

// Full account state snapshot. Returned by `AccountInfoQuery`.
type AccountInfo {
    @@immutable accountId: AccountId
    @@immutable @@nullable evmAddress: EvmAddress                     // 20-byte EVM-address alias if assigned
    @@immutable balance: NativeToken<ANY, ANY>
    @@immutable @@nullable authority: Authority
    @@immutable @@nullable accountMemo: string
    @@immutable expirationTime: zonedDateTime
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@default(0) maxAutomaticTokenAssociations: int32
    @@immutable @@default(false) receiverSignatureRequired: bool
    @@immutable @@default(0) ownedNfts: int64
    @@immutable @@default(false) deleted: bool
    @@immutable @@nullable stakedAccountId: AccountId                 // mutually exclusive with stakedNodeId
    @@immutable @@nullable stakedNodeId: int64                        // mutually exclusive with stakedAccountId
    @@immutable @@default(false) declineStakingReward: bool
}

// Paid query for the full account state.
@@finalType
AccountInfoQuery extends PaidQuery<AccountInfo> {
    @@immutable accountId: AccountId
}

// The recent transaction records associated with an account. Returned by `AccountRecordsQuery`.
// `records` mixes records of whatever transaction types the account paid for, so its elements are typed as the
// general `Record<Receipt>`. An account without qualifying records (the common case on current networks) yields
// an empty list.
type AccountRecords {
    @@immutable accountId: AccountId
    @@immutable @@default([]) records: list<Record<Receipt>>
}

// Paid query for the recent transaction records in which `accountId` was the effective payer.
// On current Hiero / Hedera networks the returned list is effectively always empty; use the mirror node to read
// an account's transaction history.
@@finalType
AccountRecordsQuery extends PaidQuery<AccountRecords> {
    @@immutable accountId: AccountId
}
```

## Examples

### Read a balance (free)

```
HieroClient client = ...;

AccountBalance balance = new AccountBalanceQuery()
    .accountId(...)
    .submit(client)
    .value;

NativeToken<ANY, ANY> nativeBalance = balance.balance;
int64 tokenAmount                    = balance.tokenBalances.get(someTokenId);
```

No cost discovery, no payment, no `maxQueryPayment`. `.value` unwraps the `QueryResponse`
envelope for callers that only need the payload.

### Read a balance with envelope metadata

```
QueryResponse<AccountBalance> response = new AccountBalanceQuery()
    .accountId(...)
    .submit(client);

AccountBalance balance = response.value;
AccountId answeringNode = response.answeredBy;  // useful for forensics
```

### Read full account state (paid)

```
PaidQueryResponse<AccountInfo> response = new AccountInfoQuery()
    .accountId(...)
    .maxQueryPayment(NativeToken.of(...))       // bound the spend
    .submit(client);

AccountInfo info = response.value;
Authority authority = info.authority;
NativeToken<ANY, ANY> paid = response.cost;     // what was actually charged
```

The client's operator pays. A configurable payer (for paymaster / custodial / sponsored
patterns) is deferred — see
[ADR-0002](../../docs/adr/0002-defer-paid-query-payer-customization.md).

### Read an account's recent records (paid)

```
AccountRecords result = new AccountRecordsQuery()
    .accountId(...)
    .submit(client)
    .value;

for (Record<Receipt> record : result.records) {
    TransactionId id = record.transactionId;
    TransactionStatus status = record.receipt.status;
}
```

`records` is typed against the base `Record<Receipt>`; callers that know a specific entry's
transaction type can narrow its `receipt` at the language level. On modern networks the list
is typically empty — see *Questions & Comments*.

## Testing

Tests run against a local [solo](https://solo.hiero.org) network and are described as
language-agnostic Given / When / Then scenarios. See
[`guidelines/testing-guideline.md`](../../guidelines/testing-guideline.md) for the test
platform, the solo lifecycle, and the shared *"`HieroClient` connected to a solo network with a
funded operator account"* fixture referenced below.

### `queries.accounts/balance-of-operator-succeeds`

- **Given** a `HieroClient` connected to a solo network with a funded operator account.
- **When** an `AccountBalanceQuery` with `accountId` set to the operator's account id is
  submitted (`submit(client)`).
- **Then** the call completes without error, returns a `QueryResponse<AccountBalance>` whose
  `value.accountId` equals the operator's account id, and whose `value.balance` is greater than
  zero (the operator is funded by solo's one-shot deployment).

### Unit tests

Offline tests of the queries and their result types, written as given / when / then. Nothing is submitted; the
comment above each test names where the current SDKs test the same.

```tests
// Source: the Python SDK (account_balance_query_test.py, test_build_account_balance_query and
// test_set_contract_id_method_chaining_resets_account_id) and the C++ SDK (AccountBalanceQueryUnitTests): a balance
// query targets either an account or a contract.
test "a balance query for an account has no contract id" {
    // given
    let account = DEFAULT(AccountId)

    // when
    let query = AccountBalanceQuery{accountId: account, contractId: null}

    // then
    assertEquals(expected: account, actual: query.accountId)
    assertNull(actual: query.contractId)
}

// Source: the Python SDK (account_balance_query_test.py, test_build_account_balance_query_with_contract_id) and the C++
// SDK (AccountBalanceQueryUnitTests).
test "a balance query for a contract has no account id" {
    // given
    let contract = DEFAULT(ContractId)

    // when
    let query = AccountBalanceQuery{accountId: null, contractId: contract}

    // then
    assertEquals(expected: contract, actual: query.contractId)
    assertNull(actual: query.accountId)
}

// Source: the Python SDK (account_info_query_test.py and account_records_query_test.py, test_constructor and
// test_get_method) and the Swift and Go SDKs (the get / set tests of AccountInfoQuery and AccountRecordsQuery).
test "the info and records queries keep their account id" {
    // given
    let account = DEFAULT(AccountId)

    // when
    let info = AccountInfoQuery{accountId: account}
    let records = AccountRecordsQuery{accountId: account}

    // then
    assertEquals(expected: account, actual: info.accountId)
    assertEquals(expected: account, actual: records.accountId)
}

// Source: this spec (the defaults of AccountInfo); the Python SDK (account_info_test.py, the initialization of
// AccountInfo).
test "an account info has these defaults" {
    // when
    let info = AccountInfo{accountId: DEFAULT, balance: DEFAULT(hedera.Hbar), expirationTime: DEFAULT}

    // then
    assertEquals(expected: 0, actual: info.maxAutomaticTokenAssociations)
    assertFalse(condition: info.receiverSignatureRequired)
    assertEquals(expected: 0, actual: info.ownedNfts)
    assertFalse(condition: info.deleted)
    assertFalse(condition: info.declineStakingReward)
    assertNull(actual: info.evmAddress)
    assertNull(actual: info.authority)
    assertNull(actual: info.accountMemo)
    assertNull(actual: info.autoRenewPeriod)
    assertNull(actual: info.stakedAccountId)
    assertNull(actual: info.stakedNodeId)
}

// Source: the Java SDK (AccountInfoTest, the account info of its fixture): expiration time Instant.ofEpochMilli(6),
// i.e. 1970-01-01T00:00:00.006Z, and auto-renew period Duration.ofDays(7), i.e. 604800 seconds. Millisecond precision.
test "an account info keeps its expiration time and auto-renew period" {
    // given
    let expirationTime = toZonedDateTime(text: "1970-01-01T00:00:00.006Z")
    let autoRenewPeriod = toSeconds(value: 604800)

    // when
    let info = AccountInfo{accountId: DEFAULT, balance: DEFAULT(hedera.Hbar), expirationTime: expirationTime,
            autoRenewPeriod: autoRenewPeriod}

    // then
    assertEquals(expected: expirationTime, actual: info.expirationTime)
    assertEquals(expected: toSeconds(value: 604800), actual: info.autoRenewPeriod)
}

// Source: this spec (an account without qualifying records yields an empty list); the Python SDK
// (account_records_query_test.py, test_account_record_query_empty_records).
test "account records without records have an empty list" {
    // when
    let records = AccountRecords{accountId: DEFAULT}

    // then
    assertEquals(expected: DEFAULT(AccountId), actual: records.accountId)
    assertEquals(expected: 0, actual: length(value: records.records))
}

// Source: this spec (@@oneOf(accountId, contractId)); the Python SDK (account_balance_query.py, _make_request: "Specify
// either account_id or contract_id, not both.").
test "a balance query for both an account and a contract is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    AccountBalanceQuery{accountId: DEFAULT(AccountId), contractId: DEFAULT(ContractId)}
}

// Source: this spec (@@oneOf(accountId, contractId)); the Python SDK (account_balance_query_e2e_test.py,
// test_integration_balance_query_raises_when_neither_source_set: the network rejects it with INVALID_ACCOUNT_ID).
test "a balance query for neither an account nor a contract is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    AccountBalanceQuery{accountId: null, contractId: null}
}
```

## Questions & Comments

- **`AccountBalanceQuery` accepts either `accountId: AccountId` or `contractId: ContractId`.**
  Both target the same underlying entity space at the protocol level, but they are distinct
  typed identifiers in V3 (see [ADR-0003](../../docs/adr/0003-three-level-address-hierarchy-with-nullability-narrowing.md)).
  The dual setter matches the V2 ergonomic of having a balance query work uniformly for
  accounts and smart contracts. `@@oneOf` enforces exactly one of the two at construction.
- **`AccountInfo.evmAddress` is the typed `EvmAddress` value type.** Carries 20 raw bytes
  with hex `toString()` and `fromString` / `fromBytes` factories — concrete language bindings
  do not need to expose a separate hex-string accessor.
- **Deleted-account behaviour.** Querying a deleted account returns an `AccountInfo` with
  `deleted = true` and most other fields cleared. Callers should branch on `deleted` rather
  than assuming the rest of the snapshot is meaningful.
- **`AccountInfo` here is distinct from `mirrornode.account.AccountInfo`.** Same conceptual
  data but different sources (consensus node gRPC vs. mirror node REST) and slightly
  different field semantics (e.g. consensus-node `balance` is always live; mirror-node
  `balance` carries a `balanceTimestamp`). The two are kept separate to avoid coupling the
  consensus-node API to mirror-node update cadence.

- **`AccountRecordsQuery` is a legacy surface and almost always returns an empty list.** HAPI's
  `CryptoGetAccountRecords` returns *threshold records* — records the network retained for an
  account because a transaction's transfer crossed the (now removed)
  `sendRecordThreshold` / `receiveRecordThreshold` configured on the account. Those threshold
  fields were deprecated and the records are no longer generated on current Hiero / Hedera
  networks, so the list is effectively always empty. The query is kept for V2 parity; callers
  that want an account's transaction history should use the mirror node
  (`mirrornode.transaction`) instead, which is the supported path and is free. We may drop this
  query before V3 GA if no consumer needs the parity — flagged for review.

- **`AccountRecords.records` is typed `list<Record<Receipt>>`, not a concrete record type.**
  The list mixes records of whatever transaction types the account paid for, so it is bound to
  the base abstraction `Record<Receipt>` from [`transactions.md`](transactions.md). Callers that
  need a typed receipt narrow the element at the language level (e.g. a pattern match / `instanceof`).
  This is the only place in the query specs that returns the base `Record` rather than a
  query-specific result type.

- **`AccountInfo` covers fewer attributes than HAPI `CryptoGetInfo`.** Not modelled (besides the
  deprecated proxy-staking and record-threshold fields): the token relationships, the alias key, the
  ledger ID, the hbar / token / NFT allowances and the Ethereum nonce. The TCK's `getAccountInfo`
  returns them (see [`tck-binding.md`](../../docs/tck-binding.md)). Are they left out on purpose (e.g. token
  relationships and allowances are read from the mirror node), or should `AccountInfo` add them?
