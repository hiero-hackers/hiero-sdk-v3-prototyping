package org.hiero.sdk.v3.metalang.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.check.ApiDifference;
import org.hiero.sdk.v3.metalang.check.java.JavaConformance;
import org.hiero.sdk.v3.metalang.check.rust.RustConformance;
import org.hiero.sdk.v3.metalang.check.ts.TsConformance;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GeneratedOutput;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.generator.java.JavaGenerator;
import org.hiero.sdk.v3.metalang.generator.java.JavaGeneratorConfig;
import org.hiero.sdk.v3.metalang.generator.java.JavaTckGenerator;
import org.hiero.sdk.v3.metalang.generator.java.TestGenerator;
import org.hiero.sdk.v3.metalang.generator.rust.RustGenerator;
import org.hiero.sdk.v3.metalang.generator.rust.RustGeneratorConfig;
import org.hiero.sdk.v3.metalang.generator.rust.RustTestGenerator;
import org.hiero.sdk.v3.metalang.generator.ts.TsGenerator;
import org.hiero.sdk.v3.metalang.generator.ts.TsGeneratorConfig;
import org.hiero.sdk.v3.metalang.generator.ts.TsTckGenerator;
import org.hiero.sdk.v3.metalang.generator.ts.TsTestGenerator;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.tck.TckBindings;
import org.hiero.sdk.v3.metalang.tck.TckCoverage;
import org.hiero.sdk.v3.metalang.tck.TckSpecifications;

