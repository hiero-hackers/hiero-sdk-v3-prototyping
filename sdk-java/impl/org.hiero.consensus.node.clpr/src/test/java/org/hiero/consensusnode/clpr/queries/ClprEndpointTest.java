package org.hiero.consensusnode.clpr.queries;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprEndpoint`, generated from the specs.
@DisplayName("ClprEndpoint")
class ClprEndpointTest {

    @Test
    @DisplayName("creates a ClprEndpoint and returns the values")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpoint/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final ClprServiceEndpoint serviceEndpoint = serviceEndpointValue();
        final byte[] tlsCertificate = tlsCertificateValue();
        final byte[] accountId = accountIdValue();
        final ClprEndpoint subject = new ClprEndpoint(serviceEndpoint, tlsCertificate, accountId);
        assertValue(serviceEndpoint, subject.serviceEndpoint());
        assertValue(tlsCertificate, subject.tlsCertificate());
        assertValue(accountId, subject.accountId());
    }

    @Test
    @DisplayName("rejects null for serviceEndpoint")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpoint/serviceEndpoint/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullServiceEndpoint() {
        assertThrows(NullPointerException.class, () -> new ClprEndpoint((ClprServiceEndpoint) null, tlsCertificateValue(), accountIdValue()));
    }

    @Test
    @DisplayName("rejects null for tlsCertificate")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpoint/tlsCertificate/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullTlsCertificate() {
        assertThrows(NullPointerException.class, () -> new ClprEndpoint(serviceEndpointValue(), (byte[]) null, accountIdValue()));
    }

    @Test
    @DisplayName("the constructor copies tlsCertificate; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpoint/tlsCertificate/copies-in")
    @Tag("hip:1535")
    void shouldCopyTlsCertificateIn() {
        final byte[] value = tlsCertificateValue();
        final byte[] expected = value.clone();
        final ClprEndpoint subject = new ClprEndpoint(serviceEndpointValue(), value, accountIdValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.tlsCertificate());
        final byte[] returned = subject.tlsCertificate();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.tlsCertificate());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpoint/accountId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new ClprEndpoint(serviceEndpointValue(), tlsCertificateValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies accountId; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpoint/accountId/copies-in")
    @Tag("hip:1535")
    void shouldCopyAccountIdIn() {
        final byte[] value = accountIdValue();
        final byte[] expected = value.clone();
        final ClprEndpoint subject = new ClprEndpoint(serviceEndpointValue(), tlsCertificateValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.accountId());
        final byte[] returned = subject.accountId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.accountId());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpoint/value-semantics")
    @Tag("hip:1535")
    void shouldCompareByValue() {
        final ClprEndpoint subject = create();
        final ClprEndpoint other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ClprServiceEndpoint serviceEndpointValue() {
        return new ClprServiceEndpoint("value", 1);
    }

    private static byte[] tlsCertificateValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] accountIdValue() {
        return new byte[] {1, 2, 3};
    }

    private static ClprEndpoint create() {
        return new ClprEndpoint(serviceEndpointValue(), tlsCertificateValue(), accountIdValue());
    }

    private static ClprEndpoint defaultSubject() {
        return new ClprEndpoint(new ClprServiceEndpoint("127.0.0.1", 50211), new byte[] {(byte) 1, (byte) 2, (byte) 3}, new byte[] {(byte) 4, (byte) 5, (byte) 6});
    }
}
