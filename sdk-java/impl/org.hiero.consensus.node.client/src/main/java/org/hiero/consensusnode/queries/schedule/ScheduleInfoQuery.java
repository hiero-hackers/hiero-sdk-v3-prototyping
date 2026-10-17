package org.hiero.consensusnode.queries.schedule;

import org.hiero.consensusnode.client.internal.ScheduleQueries;
import org.hiero.consensusnode.client.internal.Querying;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query for the current state of a stored schedule.
public final class ScheduleInfoQuery extends PaidQuery<ScheduleInfo> {

    private final Address scheduleId;

    /// Creates a new `ScheduleInfoQuery`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ScheduleInfoQuery(final Address scheduleId) {
        super();
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        this.scheduleId = scheduleId;
    }

    /// Returns the `scheduleId`.
    public Address scheduleId() {
        return scheduleId;
    }

    /// Sets the `maxAttempts`.
    ///
    /// @param maxAttempts the new value
    /// @return this object
    @Override
    public ScheduleInfoQuery setMaxAttempts(final @Nullable Integer maxAttempts) {
        super.setMaxAttempts(maxAttempts);
        return this;
    }

    /// Sets the `maxBackoff`.
    ///
    /// @param maxBackoff the new value
    /// @return this object
    @Override
    public ScheduleInfoQuery setMaxBackoff(final @Nullable Long maxBackoff) {
        super.setMaxBackoff(maxBackoff);
        return this;
    }

    /// Sets the `minBackoff`.
    ///
    /// @param minBackoff the new value
    /// @return this object
    @Override
    public ScheduleInfoQuery setMinBackoff(final @Nullable Long minBackoff) {
        super.setMinBackoff(minBackoff);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public ScheduleInfoQuery setAttemptTimeout(final @Nullable Long attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public ScheduleInfoQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
        super.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently.
    @Override
    public CompletionStage<PaidQueryResponse<ScheduleInfo>> submit(final HieroClient<?> client) {
        return Querying.paid(this, maxQueryPayment(), ScheduleQueries.SCHEDULE_INFO, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        return Querying.cost(this, ScheduleQueries.SCHEDULE_INFO, client);
    }

    @Override
    public String toString() {
        return "ScheduleInfoQuery[maxAttempts=" + maxAttempts()
                + ", maxBackoff=" + maxBackoff()
                + ", minBackoff=" + minBackoff()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + ", scheduleId=" + scheduleId()
                + "]";
    }
}
