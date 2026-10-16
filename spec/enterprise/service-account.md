# Account Service API

## Description

`AccountService` creates, updates and deletes accounts and looks them up. A new account is controlled by the given
`Authority` and can be funded with an initial balance; the operator account of the `Session` pays for the
transactions. `AccountInformation` describes an account with its balance and its current authority.

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
// required. An account is staked to another account or to a node, not to both.
@@finalType
@@oneOrNoneOf(stakedAccountId, stakedNodeId)
AccountCreateParams {
    @@immutable authority: Authority
    // Amount transferred from the payer to the new account. If absent, the account is created with a balance
    // of 0 in the native token of the network.
    @@immutable @@nullable initialBalance: NativeToken<ANY, ANY>
    @@immutable @@nullable accountMemo: string
    @@immutable @@default(false) receiverSignatureRequired: bool
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable stakedAccountId: AccountId
    @@immutable @@nullable stakedNodeId: int64
    @@immutable @@default(false) declineStakingReward: bool
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

## Questions & Comments

- The authorization parameters/fields here (`AccountInformation.authority`, `createNewAccount(authority)`, and both
  `newAuthority`/`oldAuthority` of `updateAccountAuthority`) accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.