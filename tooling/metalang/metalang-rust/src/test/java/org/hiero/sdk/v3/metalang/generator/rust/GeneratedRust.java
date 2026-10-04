package org.hiero.sdk.v3.metalang.generator.rust;

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
 * Builds and tests generated Rust workspaces with Cargo. All builds share one target directory
 * ({@code target/cargo} of this module), so that the libraries are compiled once; warnings are errors.
 */
final class GeneratedRust {

    /** The shared Cargo target directory. */
    static final Path TARGET = Path.of("target/cargo").toAbsolutePath();

    /**
     * The result of a test run.
     *
     * @param passed   the passed tests
     * @param stubs    the failed tests whose failure is a method stub ({@code not yet implemented})
     * @param failures the other failed tests with their messages
     * @param ignored  the number of ignored tests (not generated tests)
     */
    record TestRun(int passed, List<String> stubs, List<String> failures, int ignored) {
    }

    private GeneratedRust() {
    }

    /** Whether Cargo is available. */
    static boolean available() {
        try {
            return new ProcessBuilder("cargo", "--version").start().waitFor() == 0;
        } catch (final IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Writes the files into a directory and builds all targets (libraries and tests).
     *
     * @param files     the generated files
     * @param directory the workspace directory
     * @return the compiler output, empty if the build succeeded without warnings
     * @throws Exception if the build cannot run
     */
    static String build(final List<GeneratedFile> files, final Path directory) throws Exception {
        for (final GeneratedFile file : files) {
            final Path target = directory.resolve(file.path());
            Files.createDirectories(target.getParent());
            Files.writeString(target, file.content(), StandardCharsets.UTF_8);
        }
        final Result result = cargo(directory, "build", "--all-targets", "--quiet");
        return result.exit() == 0 ? "" : result.output();
    }

    /**
     * Runs the tests of a built workspace.
     *
     * @param directory the workspace
     * @return the result
     * @throws Exception if the tests cannot run
     */
    static TestRun test(final Path directory) throws Exception {
        return parse(cargo(directory, "test", "--no-fail-fast").output());
    }

    static TestRun parse(final String output) {
        int passed = 0;
        int ignored = 0;
        final List<String> failed = new ArrayList<>();
        final Matcher line = Pattern.compile("(?m)^test (\\S+) \\.\\.\\. (ok|FAILED|ignored.*)$").matcher(output);
        while (line.find()) {
            switch (line.group(2).split(",")[0]) {
                case "ok" -> passed++;
                case "FAILED" -> failed.add(line.group(1));
                default -> ignored++;
            }
        }
        final List<String> stubs = new ArrayList<>();
        final List<String> failures = new ArrayList<>();
        for (final String name : failed) {
            final int start = output.indexOf("---- " + name + " stdout ----");
            final int next = start < 0 ? -1 : output.indexOf("\n---- ", start + 5);
            final int summary = start < 0 ? -1 : output.indexOf("\nfailures:\n", start);
            final int end = next < 0 ? summary : summary < 0 ? next : Math.min(next, summary);
            final String message = start < 0 ? "" : output.substring(start, end < 0 ? output.length() : end);
            if (message.contains("not yet implemented")) {
                stubs.add(name);
            } else {
                failures.add(name + " " + message.replaceAll("\\s+", " "));
            }
        }
        return new TestRun(passed, stubs, failures, ignored);
    }

    private record Result(int exit, String output) {
    }

    private static Result cargo(final Path directory, final String... arguments) throws Exception {
        final List<String> command = new ArrayList<>(List.of("cargo"));
        command.addAll(List.of(arguments));
        final ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true);
        builder.environment().put("CARGO_TARGET_DIR", TARGET.toString());
        builder.environment().put("RUSTFLAGS", "-D warnings");
        builder.environment().put("CARGO_TERM_COLOR", "never");
        final Process process = builder.start();
        final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new Result(process.waitFor(), output);
    }
}
