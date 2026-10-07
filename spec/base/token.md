# Token API

## Description

Types that classify **HTS (Hedera Token Service) tokens** — first-class, protocol-level tokens issued
and managed directly on the consensus node, as opposed to the *native* token of the network (HBAR).

- `TokenType` tells whether a token is a divisible currency (`FUNGIBLE_COMMON`) or a collection of
  unique, serial-numbered items (`NON_FUNGIBLE_UNIQUE`).
- `TokenSupplyType` tells whether the supply of a token is unbounded (`INFINITE`) or capped by a
  maximum supply (`FINITE`).

Both are chosen when a token is created and cannot be changed afterwards. The same types are used
when creating tokens and when reading token information from a mirror node.

## Design Notes

The native token is modelled separately under [`nativeToken`](native-token.md).

This namespace is intentionally narrow today: it carries only the two enums that classify a token
(`TokenType`, `TokenSupplyType`). Both the write side
([`consensusnode.transactions.tokens`](../consensus-node-client/transactions-tokens.md)) and the
read side ([`mirrornode.token`](../mirror-node-client/mirror-node-token.md)) need exactly the same
value set, so the enums live here to avoid duplication and to keep the two surfaces in lock-step.

This namespace is also the natural future home for the typed identifier types tracked in
[`missing-features.md`](../../docs/missing-features.md) section 3.1 — `TokenId`, `NftId` (token id +
serial), and `PendingAirdropId` — once those are promoted from the generic `Address` placeholder.

## API Schema

```
namespace token

// Kind of a token: divisible currency (FUNGIBLE_COMMON) or unique-serial collection
// (NON_FUNGIBLE_UNIQUE). Set when the token is created; cannot be changed by a token update.
enum TokenType {
    FUNGIBLE_COMMON
    NON_FUNGIBLE_UNIQUE
}

// Supply policy of a token: INFINITE → no protocol-enforced ceiling; FINITE → `totalSupply ≤
// maxSupply` is enforced at every mint. Set when the token is created; cannot be changed by a
// token update.
enum TokenSupplyType {
    INFINITE
    FINITE
}
```

## Questions & Comments

- **`TokenId` / `NftId` / `PendingAirdropId` are not yet defined here.** Today both the write side
  and the read side reference token / NFT ids as the generic `Address` from `ledger`. Promoting
  them to typed identifiers is tracked in
  [`missing-features.md`](../../docs/missing-features.md) section 3.1; this namespace is the
  intended landing place.