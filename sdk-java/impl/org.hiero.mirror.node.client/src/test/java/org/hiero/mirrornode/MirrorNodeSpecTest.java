package org.hiero.mirrornode;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.ledger.MirrorNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node")
class MirrorNodeSpecTest {

    /// `mirrornode/a-mirror-node-client-talks-to-the-mirror-node-it-was-created-for`
    ///
    /// Source: the Go SDK (TestUnitClientMirrorRestClientResolvesBaseURL) and the Java SDK (ClientMirrorBaseUrlTest): the
    /// REST client of a client talks to the configured mirror node. Creating the client sends no request.
    @Test
    @DisplayName("a mirror node client talks to the mirror node it was created for")
    @Tag("testId:mirrornode/a-mirror-node-client-talks-to-the-mirror-node-it-was-created-for")
    void aMirrorNodeClientTalksToTheMirrorNodeItWasCreatedFor() {
        // given
        final MirrorNode mirrorNode = new MirrorNode("https://mainnet.mirrornode.hedera.com/api/v1");

        // when
        final MirrorNodeClient client = MirrornodeFactory.createMirrorNodeClient(mirrorNode);

        // then
        assertEquals(mirrorNode, client.mirrorNodeHttpClient().mirrorNode());
    }
}
