package org.hiero.consensusnode.client;


/// Why no answer of the node arrived, independent of the transport.
public enum TransportFailureKind {

    /// there is no connection to the node, or it broke down
    CONNECTION,
    /// no answer arrived within the attempt timeout
    TIMEOUT,
    /// the connection refuses further requests because it is exhausted
    OVERLOADED,
    /// the answer was broken off on the way
    INTERRUPTED,
    /// the answer could not be read
    PROTOCOL;
}
