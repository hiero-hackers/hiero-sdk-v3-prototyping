package org.hiero.sdk.v3.metalang.check.ts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.check.ApiDifference;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.ts.TsGenerator;
import org.hiero.sdk.v3.metalang.model.LinkedModel;

/**
 * Checks whether a TypeScript workspace provides the API that the generator derives from the specs. The declarations
 * of the generated sources and of the project are read with the TypeScript compiler API (parsing only, so the project
 * does not have to compile) and compared structurally: a declaration belongs to the namespace of its directory
 * ({@code packages/<folder>/src/<namespace>}), type names are resolved through the imports, getters count as readonly
 * properties. Additional files, declarations, members, overloads and supertypes are allowed, and so are implemented
 * methods.
 */
public final class TsConformance {

    private TsConformance() {
    }

    /**
     * The result of a check.
     *
     * @param declarations the number of expected declarations
     * @param packages     the number of expected packages
     * @param differences  the differences; empty if the project provides the expected API
     */
    public record Result(int declarations, int packages, List<ApiDifference> differences) {

        /**
         * Creates a result.
         *
         * @param declarations the number of expected declarations
         * @param packages     the number of expected packages
         * @param differences  the differences
         */
        public Result {
            differences = List.copyOf(differences);
        }
    }

    /** The API read from a workspace. */
    private record Api(SortedMap<String, Declaration> declarations, SortedMap<String, Member> members,
                       List<ApiDifference> problems) {
    }

    private record Declaration(String id, SortedMap<String, String> kinds, SortedSet<String> supertypes, String file,
                               long line) {
    }

    private record Member(String id, String key, SortedSet<String> signatures, String file, long line) {
    }

    /**
     * Checks a project against the API generated from a model.
     *
     * @param model      the linked model of the specs
     * @param generator  the generator (with its configuration)
     * @param project    the root of the npm workspace
     * @param typescript the directory of the {@code typescript} package, {@code null} for
     *                   {@code <project>/node_modules/typescript}
     * @return the result
     * @throws IOException if the project or the TypeScript compiler cannot be read
     */
    public static Result check(final LinkedModel model, final TsGenerator generator, final Path project,
                               final Path typescript) throws IOException {
        Objects.requireNonNull(project, "project must not be null");
        final Path compiler = typescript == null ? project.resolve("node_modules/typescript") : typescript;
        if (!Files.isRegularFile(compiler.resolve("lib/typescript.js"))) {
            throw new IOException("TypeScript not found in " + compiler + " (run 'npm install' in the project or "
                    + "pass --typescript=<directory of the typescript package>)");
        }
        final List<GeneratedFile> files = generator.generate(model);
        final Path expectedRoot = Files.createTempDirectory("metalang-expected-ts");
        try {
            for (final GeneratedFile file : files) {
                if (file.path().startsWith("packages/") && file.path().contains("/src/") && file.path().endsWith(".ts")
                        && !file.path().endsWith(".test.ts")) {
                    final Path target = expectedRoot.resolve(file.path());
                    Files.createDirectories(target.getParent());
                    Files.writeString(target, file.content(), StandardCharsets.UTF_8);
                }
            }
            final Api expected = read(expectedRoot, compiler);
            if (!expected.problems().isEmpty()) {
                throw new IllegalStateException("The generated code cannot be parsed: " + expected.problems());
            }
            final Api actual = read(project, compiler);
            final long packages = files.stream().filter(f -> f.path().matches("packages/[^/]+/package\\.json"))
                    .count();
            return new Result(expected.declarations().size(), (int) packages, compare(expected, actual));
        } finally {
            delete(expectedRoot);
        }
    }

