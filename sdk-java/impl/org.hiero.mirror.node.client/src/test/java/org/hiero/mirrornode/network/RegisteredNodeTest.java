package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `RegisteredNode`, generated from the specs.
@DisplayName("RegisteredNode")
class RegisteredNodeTest {

    @Test
    @DisplayName("creates a RegisteredNode and returns the values")
    @Tag("testId:mirrornode.network.RegisteredNode/create")
    void shouldCreateAndReturnTheValues() {
        final long registeredNodeId = registeredNodeIdValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable String description = descriptionValue();
        final List<RegisteredServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final TimestampRange timestamp = timestampValue();
        final RegisteredNode subject = new RegisteredNode(registeredNodeId, adminKey, createdTimestamp, description, serviceEndpoints, timestamp);
        assertValue(registeredNodeId, subject.registeredNodeId());
        assertValue(adminKey, subject.adminKey());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(description, subject.description());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("creates a RegisteredNode with the default values")
    @Tag("testId:mirrornode.network.RegisteredNode/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final RegisteredNode subject = new RegisteredNode(registeredNodeIdValue(), adminKeyValue(), createdTimestampValue(), descriptionValue(), timestampValue());
        assertValue(List.of(), subject.serviceEndpoints());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.RegisteredNode/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long registeredNodeId = registeredNodeIdValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable String description = descriptionValue();
        final List<RegisteredServiceEndpoint> serviceEndpoints = serviceEndpointsValue();
        final TimestampRange timestamp = timestampValue();
        final RegisteredNode subject = RegisteredNode.builder()
                .registeredNodeId(registeredNodeId)
                .adminKey(adminKey)
                .createdTimestamp(createdTimestamp)
                .description(description)
                .serviceEndpoints(serviceEndpoints)
                .timestamp(timestamp)
                .build();
        assertValue(registeredNodeId, subject.registeredNodeId());
        assertValue(adminKey, subject.adminKey());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(description, subject.description());
        assertValue(serviceEndpoints, subject.serviceEndpoints());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.RegisteredNode/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final RegisteredNode subject = RegisteredNode.builder()
                .registeredNodeId(registeredNodeIdValue())
                .timestamp(timestampValue())
                .build();
        assertNull(subject.adminKey());
        assertNull(subject.createdTimestamp());
        assertNull(subject.description());
        assertValue(List.of(), subject.serviceEndpoints());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.network.RegisteredNode/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> RegisteredNode.builder().build());
    }

    @Test
    @DisplayName("registeredNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.RegisteredNode/registeredNodeId/accepts-at-minimum")
    void shouldAcceptRegisteredNodeIdAtMinimum() {
        final long value = -9223372036854775808L;
        final RegisteredNode subject = new RegisteredNode(value, adminKeyValue(), createdTimestampValue(), descriptionValue(), serviceEndpointsValue(), timestampValue());
        assertValue(value, subject.registeredNodeId());
    }

    @Test
    @DisplayName("registeredNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.RegisteredNode/registeredNodeId/accepts-at-maximum")
    void shouldAcceptRegisteredNodeIdAtMaximum() {
        final long value = 9223372036854775807L;
        final RegisteredNode subject = new RegisteredNode(value, adminKeyValue(), createdTimestampValue(), descriptionValue(), serviceEndpointsValue(), timestampValue());
        assertValue(value, subject.registeredNodeId());
    }

    @Test
    @DisplayName("rejects null for serviceEndpoints")
    @Tag("testId:mirrornode.network.RegisteredNode/serviceEndpoints/rejects-null")
    void shouldRejectNullServiceEndpoints() {
        assertThrows(NullPointerException.class, () -> new RegisteredNode(registeredNodeIdValue(), adminKeyValue(), createdTimestampValue(), descriptionValue(), (List<RegisteredServiceEndpoint>) null, timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies serviceEndpoints; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.network.RegisteredNode/serviceEndpoints/copies-in")
    void shouldCopyServiceEndpointsIn() {
        final List<RegisteredServiceEndpoint> value = new ArrayList<>(serviceEndpointsValue());
        final List<RegisteredServiceEndpoint> expected = List.copyOf(value);
        final RegisteredNode subject = new RegisteredNode(registeredNodeIdValue(), adminKeyValue(), createdTimestampValue(), descriptionValue(), value, timestampValue());
        value.clear();
        assertValue(expected, subject.serviceEndpoints());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serviceEndpoints()).clear());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.network.RegisteredNode/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new RegisteredNode(registeredNodeIdValue(), adminKeyValue(), createdTimestampValue(), descriptionValue(), serviceEndpointsValue(), (TimestampRange) null));
    }

    private static long registeredNodeIdValue() {
        return 1L;
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String descriptionValue() {
        return "value";
    }

    private static List<RegisteredServiceEndpoint> serviceEndpointsValue() {
        return List.of(new RegisteredServiceEndpoint(List.of(RegisteredBlockNodeApi.OTHER), null, null, null, 1, true, RegisteredNodeType.BLOCK_NODE));
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static RegisteredNode create() {
        return new RegisteredNode(registeredNodeIdValue(), adminKeyValue(), createdTimestampValue(), descriptionValue(), serviceEndpointsValue(), timestampValue());
    }
}
