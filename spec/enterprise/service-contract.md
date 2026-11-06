# Contract Service API

## Description

`SmartContractService` deploys smart contracts and calls their functions. A contract is created either from bytecode
stored in a file or directly from bytecode, optionally with constructor parameters; `ContractCreateParams` holds the
further options of a new contract. Staking is not part of the service: to create a contract that is staked to an
account or to a node, use `ContractCreateTransaction` of the consensus node client. Function and constructor
parameters are passed as `Param` values, created with the factory functions such as `ofString`, `ofAddress`,
`ofBool` or `uint256`, which wrap a value of your language as the matching Solidity type. The values returned by a
function call are available from the `ContractCallResult` by index.

## Design Notes

`ContractCallResult.get(index)` is deliberately `ANY`: the type of a contract call result is only known at runtime (it
depends on the called Solidity function), so no static type can describe it. The concrete type of the value at `index`
is reported by `getType(index)`; callers check it before using the value.

## API Schema

```
namespace enterprise.service.contract
requires {Page} from common
requires {Address, AccountId, ContractId} from ledger
requires {EvmAddress} from evm
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Session} from enterprise.service

// Supplies the value of a parameter in the form the contract call encodes; the SDK implements it for the parameters
// that its factory functions (ofString, ofAddress, ...) create.
abstraction ParamSupplier<$$SolidityType> {
    $$SolidityType getNativeContractValue()
}

Param<$$LangType, $$SolidityType> {
    @@immutable value:$$LangType
    @@immutable nativeType:string
    @@immutable supplier:ParamSupplier<$$SolidityType>
}

// The values a contract function returned; created by the SDK from the result of a call.
abstraction ContractCallResult {
    @@immutable size: uint8
    
    // Runtime type of the result value at `index`; use it to check the value returned by get(index).
    type getType(index:uint8)

    // Returns the result value at `index`. Its type depends on the called Solidity function and is only known at
    // runtime; check it with getType(index) before using the value.
    ANY get(index:uint8)
}

@@finalType
Contract {
    @@immutable contractId: ContractId
}

// The options of a new smart contract. Where its bytecode comes from is chosen by the method that creates it.
@@finalType
ContractCreateParams {
    // The parameters of the constructor, in order.
    @@immutable @@default([]) constructorParams: list<Param<ANY, ANY>>
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
    // ContractCreateParams that have only these values.
    @@throws(service-error) Contract createContract(fileId:Address, constructorParams:Param<ANY, ANY>...)

    @@throws(service-error) Contract createContract(contents:bytes, constructorParams:Param<ANY, ANY>...)

    // Create a contract from the bytecode in a file or from the bytecode itself, with every option of a new contract:
    // the general form of the methods above.
    @@throws(service-error) Contract createContract(fileId:Address, params: ContractCreateParams)

    @@throws(service-error) Contract createContract(contents:bytes, params: ContractCreateParams)

    @@throws(service-error) ContractCallResult callContractFunction(contract:Contract, functionName:string, params:Param<ANY, ANY>...)

    @@throws(service-error) ContractCallResult callContractFunction(contractId:ContractId, functionName:string, params:Param<ANY, ANY>...)

    // Return the contract information for the given contract id
    @@throws(service-error) @@nullable Contract findById(contractId: ContractId)

    // Return all known contracts
    @@throws(service-error) Page<Contract> findAll()
}

// Factory functions that wrap values of the host language as Solidity parameter types.
// Solidity's `address` type is a 20-byte EVM address: prefer the EvmAddress overload of ofAddress;
// the string overload accepts the hex form ("0x...").

@@static Param<string, ANY> ofString(value:string)
@@static Param<string, ANY> ofBytes(value:string)
@@static Param<string, ANY> ofBytes23(value:string)
@@static Param<bytes, ANY> ofBytes(value:bytes)
@@static Param<bytes, ANY> ofBytes23(value:bytes)
@@static Param<string, ANY> ofAddress(@@pattern("^(0x)?[0-9a-fA-F]{40}$") value:string)
@@static Param<EvmAddress, ANY> ofAddress(value:EvmAddress)
@@static Param<bool, ANY> ofBool(value:bool)
@@static Param<uint8, ANY> uint8(value:uint8)
@@static Param<int8, ANY> int8(value:int8)
@@static Param<uint256, ANY> uint256(value:uint256)
@@static Param<int256, ANY> int256(value:int256)

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

// The supplier of a string parameter, created by its factory function.
instance ParamSupplier = ofString(value: "value").supplier
```

## Testing

Tests of `ContractCreateParams`, `Contract` and the parameter factory functions. The methods of
`SmartContractService` talk to the network and are not unit-tested here; a `ContractCallResult` can only be obtained
from them. A violation of `@@min` or `@@maxBytes` is rejected with `invalid-argument-error`. The ABI encoding of a `Param` is not part of this API and is not tested.

