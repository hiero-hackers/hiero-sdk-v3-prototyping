package org.hiero.mirrornode;

import java.util.Objects;
import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.http.MirrorNodeHttpClient;
import org.hiero.mirrornode.internal.DefaultAccountRepository;
import org.hiero.mirrornode.internal.DefaultContractRepository;
import org.hiero.mirrornode.internal.DefaultNetworkRepository;
import org.hiero.mirrornode.internal.DefaultNftRepository;
import org.hiero.mirrornode.internal.DefaultTokenRepository;
import org.hiero.mirrornode.internal.DefaultTopicRepository;
import org.hiero.mirrornode.internal.DefaultTransactionRepository;
import org.hiero.mirrornode.internal.MirrorNodeRest;

/// Factory methods of the package `org.hiero.mirrornode`.
public final class MirrornodeFactory {

    /// @throws MirrorNodeException if a mirror node error occurs
    public static MirrorNodeClient createMirrorNodeClient(final MirrorNode mirrorNode) {
        Objects.requireNonNull(mirrorNode, "mirrorNode must not be null");
        // one HttpClient (and connection pool) for all repositories of the client
        final MirrorNodeHttpClient httpClient = MirrorNodeRest.createHttpClient(mirrorNode);
        final MirrorNodeRest rest = new MirrorNodeRest(httpClient);
        return new MirrorNodeClient(new DefaultAccountRepository(rest), new DefaultContractRepository(rest),
                new DefaultNetworkRepository(rest), new DefaultNftRepository(rest), new DefaultTokenRepository(rest),
                new DefaultTopicRepository(rest), new DefaultTransactionRepository(rest), httpClient);
    }

    private MirrornodeFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
