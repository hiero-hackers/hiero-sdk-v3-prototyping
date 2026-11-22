package org.hiero.sdk.v3.ledger.contract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class EvmAddressModuleConsumerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void independentModuleUsesRealParsingAndFormattingWithoutRuntimeLibraries() throws Exception {
        // GIVEN
        final Path module = temporaryDirectory.resolve("module-info.java");
        final Path main = temporaryDirectory.resolve("Main.java");
        final Path output = temporaryDirectory.resolve("classes");
        Files.writeString(module, "module consumer { requires org.hiero.sdk.v3.base; }", StandardCharsets.UTF_8);
        Files.writeString(main, """
                package consumer;
                import java.util.Arrays;
                import java.util.HexFormat;
                import org.hiero.sdk.v3.ledger.EvmAddress;
                public final class Main {
                    public static void main(final String[] args) {
                        final String lower = "fb6916095ca1df60bb79ce92ce3ea74c37c5d359";
                        final String canonical = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359";
                        final byte[] source = HexFormat.of().parseHex(lower);
                        final EvmAddress fromBytes = EvmAddress.fromBytes(source);
                        final EvmAddress constructed = new EvmAddress(source);
                        source[0] = 0;
                        fromBytes.bytes()[19] = 0;
                        final EvmAddress parsed = EvmAddress.fromString("0x" + lower);
                        if (!canonical.equals(parsed.toString()) || !fromBytes.equals(parsed)
                                || !constructed.equals(parsed) || fromBytes.hashCode() != parsed.hashCode()
                                || !Arrays.equals(parsed.bytes(), HexFormat.of().parseHex(lower))) {
                            throw new AssertionError("Wrong EIP-55, ownership, parsing, or value behavior");
                        }
                        if (!EvmAddress.fromBytes(new byte[20]).toString()
                                .equals("0x0000000000000000000000000000000000000000")) {
                            throw new AssertionError("Zero bytes were not retained");
                        }
                        try {
                            EvmAddress.fromString("0X" + lower);
                            throw new AssertionError("Unsupported prefix accepted");
                        } catch (final IllegalArgumentException expected) {
                            // Strict parsing is required without input normalization.
                        }
                        for (final String forbidden : new String[] {"org.jspecify", "org.junit.jupiter.api",
                                "org.assertj.core"}) {
                            if (ModuleLayer.boot().findModule(forbidden).isPresent()) {
                                throw new AssertionError("Unexpected runtime library: " + forbidden);
                            }
                        }
                        System.out.println("evm-address-consumer-ok");
                    }
                }
                """, StandardCharsets.UTF_8);
        final Path api = Path.of(EvmAddress.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final Path annotations = Path.of(NonNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        // WHEN
        final ProcessResult compilation = run("javac", List.of("--release", "21", "-Xlint:all", "-Werror",
                "--module-path", api + File.pathSeparator + annotations,
                "-d", output.toString(), module.toString(), main.toString()));
        // THEN
        assertThat(compilation.exitCode()).as(compilation.output()).isZero();
        // WHEN: neither providers nor hashing/annotation/test libraries are available at runtime
        final ProcessResult execution = run("java", List.of("--module-path", api + File.pathSeparator + output,
                "--module", "consumer/consumer.Main"));
        // THEN
        assertThat(execution.exitCode()).as(execution.output()).isZero();
        assertThat(execution.output()).contains("evm-address-consumer-ok");
    }

    private ProcessResult run(final String executable, final List<String> arguments)
            throws IOException, InterruptedException {
        final java.util.ArrayList<String> command = new java.util.ArrayList<>();
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
