package org.hiero.mirrornode.network;


/// How a fee estimate treats the state of the network.
public enum FeeEstimateMode {

    /// from the transaction alone
    INTRINSIC,
    /// with the current state of the network
    STATE;
}
