package org.hiero.consensusnode.admin.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `SemanticVersion`, generated from the specs.
@DisplayName("SemanticVersion")
class SemanticVersionTest {

    @Test
    @DisplayName("creates a SemanticVersion and returns the values")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/create")
    void shouldCreateAndReturnTheValues() {
        final int major = majorValue();
        final int minor = minorValue();
        final int patch = patchValue();
        final String preReleaseLabel = preReleaseLabelValue();
        final String buildMetadata = buildMetadataValue();
        final SemanticVersion subject = new SemanticVersion(major, minor, patch, preReleaseLabel, buildMetadata);
        assertValue(major, subject.major());
        assertValue(minor, subject.minor());
        assertValue(patch, subject.patch());
        assertValue(preReleaseLabel, subject.preReleaseLabel());
        assertValue(buildMetadata, subject.buildMetadata());
    }

    @Test
    @DisplayName("creates a SemanticVersion with the default values")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final SemanticVersion subject = new SemanticVersion(majorValue(), minorValue(), patchValue());
        assertValue("", subject.preReleaseLabel());
        assertValue("", subject.buildMetadata());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final int major = majorValue();
        final int minor = minorValue();
        final int patch = patchValue();
        final String preReleaseLabel = preReleaseLabelValue();
        final String buildMetadata = buildMetadataValue();
        final SemanticVersion subject = SemanticVersion.builder()
                .major(major)
                .minor(minor)
                .patch(patch)
                .preReleaseLabel(preReleaseLabel)
                .buildMetadata(buildMetadata)
                .build();
        assertValue(major, subject.major());
        assertValue(minor, subject.minor());
        assertValue(patch, subject.patch());
        assertValue(preReleaseLabel, subject.preReleaseLabel());
        assertValue(buildMetadata, subject.buildMetadata());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final SemanticVersion subject = SemanticVersion.builder()
                .major(majorValue())
                .minor(minorValue())
                .patch(patchValue())
                .build();
        assertValue("", subject.preReleaseLabel());
        assertValue("", subject.buildMetadata());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> SemanticVersion.builder().build());
    }

    @Test
    @DisplayName("major: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/major/accepts-at-minimum")
    void shouldAcceptMajorAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final SemanticVersion subject = new SemanticVersion(value, minorValue(), patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.major());
    }

    @Test
    @DisplayName("major: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/major/accepts-at-maximum")
    void shouldAcceptMajorAtMaximum() {
        final int value = 2147483647;
        final SemanticVersion subject = new SemanticVersion(value, minorValue(), patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.major());
    }

    @Test
    @DisplayName("minor: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/minor/accepts-at-minimum")
    void shouldAcceptMinorAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final SemanticVersion subject = new SemanticVersion(majorValue(), value, patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.minor());
    }

    @Test
    @DisplayName("minor: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/minor/accepts-at-maximum")
    void shouldAcceptMinorAtMaximum() {
        final int value = 2147483647;
        final SemanticVersion subject = new SemanticVersion(majorValue(), value, patchValue(), preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.minor());
    }

    @Test
    @DisplayName("patch: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/patch/accepts-at-minimum")
    void shouldAcceptPatchAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final SemanticVersion subject = new SemanticVersion(majorValue(), minorValue(), value, preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.patch());
    }

    @Test
    @DisplayName("patch: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/patch/accepts-at-maximum")
    void shouldAcceptPatchAtMaximum() {
        final int value = 2147483647;
        final SemanticVersion subject = new SemanticVersion(majorValue(), minorValue(), value, preReleaseLabelValue(), buildMetadataValue());
        assertValue(value, subject.patch());
    }

    @Test
    @DisplayName("rejects null for preReleaseLabel")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/preReleaseLabel/rejects-null")
    void shouldRejectNullPreReleaseLabel() {
        assertThrows(NullPointerException.class, () -> new SemanticVersion(majorValue(), minorValue(), patchValue(), (String) null, buildMetadataValue()));
    }

    @Test
    @DisplayName("rejects null for buildMetadata")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/buildMetadata/rejects-null")
    void shouldRejectNullBuildMetadata() {
        assertThrows(NullPointerException.class, () -> new SemanticVersion(majorValue(), minorValue(), patchValue(), preReleaseLabelValue(), (String) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/value-semantics")
    void shouldCompareByValue() {
        final SemanticVersion subject = create();
        final SemanticVersion other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:consensusnode.admin.network.SemanticVersion/toString/call")
    void shouldCallToString() {
        assertNotNull(defaultSubject().toString());
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

    private static SemanticVersion defaultSubject() {
        return new SemanticVersion(0, 49, 1, "rc.1", "exp.sha.5114f85");
    }
}
