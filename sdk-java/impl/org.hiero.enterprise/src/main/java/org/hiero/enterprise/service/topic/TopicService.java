package org.hiero.enterprise.service.topic;

import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.internal.FailedStream;
import org.hiero.enterprise.service.internal.Unbound;
import org.hiero.ledger.Address;
import org.hiero.sdk.common.HieroStream;
import org.jspecify.annotations.Nullable;

public class TopicService {

    /// Creates a new `TopicService` that is not bound to a session: every method fails with `ServiceException`.
    /// A usable service is created with `TopicFactory.createService(session)`.
    public TopicService() {
    }

    /// Create a public topic (anyone may submit messages). An admin authority allows later updates and deletion.
    /// The same as createTopic with TopicCreateParams that have only these values.
    ///
    /// @throws ServiceException if a service error occurs
    public Topic createTopic() {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Topic createTopic(final String memo) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Topic createTopic(final Authority adminAuthority, final String memo) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// Create a topic with the given parameters: the general form of the methods above, for every option of a new
    /// topic.
    ///
    /// @throws ServiceException if a service error occurs
    public Topic createTopic(final TopicCreateParams params) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// @throws ServiceException if a service error occurs
    public @Nullable Topic findById(final Address topicId) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Page<Topic> getAll() {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// Delete a topic (requires the topic's admin authority)
    ///
    /// @throws ServiceException if a service error occurs
    public void deleteTopic(final Address topicId) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// Submit a message to a topic
    ///
    /// @throws ServiceException if a service error occurs
    public TopicMessage submitMessage(final Address topicId, final byte[] message) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Page<TopicMessage> getMessages(final Address topicId) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    /// @throws ServiceException if a service error occurs
    public TopicMessage getMessageBySequenceNumber(final Address topicId, final long sequenceNumber) {
        throw Unbound.error("TopicService", "TopicFactory");
    }

    public HieroStream<TopicMessage> subscribe(final Address topicId) {
        return new FailedStream<>(Unbound.error("TopicService", "TopicFactory"));
    }
}
