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
requires {Address, ContractId, EvmAddress} from ledger
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

SmartContractService {

    @@throws(service-error) Contract createContract(fileId:Address, constructorParams:Param<ANY, ANY>...)

    @@throws(service-error) Contract createContract(contents:bytes, constructorParams:Param<ANY, ANY>...)

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
