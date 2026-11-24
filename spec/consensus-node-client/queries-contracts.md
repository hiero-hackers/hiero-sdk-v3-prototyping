# Contract Queries API

This namespace defines the read-side counterpart to the contract transactions in
[`transactions-contracts.md`](transactions-contracts.md): a local call of a contract function and the two queries for
the state of a contract. All three queries are *paid*.

## Description

Queries for reading smart contracts directly from a consensus node. All three are paid, so you can bound the spend
with `maxQueryPayment`, ask for the price up front with `getCost(client)`, and read the actually paid `cost` from the
`PaidQueryResponse`.

- **`ContractCallQuery`** — calls a function of a contract on the node that answers the query, without consensus. The
  call must not change any state: it is executed like an EVM `STATICCALL`, so it suits view and pure functions. It
  creates no transaction, no receipt and no record, and it cannot transfer native tokens. It returns the
  `ContractFunctionResult` of the call.
- **`ContractInfoQuery`** — returns the properties of a contract: its account, EVM address, admin authority, memo,
  expiration, renewal, storage, balance and staking. It does not return the code; use `ContractBytecodeQuery` for that.
- **`ContractBytecodeQuery`** — returns the runtime bytecode of a contract: the code the EVM executes, not the
  initcode that deployed it.

### The local call

A `ContractCallQuery` names the contract (`contractId`), the call data (`callData`, the selector of the function
followed by its ABI-encoded arguments, see `AbiFunction.encodeCall`) and the gas the call may use (`gas`). Decode
what the function returned with `AbiFunction.decodeResult(result.result)` of the namespace `evm`.

Unlike a `ContractExecuteTransaction`, the local call is charged for **all** of its `gas`, not for the gas it used, so
`gas` should be close to what the call needs. The price that `getCost(client)` returns and that `submit(client)` pays
already contains the gas.

The call is sent by the operator account of the client, which pays for the query. Set `senderId` to execute the call
as another account (`msg.sender` in Solidity); the payment of the query must then be signed by that account as well,
which the SDK does not do today (see *Questions & Comments*).

A call that reverts, runs out of gas or fails otherwise is rejected by the node: `submit(client)` fails as described
in "The outcome of an attempt" of the client, with the status of the node (for example `CONTRACT_REVERT_EXECUTED` or
`INSUFFICIENT_GAS`).

For calls that do not need the answer of a consensus node, the mirror node offers a free, simulated call of a contract
and a gas estimation (`ContractRepository.call` of `mirrornode.contract`).

### Deleted contracts

Querying a deleted contract does not fail: `ContractInfoQuery` returns a `ContractInfo` with `deleted = true`. Check
`ContractInfo.deleted` instead of treating empty bytecode as proof of deletion.

## Design Notes

All three queries extend `PaidQuery`; see [`queries.md`](queries.md) for the payment and envelope semantics.

- **The result type of the transactions.** `ContractCallQuery` returns the `ContractFunctionResult` of
  `consensusnode.transactions` (HAPI's `ContractCallLocalResponse.functionResult` is the same message as the result in
  the record), so a local call and a `ContractExecuteTransaction` are decoded the same way. Fields a local call does
  not fill (`amount`, `signerNonce`, `contractNonces`, the logs of a view function) are empty or zero.
- **`callData`, not `functionParameters`**, and `gas` `@@min(0)`, as in `ContractExecuteTransaction` (see
  [`transactions-contracts.md`](transactions-contracts.md)).
- **No `setFunction(name, parameters)` convenience.** The V2 SDKs build the call data from a function name and
  `ContractFunctionParameters`; V3 builds it with the ABI types of [`evm.md`](../base/evm.md)
  (`AbiFunction.encodeCall`). Typed calls with the arguments as values of the programming language belong to
  `enterprise.service.contract`.
- **Not exposed:** `ContractCallLocalQuery.maxResultSize` (deprecated, ignored by the network);
  `ContractInfo.tokenRelationships` (deprecated by HIP-367, read from the mirror node); `ContractInfo.ledger_id` (see
  the envelope question in [`queries.md`](queries.md)); and of HAPI's `StakingInfo` only the configuration
  (`stakedAccountId`, `stakedNodeId`, `declineStakingReward`) is exposed, like in `AccountInfo`
  ([`queries-accounts.md`](queries-accounts.md)).
- **`ContractInfo.evmAddress` is an `EvmAddress`, not text.** HAPI returns `contractAccountID` as a hex string of the
  20-byte EVM address; every contract has one (the long-zero form of its id or the address of a `CREATE2`), so the
  field is not nullable.
