package org.hiero.consensusnode.queries.fees;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

/// The prices of every request type until `expirationTime`.
///
/// @param transactionFeeSchedules One entry per request type; leaves out the request types the SDK does not know.
/// @param expirationTime when the schedule expires and the next one becomes valid
public record FeeSchedule(
        Set<TransactionFeeSchedule> transactionFeeSchedules,
        ZonedDateTime expirationTime) {

    /// Creates a new `FeeSchedule`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FeeSchedule {
        Objects.requireNonNull(transactionFeeSchedules, "transactionFeeSchedules must not be null");
        transactionFeeSchedules = Set.copyOf(transactionFeeSchedules);
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
    }
}