    /**
     * Compares two APIs in the output format of the extraction script ({@code ts-api.mjs}).
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
                differences.add(new ApiDifference("", 0, capitalize(kinds(declaration)) + " " + declaration(id)
                        + " is missing (expected in " + declaration.file() + ")"));
                return;
            }
            declaration.kinds().forEach((kind, typeParameters) -> {
                if (!found.kinds().containsKey(kind)) {
                    differences.add(new ApiDifference(found.file(), found.line(), declaration(id) + " must be "
                            + article(kind) + ", found " + kinds(found)));
                } else if (!found.kinds().get(kind).equals(typeParameters)) {
                    differences.add(new ApiDifference(found.file(), found.line(), capitalize(kind) + " "
                            + declaration(id) + " has different type parameters: expected '" + typeParameters
                            + "', found '" + found.kinds().get(kind) + "'"));
                }
            });
            for (final String supertype : declaration.supertypes()) {
                if (!found.supertypes().contains(supertype)) {
                    differences.add(new ApiDifference(found.file(), found.line(), declaration(id) + " must "
                            + (supertype.startsWith("extends") ? "extend" : "implement") + " " + readable(supertype.substring(
                            supertype.indexOf(' ') + 1))));
                }
            }
        });
        expected.members().forEach((key, member) -> {
            if (!actual.declarations().containsKey(member.id())) {
                return; // reported as missing declaration
            }
            final Member found = actual.members().get(key);
            final Declaration owner = actual.declarations().get(member.id());
            if (found == null) {
                differences.add(new ApiDifference(owner.file(), owner.line(), capitalize(member.key()) + " of "
                        + declaration(member.id()) + " is missing"));
                return;
            }
            for (final String signature : member.signatures()) {
                if (!found.signatures().contains(signature)) {
                    differences.add(new ApiDifference(found.file(), found.line(), capitalize(member.key()) + " of "
                            + declaration(member.id()) + " has a different declaration: expected '"
                            + readable(signature) + "', found " + String.join(" / ", found.signatures().stream()
                            .map(s -> "'" + readable(s) + "'").toList())));
                }
            }
        });
        return differences.stream().sorted().toList();
    }

    /** Runs the extraction script on a workspace. */
    private static Api read(final Path root, final Path compiler) throws IOException {
        final Path script = Files.createTempFile("metalang-ts-api", ".mjs");
        try {
            try (InputStream in = TsConformance.class.getResourceAsStream("ts-api.mjs")) {
                Files.write(script, Objects.requireNonNull(in, "ts-api.mjs is missing").readAllBytes());
            }
            final Path errorFile = Files.createTempFile("metalang-ts-api", ".log");
            final Process process = new ProcessBuilder("node", script.toString(), root.toAbsolutePath().toString(),
                    compiler.toAbsolutePath().toString()).redirectError(errorFile.toFile()).start();
            process.getOutputStream().close();
            final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            final String errors;
            try {
                process.waitFor();
                errors = Files.readString(errorFile, StandardCharsets.UTF_8);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while reading the TypeScript API", e);
            } finally {
                Files.deleteIfExists(errorFile);
            }
            if (process.exitValue() != 0) {
                throw new IOException("Reading the TypeScript API failed: " + errors.strip());
            }
            return parse(output);
        } finally {
            Files.deleteIfExists(script);
        }
    }

    private static Api parse(final String output) {
        final SortedMap<String, Declaration> declarations = new TreeMap<>();
        final SortedMap<String, Member> members = new TreeMap<>();
        final List<ApiDifference> problems = new ArrayList<>();
        for (final String line : output.lines().toList()) {
            final String[] fields = line.split("\t", -1);
            switch (fields[0]) {
                case "D" -> {
                    final Declaration existing = declarations.get(fields[1]);
                    final Declaration declaration = existing != null ? existing : new Declaration(fields[1],
                            new TreeMap<>(), new TreeSet<>(), fields[4], Long.parseLong(fields[5]));
                    declaration.kinds().put(fields[2], fields[3]);
                    declarations.put(fields[1], declaration);
                }
                case "H" -> {
                    final Declaration declaration = declarations.get(fields[1]);
                    if (declaration != null) {
                        declaration.supertypes().add(fields[2] + " " + fields[3]);
                    }
                }
                case "M" -> members.computeIfAbsent(fields[1] + "\t" + fields[2], k -> new Member(fields[1], fields[2],
                        new TreeSet<>(), fields[4], Long.parseLong(fields[5]))).signatures().add(fields[3]);
                case "E" -> problems.add(new ApiDifference(fields[1], Long.parseLong(fields[2]), "Cannot parse: "
                        + fields[3]));
                default -> {
                }
            }
        }
        return new Api(declarations, members, problems);
    }

    /** A type name in a signature: {@code base:ledger#AccountId} as {@code AccountId}. */
    private static String readable(final String text) {
        return text.replaceAll("([\\w-]+):([\\w.]*)#(\\w+)", "$3");
    }

    /** A declaration: {@code base:ledger#AccountId} as {@code AccountId (base/ledger)}. */
    private static String declaration(final String id) {
        final int colon = id.indexOf(':');
        final int hash = id.indexOf('#');
        return colon < 0 || hash < 0 ? id : id.substring(hash + 1) + " (" + id.substring(0, colon) + "/"
                + id.substring(colon + 1, hash).replace('.', '/') + ")";
    }

    private static String kinds(final Declaration declaration) {
        return String.join(" and ", declaration.kinds().keySet().stream().map(TsConformance::article).toList());
    }

    private static String article(final String kind) {
        return ("aeiou".indexOf(kind.charAt(0)) >= 0 ? "an " : "a ") + kind;
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
