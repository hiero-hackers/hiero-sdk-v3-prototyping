# ADR-0018: An account signs through a `TransactionSigner`, not a `PrivateKey`

**Status:** Accepted
**Date:** 2026-11-24

## Context

### The operator key must be in memory

`Account`, the type that pays for and signs requests, holds `accountId` and `privateKey: PrivateKey` [1]. A
`HieroClient` is created from an `Account` as its operator [2], so the operator's private key must be held in the
process. That excludes keys in a cloud KMS, an HSM, a hardware wallet or a wallet bridge; `docs/missing-features.md`
§6.1 lists this as a gap [3].

### Two signing paths exist today

The external case was added beside `Account` instead of through it:

- `HieroClient.transactionSigner` signs for the operator, "by default the operator account's private key" [4]; a
  second and third `createClient` overload take a `TransactionSigner` [5], and `enterprise.createSession` repeats the
  pair [6].
- `Transaction` has a tier per path: `sign(payer: Account, nodes)` and `sign(payerId, signer, nodes)`,
  `signForBatch(payer: Account, batchKey)` and `signForBatch(payerId, signer, batchKey)` [7].
- `PackedTransaction` has `sign(account)` and `sign(signer)` [7].

A caller whose operator key is in a KMS still needs an `Account` with a `PrivateKey` plus a separate signer.

### What PR #26 showed

PR #26 implemented the V3 signing API over the V2 JavaScript SDK with AWS KMS and HashiCorp Vault signers [8]. Its
contract for external signers (async `signTransaction(body: NodeBody)`, `publicKey` on the signer, the `rule:signer-*`
rules) was adopted in commit `9870723d` [9]. The library modeled `Account { accountId, signer }`; the spec recorded
this as open because it needs an ADR of its own [10].

### What an `Account` is

An `Account` is not the account of the network but an account id together with one way to sign for it. An account
with a 2-of-3 authority has three holders, each building an `Account` with the same id and their own key, as in the
multi-sig example of the transactions spec [11]. Equality of `Account` therefore has to include how it signs; with
equality by `accountId` alone, three custodians would collapse into one element of a set. The meta-language compares a
value type by all of its fields [12], which gives exactly that.

## Decision

We will make the signer part of the account and remove the second signing path:

```
Account {
    @@immutable accountId: AccountId
    @@immutable signer: TransactionSigner
    @@static Account of(accountId: AccountId, privateKey: PrivateKey)   // = Account{accountId, TransactionSigner.of(privateKey)}
}

abstraction TransactionSigner {
    @@immutable publicKey: PublicKey
    @@threadSafe @@async NodeSignature signTransaction(body: NodeBody)
    @@static TransactionSigner of(privateKey: PrivateKey)               // publicKey = privateKey.createPublicKey()
}
```

- A key in memory is one `TransactionSigner`, created by `TransactionSigner.of(privateKey)`. There is no public type
  for it: its `publicKey` is derived from the private key, and a constructor taking both could receive a pair that
  does not match.
- Two signers created by `of(privateKey)` are equal if their key is equal. Other implementations define their own
  equality. `Account` keeps the value equality over both fields.
- Removed: `HieroClient.transactionSigner`; the `createClient` and `createSession` overloads that take a
  `TransactionSigner` (the one with `ExecutionSettings` keeps that parameter only); `Transaction.sign(payerId, signer,
  nodes)`; `Transaction.signForBatch(payerId, signer, batchKey)`; `PackedTransaction.sign(account)`.
- Kept: `signWithOperator`, `sign(payer: Account, nodes)`, `signForBatch(payer: Account, batchKey)`,
  `PackedTransaction.sign(signer)` and `sign(signatures)`. A language SDK may add `sign(account)` as a convenience
  overload that calls `sign(account.signer)`.
- Every signing method is `@@async`, as `signTransaction` already is [13].

**Rejected alternatives:**

- *An `Account` subtype that holds a key* (`KeyAccount extends Account`). The variation is in how to sign, not in the
  account; the subtype would hold the key and a signer side by side and invite `instanceof` checks for a synchronous
  path, which is the second path this decision removes.
- *`Account.publicKey` as a field of its own.* It duplicates `signer.publicKey` and could contradict it.
- *Equality of `Account` by `accountId` only.* Breaks multi-sig (see Context) and cannot be expressed by the
  meta-language without a hand-written `equals` [12].
- *A public `PrivateKeySigner { privateKey }`.* It inherits `publicKey`, so its constructor would accept a public key
  that does not belong to the private key.
- *Keeping both paths* (status quo). Every new signing entry point needs two variants, and a KMS operator still needs
  a `PrivateKey`.

**Why the keys of `of(privateKey)` match:** the public key of a private key is derived deterministically (Ed25519
from the seed, ECDSA secp256k1 as d·G) [14], so `of(privateKey)` cannot produce a mismatched pair. `KeyPair { publicKey,
privateKey }` [15] gives no such guarantee today: its constructor accepts any two keys. Whether it checks
`privateKey.createPublicKey() == publicKey` (a comparison of bytes, as an ECDSA public key is always held as the
compressed point [16]) is not part of this decision.

