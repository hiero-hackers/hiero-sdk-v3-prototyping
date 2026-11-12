package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `PluginVersion`, generated from the specs.
@DisplayName("PluginVersion")
class PluginVersionTest {

    @Test
    @DisplayName("creates a PluginVersion and returns the values")
    @Tag("testId:blocknode.status.PluginVersion/create")
    void shouldCreateAndReturnTheValues() {
        final String pluginId = pluginIdValue();
        final SemanticVersion softwareVersion = softwareVersionValue();
        final List<String> featureNames = featureNamesValue();
        final PluginVersion subject = new PluginVersion(pluginId, softwareVersion, featureNames);
        assertValue(pluginId, subject.pluginId());
        assertValue(softwareVersion, subject.softwareVersion());
        assertValue(featureNames, subject.featureNames());
    }

    @Test
    @DisplayName("rejects null for pluginId")
    @Tag("testId:blocknode.status.PluginVersion/pluginId/rejects-null")
    void shouldRejectNullPluginId() {
        assertThrows(NullPointerException.class, () -> new PluginVersion((String) null, softwareVersionValue(), featureNamesValue()));
    }

    @Test
    @DisplayName("rejects null for softwareVersion")
    @Tag("testId:blocknode.status.PluginVersion/softwareVersion/rejects-null")
    void shouldRejectNullSoftwareVersion() {
        assertThrows(NullPointerException.class, () -> new PluginVersion(pluginIdValue(), (SemanticVersion) null, featureNamesValue()));
    }

    @Test
    @DisplayName("rejects null for featureNames")
    @Tag("testId:blocknode.status.PluginVersion/featureNames/rejects-null")
    void shouldRejectNullFeatureNames() {
        assertThrows(NullPointerException.class, () -> new PluginVersion(pluginIdValue(), softwareVersionValue(), (List<String>) null));
    }

    @Test
    @DisplayName("the constructor copies featureNames; the accessor returns an unmodifiable collection")
    @Tag("testId:blocknode.status.PluginVersion/featureNames/copies-in")
    void shouldCopyFeatureNamesIn() {
        final List<String> value = new ArrayList<>(featureNamesValue());
        final List<String> expected = List.copyOf(value);
        final PluginVersion subject = new PluginVersion(pluginIdValue(), softwareVersionValue(), value);
        value.clear();
        assertValue(expected, subject.featureNames());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.featureNames()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.PluginVersion/value-semantics")
    void shouldCompareByValue() {
        final PluginVersion subject = create();
        final PluginVersion other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static String pluginIdValue() {
        return "value";
    }

    private static SemanticVersion softwareVersionValue() {
        return new SemanticVersion(0, 44, 2, "", "");
    }

    private static List<String> featureNamesValue() {
        return List.of("value");
    }

    private static PluginVersion create() {
        return new PluginVersion(pluginIdValue(), softwareVersionValue(), featureNamesValue());
    }
}
