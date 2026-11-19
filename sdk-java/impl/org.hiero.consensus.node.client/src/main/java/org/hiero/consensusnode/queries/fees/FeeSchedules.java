package org.hiero.consensusnode.queries.fees;

import java.util.Objects;

/// The current and the next fee schedule of the network, as stored in its fee schedule file. Returned by
/// `FeeScheduleQuery`.
///
/// @param currentSchedule the schedule that is valid now
/// @param nextSchedule the schedule that becomes valid when `currentSchedule` expires
public record FeeSchedules(FeeSchedule currentSchedule, FeeSchedule nextSchedule) {

    /// Creates a new `FeeSchedules`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FeeSchedules {
        Objects.requireNonNull(currentSchedule, "currentSchedule must not be null");
        Objects.requireNonNull(nextSchedule, "nextSchedule must not be null");
    }
}
