package org.hiero.enterprise.service.topic;

import org.hiero.enterprise.service.Session;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.enterprise.service.internal.DefaultTopicService;

/// Factory methods of the package `org.hiero.enterprise.service.topic`.
public final class TopicFactory {

    /// Creates the service for the given session. With a framework integration the service is usually obtained via
    /// dependency injection instead.
    public static TopicService createService(final Session session) {
        return new DefaultTopicService(DefaultSession.of(session));
    }

    private TopicFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
