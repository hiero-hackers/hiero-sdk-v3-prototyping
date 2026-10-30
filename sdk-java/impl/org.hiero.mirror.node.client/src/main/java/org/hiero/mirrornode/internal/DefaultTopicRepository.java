package org.hiero.mirrornode.internal;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.hiero.common.Page;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.FixedFee;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.topic.ChunkInfo;
import org.hiero.mirrornode.topic.Topic;
import org.hiero.mirrornode.topic.TopicMessage;
import org.hiero.mirrornode.topic.TopicRepository;
import org.jspecify.annotations.Nullable;

/// The `TopicRepository` of the SDK, on the endpoints `/topics/{topicId}`, `/topics/{topicId}/messages`,
/// `/topics/{topicId}/messages/{sequenceNumber}` and `/topics/messages/{timestamp}` of the Mirror Node REST API.
///
/// The message of a `TopicMessage` is the UTF-8 text of the message: the Base64-decoded message, or the message as it
/// is when it was requested with the encoding `utf-8` (see the `## Questions & Comments` of the spec).
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
        return getMessages(topicId, null, null, null, null, List.of());
    }

    @Override
    public CompletionStage<Page<TopicMessage>> getMessages(final Address topicId, final @Nullable String encoding,
            final @Nullable Integer limit, final @Nullable Order order, final @Nullable Long sequenceNumber,
            final List<String> timestamp) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        final String path = Query.of("/topics/" + topicId + "/messages").add("encoding", encoding)
                .add("limit", limit).add("order", order).add("sequencenumber", sequenceNumber)
                .addAll("timestamp", timestamp).build();
        return rest.page(path, "messages", topicMessage(isUtf8(encoding)));
    }

    @Override
    public CompletionStage<@Nullable TopicMessage> getMessageBySequenceNumber(final Address topicId,
            final long sequenceNumber) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        return rest.getAndMap("/topics/" + topicId + "/messages/" + sequenceNumber, topicMessage(false));
    }

    @Override
    public CompletionStage<@Nullable TopicMessage> getMessageByTimestamp(final ZonedDateTime timestamp) {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        final String text = timestamp.toEpochSecond() + "." + String.format(Locale.ROOT, "%09d", timestamp.getNano());
        return rest.getAndMap("/topics/messages/" + text, topicMessage(false));
    }

    private static boolean isUtf8(final @Nullable String encoding) {
        return encoding != null && encoding.replace("-", "").equalsIgnoreCase("utf8");
    }

    private static Topic topic(final JsonObject json) {
        final Long autoRenewPeriod = json.optLong("auto_renew_period");
        final JsonObject fees = json.optObject("custom_fees");
        return new Topic(
                Values.address(json, "topic_id"),
                Values.authority(json.optObject("admin_key")),
                Values.authority(json.optObject("submit_key")),
                Values.authority(json.optObject("fee_schedule_key")),
                Values.optAccount(json.optString("auto_renew_account")),
                autoRenewPeriod == null ? null : Duration.ofSeconds(autoRenewPeriod),
                Values.timestamp(json.optString("created_timestamp")),
                fees == null ? null : Values.timestamp(fees.optString("created_timestamp")),
                json.optBoolean("deleted"),
                Objects.requireNonNullElse(json.optString("memo"), ""),
                fees == null ? List.of()
                        : fees.objects("fixed_fees").stream().map(DefaultTopicRepository::fixedFee).toList(),
                Values.authorities(json.objects("fee_exempt_key_list")),
                Values.range(json, "timestamp"));
    }

    private static FixedFee fixedFee(final JsonObject json) {
        return new FixedFee(false, json.longValue("amount"), Values.optAccount(json.optString("collector_account_id")),
                Values.optAddress(json.optString("denominating_token_id")));
    }

    private static Function<JsonObject, TopicMessage> topicMessage(final boolean utf8) {
        return json -> {
            final JsonObject chunk = json.optObject("chunk_info");
            final String message = Objects.requireNonNullElse(json.optString("message"), "");
            return new TopicMessage(
                    chunk == null ? null : chunkInfo(chunk),
                    Values.requiredTimestamp(json, "consensus_timestamp"),
                    utf8 ? message : new String(Values.base64(message), StandardCharsets.UTF_8),
                    Values.account(json, "payer_account_id"),
                    Values.base64(json.optString("running_hash")),
                    Math.toIntExact(json.longValue("running_hash_version")),
                    json.longValue("sequence_number"),
                    Values.address(json, "topic_id"));
        };
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
