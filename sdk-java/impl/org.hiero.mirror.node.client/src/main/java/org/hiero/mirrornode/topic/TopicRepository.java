package org.hiero.mirrornode.topic;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;

public interface TopicRepository {

    /// The topic with the given id. Maps to GET /api/v1/topics/{topicId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable Topic> findById(final Address topicId);

    /// The messages of a topic. Maps to GET /api/v1/topics/{topicId}/messages.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TopicMessage>> getMessages(final Address topicId);

    /// The messages of a topic, filtered by sequence number and consensus time. `encoding` is the encoding in which the
    /// mirror node returns the messages ("base64" or "utf-8"). Maps to GET /api/v1/topics/{topicId}/messages.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TopicMessage>> getMessages(final Address topicId, final @Nullable String encoding, final @Nullable Integer limit, final @Nullable Order order, final @Nullable Long sequenceNumber, final List<String> timestamp);

    /// The message of a topic with the given sequence number.
    /// Maps to GET /api/v1/topics/{topicId}/messages/{sequenceNumber}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable TopicMessage> getMessageBySequenceNumber(final Address topicId, final long sequenceNumber);

    /// The topic message that reached consensus at the given time. Maps to GET /api/v1/topics/messages/{timestamp}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable TopicMessage> getMessageByTimestamp(final ZonedDateTime timestamp);
}
