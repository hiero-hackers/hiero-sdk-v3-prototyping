package org.hiero.enterprise.service.internal;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.topics.TopicCreateReceipt;
import org.hiero.consensusnode.transactions.topics.TopicCreateTransaction;
import org.hiero.consensusnode.transactions.topics.TopicDeleteTransaction;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.topic.Topic;
import org.hiero.enterprise.service.topic.TopicCreateParams;
import org.hiero.enterprise.service.topic.TopicMessage;
import org.hiero.enterprise.service.topic.TopicService;
import org.hiero.ledger.Address;
import org.hiero.sdk.common.HieroStream;
import org.jspecify.annotations.Nullable;

/// The `TopicService` of a session.
public final class DefaultTopicService extends TopicService {

    private final DefaultSession session;

    /// Creates the service of a session.
    ///
    /// @param session the session
    public DefaultTopicService(final DefaultSession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public Topic createTopic() {
        return createTopic(TopicCreateParams.builder().build());
    }

    @Override
    public Topic createTopic(final String memo) {
        checkMemo(memo);
        return createTopic(TopicCreateParams.builder().topicMemo(memo).build());
    }

    @Override
    public Topic createTopic(final Authority adminAuthority, final String memo) {
        Objects.requireNonNull(adminAuthority, "adminAuthority must not be null");
        checkMemo(memo);
        return createTopic(TopicCreateParams.builder().adminAuthority(adminAuthority).topicMemo(memo).build());
    }

    @Override
    public Topic createTopic(final TopicCreateParams params) {
        Objects.requireNonNull(params, "params must not be null");
        final Record<TopicCreateReceipt> record = session.executeWithRecord(new TopicCreateTransaction(
                params.adminAuthority(), params.submitAuthority(), params.topicMemo(), params.autoRenewPeriod(),
                params.autoRenewAccount()));
        final String memo = params.topicMemo();
        return new Topic(record.receipt().topicId(), params.adminAuthority(), params.submitAuthority(),
                record.consensusTimestamp(), memo == null ? "" : memo);
    }

    @Override
    public @Nullable Topic findById(final Address topicId) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        final org.hiero.mirrornode.topic.Topic topic = session.read(mirror -> mirror.topic().findById(topicId));
        return topic == null ? null : Topics.toTopic(topic);
    }

    @Override
    public Page<Topic> getAll() {
        throw new ServiceException("The Mirror Node client offers no query for all topics", null);
    }

    @Override
    public void deleteTopic(final Address topicId) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        session.execute(new TopicDeleteTransaction(topicId));
    }

    @Override
    public TopicMessage submitMessage(final Address topicId, final byte[] message) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(message, "message must not be null");
        throw new ServiceException("The consensus node client offers no transaction to submit a topic message", null);
    }

    @Override
    public Page<TopicMessage> getMessages(final Address topicId) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        return MappedPage.of(session.read(mirror -> mirror.topic().getMessages(topicId)), Topics::toMessage);
    }

    @Override
    public TopicMessage getMessageBySequenceNumber(final Address topicId, final long sequenceNumber) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        final org.hiero.mirrornode.topic.TopicMessage message =
                session.read(mirror -> mirror.topic().getMessageBySequenceNumber(topicId, sequenceNumber));
        if (message == null) {
            throw new ServiceException("Topic " + topicId + " has no message " + sequenceNumber, null);
        }
        return Topics.toMessage(message);
    }

    @Override
    public HieroStream<TopicMessage> subscribe(final Address topicId) {
        Objects.requireNonNull(topicId, "topicId must not be null");
        return new TopicMessageStream(session, topicId);
    }

    private static void checkMemo(final String memo) {
        Objects.requireNonNull(memo, "memo must not be null");
        if (memo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("memo must be at most 100 bytes long in UTF-8");
        }
    }
}
