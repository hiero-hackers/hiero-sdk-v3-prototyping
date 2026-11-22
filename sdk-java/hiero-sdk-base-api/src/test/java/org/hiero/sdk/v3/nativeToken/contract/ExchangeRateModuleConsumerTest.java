package org.hiero.sdk.v3.nativeToken.contract;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hiero.sdk.v3.nativeToken.ExchangeRate;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateModuleConsumerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void independentModuleCanObserveStateAndEvaluateExpirationWithoutRuntimeLibraries() throws Exception {
        // GIVEN
        final Path module = temporaryDirectory.resolve("module-info.java");
        final Path source = temporaryDirectory.resolve("Main.java");
        final Path output = temporaryDirectory.resolve("classes");
        Files.writeString(module, "module consumer { requires org.hiero.sdk.v3.base; }", StandardCharsets.UTF_8);
        Files.writeString(source, """
                package consumer;
                import java.time.Clock;
                import java.time.Instant;
                import java.time.ZoneId;
                import java.time.ZonedDateTime;
                import java.util.Objects;
                import org.hiero.sdk.v3.nativeToken.ExchangeRate;
                public final class Main {
                    public static void main(final String[] args) {
                        final Instant now = Instant.parse("2026-01-15T12:00:00Z");
                        final Clock clock = Clock.fixed(now, ZoneId.of("America/New_York"));
                        final ZonedDateTime expiration = now.atZone(ZoneId.of("Asia/Tokyo"));
                        final ExchangeRate equal = new Provider(expiration, 12.75, clock);
                        if (equal.expirationTime() != expiration || equal.exchangeRateInUsdCents() != 12.75
                                || equal.isExpired()) {
                            throw new AssertionError("Wrong stored state or equality boundary");
                        }
                        final ExchangeRate past = new Provider(expiration.minusNanos(1), -1.0, clock);
                        final ExchangeRate future = new Provider(expiration.plusNanos(1), 0.0, clock);
                        if (!past.isExpired() || future.isExpired()
                                || past.exchangeRateInUsdCents() != -1.0 || future.exchangeRateInUsdCents() != 0.0) {
                            throw new AssertionError("Wrong nanosecond boundary or numeric validation");
                        }
                        try {
                            new Provider(null, 12.75, clock);
                            throw new AssertionError("Null expiration was accepted");
                        } catch (final NullPointerException expected) {
                            if (!"expirationTime must not be null".equals(expected.getMessage())) {
                                throw new AssertionError(expected);
                            }
                        }
                        for (final String forbidden : new String[] {"org.jspecify", "org.junit.jupiter.api",
                                "org.assertj.core"}) {
                            if (ModuleLayer.boot().findModule(forbidden).isPresent()) {
                                throw new AssertionError("Unexpected runtime library: " + forbidden);
                            }
                        }
                        System.out.println("exchange-rate-consumer-ok");
                    }
                    // Fixed-clock contract example, not production expiration evaluation.
                    private static final class Provider extends ExchangeRate {
                        private final Clock clock;
                        private Provider(final ZonedDateTime expiration, final double usdCents, final Clock clock) {
                            super(expiration, usdCents);
                            this.clock = Objects.requireNonNull(clock, "clock must not be null");
                        }
                        @Override public boolean isExpired() {
                            return clock.instant().isAfter(expirationTime().toInstant());
                        }
                    }
                }
                """, StandardCharsets.UTF_8);
        final Path api = Path.of(ExchangeRate.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final Path annotations = Path.of(NonNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        // WHEN
        final ProcessResult compilation = run("javac", List.of("--release", "21", "-Xlint:all", "-Werror",
                "--module-path", api + File.pathSeparator + annotations,
                "-d", output.toString(), module.toString(), source.toString()));
        // THEN
        assertThat(compilation.exitCode()).as(compilation.output()).isZero();
        // WHEN: only the API and this independent consumer are present at runtime
        final ProcessResult execution = run("java", List.of("--module-path", api + File.pathSeparator + output,
                "--module", "consumer/consumer.Main"));
        // THEN
        assertThat(execution.exitCode()).as(execution.output()).isZero();
        assertThat(execution.output()).contains("exchange-rate-consumer-ok");
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
