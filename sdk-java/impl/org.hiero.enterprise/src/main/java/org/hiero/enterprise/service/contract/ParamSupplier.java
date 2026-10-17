package org.hiero.enterprise.service.contract;


/// Supplies the value of a parameter in the form the contract call encodes; the SDK implements it for the parameters
/// that its factory functions (ofString, ofAddress, ...) create.
public interface ParamSupplier<SolidityType> {

    SolidityType getNativeContractValue();
}