```tests
// Source: this spec (@@default and @@nullable of ContractCreateParams); the Python SDK (contract_create_transaction_test.py,
// test_constructor_default_values: no gas, admin key or memo).
@@testId("contract-parameters-without-values-have-the-defaults-of-the-spec")
test "contract parameters without values have the defaults of the spec" {
    // when
    let params = DEFAULT(ContractCreateParams)

    // then
    assertEquals(expected: 0, actual: length(value: params.constructorParams))
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
    let constructorParam = ofString(value: "Hello from Hiero.")

    // when
    let params = ContractCreateParams{constructorParams: [constructorParam], gas: 1_000_000, adminAuthority: authority,
            contractMemo: "memo", autoRenewPeriod: autoRenewPeriod, autoRenewAccount: account,
            maxAutomaticTokenAssociations: 10}

    // then
    assertEquals(expected: 1, actual: length(value: params.constructorParams))
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

// Source: this spec (a Param wraps the value of the host language); hiero-enterprise-java (ContractParam: the value of
// a parameter is the given value).
@@testId("a-parameter-keeps-the-value-it-wraps")
test "a parameter keeps the value it wraps" {
    // given
    let text = "Hello from Hiero."
    let data = hex(text: "0a1b2c3d")
    let hexAddress = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
    let evmAddress = EvmAddress.fromString(value: hexAddress)

    // when / then
    assertEquals(expected: text, actual: ofString(value: text).value)
    assertEquals(expected: text, actual: ofBytes(value: text).value)
    assertEquals(expected: data, actual: ofBytes(value: data).value)
    assertEquals(expected: hexAddress, actual: ofAddress(value: hexAddress).value)
    assertEquals(expected: evmAddress, actual: ofAddress(value: evmAddress).value)
    assertTrue(condition: ofBool(value: true).value)
    assertFalse(condition: ofBool(value: false).value)
}

// Source: hiero-enterprise-java (ContractParamTests: testUint8Range, testInt8Range, testUint256Range and
// testInt256Range accept the smallest and the largest value of each type); the Solidity documentation (an intN holds
// -2^(N-1) to 2^(N-1)-1, a uintN 0 to 2^N-1). The 256-bit bounds were computed with Python (2**256-1, -2**255,
// 2**255-1).
@@testId("integer-parameters-accept-the-smallest-and-the-largest-value-of-their-solidity-type")
test "integer parameters accept the smallest and the largest value of their Solidity type" {
    // when / then
    assertEquals(expected: 0, actual: uint8(value: 0).value)
    assertEquals(expected: 255, actual: uint8(value: 255).value)
    assertEquals(expected: -128, actual: int8(value: -128).value)
    assertEquals(expected: 127, actual: int8(value: 127).value)
    assertEquals(expected: 0, actual: uint256(value: 0).value)
    assertEquals(expected: 115792089237316195423570985008687907853269984665640564039457584007913129639935,
            actual: uint256(value: 115792089237316195423570985008687907853269984665640564039457584007913129639935).value)
    assertEquals(expected: -57896044618658097711785492504343953926634992332820282019728792003956564819968,
            actual: int256(value: -57896044618658097711785492504343953926634992332820282019728792003956564819968).value)
    assertEquals(expected: 57896044618658097711785492504343953926634992332820282019728792003956564819967,
            actual: int256(value: 57896044618658097711785492504343953926634992332820282019728792003956564819967).value)
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

- Long-form (shard.realm.num) Hedera id overloads of `ofAddress` (taking `AccountId` / `ContractId`) belong next to the
  other param factory functions as follow-ups once a HAPI long-form-encoding helper is decided.

- **`ContractCreateParams` is not yet aligned with `ContractCreateTransaction`**
  ([`transactions-contracts.md`](../consensus-node-client/transactions-contracts.md)): the constructor parameters are
  `Param` values instead of `AbiValue`s of the namespace `evm`, and `gas` is optional with `@@min(1)` (a default chosen
  by the service) while the transaction requires it with `@@min(0)`. The Java implementation does not implement the
  contract transactions yet, so `createContract…` and `callContractFunction` fail.
- **How is a `ContractCallResult` obtained?** `ContractCallResult` had no values of its own and is now an abstraction
  the SDK creates from the result of a call. Which source the SDK reads the result from (the record of a
  `ContractCall` transaction, a `ContractCallLocal` query, or the Mirror Node `POST /contracts/call`) and how the
  result types are known for decoding is not specified.
- **`nativeType` of a `Param` is not specified.** Hiero Enterprise Java sets it to the Solidity type name
  (`string`, `address`, `bool`, `uint8`, `uint256`, ...), and the Java implementation does the same. Stating that in
  the spec would let the tests check it for every factory function; today they check only `value`.
- **`ofBytes23` looks like a typo of `ofBytes32`** (Solidity has `bytes1` to `bytes32`; Hiero Enterprise Java has
  `bytes32`). Which length is meant? The Java implementation takes the name literally and creates a `bytes23`
  parameter.
