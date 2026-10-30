package org.hiero.mirrornode.contract;


/// The kind of a contract action.
public enum ContractActionType {

    NO_ACTION,
    CALL,
    CREATE,
    PRECOMPILE,
    SYSTEM;
}
