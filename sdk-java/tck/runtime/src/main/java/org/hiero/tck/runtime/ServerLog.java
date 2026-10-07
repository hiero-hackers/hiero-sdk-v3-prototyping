package org.hiero.tck.runtime;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.Nullable;

/**
 * The error log of the server. The TCK test driver only shows the {@code message} of a JSON-RPC error ("Internal
 * error"), not its {@code data}, so the cause of a failed call is only visible here: every distinct failure is logged
 * to {@code stderr} once with its exception, and when the server stops a summary lists them with the number of calls.
 *
 * <p>The environment variable {@code TCK_SERVER_LOG} selects the detail: {@code error} (the default) logs the first
 * call of each distinct failure, {@code debug} logs every call with the stack trace of its exception, and {@code off}
 * logs nothing. As long as the API is generated stubs, the summary lists the methods to implement next.
 */
final class ServerLog {

    /** The detail of the log, selected by {@code TCK_SERVER_LOG}. */
    enum Level {
        /** Logs nothing. */
        OFF,
        /** Logs the first call of each distinct failure. */
        ERROR,
        /** Logs every call with the stack trace of its exception. */
        DEBUG
    }

    private static final String PREFIX = "[tck-server] ";

    private final Level level;
    private final Map<String, AtomicLong> failures = new ConcurrentHashMap<>();
    private final AtomicBoolean reported = new AtomicBoolean();

    /** Creates the log with the level of the environment variable {@code TCK_SERVER_LOG}. */
    ServerLog() {
        this(level(System.getenv("TCK_SERVER_LOG")));
    }

    /**
     * Creates the log.
     *
     * @param level the detail of the log
     */
    ServerLog(final Level level) {
        this.level = level;
    }

    private static Level level(final @Nullable String value) {
        if (value == null || value.isBlank()) {
            return Level.ERROR;
        }
        try {
            return Level.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException unknown) {
            System.err.println(PREFIX + "unknown TCK_SERVER_LOG '" + value + "', using 'error'");
            return Level.ERROR;
        }
    }

    /**
     * Records a failed call and logs it.
     *
     * @param method the JSON-RPC method, {@code null} if the request did not name one
     * @param code   the JSON-RPC error code of the response
     * @param detail the {@code data.message} of the response
     * @param cause  the exception the call failed with, {@code null} if the server rejected the request itself
     */
    void failed(final @Nullable String method, final int code, final String detail,
                final @Nullable Throwable cause) {
        if (level == Level.OFF) {
            return;
        }
        final String key = (method == null ? "<no method>" : method) + " -> " + code + " " + detail;
        final boolean first = failures.computeIfAbsent(key, ignored -> new AtomicLong()).getAndIncrement() == 0L;
        if (level == Level.DEBUG) {
            System.err.println(PREFIX + key);
            if (cause != null) {
                cause.printStackTrace(System.err);
            }
        } else if (first) {
            System.err.println(PREFIX + key + " (further calls are only counted, see the summary)");
        }
    }

    /**
     * Logs the summary of the distinct failures, most frequent first. Does nothing if there was none, and logs it only
     * once: both {@code stop()} and the shutdown hook of the server ask for it.
     */
    void summary() {
        if (level == Level.OFF || failures.isEmpty() || !reported.compareAndSet(false, true)) {
            return;
        }
        System.err.println(PREFIX + "failed calls by cause (" + failures.size() + " distinct):");
        failures.entrySet().stream()
                .sorted(Comparator.comparingLong((final Map.Entry<String, AtomicLong> entry) -> entry.getValue().get())
                        .reversed().thenComparing(Map.Entry::getKey))
                .forEach(entry -> System.err.println(PREFIX + "  " + entry.getValue().get() + "x " + entry.getKey()));
    }
}
