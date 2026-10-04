package org.hiero.sdk.v3.metalang.check.rust;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.check.ApiDifference;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.rust.RustGenerator;
import org.hiero.sdk.v3.metalang.model.LinkedModel;

/**
 * Checks whether a Cargo workspace provides the API that the generator derives from the specs. The public API of the
 * generated crates and of the project is read with a small Rust program based on {@code syn} ({@code rs-api}, built
 * once with Cargo; parsing only, so the project does not have to compile): every item by its shortest public path,
 * the types in signatures resolved through the imports, the implemented traits (also the derived ones) and the
 * members of inherent implementations and traits. The comparison is structural; additional crates, items, members and
 * trait implementations are allowed, and so are implemented methods.
 */
public final class RustConformance {

    /** The files of the {@code rs-api} program (resources of this class). */
    private static final List<String> HELPER_FILES = List.of("Cargo.toml", "src/main.rs");

    private RustConformance() {
    }

    /**
     * The result of a check.
     *
     * @param declarations the number of expected declarations
     * @param crates       the number of expected crates
     * @param differences  the differences; empty if the project provides the expected API
     */
    public record Result(int declarations, int crates, List<ApiDifference> differences) {

        /**
         * Creates a result.
         *
         * @param declarations the number of expected declarations
         * @param crates       the number of expected crates
         * @param differences  the differences
         */
        public Result {
            differences = List.copyOf(differences);
        }
    }

    private record Api(SortedMap<String, Declaration> declarations, SortedSet<String> relations,
                       SortedMap<String, Member> members, List<ApiDifference> problems) {
    }

    private record Declaration(String id, String kind, String detail, String file, long line) {
    }

    private record Member(String id, String key, String signature, String file, long line) {
    }

    /**
     * Checks a project against the API generated from a model.
     *
     * @param model     the linked model of the specs
     * @param generator the generator (with its configuration)
     * @param project   the root of the Cargo workspace
     * @param cargo     the Cargo executable, {@code null} for {@code cargo} on the path
     * @return the result
     * @throws IOException if the project cannot be read or the {@code rs-api} program cannot be built
     */
    public static Result check(final LinkedModel model, final RustGenerator generator, final Path project,
                               final String cargo) throws IOException {
        Objects.requireNonNull(project, "project must not be null");
        if (!Files.isDirectory(project)) {
            throw new IOException("Project directory not found: " + project);
        }
        final Path helper = helper(cargo == null ? "cargo" : cargo);
        final List<GeneratedFile> files = generator.generate(model);
        final Path expectedRoot = Files.createTempDirectory("metalang-expected-rust");
        try {
            for (final GeneratedFile file : files) {
                if (file.path().startsWith("crates/") && (file.path().contains("/src/")
                        || file.path().endsWith("/Cargo.toml"))) {
                    final Path target = expectedRoot.resolve(file.path());
                    Files.createDirectories(target.getParent());
                    Files.writeString(target, file.content(), StandardCharsets.UTF_8);
                }
            }
            final Api expected = parse(run(helper, expectedRoot));
            if (!expected.problems().isEmpty()) {
                throw new IllegalStateException("The generated code cannot be parsed: " + expected.problems());
            }
            final Api actual = parse(run(helper, project));
            final long crates = files.stream().filter(f -> f.path().matches("crates/[^/]+/Cargo\\.toml")).count();
            return new Result(expected.declarations().size(), (int) crates, compare(expected, actual));
        } finally {
            delete(expectedRoot);
        }
    }

    /**
     * Compares two APIs in the output format of the {@code rs-api} program.
     *
     * @param expected the expected API
     * @param actual   the API of the project
     * @return the differences, sorted
     */
    static List<ApiDifference> compare(final String expected, final String actual) {
        return compare(parse(expected), parse(actual));
    }

    private static List<ApiDifference> compare(final Api expected, final Api actual) {
        final List<ApiDifference> differences = new ArrayList<>(actual.problems());
        expected.declarations().forEach((id, declaration) -> {
            final Declaration found = actual.declarations().get(id);
            if (found == null) {
                differences.add(new ApiDifference("", 0, capitalize(article(declaration.kind())) + " " + id
                        + " is missing (expected in " + declaration.file() + ")"));
            } else if (!found.kind().equals(declaration.kind())) {
                differences.add(new ApiDifference(found.file(), found.line(), id + " must be "
                        + article(declaration.kind()) + ", found " + article(found.kind())));
            } else if (!found.detail().equals(declaration.detail())) {
                final boolean type = List.of("struct", "enum", "trait").contains(declaration.kind());
                differences.add(new ApiDifference(found.file(), found.line(), capitalize(word(declaration.kind()))
                        + " " + id + (type ? " has different type parameters" : " has a different declaration")
                        + ": expected '" + declaration.detail() + "', found '" + found.detail() + "'"));
            }
        });
        for (final String relation : expected.relations()) {
            final String[] fields = relation.split("\t");
            final Declaration owner = actual.declarations().get(fields[0]);
            if (owner == null || actual.relations().contains(relation)) {
                continue; // a missing declaration is reported once
            }
            differences.add(new ApiDifference(owner.file(), owner.line(), fields[0] + " must "
                    + (fields[1].equals("extends") ? "extend " : "implement ") + fields[2]));
        }
        expected.members().forEach((key, member) -> {
            final Declaration owner = actual.declarations().get(member.id());
            if (owner == null) {
                return;
            }
            final Member found = actual.members().get(key);
            if (found == null) {
                differences.add(new ApiDifference(owner.file(), owner.line(), capitalize(member.key()) + " of "
                        + member.id() + " is missing"));
            } else if (!found.signature().equals(member.signature())) {
                differences.add(new ApiDifference(found.file(), found.line(), capitalize(member.key()) + " of "
                        + member.id() + " has a different declaration: expected '" + member.signature()
                        + "', found '" + found.signature() + "'"));
            }
        });
        return differences.stream().sorted().toList();
    }

