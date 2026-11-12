package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockNodeVersions`, generated from the specs.
@DisplayName("BlockNodeVersions")
class BlockNodeVersionsTest {

    @Test
    @DisplayName("creates a BlockNodeVersions and returns the values")
    @Tag("testId:blocknode.status.BlockNodeVersions/create")
    void shouldCreateAndReturnTheValues() {
        final SemanticVersion streamProtoVersion = streamProtoVersionValue();
        final SemanticVersion blockNodeVersion = blockNodeVersionValue();
        final List<PluginVersion> installedPluginVersions = installedPluginVersionsValue();
        final BlockNodeVersions subject = new BlockNodeVersions(streamProtoVersion, blockNodeVersion, installedPluginVersions);
        assertValue(streamProtoVersion, subject.streamProtoVersion());
        assertValue(blockNodeVersion, subject.blockNodeVersion());
        assertValue(installedPluginVersions, subject.installedPluginVersions());
    }

    @Test
    @DisplayName("rejects null for streamProtoVersion")
    @Tag("testId:blocknode.status.BlockNodeVersions/streamProtoVersion/rejects-null")
    void shouldRejectNullStreamProtoVersion() {
        assertThrows(NullPointerException.class, () -> new BlockNodeVersions((SemanticVersion) null, blockNodeVersionValue(), installedPluginVersionsValue()));
    }

    @Test
    @DisplayName("rejects null for blockNodeVersion")
    @Tag("testId:blocknode.status.BlockNodeVersions/blockNodeVersion/rejects-null")
    void shouldRejectNullBlockNodeVersion() {
        assertThrows(NullPointerException.class, () -> new BlockNodeVersions(streamProtoVersionValue(), (SemanticVersion) null, installedPluginVersionsValue()));
    }

    @Test
    @DisplayName("rejects null for installedPluginVersions")
    @Tag("testId:blocknode.status.BlockNodeVersions/installedPluginVersions/rejects-null")
    void shouldRejectNullInstalledPluginVersions() {
        assertThrows(NullPointerException.class, () -> new BlockNodeVersions(streamProtoVersionValue(), blockNodeVersionValue(), (List<PluginVersion>) null));
    }

    @Test
    @DisplayName("the constructor copies installedPluginVersions; the accessor returns an unmodifiable collection")
    @Tag("testId:blocknode.status.BlockNodeVersions/installedPluginVersions/copies-in")
    void shouldCopyInstalledPluginVersionsIn() {
        final List<PluginVersion> value = new ArrayList<>(installedPluginVersionsValue());
        final List<PluginVersion> expected = List.copyOf(value);
        final BlockNodeVersions subject = new BlockNodeVersions(streamProtoVersionValue(), blockNodeVersionValue(), value);
        value.clear();
        assertValue(expected, subject.installedPluginVersions());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.installedPluginVersions()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.BlockNodeVersions/value-semantics")
    void shouldCompareByValue() {
        final BlockNodeVersions subject = create();
        final BlockNodeVersions other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static SemanticVersion streamProtoVersionValue() {
        return new SemanticVersion(0, 44, 2, "", "");
    }

    private static SemanticVersion blockNodeVersionValue() {
        return new SemanticVersion(0, 44, 2, "", "");
    }

    private static List<PluginVersion> installedPluginVersionsValue() {
        return List.of(new PluginVersion("value", new SemanticVersion(0, 44, 2, "", ""), List.of("value")));
    }

    private static BlockNodeVersions create() {
        return new BlockNodeVersions(streamProtoVersionValue(), blockNodeVersionValue(), installedPluginVersionsValue());
    }

    private static BlockNodeVersions defaultSubject() {
        return new BlockNodeVersions(new SemanticVersion(0, 44, 2, "", ""), new SemanticVersion(0, 44, 2, "", ""), List.of());
    }
}
