package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NodeAnswer`, generated from the specs.
@DisplayName("NodeAnswer")
class NodeAnswerTest {

    @Test
    @DisplayName("creates a NodeAnswer and returns the values")
    @Tag("testId:consensusnode.client.NodeAnswer/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionStatus status = statusValue();
        final NodeAnswer subject = new NodeAnswer(status);
        assertValue(status, subject.status());
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:consensusnode.client.NodeAnswer/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new NodeAnswer((TransactionStatus) null));
    }

    private static TransactionStatus statusValue() {
        return BasicTransactionStatus.OK;
    }

    private static NodeAnswer create() {
        return new NodeAnswer(statusValue());
    }
}
