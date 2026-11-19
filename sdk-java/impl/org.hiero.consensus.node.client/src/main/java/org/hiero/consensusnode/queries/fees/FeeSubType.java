package org.hiero.consensusnode.queries.fees;


/// A variant of a request type with prices of its own, for example the mint of a fungible and of a non-fungible
/// token. The set of sub types is open: a network of a newer version can define sub types this SDK does not know.
public interface FeeSubType {

    /// the numeric code of the sub type in the protocol
    int code();
}
