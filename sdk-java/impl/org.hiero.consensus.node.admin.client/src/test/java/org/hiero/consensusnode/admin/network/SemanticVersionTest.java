package org.hiero.consensusnode.admin.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `SemanticVersion`, generated from the specs.
@DisplayName("SemanticVersion")
class SemanticVersionTest {

    @Test
    @DisplayName("creates a SemanticVersion and returns the values")
    void shouldCreateAndReturnTheValues() {
        final int major = majorValue();
        final int minor = minorValue();
        final int patch = patchValue();
        final @Nullable String preReleaseLabel = preReleaseLabelValue();
        final @Nullable String buildMetadata = buildMetadataValue();
        final SemanticVersion subject = new SemanticVersion(major, minor, patch, preReleaseLabel, buildMetadata);
        assertValue(major, subject.major());
        assertValue(minor, subject.minor());
        assertValue(patch, subject.patch());
        assertValue(preReleaseLabel, subject.preReleaseLabel());
        assertValue(buildMetadata, subject.buildMetadata());
    }

    @Test
    @DisplayName("major: accepts the minimum (-2147483648)")
    void shouldAcceptMajorAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final SemanticVersion subject = new SemanticVersion(value, minorValue(), patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.major());
    }

    @Test
    @DisplayName("major: accepts the maximum (2147483647)")
    void shouldAcceptMajorAtMaximum() {
        final int value = 2147483647;
        final SemanticVersion subject = new SemanticVersion(value, minorValue(), patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.major());
    }

    @Test
    @DisplayName("minor: accepts the minimum (-2147483648)")
    void shouldAcceptMinorAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final SemanticVersion subject = new SemanticVersion(majorValue(), value, patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.minor());
    }

    @Test
    @DisplayName("minor: accepts the maximum (2147483647)")
    void shouldAcceptMinorAtMaximum() {
        final int value = 2147483647;
        final SemanticVersion subject = new SemanticVersion(majorValue(), value, patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.minor());
    }

    @Test
    @DisplayName("patch: accepts the minimum (-2147483648)")
    void shouldAcceptPatchAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final SemanticVersion subject = new SemanticVersion(majorValue(), minorValue(), value, preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.patch());
    }

    @Test
    @DisplayName("patch: accepts the maximum (2147483647)")
    void shouldAcceptPatchAtMaximum() {
        final int value = 2147483647;
        final SemanticVersion subject = new SemanticVersion(majorValue(), minorValue(), value, preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.patch());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final SemanticVersion subject = create();
        final SemanticVersion other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static int majorValue() {
        return 1;
    }

    private static int minorValue() {
        return 1;
    }

    private static int patchValue() {
        return 1;
    }

    private static String preReleaseLabelValue() {
        return "value";
    }

    private static String buildMetadataValue() {
        return "value";
    }

    private static SemanticVersion create() {
        return new SemanticVersion(majorValue(), minorValue(), patchValue(), preReleaseLabelValue(), buildMetadataValue());
    }
}
