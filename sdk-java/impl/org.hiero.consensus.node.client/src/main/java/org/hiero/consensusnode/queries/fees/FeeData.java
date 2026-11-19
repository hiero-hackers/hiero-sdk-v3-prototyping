package org.hiero.consensusnode.queries.fees;

import java.util.Objects;

/// The prices of one sub type of a request type, split by who is paid.
///
/// @param nodeData paid to the consensus node that receives the request
/// @param networkData paid to the network for bringing the request to consensus
/// @param serviceData paid to the network for executing the request and keeping its state
/// @param subType the variant of the request type these prices apply to
public record FeeData(
        FeeComponents nodeData,
        FeeComponents networkData,
        FeeComponents serviceData,
        FeeSubType subType) {

    /// Creates a new `FeeData`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FeeData {
        Objects.requireNonNull(nodeData, "nodeData must not be null");
        Objects.requireNonNull(networkData, "networkData must not be null");
        Objects.requireNonNull(serviceData, "serviceData must not be null");
        Objects.requireNonNull(subType, "subType must not be null");
    }
}