- **`ContractInfo.storage` is `int64`** like HAPI: the bytes of contract storage the contract uses.

## API Schema

```
namespace consensusnode.queries.contracts
requires {AccountId, ContractId} from ledger
requires {EvmAddress} from evm
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {PaidQuery} from consensusnode.queries
requires {ContractFunctionResult} from consensusnode.transactions

// Paid query that calls a function of a contract on the answering node, without consensus. The call must not change
// state; it creates no transaction and no record. The query is charged for the full `gas`, not for the gas used.
@@finalType
ContractCallQuery extends PaidQuery<ContractFunctionResult> {
    @@immutable contractId: ContractId                              // the contract that is called
    @@immutable @@min(0) gas: int64                                  // the gas the call may use; all of it is charged
    // The selector of the function followed by its ABI-encoded arguments (see `AbiFunction.encodeCall`). Empty calls
    // the receive or fallback function of the contract.
    @@immutable @@default([]) callData: bytes
    // The account the call is executed as (`msg.sender`); the operator account of the client if unset.
    @@immutable @@nullable senderId: AccountId
}

// The properties of a contract. Returned by `ContractInfoQuery`.
@@oneOrNoneOf(stakedAccountId, stakedNodeId)
ContractInfo {
    @@immutable contractId: ContractId
    @@immutable accountId: AccountId                                // the account of the contract, which holds its balance
    @@immutable evmAddress: EvmAddress                              // the 20-byte EVM address of the contract
    // Allows to update and delete the contract; absent if the contract is immutable.
    @@immutable @@nullable adminAuthority: Authority
    @@immutable expirationTime: zonedDateTime
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable autoRenewAccount: AccountId              // pays the renewal; if absent, the contract pays
    @@immutable storage: int64                                       // the bytes of storage the contract uses
    @@immutable @@default("") contractMemo: string                   // empty if the contract has no memo
    @@immutable balance: NativeToken<ANY, ANY>                       // the native-token balance of the contract
    @@immutable @@default(false) deleted: bool
    @@immutable @@default(0) maxAutomaticTokenAssociations: int32    // -1: unlimited
    @@immutable @@nullable stakedAccountId: AccountId                // mutually exclusive with stakedNodeId
    @@immutable @@nullable stakedNodeId: int64                       // mutually exclusive with stakedAccountId
    @@immutable @@default(false) declineStakingReward: bool
}

// Paid query for the properties of a contract. Does not return the code of the contract — use
// `ContractBytecodeQuery` for that.
@@finalType
ContractInfoQuery extends PaidQuery<ContractInfo> {
    @@immutable contractId: ContractId
}

// The runtime bytecode of a contract. Returned by `ContractBytecodeQuery`.
ContractBytecode {
    @@immutable contractId: ContractId                              // the contract this bytecode belongs to
    @@immutable bytecode: bytes                                      // the code the EVM executes, not the initcode
}

// Paid query for the runtime bytecode of a contract.
@@finalType
ContractBytecodeQuery extends PaidQuery<ContractBytecode> {
    @@immutable contractId: ContractId
}
```

## Default Instances for Testing

```
// The contract and the sender of the contract query tests of the Java SDK (ContractCallQueryTest,
// ContractInfoQueryTest, ContractByteCodeQueryTest).
values JAVA_SDK_QUERIED_CONTRACT: ContractId = ContractId.fromString(value: "0.0.5005")
values JAVA_SDK_SENDER: AccountId = AccountId.fromString(value: "1.2.3")

// An EVM address of a contract. The Java SDK's ContractInfoTest uses "3", which is no EVM address; this is the address
// of the EvmAddress tests of the namespace evm.
values CONTRACT_EVM_ADDRESS: EvmAddress = EvmAddress.fromString(value: "0x1234567890abcdef1234567890abcdef12345678")
```

## Examples

### Call a view function

```
HieroClient client = ...;
AbiFunction balanceOf = AbiFunction.parse("balanceOf(address) returns (uint256)");

ContractFunctionResult result = new ContractCallQuery()
    .contractId(tokenContract)
    .gas(30_000)                                        // charged in full
    .callData(balanceOf.encodeCall(List.of(new AbiAddress(holder))))
    .submit(client)
    .value;

BigInteger balance = ((AbiUint) balanceOf.decodeResult(result.result).get(0)).value();
```

### Read the properties and the code of a contract

