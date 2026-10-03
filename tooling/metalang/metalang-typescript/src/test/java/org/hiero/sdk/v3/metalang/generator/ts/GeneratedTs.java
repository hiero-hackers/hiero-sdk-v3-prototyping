package org.hiero.sdk.v3.metalang.generator.ts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;

/**
 * Builds and runs generated TypeScript code with the TypeScript compiler and the Node.js test runner. The tools come
 * from the npm installation of the generated workspace ({@code generated/ts/node_modules}, created with
 * {@code npm install}); without them (or without {@code node}) {@link #available()} is false and the tests that need
 * them are skipped.
 */
final class GeneratedTs {

    /** The installed {@code node_modules} with {@code typescript} and {@code @types/node}. */
    static final Path MODULES = Path.of(System.getProperty("spec.root", "../../../spec")).toAbsolutePath().normalize()
            .resolveSibling("generated/ts/node_modules");

    /**
     * The result of a test run.
     *
     * @param passed   the number of passed tests
     * @param stubs    the failed tests whose failure is a method stub ("Not implemented yet")
     * @param failures the other failed tests with their messages
     * @param todo     the number of todo tests (not generated tests)
     */
    record TestRun(int passed, List<String> stubs, List<String> failures, int todo) {
    }

    private GeneratedTs() {
    }

    /** Whether node and the TypeScript compiler are available. */
    static boolean available() {
        if (!Files.isRegularFile(MODULES.resolve("typescript/lib/typescript.js"))) {
            return false;
        }
        try {
            return new ProcessBuilder("node", "--version").start().waitFor() == 0;
        } catch (final IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Writes the files into a workspace whose {@code node_modules} links the tools and the packages of the workspace,
     * and builds it with {@code tsc --build}.
     *
     * @param files     the generated files
     * @param directory the workspace directory
     * @param scope     the npm scope of the packages
     * @return the compiler output, empty if the build succeeded
     * @throws Exception if the build cannot run
     */
    static String build(final List<GeneratedFile> files, final Path directory, final String scope) throws Exception {
        for (final GeneratedFile file : files) {
            final Path target = directory.resolve(file.path());
            Files.createDirectories(target.getParent());
            Files.writeString(target, file.content(), StandardCharsets.UTF_8);
        }
        final Path modules = directory.resolve("node_modules");
        Files.createDirectories(modules.resolve(scope));
        Files.createSymbolicLink(modules.resolve("typescript"), MODULES.resolve("typescript"));
        Files.createSymbolicLink(modules.resolve("@types"), MODULES.resolve("@types"));
        try (var packages = Files.list(directory.resolve("packages"))) {
            for (final Path pkg : packages.toList()) {
                Files.createSymbolicLink(modules.resolve(scope).resolve(pkg.getFileName()), pkg);
            }
        }
        final Process process = new ProcessBuilder("node", MODULES.resolve("typescript/bin/tsc").toString(),
                "--build").directory(directory.toFile()).redirectErrorStream(true).start();
        final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return process.waitFor() == 0 ? "" : output;
    }

    /**
     * Runs the compiled tests.
     *
     * @param directory the built workspace
     * @return the result
     * @throws Exception if the tests cannot run
     */
    static TestRun test(final Path directory) throws Exception {
        final Path report = directory.resolve("junit.xml");
        final Process process = new ProcessBuilder("node", "--test", "--test-reporter=junit",
                "--test-reporter-destination=" + report, "packages/*/dist/**/*.test.js").directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(directory.resolve("test.log").toFile()).start();
        process.waitFor();
        final String xml = Files.readString(report, StandardCharsets.UTF_8);
        int passed = 0;
        int todo = 0;
        final List<String> stubs = new ArrayList<>();
        final List<String> failures = new ArrayList<>();
        final Matcher matcher = Pattern.compile("<testcase name=\"([^\"]*)\"[^>]*?classname=\"([^\"]*)\"[^>]*?"
                + "(/>|>(.*?)</testcase>)", Pattern.DOTALL).matcher(xml);
        while (matcher.find()) {
            final String body = matcher.group(4) == null ? "" : matcher.group(4);
            final String name = matcher.group(1);
            if (body.contains("<skipped")) {
                todo++;
            } else if (body.contains("<failure")) {
                if (body.contains("Not implemented yet")) {
                    stubs.add(name);
                } else {
                    failures.add(name + " " + body.replaceAll("\\s+", " "));
                }
            } else {
                passed++;
            }
        }
        return new TestRun(passed, stubs, failures, todo);
    }
}
