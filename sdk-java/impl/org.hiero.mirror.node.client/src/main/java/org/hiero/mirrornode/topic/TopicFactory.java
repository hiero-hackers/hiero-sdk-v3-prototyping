package org.hiero.mirrornode.topic;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultTopicRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.topic`.
public final class TopicFactory {

    public static TopicRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultTopicRepository(MirrorNodeRest.create(mirrorNode));
    }

    private TopicFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
