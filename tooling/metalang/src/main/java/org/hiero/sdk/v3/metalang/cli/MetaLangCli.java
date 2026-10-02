package org.hiero.sdk.v3.metalang.cli;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;

/**
 * Command line interface.
 *
 * <pre>
 * metalang validate [--min-severity=error|warning|info] [--fail-on=error|warning|info|never]
 *                   [--format=text|json] [--summary] &lt;spec-dir-or-file&gt;
 * metalang rules
 * </pre>
 *
 * <p>Exit codes: 0 = success, 1 = findings at or above {@code --fail-on}, 2 = usage error.
 */
public final class MetaLangCli {

    /** Exit code for a successful run. */
    public static final int EXIT_OK = 0;
    /** Exit code if findings at or above the fail-on severity exist. */
    public static final int EXIT_FINDINGS = 1;
    /** Exit code for invalid usage. */
    public static final int EXIT_USAGE = 2;

    private static final String USAGE = """
            Usage:
              metalang validate [options] <spec-dir-or-file>
              metalang rules

            Options for 'validate':
              --min-severity=error|warning|info   lowest severity to print (default: info)
              --fail-on=error|warning|info|never  lowest severity that fails the run (default: error)
              --format=text|json                  output format (default: text)
              --summary                           print counts per rule instead of every finding
            """;

    private final PrintStream out;
    private final PrintStream err;

    /**
     * Creates a CLI writing to the given streams.
     *
     * @param out standard output
     * @param err error output
     */
    public MetaLangCli(final PrintStream out, final PrintStream err) {
        this.out = out;
        this.err = err;
    }

    /**
     * Program entry point.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        System.exit(new MetaLangCli(System.out, System.err).run(args));
    }

    /**
     * Runs the CLI.
     *
     * @param args command line arguments
     * @return the exit code
     */
    public int run(final String... args) {
        if (args.length == 0) {
            err.print(USAGE);
            return EXIT_USAGE;
        }
        return switch (args[0]) {
            case "validate" -> validate(List.of(args).subList(1, args.length));
            case "rules" -> rules();
            case "help", "--help", "-h" -> {
                out.print(USAGE);
                yield EXIT_OK;
            }
            default -> usageError("Unknown command '" + args[0] + "'");
        };
    }

    private int rules() {
        for (final Rule rule : Rule.values()) {
            out.println(String.format(Locale.ROOT, "%-40s %-8s %s (%s)", rule.id(), rule.severity(),
                    rule.description(), rule.reference()));
        }
        return EXIT_OK;
    }

    private int validate(final List<String> args) {
        Severity minSeverity = Severity.INFO;
        Severity failOn = Severity.ERROR;
        boolean json = false;
        boolean summary = false;
        final List<String> paths = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("--min-severity=")) {
                minSeverity = parseSeverity(arg.substring("--min-severity=".length()));
                if (minSeverity == null) {
                    return usageError("Invalid severity in " + arg);
                }
            } else if (arg.startsWith("--fail-on=")) {
                final String value = arg.substring("--fail-on=".length());
                failOn = value.equals("never") ? null : parseSeverity(value);
                if (failOn == null && !value.equals("never")) {
                    return usageError("Invalid severity in " + arg);
                }
            } else if (arg.equals("--format=json")) {
                json = true;
            } else if (arg.equals("--format=text")) {
                json = false;
            } else if (arg.equals("--summary")) {
                summary = true;
            } else if (arg.startsWith("--")) {
                return usageError("Unknown option " + arg);
            } else {
                paths.add(arg);
            }
        }
        if (paths.size() != 1) {
            return usageError("Expected exactly one spec directory or file");
        }
        final Path root = Path.of(paths.getFirst());
        if (!Files.exists(root)) {
            return usageError("Path does not exist: " + root);
        }
        final ValidationReport report = new MetaLang().validate(root);
        final Severity threshold = minSeverity;
        final List<Diagnostic> shown = report.diagnostics().stream()
                .filter(d -> d.severity().ordinal() <= threshold.ordinal())
                .toList();
        if (json) {
            out.print(JsonReport.render(report, shown));
        } else if (summary) {
            printSummary(report, threshold);
        } else {
            shown.forEach(out::println);
            printTotals(report);
        }
        final Severity failThreshold = failOn;
        final boolean failed = failThreshold != null && report.diagnostics().stream()
                .anyMatch(d -> d.severity().ordinal() <= failThreshold.ordinal());
        return failed ? EXIT_FINDINGS : EXIT_OK;
    }

    private void printSummary(final ValidationReport report, final Severity threshold) {
        final Map<String, Severity> severities = report.severities();
        report.countByRule().forEach((rule, count) -> {
            final Severity severity = severities.get(rule);
            if (severity.ordinal() <= threshold.ordinal()) {
                out.println(String.format(Locale.ROOT, "%6d  %-8s %s", count, severity, rule));
            }
        });
        printTotals(report);
    }

    private void printTotals(final ValidationReport report) {
        out.println(String.format(Locale.ROOT, "%d spec(s): %d error(s), %d warning(s), %d info(s)",
                report.specCount(), report.count(Severity.ERROR), report.count(Severity.WARNING),
                report.count(Severity.INFO)));
    }

    private static Severity parseSeverity(final String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "error" -> Severity.ERROR;
            case "warning" -> Severity.WARNING;
            case "info" -> Severity.INFO;
            default -> null;
        };
    }

    private int usageError(final String message) {
        err.println(message);
        err.print(USAGE);
        return EXIT_USAGE;
    }
}
