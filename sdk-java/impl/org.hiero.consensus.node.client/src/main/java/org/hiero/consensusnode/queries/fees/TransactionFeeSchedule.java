package org.hiero.consensusnode.queries.fees;

import java.util.Objects;
import java.util.Set;
import org.hiero.consensusnode.requestTypes.RequestType;

/// The prices of one request type: one `FeeData` per sub type.
///
/// @param requestType the request type these prices apply to
/// @param fees one entry per sub type; empty if the network names none the SDK knows
public record TransactionFeeSchedule(RequestType requestType, Set<FeeData> fees) {

    /// Creates a new `TransactionFeeSchedule`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TransactionFeeSchedule {
        Objects.requireNonNull(requestType, "requestType must not be null");
        Objects.requireNonNull(fees, "fees must not be null");
        fees = Set.copyOf(fees);
    }
}
