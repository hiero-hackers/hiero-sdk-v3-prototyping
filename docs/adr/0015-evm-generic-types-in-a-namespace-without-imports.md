# ADR-0015: EVM-generic types live in a namespace `evm` that imports nothing

**Status:** Accepted
**Date:** 2026-11-04

## Context

### The smart contract service needs an ABI model

The contract transactions (`ContractCreate`, `ContractUpdate`, `ContractExecute`, `ContractDelete`) are the largest
gap to the current SDKs and are specified next [1]. A contract call carries its arguments ABI-encoded "per the
Ethereum contract ABI standard" [2], and its result comes back in the same encoding. The Contract ABI is "the
standard way to interact with contracts in the Ethereum ecosystem" [3]: its types (`uint<M>`, `int<M>`, `address`,
`bool`, `bytes<M>`, `bytes`, `string`, arrays, tuples) and its encoding are defined without reference to any network
[3]. Most of the type hierarchy the contract work needs is therefore not Hiero-specific.

The only ABI-like model in the specs today is the one of the enterprise layer (`ParamSupplier`, `Param`,
`ContractCallResult`), which exists only in `enterprise.service.contract` [4].

### `EvmAddress` is EVM-generic but lives in `ledger`

`EvmAddress` is a 20-byte value whose text form is the EIP-55 checksum (Keccak-256 of the lowercase digits) [5][6]; it
has no shard, realm or entity number [5]. The ABI type `address` needs exactly this value, as will log and event
decoding. Today `EvmAddress` is declared in `ledger`. Inside `base` only `ledger` uses it, for the `evmAddress` slot
of `EvmCapableAddress` and in `ContractId.fromEvmAddress` / `AccountId.fromEvmAddress` [7]. Four specs outside `base`
import it from `ledger` [8].

### A spec folder is a build unit

Every spec folder becomes one module, package or crate of the generated code, and folders must not depend on each
other in a cycle [9]. A new folder for EVM types would add an artifact per language that `base` depends on. There is
no consumer outside this SDK today; a Maven plugin that generates Java interfaces from contract definitions is
conceivable, but not planned (assumption, stated in the design discussion).

### Keccak-256 is already needed in `base`

`keys` signs ECDSA messages over their Keccak-256 hash [10], and the Java implementation computes Keccak-256 in
`org.hiero.keys.internal` and in `EvmAddress` of the module `org.hiero.base` [11], with BouncyCastle as its only
cryptography dependency [12].

## Decision

We will declare the EVM-generic types in a new namespace `evm` in the spec folder `spec/base` (file
`spec/base/evm.md`):

- `EvmAddress` moves from `ledger` to `evm`. `ledger` and every other spec import it from `evm`.
- The ABI model (values, types, functions; their encoding and decoding) is declared in `evm`. Its shape is decided
  in that spec, not here.
- Event/log decoding and RLP (the data of `EthereumTransaction`) follow in `evm` when they are needed.
- **`evm` imports no other namespace.** Hiero-specific concepts stay outside: `ContractId` / `AccountId` and their
  EVM form, long-zero addresses, the system contract (precompile) addresses and `ContractFunctionResult` live in
  `ledger` and `consensusnode.*`, which may import `evm`.

The rule is recorded here and in the Design Notes of `evm.md`; the tooling does not enforce it.

Rejected alternatives:

- **A separate spec folder `spec/evm/`** (its own module). It would make the independence visible in the build, but
  costs an artifact per language for a consumer that does not exist yet, and would force Keccak-256 either into the
  public API of `evm` (so that `keys` can use it) or into two implementations [10][11]. It is the planned next step,
  not the first one (see Follow-ups).
- **ABI types in `ledger` or under `consensusnode`.** Nothing keeps them free of Hiero types there; an `address`
  parameter would soon accept a `ContractId`, and the model could not be extracted later without untangling it.
- **`EvmAddress` stays in `ledger`, `evm` imports it.** Saves the move now, but `evm` would depend on a Hiero
  namespace from the start, and extracting it later would have to make the move anyway.
- **A third-party library as the API** (web3j, ethers, alloy). Its types would become part of the SDK's API. An
  implementation may still use such a library internally; own code is preferred, and a library is only an option
  after a per-language assessment of its size, complexity, maintenance, origin and dependencies.

