/// The built-in functions and assertions of the test language (`guidelines/test-language.md`, namespace `testing`)
/// for the tests that are generated from the specs.
///
/// Packages:
/// - `org.hiero.sdk.testing`
// auto-service-annotations ships no module descriptor; com.google.auto.service is an automatic module
@SuppressWarnings("requires-automatic")
module org.hiero.sdk.test.support {
    requires transitive org.junit.jupiter.api;
    // @AutoService writes META-INF/services for the class path; the provides below serves the module path
    requires static com.google.auto.service;

    exports org.hiero.sdk.testing;

    // publishes when a test run started and the Java runtime it runs on (with autodetection of extensions)
    provides org.junit.jupiter.api.extension.Extension with org.hiero.sdk.testing.TestEnvironment;
}