    private static Api parse(final String output) {
        final SortedMap<String, Declaration> declarations = new TreeMap<>();
        final SortedSet<String> relations = new TreeSet<>();
        final SortedMap<String, Member> members = new TreeMap<>();
        final List<ApiDifference> problems = new ArrayList<>();
        for (final String line : output.lines().toList()) {
            final String[] fields = line.split("\t", -1);
            switch (fields[0]) {
                case "D" -> declarations.put(fields[1], new Declaration(fields[1], fields[2], fields[3], fields[4],
                        Long.parseLong(fields[5])));
                case "H" -> relations.add(fields[1] + "\t" + fields[2] + "\t" + fields[3]);
                case "M" -> members.put(fields[1] + "\t" + fields[2], new Member(fields[1], fields[2], fields[3],
                        fields[4], Long.parseLong(fields[5])));
                case "E" -> problems.add(new ApiDifference(fields[1], Long.parseLong(fields[2]), "Cannot parse: "
                        + fields[3]));
                default -> {
                }
            }
        }
        return new Api(declarations, relations, members, problems);
    }

    // --- the rs-api program ------------------------------------------------------------------------

    /** Builds the {@code rs-api} program once per version of its sources and returns the executable. */
    private static Path helper(final String cargo) throws IOException {
        final StringBuilder sources = new StringBuilder();
        for (final String file : HELPER_FILES) {
            sources.append(resource(file));
        }
        final Path directory = Path.of(System.getProperty("java.io.tmpdir"), "metalang-rs-api-"
                + hash(sources.toString()));
        final boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        final Path executable = directory.resolve("target/release/rs-api" + (windows ? ".exe" : ""));
        if (Files.isExecutable(executable)) {
            return executable;
        }
        for (final String file : HELPER_FILES) {
            final Path target = directory.resolve(file);
            Files.createDirectories(target.getParent());
            Files.writeString(target, resource(file), StandardCharsets.UTF_8);
        }
        final Path log = Files.createTempFile("metalang-rs-api", ".log");
        try {
            final Process process;
            try {
                process = new ProcessBuilder(cargo, "build", "--release", "--quiet", "--manifest-path",
                        directory.resolve("Cargo.toml").toString()).redirectErrorStream(true)
                        .redirectOutput(log.toFile()).start();
            } catch (final IOException e) {
                throw new IOException("Cargo not found ('" + cargo + "'); install Rust or pass --cargo=<executable>",
                        e);
            }
            if (waitFor(process) != 0) {
                throw new IOException("Building the rs-api program failed: " + Files.readString(log,
                        StandardCharsets.UTF_8).strip());
            }
        } finally {
            Files.deleteIfExists(log);
        }
        return executable;
    }

    private static String run(final Path helper, final Path workspace) throws IOException {
        final Path errors = Files.createTempFile("metalang-rs-api", ".log");
        try {
            final Process process = new ProcessBuilder(helper.toString(), workspace.toAbsolutePath().toString())
                    .redirectError(errors.toFile()).start();
            process.getOutputStream().close();
            final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (waitFor(process) != 0) {
                throw new IOException("Reading the Rust API failed: " + Files.readString(errors,
                        StandardCharsets.UTF_8).strip());
            }
            return output;
        } finally {
            Files.deleteIfExists(errors);
        }
    }

    private static int waitFor(final Process process) throws IOException {
        try {
            return process.waitFor();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while running " + process.info().command().orElse("a process"), e);
        }
    }

    private static String resource(final String file) throws IOException {
        try (InputStream in = RustConformance.class.getResourceAsStream("rs-api/" + file)) {
            return new String(Objects.requireNonNull(in, () -> "rs-api/" + file + " is missing").readAllBytes(),
                    StandardCharsets.UTF_8);
        }
    }

    private static String hash(final String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8))).substring(0, 16);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // --- texts ---------------------------------------------------------------------------------------

    private static String word(final String kind) {
        return switch (kind) {
            case "fn" -> "function";
            case "const" -> "constant";
            case "type" -> "type alias";
            default -> kind;
        };
    }

    private static String article(final String kind) {
        final String word = word(kind);
        return ("aeiou".indexOf(word.charAt(0)) >= 0 ? "an " : "a ") + word;
    }

    private static String capitalize(final String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static void delete(final Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            for (final Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(file);
            }
        }
    }
}
