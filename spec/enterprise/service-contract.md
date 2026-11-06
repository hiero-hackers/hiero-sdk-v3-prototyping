# Contract Service API

## Description

`SmartContractService` deploys smart contracts and calls their functions. A contract is created either from bytecode
stored in a file or directly from bytecode, optionally with constructor arguments; `ContractCreateParams` holds the
further options of a new contract. Staking is not part of the service: to create a contract that is staked to an
account or to a node, use `ContractCreateTransaction` of the consensus node client.

A function is described by an `AbiFunction` of the namespace `evm`: its name and the types of its arguments and
results, read from a signature (`AbiFunction.parse("transfer(address,uint256) returns (bool)")`) or built from the
types. Arguments are `AbiValue`s, which know their type (`AbiUint{value: 1000, bits: 256}`,
`AbiAddress{value: address}`); constructor arguments are given the same way.

- `executeFunction` calls a function in a transaction: it can change the state of the contract, and the operator of
  the session pays for it. The result holds the decoded values and the gas the call used.
- `callFunction` calls a function without a transaction, on the Mirror Node: it reads the state of the contract, and
  nothing is changed or paid for. Use it for `view` and `pure` functions.
- `estimateGas` returns the gas a call of the function needs, also computed by the Mirror Node.

The values a function returned are decoded with the result types of its `AbiFunction`; a function described without
result types returns only the raw result.

## Design Notes

- **The ABI model is the one of `evm`.** Arguments, results and the description of a function are the types of
  [`evm.md`](../base/evm.md); the service has no ABI model of its own (no parameter wrappers, factory functions or
  untyped result getters). This carries out the consequence recorded in
  [ADR-0015](../../docs/adr/0015-evm-generic-types-in-a-namespace-without-imports.md).
- **Results are decoded with the result types.** ABI-encoded return data carries no type information, so the values
  can only be decoded if the result types are known; they come from `AbiFunction.outputs`, not from the result.
- **No `@@oneOf` in the parameters.** Where the bytecode comes from is chosen by the overload of `createContract`, not
  by two exclusive fields of `ContractCreateParams`.

## API Schema

```
namespace enterprise.service.contract
requires {Page} from common
requires {Address, AccountId, ContractId} from ledger
requires {AbiFunction, AbiValue} from evm
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Session} from enterprise.service

// The result of a call of a function without a transaction.
@@finalType
ContractCallResult {
    @@immutable @@default([]) values: list<AbiValue>   // the returned values, decoded with the result types of the function
    @@immutable result: bytes                          // the returned data, ABI-encoded
}

// The result of a call of a function in a transaction.
@@finalType
ContractExecuteResult {
    @@immutable @@default([]) values: list<AbiValue>   // the returned values, decoded with the result types of the function
    @@immutable result: bytes                          // the returned data, ABI-encoded
    @@immutable gasUsed: uint64                        // the gas the call used
}

@@finalType
Contract {
    @@immutable contractId: ContractId
}

// The options of a new smart contract. Where its bytecode comes from is chosen by the method that creates it.
@@finalType
ContractCreateParams {
    // The arguments of the constructor, in order.
    @@immutable @@default([]) constructorArguments: list<AbiValue>
    // The gas for running the constructor; a default of the service if absent.
    @@immutable @@nullable @@min(1) gas: int64
    // Amount transferred from the payer to the new contract.
    @@immutable @@nullable initialBalance: NativeToken<ANY, ANY>
    // Allows to update and delete the contract; without it the contract cannot be changed.
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable @@maxBytes(100) contractMemo: string
    // The renewal period applied when the expiration approaches; the network default if absent.
    @@immutable @@nullable autoRenewPeriod: seconds
    // The account that pays for the renewal.
    @@immutable @@nullable autoRenewAccount: AccountId
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
}

SmartContractService {

    // Create a contract from the bytecode in a file or from the bytecode itself. The same as createContract with
    // ContractCreateParams that have only these arguments.
    @@throws(service-error) Contract createContract(fileId: Address, constructorArguments: AbiValue...)

    @@throws(service-error) Contract createContract(contents: bytes, constructorArguments: AbiValue...)

    // Create a contract from the bytecode in a file or from the bytecode itself, with every option of a new contract:
    // the general form of the methods above.
    @@throws(service-error) Contract createContract(fileId: Address, params: ContractCreateParams)

    @@throws(service-error) Contract createContract(contents: bytes, params: ContractCreateParams)

    // Calls a function of a contract in a transaction, paid by the operator of the session. Throws if the arguments
    // do not have the argument types of the function.
    @@throws(service-error) ContractExecuteResult executeFunction(contractId: ContractId, function: AbiFunction,
            arguments: AbiValue...)

    // Calls a function of a contract without a transaction, on the Mirror Node; the state of the contract is not
    // changed. Throws if the arguments do not have the argument types of the function.
    @@throws(service-error) ContractCallResult callFunction(contractId: ContractId, function: AbiFunction,
            arguments: AbiValue...)

    // The gas a call of the function with the arguments needs, estimated by the Mirror Node.
    @@throws(service-error) int64 estimateGas(contractId: ContractId, function: AbiFunction, arguments: AbiValue...)

    // Return the contract information for the given contract id
    @@throws(service-error) @@nullable Contract findById(contractId: ContractId)

    // Return all known contracts
    @@throws(service-error) Page<Contract> findAll()
}

// Creates the service for the given session. With a framework integration the service is usually obtained via
// dependency injection instead.
@@static
SmartContractService createService(session: Session)

```

