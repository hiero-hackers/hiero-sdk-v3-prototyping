package org.hiero.mirrornode.schedule;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;

public interface ScheduleRepository {

    /// The schedule with the given id, or null if there is none. Maps to GET /api/v1/schedules/{scheduleId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable Schedule> findById(final Address scheduleId);

    /// The schedules of the network. Maps to GET /api/v1/schedules.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Schedule>> findAll();

    /// The schedules of the network, filtered by the creating account and the schedule id. Maps to
    /// GET /api/v1/schedules.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Schedule>> findAll(final List<String> accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> scheduleId);
}
