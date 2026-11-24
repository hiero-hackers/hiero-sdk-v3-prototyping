package org.hiero.common;

import static org.hiero.sdk.testing.Testing.*;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `common`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("common")
class CommonSpecTest {

    /// Versions and their canonical semver text: the known answers of the Rust (semantic_version display tests) and Swift
    /// (SemanticVersionUnitTests) SDKs, the example of `SemanticVersion.toString()`, and examples of
    /// semver.org (items 9 and 10 of the specification).
    private static Map<String, SemanticVersion> SEMANTIC_VERSION_TEXTS() {
        return Map.of("1.2.3", new SemanticVersion(1, 2, 3, "", ""), "3.1.4-15.92", new SemanticVersion(3, 1, 4, "15.92", ""), "1.41.1+6535asd", new SemanticVersion(1, 41, 1, "", "6535asd"), "0.1.4-0.9a2+sha.25531c", new SemanticVersion(0, 1, 4, "0.9a2", "sha.25531c"), "0.49.1-rc.1+exp.sha.5114f85", new SemanticVersion(0, 49, 1, "rc.1", "exp.sha.5114f85"), "1.0.0-x.7.z.92", new SemanticVersion(1, 0, 0, "x.7.z.92", ""), "1.0.0+20130313144700", new SemanticVersion(1, 0, 0, "", "20130313144700"), "1.0.0-beta+exp.sha.5114f85", new SemanticVersion(1, 0, 0, "beta", "exp.sha.5114f85"));
    }

    /// `common/a-semantic-version-is-written-in-its-canonical-semver-form`
    ///
    /// Source: the Rust (semantic_version display tests) and Swift (test_Basic, test_WithPrerelease, test_WithBuild,
    /// test_WithPrereleaseAndBuild) SDKs; the example of `SemanticVersion.toString()`; semver.org.
    @Test
    @DisplayName("a semantic version is written in its canonical semver form")
    @Tag("testId:common/a-semantic-version-is-written-in-its-canonical-semver-form")
    void aSemanticVersionIsWrittenInItsCanonicalSemverForm() {
        for (final String text : keys(SEMANTIC_VERSION_TEXTS())) {
            // when
            final String written = get(SEMANTIC_VERSION_TEXTS(), text).toString();

            // then
            assertEquals(text, written);
        }
    }

    /// `common/a-semantic-version-keeps-its-numbers-and-labels`
    ///
    /// Source: the JavaScript ("constructor stores major, minor and patch"), C++ (ConstructWithValues) and Swift
    /// (test_ParseWithPrereleaseAndBuild) SemanticVersion unit tests.
    @Test
    @DisplayName("a semantic version keeps its numbers and labels")
    @Tag("testId:common/a-semantic-version-keeps-its-numbers-and-labels")
    void aSemanticVersionKeepsItsNumbersAndLabels() {
        // when
        final SemanticVersion version = new SemanticVersion(1, 2, 0, "beta.10", "d13fe780");

        // then
        assertEquals(1, version.major());
        assertEquals(2, version.minor());
        assertEquals(0, version.patch());
        assertEquals("beta.10", version.preReleaseLabel());
        assertEquals("d13fe780", version.buildMetadata());
    }

    /// `common/a-release-version-has-no-labels-and-is-written-as-major-minor-patch`
    ///
    /// Source: the Swift SemanticVersion unit tests (test_ParseMajorMinorPatch, test_ParseZeroMinorPatch: a release has no
    /// labels); the API schema of this spec (both labels are empty for a release version).
    @Test
    @DisplayName("a release version has no labels and is written as major.minor.patch")
    @Tag("testId:common/a-release-version-has-no-labels-and-is-written-as-major-minor-patch")
    void aReleaseVersionHasNoLabelsAndIsWrittenAsMajorMinorPatch() {
        // when
        final SemanticVersion version = new SemanticVersion(0, 2, 0, "", "");

        // then
        assertEquals("", version.preReleaseLabel());
        assertEquals("", version.buildMetadata());
        assertEquals("0.2.0", version.toString());
    }
}
