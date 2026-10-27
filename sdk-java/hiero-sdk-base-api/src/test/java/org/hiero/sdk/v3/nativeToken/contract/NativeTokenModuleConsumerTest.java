package org.hiero.sdk.v3.nativeToken.contract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NativeTokenModuleConsumerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void independentModulePreservesSelfTypeWithoutRuntimeLibraries() throws Exception {
        // GIVEN / WHEN
        final ProcessResult compilation = compile("""
                final Token source = new Token(2L, Unit.LARGE);
                final Token converted = source.to(Unit.BASE);
                final NativeToken<Token, Unit> abstraction = source;
                final Token fromAbstraction = abstraction.to(Unit.BASE);
                if (converted == source || converted.amount() != 200L || converted.unit() != Unit.BASE
                        || converted.toBaseUnits() != 200L || fromAbstraction.amount() != 200L
                        || source.amount() != 2L || source.unit() != Unit.LARGE
                        || !"large".equals(source.unit().symbol()) || source.unit().baseUnitFactor() != 100L) {
                    throw new AssertionError("Token conversion or immutable state is incorrect");
                }
                try {
                    new Token(0L, null);
                    throw new AssertionError("Null unit was accepted");
                } catch (final NullPointerException expected) {
                    if (!"unit must not be null".equals(expected.getMessage())) {
                        throw new AssertionError(expected);
                    }
                }
                try {
                    source.to(null);
                    throw new AssertionError("Null target was accepted");
                } catch (final NullPointerException expected) {
                    if (!"targetUnit must not be null".equals(expected.getMessage())) {
                        throw new AssertionError(expected);
                    }
                }
                for (final String forbidden : new String[] {"org.jspecify", "org.junit.jupiter.api",
                        "org.assertj.core"}) {
                    if (ModuleLayer.boot().findModule(forbidden).isPresent()) {
                        throw new AssertionError("Unexpected runtime library: " + forbidden);
                    }
                }
                System.out.println("native-token-consumer-ok");
                """);
        // THEN
        assertThat(compilation.exitCode()).as(compilation.output()).isZero();
        // WHEN: the module path contains only API classes and this independent consumer
        final ProcessResult execution = run("java", List.of("--module-path",
                apiLocation() + File.pathSeparator + temporaryDirectory.resolve("classes"),
                "--module", "consumer/consumer.Main"));
        // THEN
        assertThat(execution.exitCode()).as(execution.output()).isZero();
        assertThat(execution.output()).contains("native-token-consumer-ok");
    }

    @ParameterizedTest
    @ValueSource(strings = {"new Token(1L, Unit.BASE).to(OtherUnit.BASE);", "new Token(1L, OtherUnit.BASE);"})
    void compilerRejectsUnrelatedUnitTypes(final String invalidUsage) throws Exception {
        // GIVEN / WHEN
        final ProcessResult compilation = compile(invalidUsage);
        // THEN
        assertThat(compilation.exitCode()).isNotZero();
        assertThat(compilation.output()).contains("incompatible types", "OtherUnit", "Unit");
    }

    private ProcessResult compile(final String usage) throws Exception {
        final Path module = temporaryDirectory.resolve("module-info.java");
        final Path source = temporaryDirectory.resolve("Main.java");
        Files.writeString(module, "module consumer { requires org.hiero.sdk.v3.base; }", StandardCharsets.UTF_8);
        Files.writeString(source, """
                package consumer;
                import java.util.Objects;
                import org.hiero.sdk.v3.nativeToken.NativeToken;
                import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
                public final class Main {
                    public static void main(final String[] args) {
                        %s
                    }
                    private enum Unit implements NativeTokenUnit {
                        BASE("base", 1L), LARGE("large", 100L);
                        private final String symbol;
                        private final long factor;
                        Unit(final String symbol, final long factor) {
                            this.symbol = Objects.requireNonNull(symbol, "symbol must not be null");
                            this.factor = factor;
                        }
                        @Override public String symbol() { return symbol; }
                        @Override public long baseUnitFactor() { return factor; }
                    }
                    private enum OtherUnit implements NativeTokenUnit {
                        BASE;
                        @Override public String symbol() { return "other"; }
                        @Override public long baseUnitFactor() { return 1L; }
                    }
                    // Test-only arithmetic; all exercised conversions are exactly representable.
                    private static final class Token extends NativeToken<Token, Unit> {
                        private Token(final long amount, final Unit unit) { super(amount, unit); }
                        @Override public Token to(final Unit targetUnit) {
                            Objects.requireNonNull(targetUnit, "targetUnit must not be null");
                            return new Token(toBaseUnits() / targetUnit.baseUnitFactor(), targetUnit);
                        }
                        @Override public long toBaseUnits() { return amount() * unit().baseUnitFactor(); }
                    }
                }
                """.formatted(usage), StandardCharsets.UTF_8);
        final Path annotations = Path.of(NonNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return run("javac", List.of("--release", "21", "-Xlint:all", "-Werror", "--module-path",
                apiLocation() + File.pathSeparator + annotations,
                "-d", temporaryDirectory.resolve("classes").toString(), module.toString(), source.toString()));
    }

    private static Path apiLocation() throws Exception {
        return Path.of(NativeToken.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
