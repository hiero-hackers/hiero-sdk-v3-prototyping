package org.hiero.consensusnode.clpr.queries;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprServiceEndpoint`, generated from the specs.
@DisplayName("ClprServiceEndpoint")
class ClprServiceEndpointTest {

    @Test
    @DisplayName("creates a ClprServiceEndpoint and returns the values")
    @Tag("testId:consensusnode.clpr.queries.ClprServiceEndpoint/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final String ipAddress = ipAddressValue();
        final int port = portValue();
        final ClprServiceEndpoint subject = new ClprServiceEndpoint(ipAddress, port);
        assertValue(ipAddress, subject.ipAddress());
        assertValue(port, subject.port());
    }

    @Test
    @DisplayName("rejects null for ipAddress")
    @Tag("testId:consensusnode.clpr.queries.ClprServiceEndpoint/ipAddress/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullIpAddress() {
        assertThrows(NullPointerException.class, () -> new ClprServiceEndpoint((String) null, portValue()));
    }

    @Test
    @DisplayName("port: accepts the minimum (0)")
    @Tag("testId:consensusnode.clpr.queries.ClprServiceEndpoint/port/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptPortAtMinimum() {
        final int value = 0;
        final ClprServiceEndpoint subject = new ClprServiceEndpoint(ipAddressValue(), value);
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: accepts the maximum (65535)")
    @Tag("testId:consensusnode.clpr.queries.ClprServiceEndpoint/port/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptPortAtMaximum() {
        final int value = 65535;
        final ClprServiceEndpoint subject = new ClprServiceEndpoint(ipAddressValue(), value);
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.clpr.queries.ClprServiceEndpoint/port/rejects-below-minimum")
    @Tag("hip:1535")
    void shouldRejectPortBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprServiceEndpoint(ipAddressValue(), -1));
    }

    @Test
    @DisplayName("port: rejects a value above the maximum (65536)")
    @Tag("testId:consensusnode.clpr.queries.ClprServiceEndpoint/port/rejects-above-maximum")
    @Tag("hip:1535")
    void shouldRejectPortAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ClprServiceEndpoint(ipAddressValue(), 65536));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.clpr.queries.ClprServiceEndpoint/value-semantics")
    @Tag("hip:1535")
    void shouldCompareByValue() {
        final ClprServiceEndpoint subject = create();
        final ClprServiceEndpoint other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static String ipAddressValue() {
        return "value";
    }

    private static int portValue() {
        return 1;
    }

    private static ClprServiceEndpoint create() {
        return new ClprServiceEndpoint(ipAddressValue(), portValue());
    }
}
