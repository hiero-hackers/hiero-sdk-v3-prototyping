package org.hiero.sdk.v3.common.contract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hiero.sdk.v3.common.Page;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleConsumerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void independentModuleCanProvideAndUsePagesWithoutRuntimeLibraries() throws Exception {
        // GIVEN
        final Path module = temporaryDirectory.resolve("module-info.java");
        final Path source = temporaryDirectory.resolve("Main.java");
        final Path output = temporaryDirectory.resolve("classes");
        Files.writeString(module, "module consumer { requires org.hiero.sdk.v3.base; }", StandardCharsets.UTF_8);
        Files.writeString(source, """
                package consumer;
                import java.util.ArrayList;
                import java.util.List;
                import java.util.concurrent.CompletableFuture;
                import java.util.concurrent.CompletionException;
                import java.util.concurrent.CompletionStage;
                import org.hiero.sdk.v3.common.MirrorNodeException;
                import org.hiero.sdk.v3.common.Page;
                public final class Main {
                    public static void main(final String[] args) {
                        if (ModuleLayer.boot().findModule("org.jspecify").isPresent()) {
                            throw new AssertionError("JSpecify was unexpectedly needed at runtime");
                        }
                        final List<String> input = new ArrayList<>(List.of("value"));
                        final Page<String> page = new Provider(input, 7, 1);
                        input.clear();
                        if (!page.data().equals(List.of("value")) || page.size() != 7 || page.pageIndex() != 1
                                || !page.hasNext() || page.isFirst()) {
                            throw new AssertionError("Page state or position is incorrect");
                        }
                        try {
                            page.data().clear();
                            throw new AssertionError("Returned data was mutable");
                        } catch (final UnsupportedOperationException expected) {
                            // An immutable list is the expected contract.
                        }
                        if (page.first().toCompletableFuture().join().pageIndex() != 0) {
                            throw new AssertionError("First page was not returned");
                        }
                        try {
                            page.next().toCompletableFuture().join();
                            throw new AssertionError("Navigation failure was not propagated");
                        } catch (final CompletionException expected) {
                            if (!(expected.getCause() instanceof MirrorNodeException failure)
                                    || !"navigation failed".equals(failure.getMessage())
                                    || !(failure.getCause() instanceof IllegalStateException)) {
                                throw new AssertionError("Wrong failure mapping", expected);
                            }
                        }
                        try {
                            new Provider(null, 0, 0);
                            throw new AssertionError("Null data was accepted");
                        } catch (final NullPointerException expected) {
                            if (!"data must not be null".equals(expected.getMessage())) {
                                throw new AssertionError(expected);
                            }
                        }
                        System.out.println("consumer-ok");
                    }
                    private static final class Provider extends Page<String> {
                        private Provider(final List<String> data, final int size, final int index) {
                            super(data, size, index);
                        }
                        @Override public boolean hasNext() { return true; }
                        @Override public boolean isFirst() { return pageIndex() == 0; }
                        @Override public CompletionStage<Page<String>> next() {
                            return CompletableFuture.failedFuture(new MirrorNodeException(
                                    "navigation failed", new IllegalStateException("provider failure")));
                        }
                        @Override public CompletionStage<Page<String>> first() {
                            return CompletableFuture.completedFuture(new Provider(List.of("first"), 1, 0));
                        }
                    }
                }
                """, StandardCharsets.UTF_8);
        final Path api = apiLocation();
        final Path annotations = Path.of(NonNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        // WHEN
        final ProcessResult compilation = run("javac", List.of("--release", "21", "-Xlint:all", "-Werror",
                "--module-path", api + File.pathSeparator + annotations,
                "-d", output.toString(), module.toString(), source.toString()));
        // THEN
        assertThat(compilation.exitCode()).as(compilation.output()).isZero();
        // WHEN: only the API and the consumer are present at runtime
        final ProcessResult execution = run("java", List.of("--module-path", api + File.pathSeparator + output,
                "--module", "consumer/consumer.Main"));
        // THEN
        assertThat(execution.exitCode()).as(execution.output()).isZero();
        assertThat(execution.output()).contains("consumer-ok");
    }

    @Test
    void compilerRejectsAnOverrideOfImmutableStateAccessors() throws Exception {
        // GIVEN
        final Path module = temporaryDirectory.resolve("module-info.java");
        final Path source = temporaryDirectory.resolve("InvalidPage.java");
        Files.writeString(module, "module consumer { requires org.hiero.sdk.v3.base; }", StandardCharsets.UTF_8);
        Files.writeString(source, """
                package consumer;
                import java.util.List;
                import org.hiero.sdk.v3.common.Page;
                public abstract class InvalidPage extends Page<String> {
                    protected InvalidPage() { super(List.of(), 0, 0); }
                    @Override public List<String> data() { return List.of("changed"); }
                }
                """, StandardCharsets.UTF_8);
        final Path annotations = Path.of(NonNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        // WHEN
        final ProcessResult compilation = run("javac", List.of("--release", "21", "--module-path",
                apiLocation() + File.pathSeparator + annotations,
                "-d", temporaryDirectory.resolve("classes").toString(), module.toString(), source.toString()));
        // THEN
        assertThat(compilation.exitCode()).isNotZero();
        assertThat(compilation.output()).contains("data()", "final");
    }

    private static Path apiLocation() throws Exception {
        return Path.of(Page.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private ProcessResult run(final String executable, final List<String> arguments)
            throws IOException, InterruptedException {
        final List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", executable).toString());
        command.addAll(arguments);
        final Path log = temporaryDirectory.resolve(executable + "-" + System.nanoTime() + ".log");
        final Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError(executable + " did not finish within 30 seconds");
        }
        return new ProcessResult(process.exitValue(), Files.readString(log, StandardCharsets.UTF_8));
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
