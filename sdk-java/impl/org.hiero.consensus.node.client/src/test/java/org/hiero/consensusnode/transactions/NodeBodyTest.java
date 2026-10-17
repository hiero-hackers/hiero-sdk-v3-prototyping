package org.hiero.consensusnode.transactions;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NodeBody`, generated from the specs.
@DisplayName("NodeBody")
class NodeBodyTest {

    @Test
    @DisplayName("creates a NodeBody and returns the values")
    @Tag("testId:consensusnode.transactions.NodeBody/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId node = nodeValue();
        final byte[] bytes = bytesValue();
        final NodeBody subject = new NodeBody(node, bytes);
        assertValue(node, subject.node());
        assertValue(bytes, subject.bytes());
    }

    @Test
    @DisplayName("rejects null for node")
    @Tag("testId:consensusnode.transactions.NodeBody/node/rejects-null")
    void shouldRejectNullNode() {
        assertThrows(NullPointerException.class, () -> new NodeBody((AccountId) null, bytesValue()));
    }

    @Test
    @DisplayName("rejects null for bytes")
    @Tag("testId:consensusnode.transactions.NodeBody/bytes/rejects-null")
    void shouldRejectNullBytes() {
        assertThrows(NullPointerException.class, () -> new NodeBody(nodeValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies bytes; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.NodeBody/bytes/copies-in")
    void shouldCopyBytesIn() {
        final byte[] value = bytesValue();
        final byte[] expected = value.clone();
        final NodeBody subject = new NodeBody(nodeValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytes());
        final byte[] returned = subject.bytes();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytes());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.transactions.NodeBody/value-semantics")
    void shouldCompareByValue() {
        final NodeBody subject = create();
        final NodeBody other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId nodeValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static byte[] bytesValue() {
        return new byte[] {1, 2, 3};
    }

    private static NodeBody create() {
        return new NodeBody(nodeValue(), bytesValue());
    }
}
