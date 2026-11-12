package org.hiero.consensusnode.clpr.queries;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprEndpointManifest`, generated from the specs.
@DisplayName("ClprEndpointManifest")
class ClprEndpointManifestTest {

    @Test
    @DisplayName("creates a ClprEndpointManifest and returns the values")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final long version = versionValue();
        final byte[] serviceAddress = serviceAddressValue();
        final List<ClprEndpoint> endpoints = endpointsValue();
        final byte[] manifestStateProof = manifestStateProofValue();
        final ClprEndpointManifest subject = new ClprEndpointManifest(version, serviceAddress, endpoints, manifestStateProof);
        assertValue(version, subject.version());
        assertValue(serviceAddress, subject.serviceAddress());
        assertValue(endpoints, subject.endpoints());
        assertValue(manifestStateProof, subject.manifestStateProof());
    }

    @Test
    @DisplayName("version: accepts the minimum (0)")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/version/accepts-at-minimum")
    @Tag("hip:1535")
    void shouldAcceptVersionAtMinimum() {
        final long value = 0L;
        final ClprEndpointManifest subject = new ClprEndpointManifest(value, serviceAddressValue(), endpointsValue(), manifestStateProofValue());
        assertValue(value, subject.version());
    }

    @Test
    @DisplayName("version: accepts the maximum (18446744073709551615)")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/version/accepts-at-maximum")
    @Tag("hip:1535")
    void shouldAcceptVersionAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final ClprEndpointManifest subject = new ClprEndpointManifest(value, serviceAddressValue(), endpointsValue(), manifestStateProofValue());
        assertValue(value, subject.version());
    }

    @Test
    @DisplayName("rejects null for serviceAddress")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/serviceAddress/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullServiceAddress() {
        assertThrows(NullPointerException.class, () -> new ClprEndpointManifest(versionValue(), (byte[]) null, endpointsValue(), manifestStateProofValue()));
    }

    @Test
    @DisplayName("the constructor copies serviceAddress; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/serviceAddress/copies-in")
    @Tag("hip:1535")
    void shouldCopyServiceAddressIn() {
        final byte[] value = serviceAddressValue();
        final byte[] expected = value.clone();
        final ClprEndpointManifest subject = new ClprEndpointManifest(versionValue(), value, endpointsValue(), manifestStateProofValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.serviceAddress());
        final byte[] returned = subject.serviceAddress();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.serviceAddress());
    }

    @Test
    @DisplayName("rejects null for endpoints")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/endpoints/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullEndpoints() {
        assertThrows(NullPointerException.class, () -> new ClprEndpointManifest(versionValue(), serviceAddressValue(), (List<ClprEndpoint>) null, manifestStateProofValue()));
    }

    @Test
    @DisplayName("the constructor copies endpoints; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/endpoints/copies-in")
    @Tag("hip:1535")
    void shouldCopyEndpointsIn() {
        final List<ClprEndpoint> value = new ArrayList<>(endpointsValue());
        final List<ClprEndpoint> expected = List.copyOf(value);
        final ClprEndpointManifest subject = new ClprEndpointManifest(versionValue(), serviceAddressValue(), value, manifestStateProofValue());
        value.clear();
        assertValue(expected, subject.endpoints());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.endpoints()).clear());
    }

    @Test
    @DisplayName("rejects null for manifestStateProof")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/manifestStateProof/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullManifestStateProof() {
        assertThrows(NullPointerException.class, () -> new ClprEndpointManifest(versionValue(), serviceAddressValue(), endpointsValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies manifestStateProof; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/manifestStateProof/copies-in")
    @Tag("hip:1535")
    void shouldCopyManifestStateProofIn() {
        final byte[] value = manifestStateProofValue();
        final byte[] expected = value.clone();
        final ClprEndpointManifest subject = new ClprEndpointManifest(versionValue(), serviceAddressValue(), endpointsValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.manifestStateProof());
        final byte[] returned = subject.manifestStateProof();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.manifestStateProof());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifest/value-semantics")
    @Tag("hip:1535")
    void shouldCompareByValue() {
        final ClprEndpointManifest subject = create();
        final ClprEndpointManifest other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long versionValue() {
        return 1L;
    }

    private static byte[] serviceAddressValue() {
        return new byte[] {1, 2, 3};
    }

    private static List<ClprEndpoint> endpointsValue() {
        return List.of(new ClprEndpoint(new ClprServiceEndpoint("127.0.0.1", 50211), new byte[] {(byte) 1, (byte) 2, (byte) 3}, new byte[] {(byte) 4, (byte) 5, (byte) 6}));
    }

    private static byte[] manifestStateProofValue() {
        return new byte[] {1, 2, 3};
    }

    private static ClprEndpointManifest create() {
        return new ClprEndpointManifest(versionValue(), serviceAddressValue(), endpointsValue(), manifestStateProofValue());
    }
}