/**
 * Command line interface.
 *
 * <pre>
 * metalang validate [--min-severity=error|warning|info] [--fail-on=error|warning|info|never]
 *                   [--format=text|json] [--summary] &lt;spec-dir-or-file&gt;
 * metalang model [--namespace=ns] [--type=ns.Type] [--fail-on=error|warning|info|never] &lt;spec-dir-or-file&gt;
 * metalang generate --language=java|ts|rust --output=dir [--fail-on=error|warning|info|never] [--show-deferred]
 *     [--show-untested]
 *     &lt;spec-dir-or-file&gt;
 * metalang check --language=java|ts|rust --project=dir [--config=file] [--typescript=dir] [--cargo=executable]
 *     [--fail-on=error|warning|info|never]
 *     &lt;spec-dir-or-file&gt;
 * metalang tck generate --language=java|ts --bindings=dir --output=dir [--config=file] [--api=dir]
 *     [--fail-on=error|warning|info|never] &lt;spec-dir-or-file&gt;
 * metalang tck check --bindings=dir --tck=dir [--fail-on=error|warning|info|never] &lt;spec-dir-or-file&gt;
 * metalang rules
 * </pre>
 *
 * <p>Exit codes: 0 = success, 1 = findings at or above {@code --fail-on} (for {@code check}: or API differences),
 * 2 = usage error.
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
              metalang model [options] <spec-dir-or-file>
              metalang generate --language=java|ts|rust --output=<dir> [options] <spec-dir-or-file>
              metalang check --language=java|ts|rust --project=<dir> [options] <spec-dir-or-file>
              metalang tck generate --language=java|ts --bindings=<dir> --output=<dir> [options] <spec-dir-or-file>
              metalang tck check --bindings=<dir> --tck=<dir> <spec-dir-or-file>
              metalang rules

            Options for 'validate':
              --min-severity=error|warning|info   lowest severity to print (default: info)
              --fail-on=error|warning|info|never  lowest severity that fails the run (default: error)
              --format=text|json                  output format (default: text)
              --summary                           print counts per rule instead of every finding

            Options for 'model' (prints the linked model as JSON):
              --namespace=<ns>                    only this namespace and its sub-namespaces
              --type=<namespace.Type>             only this type (no functions and constants)
              --fail-on=error|warning|info|never  lowest severity that fails the run (default: error);
                                                  the model is printed in any case

            Options for 'generate':
              --language=java|ts|rust             target language (required): Java, TypeScript or Rust
              --output=<dir>                      output directory (required; created if missing)
              --fail-on=error|warning|info|never  do not generate if a finding at or above this severity
                                                  exists (default: error)
              --config=<file>                     generator configuration (.properties, e.g.
                                                  sdk-java/generator.properties)
              --show-deferred                     list the types that are not generated yet and why
              --show-untested                     list the tests that cannot be generated and why

            Options for 'check' (does a project provide the API generated from the specs? Additional files,
            types and members and implemented methods are allowed):
              --language=java|ts|rust             target language (required): Java, TypeScript or Rust
              --project=<dir>                     directory of the project to check (required), e.g. the
                                                  generated code or an implementation based on it
              --config=<file>                     generator configuration (as for 'generate')
              --typescript=<dir>                  TypeScript only: the typescript package that reads the
                                                  sources (default: <project>/node_modules/typescript)
              --cargo=<executable>                Rust only: the Cargo that builds the program reading the
                                                  sources (default: cargo)
              --fail-on=error|warning|info|never  do not check if a spec finding at or above this severity
                                                  exists (default: error)

            Options for 'tck generate' (generates the contract with the runtime and the TCK server from the bindings
            into <output>/contract and <output>/server, see tck-binding.md):
              --language=java|ts                  target language (required)
              --bindings=<dir>                    directory of the bindings files (required), e.g. tck/bindings
              --output=<dir>                      output directory (required; created if missing)
              --config=<file>                     generator configuration of the API (as for 'generate')
              --api=<dir>                         TypeScript only: the generated API workspace (default: the
                                                  directory 'ts' next to <output>)
              --fail-on=error|warning|info|never  do not generate if a spec finding at or above this severity
                                                  exists (default: error)

            Options for 'tck check' (compares the bindings with the TCK test specifications):
              --bindings=<dir>                    directory of the bindings files (required)
              --tck=<dir>                         the test specifications of the TCK (required), e.g.
                                                  hiero-sdk-tck/docs/test-specifications
              --fail-on=error|warning|info|never  do not check if a spec finding at or above this severity
                                                  exists (default: error)
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
            case "model" -> model(List.of(args).subList(1, args.length));
            case "generate" -> generate(List.of(args).subList(1, args.length));
            case "check" -> check(List.of(args).subList(1, args.length));
            case "tck" -> tck(List.of(args).subList(1, args.length));
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

    private int model(final List<String> args) {
        Severity failOn = Severity.ERROR;
        String namespace = null;
        String type = null;
        final List<String> paths = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("--fail-on=")) {
                final String value = arg.substring("--fail-on=".length());
                failOn = value.equals("never") ? null : parseSeverity(value);
                if (failOn == null && !value.equals("never")) {
                    return usageError("Invalid severity in " + arg);
                }
            } else if (arg.startsWith("--namespace=")) {
                namespace = arg.substring("--namespace=".length());
            } else if (arg.startsWith("--type=")) {
                type = arg.substring("--type=".length());
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
        final LinkedModel model = LinkedModel.of(report.model());
        final String namespaceFilter = namespace;
        final String typeFilter = type;
        if (typeFilter != null && model.types().stream().noneMatch(t -> t.name().toString().equals(typeFilter))) {
            err.println("Unknown type '" + typeFilter + "'");
            return EXIT_USAGE;
        }
        out.print(ModelJson.render(report, model,
                ns -> namespaceFilter == null || ns.equals(namespaceFilter) || ns.startsWith(namespaceFilter + "."),
                t -> typeFilter == null || t.name().toString().equals(typeFilter),
                typeFilter != null));
        final Severity failThreshold = failOn;
        final boolean failed = failThreshold != null && report.diagnostics().stream()
                .anyMatch(d -> d.severity().ordinal() <= failThreshold.ordinal());
        return failed ? EXIT_FINDINGS : EXIT_OK;
    }

    private int generate(final List<String> args) {
        Severity failOn = Severity.ERROR;
        String language = null;
        String output = null;
        boolean showDeferred = false;
        boolean showUntested = false;
        String config = null;
        final List<String> paths = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("--fail-on=")) {
                final String value = arg.substring("--fail-on=".length());
                failOn = value.equals("never") ? null : parseSeverity(value);
                if (failOn == null && !value.equals("never")) {
                    return usageError("Invalid severity in " + arg);
                }
            } else if (arg.startsWith("--language=")) {
                language = arg.substring("--language=".length());
            } else if (arg.startsWith("--output=")) {
                output = arg.substring("--output=".length());
            } else if (arg.startsWith("--config=")) {
                config = arg.substring("--config=".length());
            } else if (arg.equals("--show-deferred")) {
                showDeferred = true;
            } else if (arg.equals("--show-untested")) {
                showUntested = true;
            } else if (arg.startsWith("--")) {
                return usageError("Unknown option " + arg);
            } else {
                paths.add(arg);
            }
        }
        if (!"java".equals(language) && !"ts".equals(language) && !"rust".equals(language)) {
            return usageError(language == null ? "Missing --language" : "Unsupported language '" + language + "'");
        }
        if (output == null || output.isBlank()) {
            return usageError("Missing --output");
        }
        if (paths.size() != 1) {
            return usageError("Expected exactly one spec directory or file");
        }
        final Path root = Path.of(paths.getFirst());
        if (!Files.exists(root)) {
            return usageError("Path does not exist: " + root);
        }
        final ValidationReport report = new MetaLang().validate(root);
        final Severity failThreshold = failOn;
        final long blocking = report.diagnostics().stream()
                .filter(d -> failThreshold != null && d.severity().ordinal() <= failThreshold.ordinal())
                .count();
        if (blocking > 0) {
            err.println("Not generating: the specs have " + blocking + " finding(s) at or above "
                    + failThreshold.name().toLowerCase(Locale.ROOT) + " (see 'metalang validate')");
            return EXIT_FINDINGS;
        }
        final LinkedModel model = LinkedModel.of(report.model());
        final Generation generation;
        try {
            generation = generation(language, config, model);
        } catch (final GenerationException e) {
            e.problems().forEach(p -> err.println("Cannot generate: " + p));
            return EXIT_FINDINGS;
        } catch (final IOException e) {
            err.println("Cannot read the configuration " + config + ": " + e.getMessage());
            return EXIT_FINDINGS;
        }
        final List<GeneratedFile> files = generation.files();
        final Path outputDirectory = Path.of(output);
        final GeneratedOutput.Result result;
        try {
            result = GeneratedOutput.write(outputDirectory, files, JavaGenerator.MARKER);
        } catch (final IOException e) {
            err.println("Cannot write to " + outputDirectory + ": " + e.getMessage());
            return EXIT_FINDINGS;
        }
        out.println(files.size() + " file(s) generated in " + outputDirectory + " (" + result.written()
                + " changed, " + result.removed().size() + " stale removed)");
        result.removed().forEach(p -> out.println("  removed " + p));
        final SortedMap<QualifiedName, String> deferred = generation.deferred();
        if (!deferred.isEmpty()) {
            out.println(deferred.size() + " declaration(s) deferred until the types they refer to are generated"
                    + (showDeferred ? ":" : " (--show-deferred lists them)"));
            if (showDeferred) {
                deferred.forEach((type, reason) -> out.println("  " + type + ": " + reason));
            }
        }
        final List<String> untested = generation.untested();
        if (!untested.isEmpty()) {
            out.println(untested.size() + " test(s) not generated because their values cannot be built"
                    + (showUntested ? ":" : " (--show-untested lists them; see the instance.missing warnings of "
                    + "'metalang validate')"));
            if (showUntested) {
                untested.forEach(u -> out.println("  " + u));
            }
        }
        return EXIT_OK;
    }

    private int check(final List<String> args) {
        Severity failOn = Severity.ERROR;
        String language = null;
        String project = null;
        String config = null;
        String typescript = null;
        String cargo = null;
        final List<String> paths = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("--fail-on=")) {
                final String value = arg.substring("--fail-on=".length());
                failOn = value.equals("never") ? null : parseSeverity(value);
                if (failOn == null && !value.equals("never")) {
                    return usageError("Invalid severity in " + arg);
                }
            } else if (arg.startsWith("--language=")) {
                language = arg.substring("--language=".length());
            } else if (arg.startsWith("--project=")) {
                project = arg.substring("--project=".length());
            } else if (arg.startsWith("--config=")) {
                config = arg.substring("--config=".length());
            } else if (arg.startsWith("--typescript=")) {
                typescript = arg.substring("--typescript=".length());
            } else if (arg.startsWith("--cargo=")) {
                cargo = arg.substring("--cargo=".length());
            } else if (arg.startsWith("--")) {
                return usageError("Unknown option " + arg);
            } else {
                paths.add(arg);
            }
        }
        if (!"java".equals(language) && !"ts".equals(language) && !"rust".equals(language)) {
            return usageError(language == null ? "Missing --language" : "Unsupported language '" + language + "'");
        }
        if (project == null || project.isBlank()) {
            return usageError("Missing --project");
        }
        final Path projectDirectory = Path.of(project);
        if (!Files.isDirectory(projectDirectory)) {
            return usageError("Project directory does not exist: " + projectDirectory);
        }
        if (paths.size() != 1) {
            return usageError("Expected exactly one spec directory or file");
        }
        final Path root = Path.of(paths.getFirst());
        if (!Files.exists(root)) {
            return usageError("Path does not exist: " + root);
        }
        final ValidationReport report = new MetaLang().validate(root);
        final Severity failThreshold = failOn;
        final long blocking = report.diagnostics().stream()
                .filter(d -> failThreshold != null && d.severity().ordinal() <= failThreshold.ordinal())
                .count();
        if (blocking > 0) {
            err.println("Not checking: the specs have " + blocking + " finding(s) at or above "
                    + failThreshold.name().toLowerCase(Locale.ROOT) + " (see 'metalang validate')");
            return EXIT_FINDINGS;
        }
        final List<ApiDifference> differences;
        final String expected;
        try {
            final LinkedModel model = LinkedModel.of(report.model());
            if ("rust".equals(language)) {
                final RustGenerator generator = new RustGenerator(config == null ? RustGeneratorConfig.DEFAULT
                        : RustGeneratorConfig.load(Path.of(config)));
                final RustConformance.Result result = RustConformance.check(model, generator, projectDirectory,
                        cargo);
                differences = result.differences();
                expected = result.declarations() + " declaration(s), " + result.crates() + " crate(s)";
            } else if ("ts".equals(language)) {
                final TsGenerator generator = new TsGenerator(config == null ? TsGeneratorConfig.DEFAULT
                        : TsGeneratorConfig.load(Path.of(config)));
                final TsConformance.Result result = TsConformance.check(model, generator, projectDirectory,
                        typescript == null ? null : Path.of(typescript));
                differences = result.differences();
                expected = result.declarations() + " declaration(s), " + result.packages() + " package(s)";
            } else {
                final JavaGenerator generator = new JavaGenerator(config == null ? JavaGeneratorConfig.DEFAULT
                        : JavaGeneratorConfig.load(Path.of(config)));
                final JavaConformance.Result result = JavaConformance.check(model, generator, projectDirectory);
                differences = result.differences();
                expected = result.types() + " type(s), " + result.modules() + " module(s)";
            }
        } catch (final GenerationException e) {
            e.problems().forEach(p -> err.println("Cannot generate: " + p));
            return EXIT_FINDINGS;
        } catch (final IOException e) {
            err.println("Cannot read " + (config == null ? projectDirectory : config + " or " + projectDirectory)
                    + ": " + e.getMessage());
            return EXIT_FINDINGS;
        }
        differences.forEach(out::println);
        out.println(differences.isEmpty()
                ? projectDirectory + " provides the API of the specs (" + expected + ")"
                : differences.size() + " difference(s) between " + projectDirectory + " and the API of the specs ("
                        + expected + " expected)");
        return differences.isEmpty() ? EXIT_OK : EXIT_FINDINGS;
    }

    /**
     * The result of a generator.
     *
     * @param files    the generated files
     * @param deferred the declarations that are not generated yet, with the reason
     * @param untested the tests that cannot be generated
     */
    private record Generation(List<GeneratedFile> files, SortedMap<QualifiedName, String> deferred,
                              List<String> untested) {
    }

    private static Generation generation(final String language, final String config, final LinkedModel model)
            throws IOException {
        if ("rust".equals(language)) {
            final RustGenerator generator = new RustGenerator(config == null ? RustGeneratorConfig.DEFAULT
                    : RustGeneratorConfig.load(Path.of(config)));
            final List<GeneratedFile> files = generator.generate(model);
            return new Generation(files, generator.deferredTypes(model), RustTestGenerator.untested(files));
        }
        if ("ts".equals(language)) {
            final TsGenerator generator = new TsGenerator(config == null ? TsGeneratorConfig.DEFAULT
                    : TsGeneratorConfig.load(Path.of(config)));
            final List<GeneratedFile> files = generator.generate(model);
            return new Generation(files, generator.deferredTypes(model), TsTestGenerator.untested(files));
        }
        final JavaGenerator generator = new JavaGenerator(config == null ? JavaGeneratorConfig.DEFAULT
                : JavaGeneratorConfig.load(Path.of(config)));
        final List<GeneratedFile> files = generator.generate(model);
        return new Generation(files, generator.deferredTypes(model), TestGenerator.untested(files));
    }

    private int tck(final List<String> args) {
        if (args.isEmpty() || !List.of("generate", "check").contains(args.getFirst())) {
            return usageError(args.isEmpty() ? "Missing 'tck' command" : "Unknown 'tck' command '"
                    + args.getFirst() + "'");
        }
        final boolean generate = args.getFirst().equals("generate");
        String language = null;
        String bindingsDirectory = null;
        String output = null;
        String tck = null;
        String config = null;
        String api = null;
        Severity failOn = Severity.ERROR;
        final List<String> paths = new ArrayList<>();
        for (final String arg : args.subList(1, args.size())) {
            if (arg.startsWith("--fail-on=")) {
                final String value = arg.substring("--fail-on=".length());
                failOn = value.equals("never") ? null : parseSeverity(value);
                if (failOn == null && !value.equals("never")) {
                    return usageError("Invalid severity in " + arg);
                }
            } else if (generate && arg.startsWith("--language=")) {
                language = arg.substring("--language=".length());
            } else if (arg.startsWith("--bindings=")) {
                bindingsDirectory = arg.substring("--bindings=".length());
            } else if (generate && arg.startsWith("--output=")) {
                output = arg.substring("--output=".length());
            } else if (generate && arg.startsWith("--config=")) {
                config = arg.substring("--config=".length());
            } else if (generate && arg.startsWith("--api=")) {
                api = arg.substring("--api=".length());
            } else if (!generate && arg.startsWith("--tck=")) {
                tck = arg.substring("--tck=".length());
            } else if (arg.startsWith("--")) {
                return usageError("Unknown option " + arg);
            } else {
                paths.add(arg);
            }
        }
        if (generate && !"java".equals(language) && !"ts".equals(language)) {
            return usageError(language == null ? "Missing --language" : "Unsupported language '" + language + "'");
        }
        if (bindingsDirectory == null || !Files.isDirectory(Path.of(bindingsDirectory))) {
            return usageError(bindingsDirectory == null ? "Missing --bindings"
                    : "Bindings directory does not exist: " + bindingsDirectory);
        }
        if (generate && (output == null || output.isBlank())) {
            return usageError("Missing --output");
        }
        if (!generate && (tck == null || !Files.isDirectory(Path.of(tck)))) {
            return usageError(tck == null ? "Missing --tck" : "TCK directory does not exist: " + tck);
        }
        if (paths.size() != 1) {
            return usageError("Expected exactly one spec directory or file");
        }
        final Path root = Path.of(paths.getFirst());
        if (!Files.exists(root)) {
            return usageError("Path does not exist: " + root);
        }
        final ValidationReport report = new MetaLang().validate(root);
        final Severity failThreshold = failOn;
        final long blocking = report.diagnostics().stream()
                .filter(d -> failThreshold != null && d.severity().ordinal() <= failThreshold.ordinal())
                .count();
        if (blocking > 0) {
            err.println("Stopping: the specs have " + blocking + " finding(s) at or above "
                    + failThreshold.name().toLowerCase(Locale.ROOT) + " (see 'metalang validate')");
            return EXIT_FINDINGS;
        }
        final LinkedModel model = LinkedModel.of(report.model());
        final TckBindings.Bindings bindings;
        try {
            bindings = TckBindings.read(Path.of(bindingsDirectory), model);
        } catch (final IOException e) {
            err.println("Cannot read the bindings: " + e.getMessage());
            return EXIT_FINDINGS;
        }
        bindings.diagnostics().forEach(err::println);
        if (bindings.hasErrors()) {
            return EXIT_FINDINGS;
        }
        return generate ? tckGenerate(language, model, bindings, Path.of(output), config, api)
                : tckCheck(bindings, Path.of(tck));
    }

    private int tckGenerate(final String language, final LinkedModel model, final TckBindings.Bindings bindings,
                            final Path output, final String config, final String api) {
        final List<GeneratedFile> files;
        try {
            if ("ts".equals(language)) {
                // the API workspace relative to the output: the packages reference its projects
                final Path apiDirectory = api == null ? output.toAbsolutePath().normalize().resolveSibling("ts")
                        : Path.of(api).toAbsolutePath().normalize();
                files = new TsTckGenerator(config == null ? TsGeneratorConfig.DEFAULT
                        : TsGeneratorConfig.load(Path.of(config)), output.toAbsolutePath().normalize()
                        .relativize(apiDirectory).toString().replace('\\', '/')).generate(model, bindings);
            } else {
                files = new JavaTckGenerator(config == null ? JavaGeneratorConfig.DEFAULT
                        : JavaGeneratorConfig.load(Path.of(config))).generate(model, bindings);
            }
        } catch (final GenerationException e) {
            e.problems().forEach(p -> err.println("Cannot generate: " + p));
            return EXIT_FINDINGS;
        } catch (final IOException e) {
            err.println("Cannot read the configuration " + config + ": " + e.getMessage());
            return EXIT_FINDINGS;
        }
        final GeneratedOutput.Result result;
        try {
            result = GeneratedOutput.write(output, files, JavaGenerator.MARKER);
        } catch (final IOException e) {
            err.println("Cannot write to " + output + ": " + e.getMessage());
            return EXIT_FINDINGS;
        }
        out.println(files.size() + " file(s) of the TCK contract and server generated in " + output + " (" + result.written()
                + " changed, " + result.removed().size() + " stale removed) for " + bindings.bindings().size()
                + " method(s)");
        result.removed().forEach(p -> out.println("  removed " + p));
        return EXIT_OK;
    }

    private int tckCheck(final TckBindings.Bindings bindings, final Path tck) {
        final TckCoverage.Report report;
        try {
            report = TckCoverage.check(bindings, TckSpecifications.read(tck));
        } catch (final IOException e) {
            err.println("Cannot read the TCK specifications: " + e.getMessage());
            return EXIT_FINDINGS;
        }
        report.findings().forEach(out::println);
        out.println(report.bound().size() + " TCK method(s) bound, " + report.unbound().size() + " unbound, "
                + report.findings().size() + " finding(s)");
        if (!report.unbound().isEmpty()) {
            out.println("Unbound: " + String.join(", ", report.unbound()));
        }
        return report.findings().isEmpty() ? EXIT_OK : EXIT_FINDINGS;
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
