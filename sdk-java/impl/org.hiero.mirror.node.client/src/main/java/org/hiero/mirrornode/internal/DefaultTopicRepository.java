package org.hiero.mirrornode.internal;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.topic.ChunkInfo;
import org.hiero.mirrornode.topic.Topic;
import org.hiero.mirrornode.topic.TopicMessage;
import org.hiero.mirrornode.topic.TopicRepository;
import org.jspecify.annotations.Nullable;

/// The `TopicRepository` of the SDK, on the endpoints `/topics/{id}` and `/topics/{id}/messages` of the Mirror Node
/// REST API.
///
/// The message of a `TopicMessage` is the UTF-8 text of the Base64-decoded message, and the open end of the validity
/// range of the current topic state is `Values.OPEN_END` (see the `## Questions & Comments` of the spec).
public final class DefaultTopicRepository implements TopicRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultTopicRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<@Nullable Topic> findById(final Address topicId) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        return rest.getAndMap("/topics/" + topicId, DefaultTopicRepository::topic);
    }

    @Override
    public CompletionStage<Page<TopicMessage>> getMessages(final Address topicId) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        return rest.page("/topics/" + topicId + "/messages", "messages", DefaultTopicRepository::topicMessage);
    }

    @Override
    public CompletionStage<@Nullable TopicMessage> getMessageBySequenceNumber(final Address topicId,
            final long sequenceNumber) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        return rest.getAndMap("/topics/" + topicId + "/messages/" + sequenceNumber,
                DefaultTopicRepository::topicMessage);
    }

    private static Topic topic(final JsonObject json) {
        final Long autoRenewPeriod = json.optLong("auto_renew_period");
        final JsonObject fees = json.optObject("custom_fees");
        final ZonedDateTime from = Values.rangeFrom(json, "timestamp");
        final ZonedDateTime created = Values.timestamp(json.optString("created_timestamp"));
        return new Topic(
                Values.address(json, "topic_id"),
                Values.authority(json.optObject("admin_key")),
                Values.authority(json.optObject("submit_key")),
                Values.authority(json.optObject("fee_schedule_key")),
                Values.optAccount(json.optString("auto_renew_account")),
                Duration.ofSeconds(autoRenewPeriod == null ? 0L : autoRenewPeriod),
                created == null ? from : created,
                json.bool("deleted", false),
                Objects.requireNonNullElse(json.optString("memo"), ""),
                fees == null ? List.of()
                        : fees.objects("fixed_fees").stream().map(DefaultTokenRepository::fixedFee).toList(),
                Values.authorities(json.objects("fee_exempt_key_list")),
                from,
                Values.rangeTo(json, "timestamp"));
    }

    private static TopicMessage topicMessage(final JsonObject json) {
        final JsonObject chunk = json.optObject("chunk_info");
        return new TopicMessage(
                chunk == null ? null : chunkInfo(chunk),
                Values.requiredTimestamp(json, "consensus_timestamp"),
                new String(Values.base64(json.optString("message")), StandardCharsets.UTF_8),
                Values.account(json, "payer_account_id"),
                Values.base64(json.optString("running_hash")),
                Math.toIntExact(json.longValue("running_hash_version")),
                json.longValue("sequence_number"),
                Values.address(json, "topic_id"));
    }

    private static ChunkInfo chunkInfo(final JsonObject json) {
        final JsonObject initial = json.object("initial_transaction_id");
        final Long nonce = initial.optLong("nonce");
        final int nonceValue = nonce == null ? 0 : Math.toIntExact(nonce);
        return new ChunkInfo(
                Values.transactionId(initial.string("account_id"), initial.string("transaction_valid_start"),
                        nonceValue),
                nonceValue,
                Math.toIntExact(json.longValue("number")),
                Math.toIntExact(json.longValue("total")),
                initial.bool("scheduled", false));
    }
}
