package org.hiero.enterprise.service.internal;

import org.hiero.enterprise.service.topic.Topic;
import org.hiero.enterprise.service.topic.TopicMessage;

/// Conversions of the topic data of the Mirror Node into the types of the topic service.
public final class Topics {

    private Topics() {
    }

    /// Converts a topic of the Mirror Node.
    ///
    /// @param topic the topic of the Mirror Node
    /// @return the topic
    public static Topic toTopic(final org.hiero.mirrornode.topic.Topic topic) {
        return new Topic(topic.topicId(), topic.adminAuthority(), topic.submitAuthority(), topic.createdTimestamp(),
                topic.memo());
    }

    /// Converts a topic message of the Mirror Node.
    ///
    /// @param message the message of the Mirror Node
    /// @return the message
    public static TopicMessage toMessage(final org.hiero.mirrornode.topic.TopicMessage message) {
        return new TopicMessage(message.consensusTimestamp(), message.message(), message.payerAccountId(),
                message.sequenceNumber(), message.topicId());
    }
}
