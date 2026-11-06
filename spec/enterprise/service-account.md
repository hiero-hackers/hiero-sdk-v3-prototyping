# Account Service API

## Description

`AccountService` creates, updates and deletes accounts and looks them up. A new account is controlled by the given
`Authority` and can be funded with an initial balance; the operator account of the `Session` pays for the
transactions. `AccountInformation` describes an account with its balance and its current authority.

Staking is not part of the service: to create an account that is staked to another account or to a node, use
`AccountCreateTransaction` of the consensus node client.

## API Schema

```
namespace enterprise.service.account
requires {Page} from common
requires {AccountId} from ledger
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Session} from enterprise.service

AccountInformation {
    @@immutable accountId: AccountId
    @@immutable balance: NativeToken<ANY, ANY>
    @@immutable authority: Authority
}

// The parameters of a new account: everything that can be set when an account is created. Only the authority is
// required.
@@finalType
AccountCreateParams {
    @@immutable authority: Authority
    // Amount transferred from the payer to the new account. If absent, the account is created with a balance
    // of 0 in the native token of the network.
    @@immutable @@nullable initialBalance: NativeToken<ANY, ANY>
    @@immutable @@nullable @@maxBytes(100) accountMemo: string
    @@immutable @@default(false) receiverSignatureRequired: bool
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable alias: bytes
}

AccountService {

  // Creates an account with the given authority; the same as createNewAccount with AccountCreateParams that have
  // only the authority.
  @@throws(service-error) AccountId createNewAccount(authority: Authority)

  // Creates an account with the given authority and initial balance; the same as createNewAccount with
  // AccountCreateParams that have only these two values.
  @@throws(service-error) AccountId createNewAccount(authority: Authority, initialBalance: NativeToken<ANY, ANY>)

  // Creates an account with the given parameters: the general form of the methods above, for every option of a
  // new account.
  @@throws(service-error) AccountId createNewAccount(params: AccountCreateParams)

  @@throws(service-error) void updateAccountAuthority(accountId: AccountId, newAuthority: Authority, oldAuthority: Authority)

  @@throws(service-error) void deleteAccount(account: AccountId)

  @@throws(service-error) AccountInformation findById(account: AccountId)

  @@throws(service-error) Page<AccountInformation> findAll()
}

// Creates the service for the given session. With a framework integration the service is usually obtained via
// dependency injection instead.
@@static
AccountService createService(session: Session)

```

## Default Instances for Testing

```
// The parameters of an account with only the required authority.
instance AccountCreateParams = AccountCreateParams{authority: DEFAULT}
```

## Testing

Tests of `AccountCreateParams`. The methods of `AccountService` talk to the network and are not unit-tested here.

```tests
// Source: this spec (@@default and @@nullable of AccountCreateParams).
@@testId("account-parameters-with-only-an-authority-have-the-defaults-of-the-spec")
test "account parameters with only an authority have the defaults of the spec" {
    // when
    let params = DEFAULT(AccountCreateParams)

    // then
    assertEquals(expected: DEFAULT(Authority), actual: params.authority)
    assertFalse(condition: params.receiverSignatureRequired)
    assertNull(actual: params.initialBalance)
    assertNull(actual: params.accountMemo)
    assertNull(actual: params.maxAutomaticTokenAssociations)
    assertNull(actual: params.autoRenewPeriod)
    assertNull(actual: params.alias)
}

// Source: the AccountCreateTransaction unit tests of the Java (AccountCreateTransactionTest), Go
// (account_create_transaction_unit_test.go), C++ (AccountCreateTransactionUnitTests), Swift
// (AccountCreateTransactionUnitTests) and Python (account_create_transaction_test.py) SDKs: every value that is set is
// read back unchanged. The auto-renew period is the one of the Java test (Duration.ofHours(10), which is exactly 36000
// seconds).
@@testId("account-parameters-keep-the-values-they-are-created-with")
test "account parameters keep the values they are created with" {
    // given
    let authority = DEFAULT(Authority)
    let autoRenewPeriod = toSeconds(value: 36000)

    // when
    let params = AccountCreateParams{authority: authority, accountMemo: "memo", receiverSignatureRequired: true,
            maxAutomaticTokenAssociations: 10, autoRenewPeriod: autoRenewPeriod}

    // then
    assertEquals(expected: authority, actual: params.authority)
    assertEquals(expected: "memo", actual: params.accountMemo)
    assertTrue(condition: params.receiverSignatureRequired)
    assertEquals(expected: 10, actual: params.maxAutomaticTokenAssociations)
    assertEquals(expected: autoRenewPeriod, actual: params.autoRenewPeriod)
}
```

## Questions & Comments

- The authorization parameters/fields here (`AccountInformation.authority`, `createNewAccount(authority)`, and both
  `newAuthority`/`oldAuthority` of `updateAccountAuthority`) accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.

- **`AccountInformation.authority` is required, but a hollow account has none.** An account created by a transfer to an
  EVM address (a hollow account) has no key until it signs its first transaction. The Java implementation cannot build
  an `AccountInformation` for it and fails. Should `authority` be `@@nullable`?
- **Only Hbar balances can be represented.** `AccountInformation.balance` and `createNewAccount(initialBalance)` are a
  `NativeToken<ANY, ANY>`, but the Java implementation can only read and write `Hbar`; the native token of a
  non-Hedera network fails. How does a service learn which `NativeToken` implementation the network of its session
  uses (e.g. from the `NetworkSetting`)?
- **`oldAuthority` of `updateAccountAuthority` is unused.** It cannot be used for signing, because an `Authority` holds
  no private key; see the open item about operator-only signing in [`service.md`](service.md).
