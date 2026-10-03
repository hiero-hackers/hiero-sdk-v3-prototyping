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

AccountService {

  @@throws(service-error) AccountId createNewAccount(authority: Authority)

  @@throws(service-error) AccountId createNewAccount(authority: Authority, initialBalance: NativeToken<ANY, ANY>)

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