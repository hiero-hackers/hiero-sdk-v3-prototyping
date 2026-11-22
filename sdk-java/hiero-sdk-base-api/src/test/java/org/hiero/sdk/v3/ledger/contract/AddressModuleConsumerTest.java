package org.hiero.sdk.v3.ledger.contract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hiero.sdk.v3.ledger.Address;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AddressModuleConsumerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void independentModuleUsesProductionAddressBehaviorWithoutRuntimeLibraries() throws Exception {
        // GIVEN / WHEN: no provider, parser registration, or checksum initialization is needed
        final ProcessResult compilation = compile("""
                public final class Main {
                    public static void main(final String[] args) {
                        final Address zero = Address.ZERO_ADDRESS;
                        if (zero.shard() != 0L || zero.realm() != 0L || zero.num() == null || zero.num() != 0L
                                || !zero.checksum().isEmpty() || !"0.0.0".equals(zero.toStringWithChecksum())) {
                            throw new AssertionError("Wrong sentinel state");
                        }
                        final Address address = Address.fromString("0.0.123-vfmkw");
                        final BaseAddress base = address;
                        final Network<Unit> network = new Network<>(new byte[] {0}, null, Unit.BASE);
                        final Network<OtherUnit> other = new Network<>(new byte[] {0}, null, OtherUnit.BASE);
                        if (base.num() == null || base.num() != 123L || !base.validateChecksum(network)
                                || !address.validateChecksum(other) || !"0.0.123".equals(base.toString())
                                || !"0.0.123-vfmkw".equals(base.toStringWithChecksum())) {
                            throw new AssertionError("Wrong state, formatting, or HIP-15 behavior");
                        }
                        final Address maximum = new Address(-1L, -1L, -1L, "");
                        final String unsigned = "18446744073709551615.18446744073709551615.18446744073709551615";
                        if (!unsigned.equals(maximum.toString()) || !maximum.equals(Address.fromString(unsigned))) {
                            throw new AssertionError("Unsigned values did not round trip");
                        }
                        for (final String forbidden : new String[] {"org.jspecify", "org.junit.jupiter.api",
                                "org.assertj.core"}) {
                            if (ModuleLayer.boot().findModule(forbidden).isPresent()) {
                                throw new AssertionError("Unexpected runtime library: " + forbidden);
                            }
                        }
                        System.out.println("address-consumer-ok");
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
                """);
        // THEN
        assertThat(compilation.exitCode()).as(compilation.output()).isZero();
        // WHEN: only API and consumer modules are available at runtime
        final ProcessResult execution = run("java", List.of("--module-path",
                apiLocation() + File.pathSeparator + temporaryDirectory.resolve("classes"),
                "--module", "consumer/consumer.Main"));
        // THEN
        assertThat(execution.exitCode()).as(execution.output()).isZero();
        assertThat(execution.output()).contains("address-consumer-ok");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "public final class Main extends Address { public Main() { super(0L, 0L, 0L, \"\"); } }|final",
            "public abstract class Main extends BaseAddress { protected Main() { super(0L, 0L, null, \"\"); } public long num() { return 0L; } }|not compatible",
            "public abstract class Main extends BaseAddress { protected Main() { super(0L, 0L, null, \"\"); } public long shard() { return 0L; } }|final"})
    void compilerRejectsFinalSubtypeExtensionAndInvalidAccessorOverrides(final String source, final String reason)
            throws Exception {
        // GIVEN / WHEN
        final ProcessResult compilation = compile(source);
        // THEN
        assertThat(compilation.exitCode()).isNotZero();
        assertThat(compilation.output()).contains("error:", reason);
    }

    private ProcessResult compile(final String source) throws Exception {
        final Path module = temporaryDirectory.resolve("module-info.java");
        final Path main = temporaryDirectory.resolve("Main.java");
        Files.writeString(module, "module consumer { requires org.hiero.sdk.v3.base; }", StandardCharsets.UTF_8);
        Files.writeString(main, """
                package consumer;
                import org.hiero.sdk.v3.ledger.Address;
                import org.hiero.sdk.v3.ledger.BaseAddress;
                import org.hiero.sdk.v3.ledger.Network;
                import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
                %s
                """.formatted(source), StandardCharsets.UTF_8);
        final Path annotations = Path.of(NonNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return run("javac", List.of("--release", "21", "-Xlint:all", "-Werror", "--module-path",
                apiLocation() + File.pathSeparator + annotations,
                "-d", temporaryDirectory.resolve("classes").toString(), module.toString(), main.toString()));
    }

    private static Path apiLocation() throws Exception {
        return Path.of(Address.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
