package org.hiero.tck.runtime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * The log of the calls of every single TCK test, for its Open Test Reporting result. The TCK sends the id of the test
 * that makes a call in the header {@value #TEST_ID_HEADER} (tooling/tck/otr-test-output.cjs); every call is appended
 * to the file {@code <id>.server.log} in the directory of the environment variable {@code OTR_TEST_OUTPUT_DIR}: the
 * request, the response and the stack trace of the exception the call failed with. The reporter of the TCK attaches
 * the file to the test. Without the variable or the header nothing is written.
 */
final class TestLog {

    /** The header that names the test of a request. */
    static final String TEST_ID_HEADER = "X-TCK-Test-Id";

    private final @Nullable Path directory;

    /** Creates the log in the directory of the environment variable {@code OTR_TEST_OUTPUT_DIR}. */
    TestLog() {
        final String directory = System.getenv("OTR_TEST_OUTPUT_DIR");
        this.directory = directory == null || directory.isBlank() ? null : Path.of(directory);
    }

    /**
     * Appends a call to the log of its test.
     *
     * @param testId   the id of the test, {@code null} if the request named none
     * @param request  the JSON-RPC request
     * @param response the JSON-RPC response
     * @param cause    the exception the call failed with, {@code null} if it did not fail with one
     */
    void call(final @Nullable String testId, final String request, final String response,
              final @Nullable Throwable cause) {
        if (directory == null || testId == null || testId.isBlank()) {
            return;
        }
        final StringBuilder entry = new StringBuilder()
                .append(Instant.now()).append(" --> ").append(request).append('\n')
                .append(Instant.now()).append(" <-- ").append(response).append('\n');
        if (cause != null) {
            final StringWriter stackTrace = new StringWriter();
            cause.printStackTrace(new PrintWriter(stackTrace));
            entry.append(stackTrace);
        }
        final Path file = directory.resolve(testId.replaceAll("[^A-Za-z0-9_-]", "_") + ".server.log");
        // the calls of one test come one after the other; synchronized only against interleaved lines
        synchronized (this) {
            try {
                Files.createDirectories(directory);
                Files.writeString(file, entry, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (final IOException e) {
                System.err.println("[tck-server] cannot write the log of test " + testId + ": " + e.getMessage());
            }
        }
    }
}
