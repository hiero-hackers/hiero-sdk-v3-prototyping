package org.hiero.mirrornode.topic;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.MirrorNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TopicFactory`, generated from the specs.
@DisplayName("TopicFactory")
class TopicFactoryTest {

    @Test
    @DisplayName("createRepository(mirrorNode) can be called and returns a value")
    @Tag("testId:mirrornode.topic/createRepository/call")
    void shouldCallCreateRepository() {
        assertNotNull(TopicFactory.createRepository(mirrorNodeValue()));
    }

    @Test
    @DisplayName("createRepository rejects null for mirrorNode")
    @Tag("testId:mirrornode.topic/createRepository/mirrorNode/rejects-null")
    void shouldRejectNullMirrorNodeInCreateRepository() {
        assertThrows(NullPointerException.class, () -> TopicFactory.createRepository((MirrorNode) null));
    }

    private static MirrorNode mirrorNodeValue() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
