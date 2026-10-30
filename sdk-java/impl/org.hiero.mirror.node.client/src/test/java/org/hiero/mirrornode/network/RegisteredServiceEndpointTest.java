package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `RegisteredServiceEndpoint`, generated from the specs.
@DisplayName("RegisteredServiceEndpoint")
class RegisteredServiceEndpointTest {

    @Test
    @DisplayName("creates a RegisteredServiceEndpoint and returns the values")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/create")
    void shouldCreateAndReturnTheValues() {
        final List<RegisteredBlockNodeApi> blockNodeApis = blockNodeApisValue();
        final @Nullable String domainName = domainNameValue();
        final @Nullable String generalServiceDescription = generalServiceDescriptionValue();
        final @Nullable String ipAddress = ipAddressValue();
        final int port = portValue();
        final boolean requiresTls = requiresTlsValue();
        final RegisteredNodeType type = typeValue();
        final RegisteredServiceEndpoint subject = new RegisteredServiceEndpoint(blockNodeApis, domainName, generalServiceDescription, ipAddress, port, requiresTls, type);
        assertValue(blockNodeApis, subject.blockNodeApis());
        assertValue(domainName, subject.domainName());
        assertValue(generalServiceDescription, subject.generalServiceDescription());
        assertValue(ipAddress, subject.ipAddress());
        assertValue(port, subject.port());
        assertValue(requiresTls, subject.requiresTls());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("creates a RegisteredServiceEndpoint with the default values")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final RegisteredServiceEndpoint subject = new RegisteredServiceEndpoint(domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), portValue(), requiresTlsValue(), typeValue());
        assertValue(List.of(), subject.blockNodeApis());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final List<RegisteredBlockNodeApi> blockNodeApis = blockNodeApisValue();
        final @Nullable String domainName = domainNameValue();
        final @Nullable String generalServiceDescription = generalServiceDescriptionValue();
        final @Nullable String ipAddress = ipAddressValue();
        final int port = portValue();
        final boolean requiresTls = requiresTlsValue();
        final RegisteredNodeType type = typeValue();
        final RegisteredServiceEndpoint subject = RegisteredServiceEndpoint.builder()
                .blockNodeApis(blockNodeApis)
                .domainName(domainName)
                .generalServiceDescription(generalServiceDescription)
                .ipAddress(ipAddress)
                .port(port)
                .requiresTls(requiresTls)
                .type(type)
                .build();
        assertValue(blockNodeApis, subject.blockNodeApis());
        assertValue(domainName, subject.domainName());
        assertValue(generalServiceDescription, subject.generalServiceDescription());
        assertValue(ipAddress, subject.ipAddress());
        assertValue(port, subject.port());
        assertValue(requiresTls, subject.requiresTls());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final RegisteredServiceEndpoint subject = RegisteredServiceEndpoint.builder()
                .port(portValue())
                .requiresTls(requiresTlsValue())
                .type(typeValue())
                .build();
        assertValue(List.of(), subject.blockNodeApis());
        assertNull(subject.domainName());
        assertNull(subject.generalServiceDescription());
        assertNull(subject.ipAddress());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> RegisteredServiceEndpoint.builder().build());
    }

    @Test
    @DisplayName("rejects null for blockNodeApis")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/blockNodeApis/rejects-null")
    void shouldRejectNullBlockNodeApis() {
        assertThrows(NullPointerException.class, () -> new RegisteredServiceEndpoint((List<RegisteredBlockNodeApi>) null, domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), portValue(), requiresTlsValue(), typeValue()));
    }

    @Test
    @DisplayName("the constructor copies blockNodeApis; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/blockNodeApis/copies-in")
    void shouldCopyBlockNodeApisIn() {
        final List<RegisteredBlockNodeApi> value = new ArrayList<>(blockNodeApisValue());
        final List<RegisteredBlockNodeApi> expected = List.copyOf(value);
        final RegisteredServiceEndpoint subject = new RegisteredServiceEndpoint(value, domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), portValue(), requiresTlsValue(), typeValue());
        value.clear();
        assertValue(expected, subject.blockNodeApis());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.blockNodeApis()).clear());
    }

    @Test
    @DisplayName("port: accepts the minimum (0)")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/port/accepts-at-minimum")
    void shouldAcceptPortAtMinimum() {
        final int value = 0;
        final RegisteredServiceEndpoint subject = new RegisteredServiceEndpoint(blockNodeApisValue(), domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), value, requiresTlsValue(), typeValue());
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: accepts the maximum (65535)")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/port/accepts-at-maximum")
    void shouldAcceptPortAtMaximum() {
        final int value = 65535;
        final RegisteredServiceEndpoint subject = new RegisteredServiceEndpoint(blockNodeApisValue(), domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), value, requiresTlsValue(), typeValue());
        assertValue(value, subject.port());
    }

    @Test
    @DisplayName("port: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/port/rejects-below-minimum")
    void shouldRejectPortBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new RegisteredServiceEndpoint(blockNodeApisValue(), domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), -1, requiresTlsValue(), typeValue()));
    }

    @Test
    @DisplayName("port: rejects a value above the maximum (65536)")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/port/rejects-above-maximum")
    void shouldRejectPortAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new RegisteredServiceEndpoint(blockNodeApisValue(), domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), 65536, requiresTlsValue(), typeValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new RegisteredServiceEndpoint(blockNodeApisValue(), domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), portValue(), requiresTlsValue(), (RegisteredNodeType) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.RegisteredServiceEndpoint/value-semantics")
    void shouldCompareByValue() {
        final RegisteredServiceEndpoint subject = create();
        final RegisteredServiceEndpoint other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static List<RegisteredBlockNodeApi> blockNodeApisValue() {
        return List.of(RegisteredBlockNodeApi.OTHER);
    }

    private static String domainNameValue() {
        return "value";
    }

    private static String generalServiceDescriptionValue() {
        return "value";
    }

    private static String ipAddressValue() {
        return "value";
    }

    private static int portValue() {
        return 1;
    }

    private static boolean requiresTlsValue() {
        return true;
    }

    private static RegisteredNodeType typeValue() {
        return RegisteredNodeType.BLOCK_NODE;
    }

    private static RegisteredServiceEndpoint create() {
        return new RegisteredServiceEndpoint(blockNodeApisValue(), domainNameValue(), generalServiceDescriptionValue(), ipAddressValue(), portValue(), requiresTlsValue(), typeValue());
    }
}