## Default Instances for Testing

```
// The options of a contract that sets none of them.
instance ContractCreateParams = ContractCreateParams{}

// A contract addressed by the default contract id.
instance Contract = Contract{contractId: DEFAULT}
```

## Testing

Tests of `ContractCreateParams` and `Contract`. The methods of `SmartContractService` talk to the network and are not
unit-tested here; the encoding of the arguments and the decoding of the results are tested with the types of `evm`. A
violation of `@@min` or `@@maxBytes` is rejected with `invalid-argument-error`.

```tests
// Source: this spec (@@default and @@nullable of ContractCreateParams); the Python SDK (contract_create_transaction_test.py,
// test_constructor_default_values: no gas, admin key or memo).
@@testId("contract-parameters-without-values-have-the-defaults-of-the-spec")
test "contract parameters without values have the defaults of the spec" {
    // when
    let params = DEFAULT(ContractCreateParams)

    // then
    assertEquals(expected: 0, actual: length(value: params.constructorArguments))
    assertNull(actual: params.gas)
    assertNull(actual: params.initialBalance)
    assertNull(actual: params.adminAuthority)
    assertNull(actual: params.contractMemo)
    assertNull(actual: params.autoRenewPeriod)
    assertNull(actual: params.autoRenewAccount)
    assertNull(actual: params.maxAutomaticTokenAssociations)
}

// Source: the ContractCreateTransaction unit tests of the Java (ContractCreateTransactionTest), Go
// (contract_create_transaction_unit_test.go), C++ (ContractCreateTransactionUnitTests), Swift
// (ContractCreateTransactionUnitTests) and Python (contract_create_transaction_test.py, test_constructor_with_parameters)
// SDKs; hiero-enterprise-java (ProtocolLayerDataCreationTests, testContractCreateRequestCreation: gas, an admin key and
// constructor parameters): every value that is set is read back unchanged. The auto-renew period is the
// one of the Java test (Duration.ofHours(10), which is exactly 36000 seconds).
@@testId("contract-parameters-keep-the-values-they-are-created-with")
test "contract parameters keep the values they are created with" {
    // given
    let authority = DEFAULT(Authority)
    let autoRenewPeriod = toSeconds(value: 36000)
    let account = DEFAULT(AccountId)
    let constructorArgument: AbiValue = evm.AbiString{value: "Hello from Hiero."}

    // when
    let params = ContractCreateParams{constructorArguments: [constructorArgument], gas: 1_000_000, adminAuthority: authority,
            contractMemo: "memo", autoRenewPeriod: autoRenewPeriod, autoRenewAccount: account,
            maxAutomaticTokenAssociations: 10}

    // then
    assertEquals(expected: [constructorArgument], actual: params.constructorArguments)
    assertEquals(expected: 1_000_000, actual: params.gas)
    assertEquals(expected: authority, actual: params.adminAuthority)
    assertEquals(expected: "memo", actual: params.contractMemo)
    assertEquals(expected: autoRenewPeriod, actual: params.autoRenewPeriod)
    assertEquals(expected: account, actual: params.autoRenewAccount)
    assertEquals(expected: 10, actual: params.maxAutomaticTokenAssociations)
}

// Source: this spec (@@min(1) of gas and @@maxBytes(100) of contractMemo); HAPI limits a memo to 100 bytes, which
// ASCII text of 100 characters is.
@@testId("contract-parameters-accept-a-gas-of-1-and-a-memo-of-100-characters")
test "contract parameters accept a gas of 1 and a memo of 100 characters" {
    // given
    let memo = repeat(value: "a", count: 100)

    // when
    let params = ContractCreateParams{gas: 1, contractMemo: memo}

    // then
    assertEquals(expected: 1, actual: params.gas)
    assertEquals(expected: memo, actual: params.contractMemo)
}

// Source: this spec (@@min(1) of gas); the Python SDK (contract_create_transaction_test.py,
// test_set_gas_rejects_negative_values and test_constructor_rejects_negative_gas reject a negative gas).
@@testId("contract-parameters-reject-a-gas-of-0")
test "contract parameters reject a gas of 0" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractCreateParams{gas: 0}
}

// Source: this spec (@@maxBytes(100) of contractMemo); HAPI limits a memo to 100 bytes.
@@testId("contract-parameters-reject-a-memo-of-101-characters")
test "contract parameters reject a memo of 101 characters" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractCreateParams{contractMemo: repeat(value: "a", count: 101)}
}

// Source: this spec (Contract is a value of its contract id).
@@testId("contracts-with-the-same-contract-id-are-equal")
test "contracts with the same contract id are equal" {
    // given
    let contractId = DEFAULT(ContractId)

    // when
    let contract = Contract{contractId: contractId}

    // then
    assertEquals(expected: contractId, actual: contract.contractId)
    assertEquals(expected: DEFAULT(Contract), actual: contract)
    assertNotEquals(unexpected: Contract{contractId: ContractId{shard: 0, realm: 0, num: 1002, checksum: "",
            evmAddress: null}}, actual: contract)
}
```

