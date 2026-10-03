package org.hiero.sdk.v3.metalang.generator.java;

import java.util.Map;
import java.util.Optional;

/**
 * Mapping of {@code @@throws} error identifiers to Java exceptions ("Exception Handling" in
 * {@code guidelines/api-best-practices-java.md}).
 */
final class JavaExceptions {

    /** Error identifiers the Java guide maps to unchecked exceptions of the Java standard library. */
    private static final Map<String, String> UNCHECKED_STANDARD = Map.of(
            "invalid-argument-error", "IllegalArgumentException",
            "illegal-format", "IllegalArgumentException",
            "invalid-state-error", "IllegalStateException",
            "not-found-error", "java.util.NoSuchElementException",
            "unsupported-error", "UnsupportedOperationException");

    private JavaExceptions() {
    }

    /**
     * Returns the unchecked standard exception for an error identifier. Checked exceptions and SDK-specific
     * exceptions are not mapped yet; they need a {@code throws} clause or a generated exception class.
     *
     * @param errorId the meta-language error identifier, e.g. {@code not-found-error}
     * @return the exception type as it can be referenced in Javadoc, if mapped
     */
    static Optional<String> uncheckedStandardException(final String errorId) {
        return Optional.ofNullable(UNCHECKED_STANDARD.get(errorId));
    }
}
