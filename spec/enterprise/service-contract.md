# Contract Service API

## Description

`SmartContractService` deploys smart contracts and calls their functions. A contract is created either from bytecode
stored in a file or directly from bytecode, optionally with constructor parameters. Function and constructor
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
requires {Address, AccountId, ContractId, EvmAddress} from ledger
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Session} from enterprise.service

ParamSupplier<$$SolidityType> {
    $$SolidityType getNativeContractValue()
}

Param<$$LangType, $$SolidityType> {
    @@immutable value:$$LangType
    @@immutable nativeType:string
    @@immutable supplier:ParamSupplier<$$SolidityType>
}

ContractCallResult {
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

// The parameters of a new smart contract: everything that can be set when it is created. The bytecode comes either
// from a file or directly; a contract is staked to an account or to a node, not to both.
@@finalType
@@oneOf(fileId, bytecode)
@@oneOrNoneOf(stakedAccountId, stakedNodeId)
ContractCreateParams {
    // The file that holds the bytecode.
    @@immutable @@nullable fileId: Address
    // The bytecode itself.
    @@immutable @@nullable bytecode: bytes
    // The parameters of the constructor, in order.
    @@immutable @@default([]) constructorParams: list<Param<ANY, ANY>>
    // The gas for running the constructor; a default of the service if absent.
    @@immutable @@nullable @@min(1) gas: int64
    // Amount transferred from the payer to the new contract.
    @@immutable @@nullable initialBalance: NativeToken<ANY, ANY>
    // Allows to update and delete the contract; without it the contract cannot be changed.
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable @@maxLength(100) contractMemo: string
    // The renewal period applied when the expiration approaches; the network default if absent.
    @@immutable @@nullable autoRenewPeriod: seconds
    // The account that pays for the renewal.
    @@immutable @@nullable autoRenewAccount: AccountId
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable stakedAccountId: AccountId
    @@immutable @@nullable stakedNodeId: int64
    @@immutable @@default(false) declineStakingReward: bool
}

SmartContractService {

    // Create a contract from the bytecode in a file or from the bytecode itself. The same as createContract with
    // ContractCreateParams that have only these values.
    @@throws(service-error) Contract createContract(fileId:Address, constructorParams:Param<ANY, ANY>...)

    @@throws(service-error) Contract createContract(contents:bytes, constructorParams:Param<ANY, ANY>...)

    // Create a contract with the given parameters: the general form of the methods above, for every option of a new
    // contract.
    @@throws(service-error) Contract createContract(params: ContractCreateParams)

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
@@static Param<string, ANY> ofAddress(value:string)
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

## Questions & Comments

- Long-form (shard.realm.num) Hedera id overloads of `ofAddress` (taking `AccountId` / `ContractId`) belong next to the
  other param factory functions as follow-ups once a HAPI long-form-encoding helper is decided.

- `ContractCreateParams` follows the HAPI `ContractCreateTransactionBody` (bytecode from a file or inline, gas,
  constructor parameters, admin authority, staking), because the consensus layer has no `ContractCreateTransaction`
  yet. Once it exists, the fields should be aligned with it, as the other `…CreateParams` types are with their
  transactions. Open: whether `gas` should stay optional (a default chosen by the service) or be required as in HAPI.
