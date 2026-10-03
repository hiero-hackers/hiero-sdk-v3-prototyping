package org.hiero.sdk.v3.metalang.generator.java;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps the error identifiers of {@code @@throws} to Java exceptions (see "Exception Handling" in
 * {@code guidelines/api-best-practices-java.md}): identifiers with a natural equivalent in the JDK use the standard
 * exception, every other identifier gets its own exception class ({@link ExceptionGenerator}).
 */
final class JavaExceptions {

    /**
     * A Java exception.
     *
     * @param packageName the package
     * @param simpleName  the simple class name
     * @param checked     whether it is a checked exception (must be declared with {@code throws})
     */
    record JavaException(String packageName, String simpleName, boolean checked) {
    }

    private static final Map<String, JavaException> STANDARD = Map.of(
            "timeout-error", new JavaException("java.util.concurrent", "TimeoutException", true),
            "invalid-argument-error", new JavaException("java.lang", "IllegalArgumentException", false),
            "illegal-format", new JavaException("java.lang", "IllegalArgumentException", false),
            "invalid-state-error", new JavaException("java.lang", "IllegalStateException", false),
            "io-error", new JavaException("java.io", "IOException", true),
            "not-found-error", new JavaException("java.util", "NoSuchElementException", false),
            "unsupported-error", new JavaException("java.lang", "UnsupportedOperationException", false));

    private JavaExceptions() {
    }

    /**
     * Returns the standard JDK exception of an error identifier.
     *
     * @param errorId the error identifier, e.g. {@code not-found-error}
     * @return the standard exception, if the identifier has one
     */
    static Optional<JavaException> standard(final String errorId) {
        return Optional.ofNullable(STANDARD.get(errorId));
    }

    /**
     * Returns the class name of a custom exception: PascalCase, the {@code -error} suffix replaced by
     * {@code Exception} ({@code client-closed-error} becomes {@code ClientClosedException}).
     *
     * @param errorId the error identifier
     * @return the class name
     */
    static String className(final String errorId) {
        final String base = errorId.endsWith("-error") ? errorId.substring(0, errorId.length() - "-error".length())
                : errorId;
        return Arrays.stream(base.split("-")).filter(p -> !p.isEmpty())
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1))
                .collect(Collectors.joining()) + "Exception";
    }

    /**
     * Returns the error with article for the documentation ({@code io-error} becomes {@code an io error}).
     *
     * @param errorId the error identifier
     * @return the words with article
     */
    static String anError(final String errorId) {
        final String words = words(errorId);
        return ("aeiou".indexOf(words.charAt(0)) >= 0 ? "an " : "a ") + words + " error";
    }

    /**
     * Returns the error in words for the documentation ({@code client-closed-error} becomes {@code client closed}).
     *
     * @param errorId the error identifier
     * @return the words
     */
    static String words(final String errorId) {
        final String base = errorId.endsWith("-error") ? errorId.substring(0, errorId.length() - "-error".length())
                : errorId;
        return base.replace('-', ' ');
    }
}
