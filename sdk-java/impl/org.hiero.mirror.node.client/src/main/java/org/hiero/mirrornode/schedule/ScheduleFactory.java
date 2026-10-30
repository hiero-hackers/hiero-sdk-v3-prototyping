package org.hiero.mirrornode.schedule;

import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.internal.DefaultScheduleRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode.schedule`.
public final class ScheduleFactory {

    public static ScheduleRepository createRepository(final MirrorNode mirrorNode) {
        return new DefaultScheduleRepository(MirrorNodeRest.create(mirrorNode));
    }

    private ScheduleFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
