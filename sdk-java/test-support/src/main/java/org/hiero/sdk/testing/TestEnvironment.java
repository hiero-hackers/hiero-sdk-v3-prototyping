package org.hiero.sdk.testing;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/// Publishes the environment of a test run once, at the root of the run: when it started and the Java runtime it runs
/// on - vendor and distribution included, which the results of JUnit do not name (Open Test Reporting writes only the
/// Java version). JUnit registers it for every test class of a module that has the test support as dependency
/// (`junit.jupiter.extensions.autodetection.enabled`, `ServiceLoader`).
public final class TestEnvironment implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(
            TestEnvironment.class);

    /// Creates the extension (used by the `ServiceLoader`).
    public TestEnvironment() {
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        final ExtensionContext root = context.getRoot();
        root.getStore(NAMESPACE).computeIfAbsent("published", key -> {
            root.publishReportEntry(entries());
            return Boolean.TRUE;
        }, Boolean.class);
    }

    /// The entries: the start of the run (the first test class) and the Java runtime.
    private static Map<String, String> entries() {
        final Map<String, String> entries = new LinkedHashMap<>();
        entries.put("run.started", Instant.now().toString());
        for (final String property : new String[] {"java.vendor", "java.vendor.version", "java.runtime.version",
                "java.vm.name", "java.home"}) {
            final String value = System.getProperty(property);
            if (value != null) {
                entries.put(property, value);
            }
        }
        return entries;
    }
}
