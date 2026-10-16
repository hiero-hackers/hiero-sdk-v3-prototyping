package org.hiero.consensusnode.admin.nodes;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.IpAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `ServiceEndpoint`, generated from the specs.
@DisplayName("ServiceEndpoint")
class ServiceEndpointTest {

    @Test
    @DisplayName("creates a ServiceEndpoint and returns the values")
    void shouldCreateAndReturnTheValues() {
        final @Nullable IpAddress ipAddress = ipAddressValue();
        final @Nullable String domainName = (String) null;
        final int port = portValue();
        final ServiceEndpoint subject = new ServiceEndpoint(ipAddress, domainName, port);
        assertValue(ipAddress, subject.ipAddress());
        assertValue(domainName, subject.domainName());
        assertValue(port, subject.port());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable IpAddress ipAddress = ipAddressValue();
        final @Nullable String domainName = (String) null;
        final int port = portValue();
        final ServiceEndpoint subject = ServiceEndpoint.builder()
                .ipAddress(ipAddress)
                .domainName(domainName)
                .port(port)
                .build();
        assertValue(ipAddress, subject.ipAddress());
        assertValue(domainName, subject.domainName());
        assertValue(port, subject.port());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final ServiceEndpoint subject = ServiceEndpoint.builder()
                .ipAddress(ipAddressValue())
                .port(portValue())
                .build();
        assertValue(ipAddressValue(), subject.ipAddress());
        assertNull(subject.domainName());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ServiceEndpoint.builder().build());
    }

    @Test
    @DisplayName("port: accepts the minimum (0)")
    void shouldAcceptPortAtMinimum() {
        final int value = 0;
        final ServiceEndpoint subject = new ServiceEndpoint(ipAddressValue(), (String) null, value);
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: accepts the maximum (65535)")
    void shouldAcceptPortAtMaximum() {
        final int value = 65535;
        final ServiceEndpoint subject = new ServiceEndpoint(ipAddressValue(), (String) null, value);
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: rejects a value below the minimum (-1)")
    void shouldRejectPortBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ServiceEndpoint(ipAddressValue(), (String) null, -1));
    }

    @Test
    @DisplayName("port: rejects a value above the maximum (65536)")
    void shouldRejectPortAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ServiceEndpoint(ipAddressValue(), (String) null, 65536));
    }

    @Test
    @DisplayName("rejects ipAddress and domainName together (@@oneOf)")
    void shouldRejectTwoOfIpAddressOrDomainName() {
        assertThrows(IllegalArgumentException.class, () -> new ServiceEndpoint(ipAddressValue(), domainNameValue(), portValue()));
    }

    @Test
    @DisplayName("rejects none of ipAddress, domainName (@@oneOf)")
    void shouldRejectNoneOfIpAddressOrDomainName() {
        assertThrows(IllegalArgumentException.class, () -> new ServiceEndpoint((IpAddress) null, (String) null, portValue()));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final ServiceEndpoint subject = create();
        final ServiceEndpoint other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static IpAddress ipAddressValue() {
        return new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1});
    }

    private static String domainNameValue() {
        return "value";
    }

    private static int portValue() {
        return 1;
    }

    private static ServiceEndpoint create() {
        return new ServiceEndpoint(ipAddressValue(), (String) null, portValue());
    }
}
