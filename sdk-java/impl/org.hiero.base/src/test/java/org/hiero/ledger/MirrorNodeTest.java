package org.hiero.ledger;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `MirrorNode`, generated from the specs.
@DisplayName("MirrorNode")
class MirrorNodeTest {

    @Test
    @DisplayName("creates a MirrorNode and returns the values")
    @Tag("testId:ledger.MirrorNode/create")
    void shouldCreateAndReturnTheValues() {
        final String restBaseUrl = restBaseUrlValue();
        final MirrorNode subject = new MirrorNode(restBaseUrl);
        assertValue(restBaseUrl, subject.restBaseUrl());
    }

    @Test
    @DisplayName("rejects null for restBaseUrl")
    @Tag("testId:ledger.MirrorNode/restBaseUrl/rejects-null")
    void shouldRejectNullRestBaseUrl() {
        assertThrows(NullPointerException.class, () -> new MirrorNode((String) null));
    }

    @Test
    @DisplayName("restBaseUrl: rejects a value that is no URL")
    @Tag("testId:ledger.MirrorNode/restBaseUrl/rejects-that-is-no-url")
    void shouldRejectRestBaseUrlThatIsNoUrl() {
        assertThrows(IllegalArgumentException.class, () -> new MirrorNode("not a url"));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:ledger.MirrorNode/value-semantics")
    void shouldCompareByValue() {
        final MirrorNode subject = create();
        final MirrorNode other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static String restBaseUrlValue() {
        return "https://example.com/v0";
    }

    private static MirrorNode create() {
        return new MirrorNode(restBaseUrlValue());
    }

    private static MirrorNode defaultSubject() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
