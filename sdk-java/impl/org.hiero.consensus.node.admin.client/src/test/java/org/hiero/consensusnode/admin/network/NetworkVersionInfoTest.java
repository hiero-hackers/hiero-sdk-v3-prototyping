package org.hiero.consensusnode.admin.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkVersionInfo`, generated from the specs.
@DisplayName("NetworkVersionInfo")
class NetworkVersionInfoTest {

    @Test
    @DisplayName("creates a NetworkVersionInfo and returns the values")
    void shouldCreateAndReturnTheValues() {
        final SemanticVersion hapiVersion = hapiVersionValue();
        final SemanticVersion servicesVersion = servicesVersionValue();
        final NetworkVersionInfo subject = new NetworkVersionInfo(hapiVersion, servicesVersion);
        assertValue(hapiVersion, subject.hapiVersion());
        assertValue(servicesVersion, subject.servicesVersion());
    }

    @Test
    @DisplayName("rejects null for hapiVersion")
    void shouldRejectNullHapiVersion() {
        assertThrows(NullPointerException.class, () -> new NetworkVersionInfo((SemanticVersion) null, servicesVersionValue()));
    }

    @Test
    @DisplayName("rejects null for servicesVersion")
    void shouldRejectNullServicesVersion() {
        assertThrows(NullPointerException.class, () -> new NetworkVersionInfo(hapiVersionValue(), (SemanticVersion) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final NetworkVersionInfo subject = create();
        final NetworkVersionInfo other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static SemanticVersion hapiVersionValue() {
        return new SemanticVersion(1, 1, 1, null, null);
    }

    private static SemanticVersion servicesVersionValue() {
        return new SemanticVersion(1, 1, 1, null, null);
    }

    private static NetworkVersionInfo create() {
        return new NetworkVersionInfo(hapiVersionValue(), servicesVersionValue());
    }
}
