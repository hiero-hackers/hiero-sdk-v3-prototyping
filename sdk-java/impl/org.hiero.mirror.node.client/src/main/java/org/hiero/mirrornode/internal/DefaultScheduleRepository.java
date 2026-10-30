package org.hiero.mirrornode.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.schedule.Schedule;
import org.hiero.mirrornode.schedule.ScheduleRepository;
import org.hiero.mirrornode.schedule.ScheduleSignature;
import org.hiero.mirrornode.schedule.SignatureType;
import org.jspecify.annotations.Nullable;

/// The `ScheduleRepository` of the SDK, on the endpoints `/schedules` and `/schedules/{scheduleId}` of the Mirror
/// Node REST API.
public final class DefaultScheduleRepository implements ScheduleRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultScheduleRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<@Nullable Schedule> findById(final Address scheduleId) {
        Objects.requireNonNull(scheduleId, "scheduleId must not be null");
        return rest.getAndMap("/schedules/" + scheduleId, DefaultScheduleRepository::schedule);
    }

    @Override
    public CompletionStage<Page<Schedule>> findAll() {
        return findAll(List.of(), null, null, List.of());
    }

    @Override
    public CompletionStage<Page<Schedule>> findAll(final List<String> accountId, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> scheduleId) {
        final String path = Query.of("/schedules").addAll("account.id", accountId).add("limit", limit)
                .add("order", order).addAll("schedule.id", scheduleId).build();
        return rest.page(path, "schedules", DefaultScheduleRepository::schedule);
    }

    private static Schedule schedule(final JsonObject json) {
        return new Schedule(
                Values.authority(json.optObject("admin_key")),
                Values.requiredTimestamp(json, "consensus_timestamp"),
                Values.account(json, "creator_account_id"),
                json.bool("deleted", false),
                Values.timestamp(json.optString("executed_timestamp")),
                Values.timestamp(json.optString("expiration_time")),
                Objects.requireNonNullElse(json.optString("memo"), ""),
                Values.account(json, "payer_account_id"),
                Values.address(json, "schedule_id"),
                json.objects("signatures").stream().map(DefaultScheduleRepository::signature).toList(),
                Values.base64(json.optString("transaction_body")),
                json.bool("wait_for_expiry", false));
    }

    private static ScheduleSignature signature(final JsonObject json) {
        return new ScheduleSignature(
                Values.requiredTimestamp(json, "consensus_timestamp"),
                Values.base64(json.optString("public_key_prefix")),
                Values.base64(json.optString("signature")),
                SignatureType.valueOf(json.string("type")));
    }
}
