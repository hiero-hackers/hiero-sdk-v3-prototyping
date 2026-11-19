package org.hiero.consensusnode.requestTypes;


/// A type of request of the protocol: a transaction or query type, identified by a numeric code. The set of request
/// types is open: optional and custom services on the consensus node can define their own.
public interface RequestType {

    /// the numeric code of the request type in the protocol
    int code();
}