## Questions & Comments

- **An `AbiAddress` of an account or contract without an EVM address.** An argument of type `address` is an
  `EvmAddress`. For an `AccountId` or `ContractId` that has only a number, its address is the long-zero form (shard,
  realm and number in 20 bytes), but no type offers this conversion. Should `ledger` offer it (e.g.
  `ContractId.toEvmAddress()`), or should the enterprise layer accept ids as arguments?

- **`ContractCreateParams` is not yet aligned with `ContractCreateTransaction`**
  ([`transactions-contracts.md`](../consensus-node-client/transactions-contracts.md)): `gas` is optional with
  `@@min(1)` (a default chosen by the service) while the transaction requires it with `@@min(0)`. Which default gas
  the service uses is not specified. The Java implementation does not implement the contract transactions yet, so
  `createContract…` and `executeFunction` fail after the arguments have been checked.

- **`executeFunction` has no gas, no payable amount and no logs.** The service chooses the gas and sends no native
  tokens, and `ContractExecuteResult` holds neither the logs nor an error message of a reverted call. Should there be
  an `executeFunction` with options (gas, payable amount), and should the result carry the logs (`ContractLog` of
  `consensusnode.transactions`, decoded with `AbiEvent`)?

- **Who is the sender of `callFunction`?** The Mirror Node simulates the call without a sender unless one is given, so a
  function that depends on `msg.sender` returns another result than in a transaction. Should the operator of the
  session be the sender?
