package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ServiceEndpoint`, generated from the specs.
@DisplayName("ServiceEndpoint")
class ServiceEndpointTest {

    @Test
    @DisplayName("creates a ServiceEndpoint and returns the values")
    @Tag("testId:mirrornode.network.ServiceEndpoint/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable String ipAddressV4 = ipAddressV4Value();
        final int port = portValue();
        final @Nullable String domainName = domainNameValue();
        final ServiceEndpoint subject = new ServiceEndpoint(ipAddressV4, port, domainName);
        assertValue(ipAddressV4, subject.ipAddressV4());
        assertValue(port, subject.port());
        assertValue(domainName, subject.domainName());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.ServiceEndpoint/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable String ipAddressV4 = ipAddressV4Value();
        final int port = portValue();
        final @Nullable String domainName = domainNameValue();
        final ServiceEndpoint subject = ServiceEndpoint.builder()
                .ipAddressV4(ipAddressV4)
                .port(port)
                .domainName(domainName)
                .build();
        assertValue(ipAddressV4, subject.ipAddressV4());
        assertValue(port, subject.port());
        assertValue(domainName, subject.domainName());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.ServiceEndpoint/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ServiceEndpoint subject = ServiceEndpoint.builder()
                .port(portValue())
                .build();
        assertNull(subject.ipAddressV4());
        assertNull(subject.domainName());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.network.ServiceEndpoint/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ServiceEndpoint.builder().build());
    }

    @Test
    @DisplayName("port: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.network.ServiceEndpoint/port/accepts-at-minimum")
    void shouldAcceptPortAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ServiceEndpoint subject = new ServiceEndpoint(ipAddressV4Value(), value, domainNameValue());
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.network.ServiceEndpoint/port/accepts-at-maximum")
    void shouldAcceptPortAtMaximum() {
        final int value = 2147483647;
        final ServiceEndpoint subject = new ServiceEndpoint(ipAddressV4Value(), value, domainNameValue());
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.ServiceEndpoint/value-semantics")
    void shouldCompareByValue() {
        final ServiceEndpoint subject = create();
        final ServiceEndpoint other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static String ipAddressV4Value() {
        return "value";
    }

    private static int portValue() {
        return 1;
    }

    private static String domainNameValue() {
        return "value";
    }

    private static ServiceEndpoint create() {
        return new ServiceEndpoint(ipAddressV4Value(), portValue(), domainNameValue());
    }
}
