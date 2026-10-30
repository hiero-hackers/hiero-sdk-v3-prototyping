package org.hiero.mirrornode.contract;


/// The operation of a call in the EVM.
public enum ContractCallOperationType {

    CALL,
    CALLCODE,
    CREATE,
    CREATE2,
    DELEGATECALL,
    STATICCALL,
    UNKNOWN;
}
