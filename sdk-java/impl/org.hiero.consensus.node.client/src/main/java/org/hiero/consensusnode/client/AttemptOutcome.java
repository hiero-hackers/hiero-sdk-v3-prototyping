package org.hiero.consensusnode.client;


/// How the SDK classifies the outcome of one attempt. The classification is the same in every SDK.
public enum AttemptOutcome {

    /// the node accepted the request, or the requested receipt or record is final
    SUCCESS,
    /// the node could not be reached, refused further requests or broke off the answer
    NODE_UNAVAILABLE,
    /// the node did not answer within the attempt timeout
    NODE_TIMEOUT,
    /// the node answered that it is busy, throttles the request, or is not ready to handle it
    NODE_BUSY,
    /// the node answered that the request is meant for another node
    WRONG_NODE,
    /// the requested receipt or record is not available yet
    PENDING,
    /// the node rejected the request, or its answer could not be read
    REJECTED;
}