```
ContractInfo info = new ContractInfoQuery()
    .contractId(contractId)
    .submit(client)
    .value;

if (info.deleted) {
    return;                                             // the contract no longer exists
}
EvmAddress address = info.evmAddress;
Authority admin = info.adminAuthority;                  // null if the contract is immutable

bytes code = new ContractBytecodeQuery()
    .contractId(contractId)
    .submit(client)
    .value
    .bytecode;
```

## Testing

Offline tests of the contract queries and their result types, written as given / when / then. Nothing is submitted;
the call of a contract, its cost and the answers of the network are tested by the TCK. The comment above each test
names where the current SDKs test the same.

```tests
// Source: the Java SDK (ContractCallQueryTest.shouldSerialize and setFunctionParameters: contract 0.0.5005, gas 1541,
// sender 1.2.3).
@@testId("a-contract-call-query-keeps-its-attributes")
test "a contract call query keeps its attributes" {
    // given
    let callData = hex(text: "182b0b")

    // when
    let query = ContractCallQuery{contractId: JAVA_SDK_QUERIED_CONTRACT, gas: 1541, callData: callData,
            senderId: JAVA_SDK_SENDER}

    // then
    assertEquals(expected: JAVA_SDK_QUERIED_CONTRACT, actual: query.contractId)
    assertEquals(expected: 1541, actual: query.gas)
    assertEquals(expected: callData, actual: query.callData)
    assertEquals(expected: JAVA_SDK_SENDER, actual: query.senderId)
    assertNull(actual: query.maxQueryPayment)
}

// Source: this spec (@@default([]) of callData, @@nullable of senderId: the operator account is the sender).
@@testId("a-contract-call-query-without-call-data-and-sender-has-empty-call-data-and-no-sender")
test "a contract call query without call data and sender has empty call data and no sender" {
    // when
    let query = ContractCallQuery{contractId: JAVA_SDK_QUERIED_CONTRACT, gas: 0}

    // then
    assertEquals(expected: 0, actual: length(value: query.callData))
    assertNull(actual: query.senderId)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz(uint32,bool) with 69 and
// true).
@@testId("the-call-data-of-a-contract-call-query-is-the-selector-and-the-abi-encoded-arguments")
test "the call data of a contract call query is the selector and the ABI-encoded arguments" {
    // given
    let baz = evm.AbiFunction.parse(signature: "baz(uint32,bool) returns (bool)")

    // when
    let query = ContractCallQuery{contractId: JAVA_SDK_QUERIED_CONTRACT, gas: 1541,
            callData: baz.encodeCall(arguments: [evm.AbiUint{value: 69, bits: 32}, evm.AbiBool{value: true}])}

    // then
    assertEquals(expected: hex(text: "cdcd77c0"
            + "0000000000000000000000000000000000000000000000000000000000000045"
            + "0000000000000000000000000000000000000000000000000000000000000001"), actual: query.callData)
}

// Source: this spec (@@min(0) of gas, as for ContractExecuteTransaction).
@@testId("a-contract-call-query-rejects-negative-gas")
test "a contract call query rejects negative gas" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractCallQuery{contractId: JAVA_SDK_QUERIED_CONTRACT, gas: -1}
}

// Source: the Java SDK (ContractInfoQueryTest.shouldSerialize and ContractByteCodeQueryTest.shouldSerialize: contract
// 0.0.5005).
@@testId("the-info-and-bytecode-queries-keep-the-id-of-their-contract")
test "the info and bytecode queries keep the id of their contract" {
    // when
    let info = ContractInfoQuery{contractId: JAVA_SDK_QUERIED_CONTRACT}
    let bytecode = ContractBytecodeQuery{contractId: JAVA_SDK_QUERIED_CONTRACT}

    // then
    assertEquals(expected: JAVA_SDK_QUERIED_CONTRACT, actual: info.contractId)
    assertEquals(expected: JAVA_SDK_QUERIED_CONTRACT, actual: bytecode.contractId)
}

// Source: the Java SDK (ContractInfoTest, the contract info of its fixture: contract 0.0.1, account 0.0.2, expiration
// time Instant.ofEpochMilli(4) = 1970-01-01T00:00:00.004Z, auto renew period Duration.ofDays(5) = 432000 seconds,
// storage 6, memo "7"). Millisecond precision.
@@testId("a-contract-info-keeps-its-attributes")
test "a contract info keeps its attributes" {
    // given
    let expirationTime = toZonedDateTime(text: "1970-01-01T00:00:00.004Z")

    // when
    let info = ContractInfo{contractId: ContractId.fromString(value: "0.0.1"),
            accountId: AccountId.fromString(value: "0.0.2"), evmAddress: CONTRACT_EVM_ADDRESS, expirationTime: expirationTime,
            autoRenewPeriod: toSeconds(value: 432000), storage: 6, contractMemo: "7", balance: DEFAULT(hedera.Hbar)}

    // then
    assertEquals(expected: ContractId.fromString(value: "0.0.1"), actual: info.contractId)
    assertEquals(expected: AccountId.fromString(value: "0.0.2"), actual: info.accountId)
    assertEquals(expected: CONTRACT_EVM_ADDRESS, actual: info.evmAddress)
    assertEquals(expected: toZonedDateTime(text: "1970-01-01T00:00:00.004Z"), actual: info.expirationTime)
    assertEquals(expected: toSeconds(value: 432000), actual: info.autoRenewPeriod)
    assertEquals(expected: 6, actual: info.storage)
    assertEquals(expected: "7", actual: info.contractMemo)
}

// Source: this spec (the defaults of ContractInfo).
@@testId("a-contract-info-has-these-defaults")
test "a contract info has these defaults" {
    // when
    let info = ContractInfo{contractId: DEFAULT, accountId: DEFAULT, evmAddress: CONTRACT_EVM_ADDRESS, expirationTime: DEFAULT,
            storage: 0, balance: DEFAULT(hedera.Hbar)}

    // then
    assertNull(actual: info.adminAuthority)
    assertNull(actual: info.autoRenewPeriod)
    assertNull(actual: info.autoRenewAccount)
    assertEquals(expected: "", actual: info.contractMemo)
    assertFalse(condition: info.deleted)
    assertEquals(expected: 0, actual: info.maxAutomaticTokenAssociations)
    assertNull(actual: info.stakedAccountId)
    assertNull(actual: info.stakedNodeId)
    assertFalse(condition: info.declineStakingReward)
}

// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI basic_types.proto (StakingInfo.staked_id is a
// oneof).
@@testId("a-contract-info-rejects-being-staked-to-an-account-and-to-a-node")
test "a contract info rejects being staked to an account and to a node" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractInfo{contractId: DEFAULT, accountId: DEFAULT, evmAddress: CONTRACT_EVM_ADDRESS, expirationTime: DEFAULT, storage: 0,
            balance: DEFAULT(hedera.Hbar), stakedAccountId: AccountId.fromString(value: "0.0.3"), stakedNodeId: 4}
}
```

