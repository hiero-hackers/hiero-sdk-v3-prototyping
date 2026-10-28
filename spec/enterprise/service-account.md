# Account Service API

Service definition for account handling.

## Description

Provides high-level operations for accounts: creation, update, deletion and lookup of account information. Account ids are `ledger.AccountId` (pure shard.realm.num). Authorization parameters accept the full `Authority` type.

### Defining a new account

Creating or Updating an account involves many meaningful and mostly optional attributes (authority, initial balance, memo, receiver-signature requirement, automatic token associations, auto-renew period, staking, EVM alias, delegation address). Expressing all of them as method parameters would require a combinatorial explosion of overloads, while the two overloads that exist currently silently drop most of the options.

Therefore both operations use single fully immutable object, `AccountParams`, that carries the attributes of the account. `createNewAccount(def)` is the canonical create operation and `updateAccount(accountId, def)` is the canonical update operation; the `createNewAccount` overloads are pure convenience shortcuts for create and are specified in terms of an equivalent `AccountParams`.

`AccountParams` is immutable and has many optional fields, languages should offer their idiomatic construction pattern for it a builder in Java (see
[Builder Pattern](../../guidelines/api-best-practices-java.md#builder-pattern)), an options object in JavaScript, a struct literal with defaults in Rust, keyword arguments in Python.

### AccountParams holds only the fields valid for both operations

Every field of `AccountParams` is optional on the type itself, because the same type describes "the new account" on `create` and "the changes to apply" on `update`. What each field means depends on the operation:

| Field |	createAccount |	updateAccount |
| :--- | :--- | :--- |
| `authority` |	required |	rejected, use `updateAccountAuthority` |
| `initialBalance` |	optional, default 0 |	rejected |
| `evmAlias` |	optional |	rejected  (cannot be changed) |
| `expirationTime` |	rejected |	optional |
| `memo`, `receiverSignatureRequired`, `maxAutomaticTokenAssociations`, `autoRenewPeriod`,  `stakedAccountId`, `stakedNodeId`, `declineStakingReward`, `delegationAddress` |	optional,  default when unset |	optional, unchanged when unset |

A field that is not allowed for the operation makes the call fail with a `service-error`; it is never silently ignored.


## API Schema

```
namespace enterprise.service.account
requires {Page} from common
requires {AccountId} from ledger
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Session} from enterprise.service

// Immutable definition of AccountParams, use to create/update the new accounts.
@@finalType
AccountParams {
    @@immutable @@nullable authority: Authority                              // controls the account (single key, multisig, contract); create only, required there
    @@immutable @@nullable initialBalance: NativeToken<ANY, ANY>             // create only; balance funded from the operator account, default 0, must not be negative
    @@immutable @@nullable @@maxLength(100) memo: string                     // short, human-readable label on the account
    @@immutable @@nullable receiverSignatureRequired: bool                   // when true, every incoming transfer must be signed by the account; default false
    @@immutable @@nullable maxAutomaticTokenAssociations: int32              // token associations created automatically on first transfer; -1 = unlimited; default 0
    @@immutable @@nullable autoRenewPeriod: seconds                          // renewal window; protocol default applies when unset
    @@immutable @@nullable expirationTime: zonedDateTime                     // update only; can only be extended
    @@immutable @@nullable stakedAccountId: AccountId                        // stake to the node of this account; mutually exclusive with stakedNodeId
    @@immutable @@nullable stakedNodeId: int64                               // stake directly to this node; mutually exclusive with stakedAccountId
    @@immutable @@nullable declineStakingReward: bool                        // when true the account earns no staking rewards; default false
    @@immutable @@nullable evmAlias: bytes                                   // create only; 20-byte EVM address used as alias of the new account
    @@immutable @@nullable delegationAddress: bytes                          // (HIP-1340) 20-byte EVM address of the contract whose code runs when the account is called from a smart contract
}


AccountInformation {
    @@immutable accountId: AccountId
    @@immutable balance: NativeToken<ANY, ANY>
    @@immutable authority: Authority
}

AccountService {
  @@throws(service-error) AccountId createNewAccount(params: AccountParams)

  @@throws(service-error) AccountId createNewAccount(authority: Authority)

  @@throws(service-error) AccountId createNewAccount(authority: Authority, initialBalance: NativeToken<ANY, ANY>)

  @@throws(service-error) void updateAccount(accountId: AccountId, params: AccountParams)

  @@throws(service-error) void updateAccountAuthority(accountId: AccountId, newAuthority: Authority, oldAuthority: Authority)

  // Delete an account; its remaining balance is transferred to the operator account.
  @@throws(service-error) void deleteAccount(account: AccountId)

  // Delete an account; its remaining balance is transferred to `transferAccount`.
  @@throws(service-error) void deleteAccount(account: AccountId, transferAccount: AccountId)

  @@throws(service-error) AccountInformation findById(account: AccountId)

  @@throws(service-error) Page<AccountInformation> findAll()
}

//Factory method to create Service (not needed for real framework integration where injection is used)
@@static
AccountService createService(session: Session)

```

## Examples

### Create an account from a definition (Java)
```java
AccountService accountService = ...;

AccountParams params = AccountParams.builder()
    .authority(Authority.of(publicKey))
    .memo("Treasury account")
    .maxAutomaticTokenAssociations(10)
    .delegationAddress(delegationAddressBytes)           // (HIP1340) 20-byte EVM address of the delegated contract
    .build();

AccountId accountId = accountService.createNewAccount(params);
```

### Update an account from definition (Java)

```java
// Only the fields that are set are changed:
accountService.updateAccount(accountId, AccountParams.builder()
    .memo("Updated memo")
    .delegationAddress(newDelegationAddressBytes)
    .build());
```


## Questions & Comments

- The authorization parameters/fields here (`AccountInformation.authority`, `createNewAccount(authority)`, and both
  `newAuthority`/`oldAuthority` of `updateAccountAuthority`) accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.
- Using `AccountParams` for both create and update means "required" and "allowed" fields are no longer visible on the type, which fields are create-only (authority, initialBalance, evmAlias) or update-only (expirationTime) is only prose and checked at runtime.