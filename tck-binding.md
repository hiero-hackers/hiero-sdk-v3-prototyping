# Generated TCK servers: binding the existing Hiero TCK to the V3 API

> **Status:** idea, not a decision. Captured before a first spike (see [Spike](#spike)). Analysis based on
> [`hiero-ledger/hiero-sdk-tck`](https://github.com/hiero-ledger/hiero-sdk-tck) at revision `a3ea777`
> (2026-10-02).

## Goal

Run the **existing** Hiero SDK TCK unchanged against every V3 SDK, with the JSON-RPC server that the TCK needs
**generated** from the specs instead of hand-written per language. The generated server uses nothing but the public
V3 API, so a passing TCK run shows that the public API is sufficient for everything the TCK checks and behaves like the
V2 SDKs.

### Relation to the other TCK documents

[`TCK.md`](TCK.md) and [`tck-ideas.md`](tck-ideas.md) follow other approaches; they are compared in
[Comparison with the other TCK approaches](#comparison-with-the-other-tck-approaches). In short: this document makes
the spec-driven adapter generation that `TCK.md` only outlines concrete, and it is about **compatibility with the
existing TCK**, not about the V3 TCK that `tck-ideas.md` designs.

## How the TCK works

- A TypeScript/Mocha test driver (`src/tests/**`) sends **JSON-RPC 2.0** requests to a server that the SDK provides
  (default `http://localhost:8544`) and checks the responses, partly against the Mirror Node.
- **65 methods**, specified in `docs/test-specifications/**.md` (parameter table, output table, numbered test cases with
  boundary values): `setup`, `reset`, `setOperator`, `generateKey`, `ping`, `pingAll`, account (`createAccount`,
  `updateAccount`, `deleteAccount`, `transferCrypto`, `approveAllowance`, `deleteAllowance`, `getAccountInfo`, ...),
  token (`createToken`, `mintToken`, `airdropToken`, ...), file, topic, schedule, node, contract and Ethereum
  methods, and Mirror Node balance queries.
- **Conventions of the parameters:**
  - keys are DER-encoded hex strings; key lists and threshold keys are the hex of the protobuf `Key`;
  - amounts are tinybar as decimal strings, durations are seconds as strings, entity IDs are `"0.0.x"`;
  - every transaction method accepts `commonTransactionParams`: `transactionId`, `maxTransactionFee`,
    `validTransactionDuration`, `memo`, `regenerateTransactionId`, `signers` (additional private keys).
- **Results:** the method's output fields (e.g. `accountId`, `status` from the receipt).
- **Errors:** `-32001` *Hiero error* with `data.status` (the name of the precheck or receipt status, e.g.
  `KEY_REQUIRED`) when the network rejects a request; `-32603` *internal error* when the SDK rejects it before
  sending (e.g. an invalid key).

## Observation: the server is a uniform mapping

Almost every method follows the same flow:

1. convert the JSON parameters to API values;
2. create the transaction or query and set its attributes;
3. apply the `commonTransactionParams`;
4. sign with the operator and the additional signers, submit, query the receipt;
5. return the requested receipt or query fields — or map the error to `-32001`/`-32603`.

What varies between methods is only *which* type is created, *which* parameter goes to *which* attribute with *which*
conversion, and *which* fields form the result. That is data, not code — and it can be declared once, language
neutrally, and turned into a server per language by the generators of `tooling/metalang`, which already know how
every API element is named and typed in Java, TypeScript and Rust.

## Proposal

### 1. A binding per TCK method

A language-neutral binding file (e.g. `tck/bindings/crypto-service.md`) declares each TCK method against the
meta-language model. Sketch (syntax to be designed in the spike):

```
binding createAccount -> AccountCreateTransaction {
    key                       -> authority                      via authority
    initialBalance            -> initialBalance                 via tinybar
    receiverSignatureRequired -> receiverSignatureRequired
    autoRenewPeriod           -> autoRenewPeriod                via seconds
    memo                      -> accountMemo
    maxAutoTokenAssociations  -> maxAutomaticTokenAssociations
    stakedAccountId           -> stakedAccountId                via accountId
    stakedNodeId              -> stakedNodeId                   via int64
    declineStakingReward      -> declineStakingReward
    alias                     -> alias                          via hexBytes
    result accountId = receipt.accountId
    result status    = receipt.status
}

binding getAccountInfo -> AccountInfoQuery {
    accountId -> accountId via accountId
    result *  = response    // all attributes, named like the TCK output table
}
```

The binding names TCK parameters on the left and model elements on the right, so the existing validator can check it:
every target type and attribute exists, the converter produces the attribute's type, every result path exists.

### 2. A fixed catalogue of converters

A small set of named conversions between the TCK's JSON conventions and API values, implemented once per language
(as support files, like `guidelines/*-files`): `tinybar` (string → `NativeToken`), `seconds`, `int64`, `accountId`,
`tokenId`, `hexBytes`, `privateKey`/`publicKey` (DER hex), `authority` (DER hex of a key, or protobuf hex of a key list
or threshold key), `transactionId`, and the reverse directions for results (`status` → status name, IDs → `"0.0.x"`).

### 3. A generator for the TCK server

`metalang generate --language=<lang> --target=tck-server` generates per language: the JSON-RPC dispatch, one handler
per binding (parameter conversion, attribute setters, common parameters, signing, submission, result mapping), the
error mapping, and the utility methods (`setup`, `reset`, `setOperator`, `generateKey`). The handlers call the public
API only; method names, overload names and types come from the same model as the generated API, so a spec change
that breaks a binding fails the build, not the TCK run.

### 4. A coverage check against the TCK

A check reads the parameter tables of the TCK test specifications and reports TCK methods or parameters without
binding, and bindings for parameters the TCK no longer has. Like `metalang check`, it keeps the bindings and the
TCK in sync deterministically.

### What is deterministic

The bindings, the generated servers and the coverage check. The TCK runs themselves are not: they go against a real
network (Solo, local node or testnet).

## What changes when the TCK changes

The test definitions of the existing TCK — the TypeScript tests in `src/tests` and the test specifications — are used
unchanged. When the TCK gets new tests, the work depends on what they need:

| A new test needs ... | Change |
|---|---|
| only existing JSON-RPC methods and parameters (e.g. another boundary value for `createAccount`) | **nothing** — no binding, no code in any language |
| a new method or a new parameter with a known value form (tinybar, seconds, ID, DER key) | **the binding only** (once, language neutral), then the servers are regenerated — no hand-written code in any language |
| a value form that the converter catalogue does not know yet (e.g. a new key encoding) | **a new converter per language** — a small, isolated function |

So the only hand-written code per language is the converter catalogue (and the JSON-RPC runtime), and it only grows
with new value forms. Three limits apply:

1. **Methods that do not fit the uniform flow.** Some TCK methods do not follow "set the parameters → sign → submit →
   receipt" (`transferCrypto` with lists of transfers, `airdropToken`, topic messages with chunks, the Mirror Node
   queries). Either the binding format can express them, or they need a hand-written handler per language as escape
   hatch. This is the most important question for the [spike](#spike): it decides whether "no code per language"
   holds always or only mostly.
2. **Functionality missing in the V3 API needs a spec change first** (e.g. the contract service, see
   [Gaps in the V3 API](#gaps-in-the-v3-api)). The generators produce the API in all languages from it; the
   *implementation* of the new API methods is written per language by the SDK teams — the generators produce the API
   with stubs only. That is SDK development, not TCK binding.
3. **What is tested is the SDK implementation.** The generated server only calls the public API; whether a test passes
   is decided by the hand-written implementation behind that API — which is exactly what the TCK is meant to check.

The [coverage check](#4-a-coverage-check-against-the-tck) makes changes of the TCK visible: a new TCK method or
parameter without binding, or a binding for a parameter the TCK no longer has, is reported. A change of the TCK
becomes the task "extend the binding" instead of a silent failure of a TCK run.

## Gaps in the V3 API

The TCK only uses the public API, so the public API is the right — and in principle sufficient — basis. Today the
V3 specs lack what these TCK features need:

| TCK needs | State of the V3 specs |
|---|---|
| The status of a rejected request (`-32001` with `data.status`) | `submit`, `signWithOperatorAndSubmit` and `queryReceipt` ([`spec/consensus-node-client/transactions.md`](spec/consensus-node-client/transactions.md)) declare no `@@throws`; there is no error type that carries a `TransactionStatus`. |
| The names of all HAPI status codes (`KEY_REQUIRED`, `INVALID_INITIAL_BALANCE`, ...) | `BasicTransactionStatus` is explicitly incomplete. |
| Key lists and threshold keys as protobuf hex, in both directions (input of every key parameter, output of `generateKey`) | `Authority` ([`spec/base/authority.md`](spec/base/authority.md)) has no `fromBytes`/`toBytes`. |
| A DER key without knowing whether it is private or public | `createPrivateKey(string)`/`createPublicKey(string)` exist ([`spec/base/keys.md`](spec/base/keys.md)); the converter can try both, an API function would make this explicit. |
| `commonTransactionParams.transactionId` (a given transaction ID) and `regenerateTransactionId` | `pack`/`sign` create the transaction ID; it cannot be given. |
| `signers`: sign additionally with a bare private key | `PackedTransaction.sign` takes an `Account`, a `TransactionSigner` or node signatures; a signer for a `PrivateKey` is not specified. |
| Contract service, `createEthereumTransaction`, `submitTopicMessage`, `updateTokenFeeSchedule`, `ping`/`pingAll` | No corresponding transactions or methods in `spec/`. |

Most of the remaining methods have a counterpart (account, token, file, schedule, topic and node transactions and
queries; `setup` maps to `NetworkSetting`/`ConsensusNode`/`MirrorNode` and `createClient`). Differences in names
(`memo` ↔ `accountMemo`, `maxAutoTokenAssociations` ↔ `maxAutomaticTokenAssociations`, `key` ↔ `authority`) belong
in the bindings, not in the API.

## Comparison with the other TCK approaches

The three documents answer different questions:

- [`TCK.md`](TCK.md): how can the **existing** TCK be used for V3 and extended?
- [`tck-ideas.md`](tck-ideas.md): how is a **new** V3 TCK built?
- this document: how are V3 SDKs bound to the existing TCK **deterministically**?

### Overview

| | `TCK.md` | `tck-ideas.md` | This document |
|---|---|---|---|
| Test suite | The existing TCK, extended with new namespaces (`keys.*`, `mirror.*`, `enterprise.*`) | A new repository `hiero-sdk-v3-tck`, tests written anew | The existing TCK, unchanged |
| Who writes the tests | The TCK project (TypeScript/Mocha) plus new specification documents | Option A: Java/JUnit; Option B: a scenario DSL in the meta-language | Nobody anew: the existing 65 methods and their test specifications |
| Bridge to the SDK | A JSON-RPC adapter per language, hand-written; generation only as an outlook | A: a driver per SDK, wire protocol open; B: no bridge, native tests generated per language | A JSON-RPC server per language, **generated** from bindings and a converter catalogue |
| Effort per new language | An adapter by hand | A: a driver by hand; B: a code generation backend | A server backend for the generator (the API generators exist) plus the converters |
| What it shows | V3 behaves like V2 on the network, plus V3 areas in new namespaces | Conformance with the V3 API, including V3 specifics (edge cases, `@@oneOf`, `ZERO_ADDRESS`) | V3 passes the same behaviour tests as the V2 SDKs, using the public API only |

### Where they really differ

**Compatibility versus conformance.** `TCK.md` and this document measure V3 against the *existing* behavioural
standard: what the TCK checks is the established expectation of a Hiero SDK. `tck-ideas.md` measures V3 against *its
own specs* and can test concepts the TCK does not know: `@@async`/`@@streaming` as a contract, generics, `@@oneOf`,
the SPI. These are two different kinds of evidence; neither replaces the other.

**The driver question** — the main cost driver:

- `TCK.md`: one hand-written adapter per language, maintained N times. This is exactly the problem `tck-ideas.md`
  names.
- `tck-ideas.md`: Option A has the same problem; Option B removes it (no drivers at all). Both reject the V2 runner
  as the basis of the V3 TCK.
- This document: the driver remains, but as generated code. Maintenance shrinks to the bindings (once) and the
  converters per language (small and stable). It is the concrete form of what `TCK.md` outlines in "How to extend it
  for V3".

**The objections of `tck-ideas.md` against the V2 approach** apply here as well, but weigh less:

| Objection | Does it affect this document? |
|---|---|
| TypeScript cannot distinguish synchronous from asynchronous calls syntactically | Yes, but it does not matter: the TCK tests network behaviour, not the async contract. That contract is checked by the generated unit tests of each language (`block_on`, `Promise`, `CompletableFuture`). |
| JSON loses generics (`Transaction<$$Receipt>`) | Yes. The generated server is typed, though: the binding states which concrete type is created. Type fidelity lives in the server, not on the wire. |
| Re-targeting locks in V2's driver shape | No, as long as the goal is compatibility and not the V3 test suite. |

**Dependence on a stable spec.** `tck-ideas.md` rates Option B as risky while the spec still moves: the code generation
chases the spec. This document carries that risk on a small scale, too, but `tooling/metalang` already maintains the
API generators, and a binding that no longer fits is a build error instead of a failed TCK run. Hand-written adapters
(`TCK.md`, Option A) break silently.

**Coverage of the V3 areas.** This document covers only what the TCK tests today, essentially `consensusnode.*`;
nothing for `base.*`, `mirrornode.*`, `enterprise.*` or the SPI. `TCK.md` covers these areas with new TCK namespaces,
`tck-ideas.md` with its own scenarios.

**Network.** `tck-ideas.md` settles on Solo with one instance per run. The TCK supports testnet, local node and custom
networks; Solo fits as a custom network, and `setup` maps to `NetworkSetting`.

### How they fit together

The approaches can be layered rather than chosen exclusively:

1. **Short term — this document.** With moderate effort it shows that the V3 API covers the functionality of the V2
   SDKs, makes the API gaps visible (error and status model, `Authority` bytes, signing with a bare key), and uses a
   broad existing test base (65 methods, about 13,000 lines of test specifications).
2. **Medium term — `tck-ideas.md`, Option B,** once the spec is stable, for the V3-specific concepts. The
   infrastructure of layer 1 can be reused: bindings and the scenario DSL would both be extensions of the
   meta-language with the same code generation.
3. **`TCK.md` as the frame.** Its extensions (`mirror.*`, `enterprise.*`, the Mirror Node oracle as a problem) stay
   relevant if the existing TCK is to be extended for the V3 areas instead of testing them in the new V3 TCK. That is
   the actual open question between `TCK.md` and `tck-ideas.md`; this document does not decide it.

Two real conflicts remain:

- `TCK.md` proposes *hand-written* adapters, this document *generated* ones; this document replaces that part of
  `TCK.md`.
- `tck-ideas.md` rejects the V2 runner — as the basis of the V3 TCK. For a compatibility check that rejection
  applies only in part; this should be stated explicitly in the team, otherwise this document reads like a step back.

## Spike

1. Design the binding format and the converter catalogue for the crypto-service methods (`createAccount`,
   `updateAccount`, `deleteAccount`, `transferCrypto`, `getAccountInfo`) plus `setup`, `reset` and `generateKey`.
2. Record the API gaps above as open points in the `## Questions & Comments` of the affected specs.
3. Generate the TCK server for one language from the bindings (against stubs of the generated API it compiles, but
   cannot pass); evaluate whether the bindings are complete enough to need no hand-written handler code.
4. Implement the coverage check for the crypto-service test specifications.

## Open questions

- **Format of the bindings:** an extension of the meta-language (new start rule, like `## Default Instances`) or a
  separate format? An extension gets parsing, linking and validation for free.
- **Methods that do not fit the uniform flow** (`transferCrypto` with lists of transfers, `airdropToken`,
  `submitTopicMessage` with chunks, the Mirror Node queries): can the binding express them, or do they need a
  hand-written handler as escape hatch?
- **Error mapping:** which errors of the API are `-32001` (network rejected) and which `-32603` (SDK rejected) — to be
  derived from the `@@throws` ids once the gaps above are closed.
- **Hedera specifics in the TCK** (status strings, HBAR as fee unit) versus V3's network-neutral API (see
  [`TCK.md`](TCK.md), "Caveats"): the converters are the place to map them.
- **Where the bindings live:** in this repository next to `spec/`, or in the TCK repository.