## Questions & Comments

- **Does the SDK add a margin to the cost of a `ContractCallQuery`?** The V2 Java SDK multiplies the quoted cost by
  1.1 ("network bug: ContractCallLocal cost estimate is too low"). `queries.md` specifies that the SDK pays exactly
  the quoted price. If the bug still exists on current consensus nodes, a margin has to become a behavior rule of this
  namespace (and `rule:query-payment` an exception); if not, nothing is needed. To be checked against the consensus
  node and the TCK.
- **What does a reverted local call return?** HAPI documents that `ContractCallLocalResponse.functionResult` is unset
  if the call failed, and the node answers with a status such as `CONTRACT_REVERT_EXECUTED`, so `submit(client)` fails
  with `attempts-exhausted-error`. Some nodes return the revert data in `functionResult.errorMessage` nonetheless. Should
  the error carry the `ContractFunctionResult` (or at least the revert data) so that a caller can decode the reason
  (`Error(string)`, custom errors)? The same question about `errorMessage` as text or bytes is open in
  [`transactions-contracts.md`](transactions-contracts.md).
- **`senderId` needs the signature of the sender.** HAPI requires that the payment of the query (or foreign
  transaction data) is signed by `senderId` if it differs from the payer. A `PaidQuery` is always paid by the operator
  and signed by the signer of the client's operator account, so a call as another account fails today unless the operator is that
  account. Should the query take an additional signer (similar to the deferred configurable payer of ADR-0002), or
  should `senderId` be dropped until there is a use case?
- **Is the local call a query of the consensus node at all?** The HAPI documentation recommends the mirror node for
  most view calls: `ContractRepository.call` (`mirrornode.contract`) is free and does not charge the gas in full.
  Should the documentation of `ContractCallQuery` steer callers there, and should `enterprise.service.contract` use the
  mirror node for view calls?
- **No `ledgerId` and no token relationships on `ContractInfo`**, as on `AccountInfo`; see
  [`queries-accounts.md`](queries-accounts.md) and the envelope question in [`queries.md`](queries.md).
