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

FungibleTokenService {

    // Create a fungible token and return its id. The treasury defaults to the operator account; a supply authority
    // enables minting and burning.
    @@throws(service-error) Address createToken(name: string, symbol: string)

    @@throws(service-error) Address createToken(name: string, symbol: string, supplyAuthority: Authority)

    @@throws(service-error) Address createToken(name: string, symbol: string, treasuryAccount: AccountId, supplyAuthority: Authority)

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

## Questions & Comments

- The `supplyAuthority` parameters of `createToken` accept the full `Authority` type, so multisig (m-of-n) and
  contract-controlled supply keys work at the enterprise layer too — not just single public keys. See
  [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.

We need better "annotation" support to define ranges of params. For example:

```
@@throws(service-error) @@positiveValue int64 mintToken(tokenId: Address, @@positiveValue amount: int64)
```
Here `@@positiveValue` is added at 2 positions to define a better restriction.
