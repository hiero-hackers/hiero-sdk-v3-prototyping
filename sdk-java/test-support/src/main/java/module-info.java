/// The built-in functions and assertions of the test language (`guidelines/test-language.md`, namespace `testing`)
/// for the tests that are generated from the specs.
///
/// Packages:
/// - `org.hiero.sdk.testing`
module org.hiero.sdk.test.support {
    requires transitive org.junit.jupiter.api;

    exports org.hiero.sdk.testing;

    // publishes when a test run started and the Java runtime it runs on (with autodetection of extensions)
    provides org.junit.jupiter.api.extension.Extension with org.hiero.sdk.testing.TestEnvironment;
}