## Consequences

### Positive

- One signing path. An operator or payer with a key in a KMS, HSM or hardware wallet is an ordinary `Account`, which
  closes `missing-features.md` §6.1 [3] for transactions and query payments.
- Fewer methods on `HieroClient`, `Transaction`, `PackedTransaction` and `enterprise` [4][5][6][7].
- The one-line case stays one line: `Account.of(accountId, privateKey)`.
- `pack(payer: Account, …)` no longer forces a caller to hold a private key, the problem raised in the
  `transactions.md` questions [17].

### Negative

- Signing with a key in memory becomes asynchronous: `sign(payer, nodes)`, `signForBatch(payer, batchKey)` and the
  former `PackedTransaction.sign(account)` were synchronous [7]. Synchronous callers block per language (e.g.
  `join()` in Java). Spec tests are unaffected: the test language awaits `@@async` calls implicitly [13].
- The private key cannot be read back from an `Account`. Code that needs it keeps its own reference.
- A breaking change across specs, examples and tests: every `createClient(…, transactionSigner)`, every
  `sign(payerId, signer, …)` and every `sign(account: …)` changes, and `sdk-java/impl` has to follow.
- The CLPR ownership proofs are **not** solved: `ClprChannelOwnershipProof.create(channelId, privateKey)` and
  `ClprConnectorOwnershipProof.create(…, privateKey)` sign bytes that are not a `TransactionBody` [18], so a
  `TransactionSigner` does not fit them. They keep taking a `PrivateKey`, and HSM keys stay excluded there.

### Follow-ups

- Apply the decision to `client.md`, `transactions.md`, `transactions-batch.md`, `enterprise/service.md` and the
  examples and tests that use the removed methods (done with the acceptance); implement it in `sdk-java/impl`
  (done).
- TODO: revisit the configurable payer of paid queries. ADR-0002 deferred it partly because `payer: Account` could
  only hold a key in memory and an external-signer variant had to be designed separately [19]; with this decision a
  `payer: Account` covers both. ADR-0002 stays deferred until that review.
- CLPR ownership proofs with keys outside the process: decide whether a general signing abstraction (bytes in,
  signature out) is needed; record it as open in the CLPR spec.
- Remove the resolved open point from the `client.md` questions [10] and §6.1 from `missing-features.md` [3].
- **Revisit** if the forced asynchrony of in-memory signing shows real costs in a target language (e.g. Python or
  Go); then bring back a synchronous variant for signers created by `of(privateKey)`.

---

**References:**

1. `spec/consensus-node-client/client.md:206` — `Account { accountId, privateKey }`.
2. `spec/consensus-node-client/client.md:391` — `createClient(networkSettings, operatorAccount)`.
3. `docs/missing-features.md:204` — §6.1 "Operator identity is coupled to an in-memory `PrivateKey`".
4. `spec/consensus-node-client/client.md:379` — `HieroClient.transactionSigner`.
5. `spec/consensus-node-client/client.md:395`, `:399-400` — `createClient` overloads with a `TransactionSigner`.
6. `spec/enterprise/service.md:114`, `:117` — `createSession` overloads.
7. `spec/consensus-node-client/transactions.md:195-269` — `pack`, the `sign` and `signForBatch` tiers,
   `PackedTransaction.sign(account | signer | signatures)`.
8. https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/pull/26 (open, accessed 2026-11-24).
9. Commit `9870723d` "feat: asynchronous TransactionSigner and the external signing contract (PR #26)".
10. `spec/consensus-node-client/client.md:803-808` — open point "`Account` still requires a `PrivateKey`".
11. `spec/consensus-node-client/transactions.md:412-437` — example 4, "Multi-sig collected across processes".
12. `guidelines/api-guideline.md:1400` — `equals(other: ANY)` may only be declared explicitly; value types otherwise
    compare by their fields.
13. `spec/consensus-node-client/client.md` "External signing" (`signTransaction` is async); `guidelines/test-language.md:431`
    — `@@async` calls are awaited implicitly.
14. `spec/base/keys.md:147` — `PrivateKey.createPublicKey()`; RFC 8032 §5.1.5 (Ed25519 key generation); SEC 1 v2 §3.2.1
    (EC key pair, Q = d·G).
15. `spec/base/keys.md:118` — `KeyPair`.
16. `spec/base/keys.md:39` — an ECDSA public key is held as the compressed point.
17. `spec/consensus-node-client/transactions.md:876-878` — "`pack(payer: Account, nodes)` only needs the payer's id".
18. `spec/consensus-node-clpr/transactions.md:129`, `:148` — ownership proofs take a `PrivateKey`.
19. `docs/adr/0002-defer-paid-query-payer-customization.md:34`.
