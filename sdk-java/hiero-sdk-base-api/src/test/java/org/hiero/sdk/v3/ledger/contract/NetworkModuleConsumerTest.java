package org.hiero.sdk.v3.ledger.contract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hiero.sdk.v3.ledger.Network;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkModuleConsumerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void independentModuleCanUseTypedNetworkWithoutRuntimeLibraries() throws Exception {
        // GIVEN / WHEN
        final ProcessResult compilation = compile("""
                final byte[] input = {0, 1};
                final Network<Unit> network = new Network<>(input, null, Unit.BASE);
                final Unit unit = network.nativeTokenUnit();
                input[0] = 42;
                final byte[] observed = network.id();
                observed[1] = 42;
                if (!Arrays.equals(network.id(), new byte[] {0, 1}) || network.name() != null || unit != Unit.BASE) {
                    throw new AssertionError("Wrong network state, unit type, or array ownership");
                }
                final Network<Unit> named = new Network<>(new byte[0], "  example  ", Unit.BASE);
                if (!"  example  ".equals(named.name()) || named.id().length != 0
                        || !network.equals(new Network<>(new byte[] {0, 1}, null, Unit.BASE))) {
                    throw new AssertionError("Wrong optional-name, empty-ID, or value semantics");
                }
                for (final String forbidden : new String[] {"org.jspecify", "org.junit.jupiter.api",
                        "org.assertj.core"}) {
                    if (ModuleLayer.boot().findModule(forbidden).isPresent()) {
                        throw new AssertionError("Unexpected runtime library: " + forbidden);
                    }
                }
                System.out.println("network-consumer-ok");
                """);
        // THEN
        assertThat(compilation.exitCode()).as(compilation.output()).isZero();
        // WHEN: the module path contains only the API and this consumer
        final ProcessResult execution = run("java", List.of("--module-path",
                apiLocation() + File.pathSeparator + temporaryDirectory.resolve("classes"),
                "--module", "consumer/consumer.Main"));
        // THEN
        assertThat(execution.exitCode()).as(execution.output()).isZero();
        assertThat(execution.output()).contains("network-consumer-ok");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "final Network<String> invalid = new Network<>(new byte[0], null, \"unit\");",
            "final OtherUnit invalid = new Network<>(new byte[0], null, Unit.BASE).nativeTokenUnit();"})
    void compilerRejectsInvalidOrUnrelatedUnitTypes(final String invalidUsage) throws Exception {
        // GIVEN / WHEN
        final ProcessResult compilation = compile(invalidUsage);
        // THEN
        assertThat(compilation.exitCode()).isNotZero();
        assertThat(compilation.output()).contains("error:");
        assertThat(compilation.output()).containsAnyOf("bounds", "incompatible types");
    }

    private ProcessResult compile(final String usage) throws Exception {
        final Path module = temporaryDirectory.resolve("module-info.java");
        final Path source = temporaryDirectory.resolve("Main.java");
        Files.writeString(module, "module consumer { requires org.hiero.sdk.v3.base; }", StandardCharsets.UTF_8);
        Files.writeString(source, """
                package consumer;
                import java.util.Arrays;
                import org.hiero.sdk.v3.ledger.Network;
                import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
                public final class Main {
                    public static void main(final String[] args) {
                        %s
                    }
                    private enum Unit implements NativeTokenUnit {
                        BASE;
                        @Override public String symbol() { return "base"; }
                        @Override public long baseUnitFactor() { return 1L; }
                    }
                    private enum OtherUnit implements NativeTokenUnit {
                        BASE;
                        @Override public String symbol() { return "other"; }
                        @Override public long baseUnitFactor() { return 1L; }
                    }
                }
                """.formatted(usage), StandardCharsets.UTF_8);
        final Path annotations = Path.of(NonNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return run("javac", List.of("--release", "21", "-Xlint:all", "-Werror", "--module-path",
                apiLocation() + File.pathSeparator + annotations,
                "-d", temporaryDirectory.resolve("classes").toString(), module.toString(), source.toString()));
    }

    private static Path apiLocation() throws Exception {
        return Path.of(Network.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