## Consequences

### Positive

- One ABI model for the consensus node, the mirror node and the enterprise layer instead of one per layer [4].
- `evm` can later become its own spec folder by moving `evm.md`; the namespace name and every `requires` stay the
  same, because a namespace already belongs to exactly one folder [9].
- The ABI can be tested without a network, against the published vectors of the ABI specification (e.g. the
  selector `0xcdcd77c0` of `baz(uint32,bool)`) [3].
- No new artifact, and `keys` and `evm` share one Keccak-256 implementation inside `org.hiero.base` [11].

### Negative

- Moving `EvmAddress` touches `ledger.md` (declaration, the tests of parsing and EIP-55) and the `requires` of four
  specs [7][8], and in `sdk-java/impl` the class moves from the package `org.hiero.ledger` to `org.hiero.evm`; the TCK
  runtime must follow.
- The independence of `evm` rests on review only. A single `requires … from ledger` in `evm.md` breaks it, and nothing
  reports it.
- The independence is prepared without a consumer; if none ever appears, the separation is structure without a
  measurable benefit.
- Until `evm` is extracted, an outside tool that wants the ABI still has to depend on all of `base`.

### Follow-ups

- Create `spec/base/evm.md` with `EvmAddress` and the ABI model, and spec tests from the vectors of the ABI
  specification [3].
- Move `EvmAddress` in the specs, in `sdk-java/impl` and in the TCK runtime.
- Replace the enterprise ABI model (`Param`, `ContractCallResult`) by `evm` once the ABI model exists [4].
- Decide separately how a transaction exposes its record content (the result of `ContractExecute`), see
  `docs/missing-features.md` §3.5 [13].
- **Revisit** when a consumer outside this SDK needs the ABI (such as a generator of contract interfaces): then move
  `evm.md` into its own spec folder, and decide whether Keccak-256 becomes public API of `evm`.

---

**References:**

1. `docs/missing-features.md`, §1.4 "Smart contract service" and §8 "Suggested prioritization".
2. `tooling/protobuf/consensus-node/services/contract_call.proto:67-68` (`functionParameters`: "The application binary
   interface (ABI) encoding of the function call per the Ethereum contract ABI standard").
3. Solidity documentation, "Contract ABI Specification", https://docs.soliditylang.org/en/latest/abi-spec.html
   (accessed 2026-11-04): definition, elementary types, example `baz(uint32,bool)` with selector `0xcdcd77c0`.
4. `spec/enterprise/service-contract.md:29-50` (`ParamSupplier`, `Param`, `ContractCallResult`).
5. `spec/base/ledger.md:200-219` (declaration of `EvmAddress`).
6. ERC-55 "Mixed-case checksum address encoding", status Final, https://eips.ethereum.org/EIPS/eip-55 (accessed
   2026-11-04).
7. `spec/base/ledger.md:227` (`EvmCapableAddress.evmAddress`), `:245` (`ContractId.fromEvmAddress`), `:270`
   (`AccountId.fromEvmAddress`); `spec/base/keys.md:948` mentions it only in an open question.
8. `spec/consensus-node-client/queries-accounts.md:33`, `spec/mirror-node-client/mirror-node-account.md:53`,
   `spec/mirror-node-client/mirror-node-contract.md:74`, `spec/enterprise/service-contract.md:22`.
9. `tooling/metalang/metalang-core/src/main/java/org/hiero/sdk/v3/metalang/generator/SpecFolders.java:17-19`;
   `CLAUDE.md`, "one spec folder is one module/package/crate of the generated code".
10. `spec/base/keys.md:141` (ECDSA signs the Keccak-256 hash of the message).
11. `sdk-java/impl/org.hiero.base/src/main/java/org/hiero/keys/internal/Keys.java:139,166`
    (`Secp256k1.keccak256`), `sdk-java/impl/org.hiero.base/src/main/java/org/hiero/ledger/EvmAddress.java:56`
    (EIP-55 over Keccak-256).
12. `sdk-java/impl/org.hiero.base/pom.xml:29` (`bcprov-jdk18on`).
13. `docs/missing-features.md`, §3.5 "Records / receipts — fields on `Receipt` / `Record`".
